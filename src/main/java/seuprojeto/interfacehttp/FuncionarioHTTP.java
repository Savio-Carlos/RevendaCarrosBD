package seuprojeto.interfacehttp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import seuprojeto.negocio.dao.FuncionarioDAO;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class FuncionarioHTTP implements HttpHandler {
    private final FuncionarioDAO dao = new FuncionarioDAO();
    private final Gson gson = new GsonBuilder().create();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String metodo = exchange.getRequestMethod();

        if ("OPTIONS".equalsIgnoreCase(metodo)) {
            handleOptionsRequest(exchange);
            return;
        }

        if ("GET".equalsIgnoreCase(metodo)) {
            handleGet(exchange);
        } else {
            sendError(exchange, 405, "Metodo nao permitido");
        }
    }

    private void handleOptionsRequest(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        exchange.sendResponseHeaders(204, -1);
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        try {
            var lista = dao.listarResumido();
            sendJson(exchange, 200, gson.toJson(lista));
        } catch (SQLException e) {
            e.printStackTrace();
            sendError(exchange, 500, "Erro ao buscar funcionarios: " + e.getMessage());
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
}
