// Pacote: seuprojeto.requests
package seuprojeto.interfacehttp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import seuprojeto.negocio.bo.Venda;
import seuprojeto.excecao.ValidacaoExcecao;
import seuprojeto.negocio.servicos.VendaServico;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class VendaHTTP implements HttpHandler {

    private final VendaServico vendaServico = new VendaServico();
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

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
            // Futuramente, podemos implementar o GET para buscar vendas
            handleMethodNotAllowed(exchange);
        }
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            // /api/vendas/{id}
            if (parts.length == 4) {
                int id = Integer.parseInt(parts[3]);
                var venda = vendaServico.buscarResumoPorId(id);
                if (venda != null) {
                    sendJsonResponse(exchange, 200, venda);
                } else {
                    sendErrorResponse(exchange, 404, "Venda nao encontrada.");
                }
                return;
            }
            // /api/vendas (lista enriquecida)
            var vendas = vendaServico.buscarTodosResumo();
            sendJsonResponse(exchange, 200, vendas);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "ID invalido.");
        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Erro interno ao buscar vendas.");
        }
    }
    private void handleOptionsRequest(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        exchange.sendResponseHeaders(204, -1); // 204 No Content
    }
    private void handlePostRequest(HttpExchange exchange) throws IOException {
        try {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            Venda venda = gson.fromJson(isr, Venda.class);

            // Validação básica de payload
            if (venda == null) {
                throw new ValidacaoExcecao("Payload de venda ausente");
            }
            if (venda.getIdCliente() <= 0) {
                throw new ValidacaoExcecao("'idCliente' obrigatorio");
            }
            if (venda.getIdFuncionario() <= 0) {
                throw new ValidacaoExcecao("'idFuncionario' obrigatorio");
            }
            if (venda.getNumChassiVeiculo() == null || venda.getNumChassiVeiculo().trim().isEmpty()) {
                throw new ValidacaoExcecao("'numChassiVeiculo' obrigatorio");
            }
            if (venda.getPrecoVendaVeiculo() <= 0) {
                throw new ValidacaoExcecao("'precoVendaVeiculo' deve ser > 0");
            }

            // Chama serviço transacional: retorna venda com idVenda preenchido
            vendaServico.venderCarro(venda);

            sendJsonResponse(exchange, 201, venda);

        } catch (ValidacaoExcecao e) {
            sendErrorResponse(exchange, 400, "Erro de validacao: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Erro interno no servidor ao processar a venda: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 400, "Erro nos dados da requisicao: " + e.getMessage());
        }
    }

    private void handleMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendErrorResponse(exchange, 405, "Metodo nao permitido. Use POST para criar uma venda.");
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = gson.toJson(data);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
    exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
