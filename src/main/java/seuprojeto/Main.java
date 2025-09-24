// Pacote: com.seuprojeto
package seuprojeto;

import com.sun.net.httpserver.HttpServer;
import seuprojeto.interfacehttp.ClienteHTTP;
import seuprojeto.interfacehttp.VeiculoHTTP;
import seuprojeto.interfacehttp.EnderecoHTTP;
import seuprojeto.interfacehttp.FuncionarioHTTP;

import java.io.IOException;
import java.net.InetSocketAddress;
import seuprojeto.interfacehttp.VendaHTTP;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        System.out.println("Servidor iniciado na porta " + port);

        server.createContext("/api/clientes", new ClienteHTTP());
        server.createContext("/api/veiculos", new VeiculoHTTP());
        server.createContext("/api/vendas", new VendaHTTP());
        server.createContext("/api/enderecos", new EnderecoHTTP());
    server.createContext("/api/funcionarios", new FuncionarioHTTP());

        server.setExecutor(null);
        server.start();
    }
}