package seuprojeto.interfacehttp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import seuprojeto.negocio.bo.Endereco;
import seuprojeto.infra.bd.ConexaoBancoDados;
import seuprojeto.negocio.dao.EnderecoDAO;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

public class EnderecoHTTP implements HttpHandler {
    private final Gson gson = new GsonBuilder().create();

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
            handlePost(exchange);
            return;
        }
    exchange.getResponseHeaders().add("Allow", "POST, OPTIONS");
    sendError(exchange, 405, "Metodo nao permitido");
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            Endereco payload = gson.fromJson(isr, Endereco.class);

            // Sanitiza CEP (apenas dígitos) caso venha com máscara
            if (payload != null && payload.getLogradouroCEP() != null) {
                String onlyDigitsCep = payload.getLogradouroCEP().replaceAll("\\D", "");
                payload.setLogradouroCEP(onlyDigitsCep);
            }

            if (payload == null || payload.getLogradouroCEP() == null || payload.getLogradouroCEP().length() != 8 ||
                payload.getNumeroEndereco() == null || payload.getNumeroEndereco().isEmpty()) {
                sendError(exchange, 400, "Payload invalido para Endereco");
                return;
            }

            // DEBUG: logar o que chegou para diagnosticar mistura de campos (ex.: complemento vs bairro)
            System.out.println("[EnderecoHTTP] POST body => CEP=" + payload.getLogradouroCEP() + ", numero=" + payload.getNumeroEndereco() +
                    ", complemento=" + payload.getComplementoEndereco() + ", referencia=" + payload.getReferencia());

            try (Connection conn = ConexaoBancoDados.criarConexao()) {
                EnderecoDAO dao = new EnderecoDAO(conn);

                // Auto-seed: se CEP não existe, criar cadeia UF/Cidade/Bairro/Logradouro mínimas
                boolean logradouroExiste = dao.existeLogradouroCep(payload.getLogradouroCEP());
                if (!logradouroExiste) {
                    String siglaUF = getQueryParam(exchange, "uf");
                    String nomeUF = getQueryParam(exchange, "nomeUF");
                    String nomeCidade = getQueryParam(exchange, "cidade");
                    String nomeBairro = getQueryParam(exchange, "bairro");
                    String nomeLogradouro = getQueryParam(exchange, "logradouro");
                    String siglaLog = getQueryParam(exchange, "siglaLog");

                    if (siglaUF == null || siglaUF.isBlank()) siglaUF = "NA"; // UF desconhecida provisória
                    if (nomeUF == null || nomeUF.isBlank()) nomeUF = "N/A";
                    if (nomeCidade == null || nomeCidade.isBlank()) nomeCidade = "Desconhecida";
                    if (nomeBairro == null || nomeBairro.isBlank()) nomeBairro = "Centro";
                    if (nomeLogradouro == null || nomeLogradouro.isBlank()) nomeLogradouro = "Logradouro";
                    if (siglaLog == null || siglaLog.isBlank()) siglaLog = "R"; // Rua

                    // Normaliza UF para maiúsculas
                    siglaUF = siglaUF.toUpperCase();

                    conn.setAutoCommit(false);
                    try {
                        dao.upsertUF(siglaUF, nomeUF);
                        int idCidade = dao.upsertCidade(nomeCidade, siglaUF);
                        int idBairro = dao.upsertBairro(nomeBairro);
                        dao.ensureSiglaLogradouro(siglaLog, siglaLog);
                        dao.upsertLogradouro(payload.getLogradouroCEP(), nomeLogradouro, siglaLog, idBairro, idCidade);
                        conn.commit();
                    } catch (SQLException ex) {
                        conn.rollback();
                        String msg = String.format("Falha ao auto-popular CEP (state=%s, code=%d): %s", ex.getSQLState(), ex.getErrorCode(), ex.getMessage());
                        System.err.println("[EnderecoHTTP] " + msg);
                        sendError(exchange, 500, msg);
                        return;
                    } finally {
                        conn.setAutoCommit(true);
                    }
                }

                try {
                    Endereco salvo = dao.inserir(payload);
                    sendJson(exchange, 201, "{\"idEndereco\":" + salvo.getIdEndereco() + "}");
                } catch (java.sql.SQLIntegrityConstraintViolationException dup) {
                    String msg = String.format("Violacao de integridade (state=%s, code=%d): %s", dup.getSQLState(), dup.getErrorCode(), dup.getMessage());
                    System.err.println("[EnderecoHTTP] " + msg);
                    sendError(exchange, 409, msg);
                }
            }
        } catch (SQLException e) {
            String msg = String.format("Erro de SQL (state=%s, code=%d): %s", e.getSQLState(), e.getErrorCode(), e.getMessage());
            System.err.println("[EnderecoHTTP] " + msg);
            sendError(exchange, 500, msg);
        } catch (Exception e) {
            System.err.println("[EnderecoHTTP] Erro ao processar JSON: " + e);
            sendError(exchange, 400, "Erro ao processar JSON: " + e.getMessage());
        }
    }

    private void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private void sendError(HttpExchange ex, int status, String msg) throws IOException {
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
