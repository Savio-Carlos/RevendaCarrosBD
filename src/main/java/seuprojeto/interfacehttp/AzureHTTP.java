package seuprojeto.interfacehttp;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AzureHTTP implements HttpHandler {
    private static final Gson gson = new GsonBuilder().create();
    private static final String[] INTENTS = new String[]{
        "listar_carros_disponiveis",
        "detalhes_carro",
        "listar_clientes",
        "consultar_cliente_por_id",
        "listar_vendas",
        "consultar_venda_por_id",
        "iniciar_venda",
        "ajuda",
        "UNKNOWN"
    };
    private static final java.util.Set<String> INTENT_SET = new java.util.HashSet<>(java.util.Arrays.asList(INTENTS));

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            permitirCors(exchange);
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            permitirCors(exchange);
            enviar(exchange, 405, "Método não permitido");
            return;
        }

        try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            JsonObject body = JsonParser.parseReader(isr).getAsJsonObject();
            String texto = body.has("texto") ? body.get("texto").getAsString() : null;
            if (texto == null || texto.isBlank()) {
                permitirCors(exchange);
                enviar(exchange, 400, "Campo 'texto' é obrigatório");
                return;
            }

            String normalizado = normalizarBasico(texto);
            if (ehListarCarrosHeuristica(normalizado)) {
                JsonObject respostaHeuristica = new JsonObject();
                respostaHeuristica.addProperty("intent", "listar_carros_disponiveis");
                respostaHeuristica.add("entities", new JsonObject());
                if (debugEnabled()) {
                    System.out.println("[AzureHTTP][DEBUG] heuristica_pre_modelo acionada -> listar_carros_disponiveis");
                }
                permitirCors(exchange);
                enviarJson(exchange, 200, respostaHeuristica);
                return;
            }

            String endpoint = getenv("AZURE_OPENAI_ENDPOINT");
            String apiKey = getenv("AZURE_OPENAI_API_KEY");
            String deployment = getenv("AZURE_OPENAI_DEPLOYMENT");
            String apiVersion = getenvOrDefault("AZURE_OPENAI_API_VERSION", "2025-04-16");

            if (isBlank(endpoint) || isBlank(apiKey) || isBlank(deployment)) {
                System.err.println("[AzureHTTP] Configuração Azure ausente: " +
                    "endpoint=" + (isBlank(endpoint) ? "<vazio>" : "<ok>") + ", " +
                    "apiKey=" + (isBlank(apiKey) ? "<vazio>" : "<ok>") + ", " +
                    "deployment=" + (isBlank(deployment) ? "<vazio>" : "<ok>") + ", " +
                    "apiVersion=" + (isBlank(apiVersion) ? "<vazio>" : apiVersion));
                JsonObject fallback = new JsonObject();
                fallback.addProperty("intent", "desconhecido");
                fallback.add("entities", new JsonObject());
                permitirCors(exchange);
                enviarJson(exchange, 200, fallback);
                return;
            }

            String url = String.format("%s/openai/deployments/%s/chat/completions?api-version=%s",
                    endpoint.replaceAll("/$", ""), deployment, apiVersion);

            JsonObject payload = new JsonObject();
            JsonArray messages = new JsonArray();

            JsonObject sys = new JsonObject();
            sys.addProperty("role", "system");
            sys.addProperty("content", "Você é um CLASSIFICADOR ESTRITO. Retorne somente json válido exatamente no formato {\"intent\":\"<INTENT>\",\"entities\":{}}. INTENTS: listar_carros_disponiveis, detalhes_carro, listar_clientes, consultar_cliente_por_id, listar_vendas, consultar_venda_por_id, iniciar_venda, ajuda, UNKNOWN. Se não corresponder claramente a uma dessas, use UNKNOWN. Não invente novas intents. Entities válidas: marca, modelo, chassi, idCliente, idVenda, preco. Omitir se não houver evidência clara. Perguntas gerais -> UNKNOWN.");
            messages.add(sys);

            addExemplo(messages, "quero ver os carros disponíveis da fiat", "{\"intent\":\"listar_carros_disponiveis\",\"entities\":{\"marca\":\"fiat\"}}" );
            addExemplo(messages, "detalhes do carro chassi 9BWZZZ377VT004251", "{\"intent\":\"detalhes_carro\",\"entities\":{\"chassi\":\"9BWZZZ377VT004251\"}}" );
            addExemplo(messages, "listar clientes", "{\"intent\":\"listar_clientes\",\"entities\":{}}" );
            addExemplo(messages, "ver cliente 7", "{\"intent\":\"consultar_cliente_por_id\",\"entities\":{\"idCliente\":7}}" );
            addExemplo(messages, "listar vendas", "{\"intent\":\"listar_vendas\",\"entities\":{}}" );
            addExemplo(messages, "iniciar venda chassi ABC123 cliente 5 por 75000", "{\"intent\":\"iniciar_venda\",\"entities\":{\"chassi\":\"ABC123\",\"idCliente\":5,\"preco\":75000}}" );
            addExemplo(messages, "qual a capital da França?", "{\"intent\":\"UNKNOWN\",\"entities\":{}}" );
            addExemplo(messages, "listar carros", "{\"intent\":\"listar_carros_disponiveis\",\"entities\":{}}" );
            addExemplo(messages, "ver carros", "{\"intent\":\"listar_carros_disponiveis\",\"entities\":{}}" );
            addExemplo(messages, "mostrar veículos", "{\"intent\":\"listar_carros_disponiveis\",\"entities\":{}}" );
            addExemplo(messages, "listar veículos disponíveis", "{\"intent\":\"listar_carros_disponiveis\",\"entities\":{}}" );
            addExemplo(messages, "quais carros", "{\"intent\":\"listar_carros_disponiveis\",\"entities\":{}}" );
            addExemplo(messages, "listar car", "{\"intent\":\"UNKNOWN\",\"entities\":{}}" );

            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", texto);
            messages.add(userMsg);

            payload.add("messages", messages);
            JsonObject responseFormat = new JsonObject();
            responseFormat.addProperty("type", "json_object");
            payload.add("response_format", responseFormat);
            payload.addProperty("top_p", 1);
            payload.addProperty("max_completion_tokens", 256);

            JsonObject azureResp;
            try {
                azureResp = chamarAzure(url, apiKey, payload);
                if (debugEnabled()) {
                    System.out.println("[AzureHTTP][DEBUG] chamada OK intent-clf status choices=" + (azureResp.has("choices")));                }
            } catch (IOException ioEx) {
                if (debugEnabled()) {
                    System.out.println("[AzureHTTP][DEBUG] falha chamada Azure: " + ioEx.getMessage());
                }
                System.err.println("[AzureHTTP] Falha ao chamar Azure: " + ioEx.getMessage());
                JsonObject fallback = new JsonObject();
                fallback.addProperty("intent", "desconhecido");
                fallback.add("entities", new JsonObject());
                permitirCors(exchange);
                enviarJson(exchange, 200, fallback);
                return;
            }

            if (!azureResp.has("choices") || !azureResp.get("choices").isJsonArray() || azureResp.getAsJsonArray("choices").size() == 0) {
                JsonObject result = new JsonObject();
                result.addProperty("intent", "fallback");
                result.add("entities", new JsonObject());
                permitirCors(exchange);
                enviarJson(exchange, 200, result);
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
                resultJson.addProperty("intent", "UNKNOWN");
                resultJson.add("entities", new JsonObject());
            }

            // Validação e sanitização ANTES de enviar
            String intent = resultJson.has("intent") && resultJson.get("intent").isJsonPrimitive() ? resultJson.get("intent").getAsString() : "UNKNOWN";
            if (!INTENT_SET.contains(intent)) intent = "UNKNOWN";
            JsonObject entities = resultJson.has("entities") && resultJson.get("entities").isJsonObject() ? resultJson.getAsJsonObject("entities") : new JsonObject();
            JsonObject sanitized = new JsonObject();
            if (entities.has("chassi") && entities.get("chassi").isJsonPrimitive()) {
                String chassi = entities.get("chassi").getAsString();
                if (chassi != null && chassi.length() <= 25 && !chassi.contains(" ")) sanitized.addProperty("chassi", chassi);
            }
            if (entities.has("idCliente")) { try { int idc = entities.get("idCliente").getAsInt(); if (idc>0) sanitized.addProperty("idCliente", idc);} catch(Exception ignore){} }
            if (entities.has("idVenda")) { try { int idv = entities.get("idVenda").getAsInt(); if (idv>0) sanitized.addProperty("idVenda", idv);} catch(Exception ignore){} }
            if (entities.has("preco")) { try { double p = entities.get("preco").getAsDouble(); if (p>0) sanitized.addProperty("preco", p);} catch(Exception ignore){} }
            if (entities.has("marca") && entities.get("marca").isJsonPrimitive()) { String marca = entities.get("marca").getAsString(); if (marca!=null && marca.length()<=40) sanitized.addProperty("marca", marca);} 
            if (entities.has("modelo") && entities.get("modelo").isJsonPrimitive()) { String modelo = entities.get("modelo").getAsString(); if (modelo!=null && modelo.length()<=60) sanitized.addProperty("modelo", modelo);} 

            if ("UNKNOWN".equals(intent) && pareceListarCarros(normalizarBasico(texto))) {
                if (debugEnabled()) {
                    System.out.println("[AzureHTTP][DEBUG] heuristica_pos_modelo override UNKNOWN -> listar_carros_disponiveis");
                }
                intent = "listar_carros_disponiveis";
            }

            JsonObject finalObj = new JsonObject();
            finalObj.addProperty("intent", intent);
            finalObj.add("entities", sanitized);
            if (debugEnabled()) {
                System.out.println("[AzureHTTP][DEBUG] intent_bruta=" + (resultJson.has("intent")?resultJson.get("intent").getAsString():"<null>") + " intent_final="+intent+" entities="+finalObj.get("entities"));
            }
            permitirCors(exchange);
            enviarJson(exchange, 200, finalObj);
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject fallback = new JsonObject();
            fallback.addProperty("intent", "desconhecido");
            fallback.add("entities", new JsonObject());
            permitirCors(exchange);
            enviarJson(exchange, 200, fallback);
        }
    }

    private static String getenv(String key) { return System.getenv(key); }
    private static String getenvOrDefault(String key, String def) {
        String v = System.getenv(key);
        return v != null && !v.isBlank() ? v : def;
    }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static JsonObject chamarAzure(String urlStr, String apiKey, JsonObject payload) throws IOException {
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
                String msg = obj.has("error") ? obj.get("error").toString() : obj.toString();
                throw new IOException("Azure HTTP " + code + ": " + msg);
            }
            return obj;
        }
    }

    private static void permitirCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
    }

    private static void enviar(HttpExchange ex, int status, String msg) throws IOException {
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private static void enviarJson(HttpExchange ex, int status, JsonObject json) throws IOException {
        byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private static void addExemplo(JsonArray messages, String user, String respostaJson){
        JsonObject u = new JsonObject();
        u.addProperty("role","user");
        u.addProperty("content", user);
        JsonObject a = new JsonObject();
        a.addProperty("role","assistant");
        a.addProperty("content", respostaJson);
        messages.add(u);
        messages.add(a);
    }

    private static boolean debugEnabled(){
        String v = System.getenv("AZURE_DEBUG_LOG");
        return v != null && (v.equalsIgnoreCase("1") || v.equalsIgnoreCase("true") || v.equalsIgnoreCase("sim"));
    }

    private static String normalizarBasico(String s){
        if (s==null) return "";
        String lower = s.toLowerCase();
        lower = lower.replaceAll("[áàâã]","a").replaceAll("[éê]","e").replaceAll("[í]","i").replaceAll("[óôõ]","o").replaceAll("[ú]","u").replaceAll("ç","c");
        return lower.trim();
    }
    private static boolean ehListarCarrosHeuristica(String n){
        return n.matches("^(listar|ver|mostrar|exibir|quais|verificar) (carros|carro|veiculos|veiculo)s?( disponiveis)?$");
    }
    private static boolean pareceListarCarros(String n){
        return n.contains("carro") || n.contains("carros") || n.contains("veiculo") || n.contains("veiculos");
    }

}
