package seuprojeto.negocio.dao;

import seuprojeto.negocio.bo.Telefone;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TelefoneDAO {
    private final Connection conn;

    public TelefoneDAO(Connection conn) {
        this.conn = conn;
    }

    public void inserir(Telefone telefone) throws SQLException {
        String sql = "INSERT INTO Telefone (numeroTelefone, ddd, ddi, idPessoa) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, telefone.getNumeroTelefone());
            stmt.setInt(2, telefone.getDdd());
            stmt.setInt(3, telefone.getDdi());
            stmt.setInt(4, telefone.getIdPessoa());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    telefone.setIdTelefone(rs.getInt(1));
                }
            }
        }
    }

    public Telefone buscarPorId(int idTelefone) throws SQLException {
        String sql = "SELECT * FROM Telefone WHERE idTelefone = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idTelefone);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Telefone> buscarPorPessoa(int idPessoa) throws SQLException {
        List<Telefone> lista = new ArrayList<>();
        String sql = "SELECT * FROM Telefone WHERE idPessoa = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPessoa);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSet(rs));
                }
            }
        }
        return lista;
    }

    public void atualizar(Telefone telefone) throws SQLException {
        String sql = "UPDATE Telefone SET numeroTelefone = ?, ddd = ?, ddi = ?, idPessoa = ? WHERE idTelefone = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, telefone.getNumeroTelefone());
            stmt.setInt(2, telefone.getDdd());
            stmt.setInt(3, telefone.getDdi());
            stmt.setInt(4, telefone.getIdPessoa());
            stmt.setInt(5, telefone.getIdTelefone());
            stmt.executeUpdate();
        }
    }

    public void deletar(int idTelefone) throws SQLException {
        String sql = "DELETE FROM Telefone WHERE idTelefone = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idTelefone);
            stmt.executeUpdate();
        }
    }

    private Telefone mapResultSet(ResultSet rs) throws SQLException {
        Telefone telefone = new Telefone();
        telefone.setIdTelefone(rs.getInt("idTelefone"));
        telefone.setNumeroTelefone(rs.getString("numeroTelefone"));
        telefone.setDdd(rs.getInt("ddd"));
        telefone.setDdi(rs.getInt("ddi"));
        telefone.setIdPessoa(rs.getInt("idPessoa"));
        return telefone;
    }
}
