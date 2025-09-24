package seuprojeto.interfacehttp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import seuprojeto.negocio.bo.Endereco;
import seuprojeto.negocio.servicos.EnderecoServico;
import seuprojeto.excecao.ValidacaoExcecao;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

public class EnderecoHTTP implements HttpHandler {
    private final Gson gson = new GsonBuilder().create();
    private final EnderecoServico servico = new EnderecoServico();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
            exchange.sendResponseHeaders(204, -1);
            return;
        }

    if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
        tratarPost(exchange);
            return;
        }
    exchange.getResponseHeaders().add("Allow", "POST, OPTIONS");
    enviarErro(exchange, 405, "Metodo nao permitido");
    }

    private void tratarPost(HttpExchange exchange) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            Endereco payload = gson.fromJson(isr, Endereco.class);

            java.util.Map<String,String> seed = new java.util.HashMap<>();
            seed.put("uf", getQueryParam(exchange, "uf"));
            seed.put("nomeUF", getQueryParam(exchange, "nomeUF"));
            seed.put("cidade", getQueryParam(exchange, "cidade"));
            seed.put("bairro", getQueryParam(exchange, "bairro"));
            seed.put("logradouro", getQueryParam(exchange, "logradouro"));
            seed.put("siglaLog", getQueryParam(exchange, "siglaLog"));

            Endereco salvo = servico.inserir(payload, seed);
        enviarJson(exchange, 201, "{\"idEndereco\":" + salvo.getIdEndereco() + "}");

        } catch (ValidacaoExcecao e) {
        enviarErro(exchange, 400, "Erro de validacao: " + e.getMessage());
        } catch (java.sql.SQLIntegrityConstraintViolationException dup) {
            String msg = String.format("Violacao de integridade: %s", dup.getMessage());
        enviarErro(exchange, 409, msg);
        } catch (SQLException e) {
        enviarErro(exchange, 500, "Erro de SQL: " + e.getMessage());
        } catch (Exception e) {
        enviarErro(exchange, 400, "Erro ao processar JSON: " + e.getMessage());
        }
    }

    private void enviarJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private void enviarErro(HttpExchange ex, int status, String msg) throws IOException {
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private String getQueryParam(HttpExchange exchange, String key) {
        String q = exchange.getRequestURI().getQuery();
        if (q == null || q.isEmpty()) return null;
        for (String p : q.split("&")) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                return java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
