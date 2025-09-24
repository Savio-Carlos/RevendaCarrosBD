// Pacote: seuprojeto.requests
package seuprojeto.interfacehttp;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import seuprojeto.negocio.bo.Garantia;
import seuprojeto.excecao.ValidacaoExcecao;
import seuprojeto.negocio.servicos.GarantiaServico;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class GarantiaHTTP implements HttpHandler {

    private final GarantiaServico garantiaServico = new GarantiaServico();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String metodo = exchange.getRequestMethod();
        if ("OPTIONS".equalsIgnoreCase(metodo)) {
            tratarOptions(exchange);
            return;
        }

        if ("GET".equalsIgnoreCase(metodo)) {
            tratarGet(exchange);
        } else {
            metodoNaoPermitido(exchange);
        }
    }
    private void tratarOptions(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        exchange.sendResponseHeaders(204, -1); // 204 No Content
    }
    private void tratarGet(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");

            if (pathParts.length == 4) { 
                int id = Integer.parseInt(pathParts[3]);
                Garantia garantia = garantiaServico.buscarPorId(id);

                if (garantia != null) {
                    enviarJson(exchange, 200, garantia);
                } else {
                    enviarErro(exchange, 404, "Garantia nao encontrada.");
                }
            } else {
                enviarErro(exchange, 400, "Formato de URL invalido. Use /api/garantias/{id}.");
            }
        } catch (NumberFormatException e) {
            enviarErro(exchange, 400, "ID da garantia invalido.");
        } catch (ValidacaoExcecao e) {
            enviarErro(exchange, 400, "Erro de validacao: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            enviarErro(exchange, 500, "Erro interno no servidor ao acessar o banco de dados.");
        }
    }

    private void metodoNaoPermitido(HttpExchange exchange) throws IOException {
        enviarErro(exchange, 405, "Metodo nao permitido. Use GET para consultar uma garantia.");
    }

    private void enviarJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = gson.toJson(data);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void enviarErro(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
