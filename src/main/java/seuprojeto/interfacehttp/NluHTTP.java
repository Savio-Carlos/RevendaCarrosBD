package seuprojeto.interfacehttp;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class NluHTTP implements HttpHandler {
    private static final Gson gson = new GsonBuilder().create();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            allowCors(exchange);
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            allowCors(exchange);
            send(exchange, 405, "Método não permitido");
            return;
        }

        try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            JsonObject body = JsonParser.parseReader(isr).getAsJsonObject();
            String texto = body.has("texto") ? body.get("texto").getAsString() : null;
            if (texto == null || texto.isBlank()) {
                allowCors(exchange);
                send(exchange, 400, "Campo 'texto' é obrigatório");
                return;
            }

            // Env vars 
            String endpoint = getenv("AZURE_OPENAI_ENDPOINT");
            String apiKey = getenv("AZURE_OPENAI_API_KEY");
            String deployment = getenv("AZURE_OPENAI_DEPLOYMENT");
            String apiVersion = getenvOrDefault("AZURE_OPENAI_API_VERSION", "2025-04-16");

        if (isBlank(endpoint) || isBlank(apiKey) || isBlank(deployment)) {
        // Loga diagnóstico (sem expor segredos)
        System.err.println("[NLU] Configuração Azure ausente: " +
            "endpoint=" + (isBlank(endpoint) ? "<vazio>" : "<ok>") + ", " +
            "apiKey=" + (isBlank(apiKey) ? "<vazio>" : "<ok>") + ", " +
            "deployment=" + (isBlank(deployment) ? "<vazio>" : "<ok>") + ", " +
            "apiVersion=" + (isBlank(apiVersion) ? "<vazio>" : apiVersion));
        // Degrada com fallback para evitar 500 no frontend
                JsonObject fallback = new JsonObject();
                fallback.addProperty("intent", "desconhecido");
                fallback.add("entities", new JsonObject());
                allowCors(exchange);
                sendJson(exchange, 200, fallback);
                return;
            }

            String url = String.format("%s/openai/deployments/%s/chat/completions?api-version=%s",
                    endpoint.replaceAll("/$", ""), deployment, apiVersion);

            JsonObject payload = new JsonObject();
            JsonArray messages = new JsonArray();

            JsonObject sys = new JsonObject();
            sys.addProperty("role", "system");
            sys.addProperty("content", "Responda apenas JSON válido com os campos: { intent: string, entities: object }. Se não entender, use intent=\"fallback\" e entities={}.");
            messages.add(sys);

            JsonObject user = new JsonObject();
            user.addProperty("role", "user");
            user.addProperty("content", texto);
            messages.add(user);

            payload.add("messages", messages);
            JsonObject responseFormat = new JsonObject();
            responseFormat.addProperty("type", "json_object");
            payload.add("response_format", responseFormat);

            JsonObject azureResp;
            try {
                azureResp = callAzure(url, apiKey, payload);
            } catch (IOException ioEx) {
                System.err.println("[NLU] Falha ao chamar Azure: " + ioEx.getMessage());
                // Falha na Azure: retorna fallback em 200 para UX melhor
                JsonObject fallback = new JsonObject();
                fallback.addProperty("intent", "desconhecido");
                fallback.add("entities", new JsonObject());
                allowCors(exchange);
                sendJson(exchange, 200, fallback);
                return;
            }
            // Parse assistant message content as JSON
            if (!azureResp.has("choices") || !azureResp.get("choices").isJsonArray() || azureResp.getAsJsonArray("choices").size() == 0) {
                JsonObject result = new JsonObject();
                result.addProperty("intent", "fallback");
                result.add("entities", new JsonObject());
                allowCors(exchange);
                sendJson(exchange, 200, result);
                return;
            }

            JsonArray choices = azureResp.getAsJsonArray("choices");
            JsonObject first = choices.get(0).getAsJsonObject();
            JsonObject message = first.getAsJsonObject("message");
            String content = message.get("content").getAsString();

            JsonObject resultJson;
            try {
                resultJson = JsonParser.parseString(content).getAsJsonObject();
            } catch (Exception ex) {
                resultJson = new JsonObject();
                resultJson.addProperty("intent", "fallback");
                resultJson.add("entities", new JsonObject());
            }

            allowCors(exchange);
            sendJson(exchange, 200, resultJson);
        } catch (Exception e) {
            e.printStackTrace();
            // Erro inesperado: ainda assim devolve um fallback JSON
            JsonObject fallback = new JsonObject();
            fallback.addProperty("intent", "desconhecido");
            fallback.add("entities", new JsonObject());
            allowCors(exchange);
            sendJson(exchange, 200, fallback);
        }
    }

    private static String getenv(String key) { return System.getenv(key); }
    private static String getenvOrDefault(String key, String def) {
        String v = System.getenv(key);
        return v != null && !v.isBlank() ? v : def;
    }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static JsonObject callAzure(String urlStr, String apiKey, JsonObject payload) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("api-key", apiKey);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] out = payload.toString().getBytes(StandardCharsets.UTF_8);
            os.write(out);
        }

        int code = conn.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            JsonElement el = JsonParser.parseReader(isr);
            if (!el.isJsonObject()) throw new IOException("Azure response not JSON object");
            JsonObject obj = el.getAsJsonObject();
            if (code < 200 || code >= 300) {
                // Bubble up Azure error details
                String msg = obj.has("error") ? obj.get("error").toString() : obj.toString();
                throw new IOException("Azure HTTP " + code + ": " + msg);
            }
            return obj;
        }
    }

    private static void allowCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
    }

    private static void send(HttpExchange ex, int status, String msg) throws IOException {
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private static void sendJson(HttpExchange ex, int status, JsonObject json) throws IOException {
        byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }
}