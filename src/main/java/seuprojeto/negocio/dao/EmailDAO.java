package seuprojeto.negocio.dao;

import seuprojeto.negocio.bo.Email;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmailDAO {

    public void salvar(Email email, Connection conn) throws SQLException {
        String sql = "INSERT INTO Email (email, idPessoa) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email.getEmail());
            pstmt.setInt(2, email.getIdPessoa());
            pstmt.executeUpdate();
        }
    }

    public List<Email> getEmailsByPessoaId(Connection conn, int idPessoa) throws SQLException {
        String sql = "SELECT idEmail, email, idPessoa FROM Email WHERE idPessoa = ?";
        List<Email> lista = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idPessoa);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Email e = new Email();
                    e.setIdEmail(rs.getInt("idEmail"));
                    e.setEmail(rs.getString("email"));
                    e.setIdPessoa(rs.getInt("idPessoa"));
                    lista.add(e);
                }
            }
        }
        return lista;
    }

    public String getPrimaryEmail(Connection conn, int idPessoa) throws SQLException {
        String sql = "SELECT email FROM Email WHERE idPessoa = ? LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idPessoa);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        }
        return null;
    }
}
