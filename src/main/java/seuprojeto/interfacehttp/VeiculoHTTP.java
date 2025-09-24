// Pacote: com.seuprojeto.handler
package seuprojeto.interfacehttp;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import seuprojeto.excecao.ValidacaoExcecao;
import seuprojeto.negocio.bo.Veiculo;
import seuprojeto.negocio.servicos.VeiculoServico;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public class VeiculoHTTP implements HttpHandler {

    private final VeiculoServico veiculoServico = new VeiculoServico();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String metodo = exchange.getRequestMethod();

        if ("OPTIONS".equalsIgnoreCase(metodo)) {
            handleOptionsRequest(exchange);
            return;
        }

        if ("GET".equalsIgnoreCase(metodo)) {
            handleGetRequest(exchange);
        } else if ("POST".equalsIgnoreCase(metodo)) {
            handlePostRequest(exchange);
        } else {
            handleMethodNotAllowed(exchange);
        }
    }
    private void handleOptionsRequest(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        exchange.sendResponseHeaders(204, -1); // 204 No Content
    }
    private void handleGetRequest(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");

            // Se o path for /api/veiculos/{chassi}, o tamanho será 4
            if (pathParts.length == 4) {
                String chassi = pathParts[3];
                Veiculo veiculo = veiculoServico.buscarPorChassi(chassi);

                if (veiculo != null) {
                    sendJsonResponse(exchange, 200, veiculo);
                } else {
                    sendErrorResponse(exchange, 404, "Veiculo nao encontrado.");
                }
            } else { // Se for /api/veiculos, o tamanho será 3
                List<Veiculo> veiculos = veiculoServico.buscarTodos();
                sendJsonResponse(exchange, 200, veiculos);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Erro interno no servidor ao acessar o banco de dados.");
        }
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        try {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            Veiculo veiculo = gson.fromJson(isr, Veiculo.class);

            // Chamada ao método correto no serviço!
            veiculoServico.cadastrarVeiculo(veiculo);

            sendErrorResponse(exchange, 201, "Veiculo criado com sucesso!");

        } catch (ValidacaoExcecao e) {
            sendErrorResponse(exchange, 400, "Erro de validacao: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Erro interno no servidor ao acessar o banco de dados.");
        }
    }

    private void handleMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendErrorResponse(exchange, 405, "Metodo nao permitido");
    }

    // Métodos auxiliares para enviar respostas
    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = gson.toJson(data);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
