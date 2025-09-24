package seuprojeto.interfacehttp;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import seuprojeto.excecao.ValidacaoExcecao;
import seuprojeto.negocio.bo.Veiculo;
import seuprojeto.negocio.servicos.VeiculoServico;
import seuprojeto.negocio.validacao.Validador;

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
        tratarOptions(exchange);
            return;
        }

        if ("GET".equalsIgnoreCase(metodo)) {
        tratarGet(exchange);
        } else if ("POST".equalsIgnoreCase(metodo)) {
        tratarPost(exchange);
        } else {
        metodoNaoPermitido(exchange);
        }
    }
    private void tratarOptions(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        exchange.sendResponseHeaders(204, -1);
    }
    private void tratarGet(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");

            // se o path for /api/veiculos/{chassi}, o tamanho sera 4
            if (pathParts.length == 4) {
                String chassi = pathParts[3];
                Veiculo veiculo = veiculoServico.buscarPorChassi(chassi);

                if (veiculo != null) {
                    enviarJson(exchange, 200, veiculo);
                } else {
                    enviarErro(exchange, 404, "Veiculo nao encontrado.");
                }
            } else { // se for /api/veiculos, o tamanho sera 3
                List<Veiculo> veiculos = veiculoServico.buscarTodos();
                enviarJson(exchange, 200, veiculos);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            enviarErro(exchange, 500, "Erro interno no servidor ao acessar o banco de dados.");
        }
    }

    private void tratarPost(HttpExchange exchange) throws IOException {
        try {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            Veiculo veiculo = gson.fromJson(isr, Veiculo.class);

            if (veiculo == null) {
                throw new ValidacaoExcecao("Payload de veiculo ausente");
            }

            // Delega validacoes e cadastro ao serviço
            veiculoServico.cadastrarVeiculo(veiculo);

            enviarErro(exchange, 201, "Veiculo criado com sucesso!");

        } catch (ValidacaoExcecao e) {
            enviarErro(exchange, 400, "Erro de validacao: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            enviarErro(exchange, 500, "Erro interno no servidor ao acessar o banco de dados.");
        }
    }

    private void metodoNaoPermitido(HttpExchange exchange) throws IOException {
        enviarErro(exchange, 405, "Metodo nao permitido");
    }

    private void enviarJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = gson.toJson(data);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
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
