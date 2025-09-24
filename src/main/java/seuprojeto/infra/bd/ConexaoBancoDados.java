// Pacote: com.seuprojeto.config
package seuprojeto.infra.bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBancoDados {
    private static final String URL = "jdbc:mysql://localhost:3306/concessionaria_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
    private static final String USUARIO = "root";
    private static final String SENHA = "123";

    public static Connection criarConexao() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            //checagem de JDBC
            System.err.println("Driver JDBC não encontrado: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}
