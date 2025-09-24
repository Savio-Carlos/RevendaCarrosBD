// Pacote: com.seuprojeto.handler
package seuprojeto.interfacehttp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import seuprojeto.negocio.bo.*;
import seuprojeto.exception.ValidacaoExcecao;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public class ClienteHTTP implements HttpHandler {

    private final seuprojeto.negocio.servicos.ClienteServico clienteServico = new seuprojeto.negocio.servicos.ClienteServico();
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

            if (pathParts.length == 4) {
                int id = Integer.parseInt(pathParts[3]);
                Cliente cliente = clienteServico.buscarPorId(id);

                if (cliente != null) {
                    sendJsonResponse(exchange, 200, cliente);
                } else {
                    sendErrorResponse(exchange, 404, "Cliente nao encontrado.");
                }
            } else {
                List<Cliente> clientes = clienteServico.buscarTodos();
                sendJsonResponse(exchange, 200, clientes);
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "ID invalido.");
        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Erro interno no servidor ao acessar o banco de dados.");
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Erro inesperado: " + e.getMessage());
        }
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        try {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);

            com.google.gson.JsonElement tree = com.google.gson.JsonParser.parseReader(isr);
            com.google.gson.JsonObject jsonObject = tree.getAsJsonObject();

            Cliente cliente = new Cliente();
            if (!jsonObject.has("senhaHash") || jsonObject.get("senhaHash").getAsString().length() < 3) {
                throw new ValidacaoExcecao("'senhaHash' obrigatoria (min 3 chars)");
            }
            cliente.setSenhaHash(jsonObject.get("senhaHash").getAsString());

            if (jsonObject.has("cpf")) {
                PessoaFisica pf = gson.fromJson(jsonObject, PessoaFisica.class);

                // Email: aceitar na raiz e criar lista
                if (jsonObject.has("email") && !jsonObject.get("email").isJsonNull()) {
                    Email email = new Email();
                    email.setEmail(jsonObject.get("email").getAsString());
                    pf.setEmails(java.util.List.of(email));
                    pf.setEmail(email.getEmail());
                }

                // Telefone: aceitar 'telefone' simples ou lista 'telefones'
                if (jsonObject.has("telefone") && !jsonObject.get("telefone").isJsonNull()) {
                    String numero = jsonObject.get("telefone").getAsString();
                    Telefone tel = new Telefone();
                    tel.setNumeroTelefone(numero);
                    pf.setTelefones(java.util.List.of(tel));
                } else if (jsonObject.has("telefones") && !jsonObject.get("telefones").isJsonNull()) {
                    pf.setTelefones(gson.fromJson(jsonObject.get("telefones"), new TypeToken<List<Telefone>>(){}.getType()));
                }

                // Endereco: idEndereco/complementoEndereco opcionais
                if (jsonObject.has("idEndereco") && !jsonObject.get("idEndereco").isJsonNull()) {
                    try { pf.setIdEndereco(jsonObject.get("idEndereco").getAsInt()); } catch (Exception ignored) {}
                }
                if (jsonObject.has("complementoEndereco") && !jsonObject.get("complementoEndereco").isJsonNull()) {
                    pf.setComplementoEndereco(jsonObject.get("complementoEndereco").getAsString());
                }

                cliente.setPessoa(pf);

            } else { // Adicionar lógica para Pessoa Jurídica se necessário
                throw new ValidacaoExcecao("O JSON deve conter um 'cpf'.");
            }

            clienteServico.cadastrarCliente(cliente);
            String responseMessage = "{\"message\": \"Cliente criado com sucesso!\"}";
            sendJsonResponse(exchange, 201, responseMessage);

        } catch (ValidacaoExcecao e) {
            sendErrorResponse(exchange, 400, "Erro de validacao: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            // AQUI ESTÁ A MUDANÇA: Retornamos a mensagem de erro real do MySQL!
            String errorMessage = "Erro de SQL: " + e.getMessage();
            sendErrorResponse(exchange, 500, errorMessage);
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 400, "Erro ao processar o JSON: " + e.getMessage());
        }
    }

    private void handleMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendErrorResponse(exchange, 405, "Metodo nao permitido");
    }


    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = gson.toJson(data);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        exchange.getResponseHeaders().set("Content-Type", "application/json");
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
