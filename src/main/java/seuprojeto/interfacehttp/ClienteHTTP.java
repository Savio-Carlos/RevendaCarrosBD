// Pacote: com.seuprojeto.handler
package seuprojeto.interfacehttp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import seuprojeto.negocio.bo.*;
import seuprojeto.excecao.ValidacaoExcecao;
// Validacoes agora centralizadas no servico

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
        exchange.sendResponseHeaders(204, -1); // 204 No Content
    }

    private void tratarGet(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");

            if (pathParts.length == 4) {
                int id = Integer.parseInt(pathParts[3]);
                Cliente cliente = clienteServico.buscarPorId(id);

                if (cliente != null) {
                    enviarJson(exchange, 200, cliente);
                } else {
                    enviarErro(exchange, 404, "Cliente nao encontrado.");
                }
            } else {
                List<Cliente> clientes = clienteServico.buscarTodos();
                enviarJson(exchange, 200, clientes);
            }
        } catch (NumberFormatException e) {
            enviarErro(exchange, 400, "ID invalido.");
        } catch (SQLException e) {
            e.printStackTrace();
            enviarErro(exchange, 500, "Erro interno no servidor ao acessar o banco de dados.");
        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(exchange, 500, "Erro inesperado: " + e.getMessage());
        }
    }

    private void tratarPost(HttpExchange exchange) throws IOException {
        try {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);

            com.google.gson.JsonElement tree = com.google.gson.JsonParser.parseReader(isr);
            com.google.gson.JsonObject jsonObject = tree.getAsJsonObject();

            // Montagem leve do modelo a partir do JSON "flat" do front
            Cliente cliente = new Cliente();
            if (jsonObject.has("senhaHash") && !jsonObject.get("senhaHash").isJsonNull()) {
                cliente.setSenhaHash(jsonObject.get("senhaHash").getAsString());
            }

            if (jsonObject.has("cpf")) {
                PessoaFisica pf = gson.fromJson(jsonObject, PessoaFisica.class);

                // Email: aceitar na raiz e/ou lista sem validar aqui
                if (jsonObject.has("email") && !jsonObject.get("email").isJsonNull()) {
                    Email email = new Email();
                    email.setEmail(jsonObject.get("email").getAsString());
                    pf.setEmails(java.util.List.of(email));
                    pf.setEmail(email.getEmail());
                } else if (jsonObject.has("emails") && !jsonObject.get("emails").isJsonNull()) {
                    java.util.List<Email> emails = gson.fromJson(jsonObject.get("emails"), new TypeToken<List<Email>>(){}.getType());
                    pf.setEmails(emails);
                }

                // Telefone: aceitar 'telefone' simples ou lista 'telefones' sem validar aqui
                if (jsonObject.has("telefone") && !jsonObject.get("telefone").isJsonNull()) {
                    pf.setTelefone(jsonObject.get("telefone").getAsString());
                } else if (jsonObject.has("telefones") && !jsonObject.get("telefones").isJsonNull()) {
                    java.util.List<Telefone> tels = gson.fromJson(jsonObject.get("telefones"), new TypeToken<List<Telefone>>(){}.getType());
                    pf.setTelefones(tels);
                }

                // Endereco: idEndereco/complementoEndereco opcionais
                if (jsonObject.has("idEndereco") && !jsonObject.get("idEndereco").isJsonNull()) {
                    try { pf.setIdEndereco(jsonObject.get("idEndereco").getAsInt()); } catch (Exception ignored) {}
                }
                if (jsonObject.has("complementoEndereco") && !jsonObject.get("complementoEndereco").isJsonNull()) {
                    pf.setComplementoEndereco(jsonObject.get("complementoEndereco").getAsString());
                }

                cliente.setPessoa(pf);
            } else {
                // Suporte futuro para PJ pode ser adicionado aqui; por ora, exigimos CPF enviado pelo front
                throw new ValidacaoExcecao("O JSON deve conter um 'cpf'.");
            }

            // Delegar toda a validacao/normalizacao ao servico
            clienteServico.cadastrarCliente(cliente);
            java.util.Map<String,Object> ok = java.util.Map.of("message","Cliente criado com sucesso!");
            enviarJson(exchange, 201, ok);

        } catch (ValidacaoExcecao e) {
            enviarErro(exchange, 400, "Erro de validacao: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            String errorMessage = "Erro de SQL: " + e.getMessage();
            enviarErro(exchange, 500, errorMessage);
        } catch (Exception e) {
            e.printStackTrace();
            enviarErro(exchange, 400, "Erro ao processar o JSON: " + e.getMessage());
        }
    }

    private void metodoNaoPermitido(HttpExchange exchange) throws IOException {
        enviarErro(exchange, 405, "Metodo nao permitido");
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
