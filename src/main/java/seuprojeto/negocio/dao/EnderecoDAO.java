package seuprojeto.negocio.dao;

import seuprojeto.negocio.bo.Endereco;

import java.sql.*;

public class EnderecoDAO {
    private final Connection conn;

    public EnderecoDAO(Connection conn) { this.conn = conn; }

    public Endereco inserir(Endereco e) throws SQLException {
        String sql = "INSERT INTO Endereco (Logradouro_CEP, numeroEndereco, complementoEndereco, referencia) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, e.getLogradouroCEP());
            stmt.setString(2, e.getNumeroEndereco());
            stmt.setString(3, e.getComplementoEndereco());
            stmt.setString(4, e.getReferencia());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) e.setIdEndereco(rs.getInt(1));
            }
        }
        return e;
    }

    public boolean existePorId(int idEndereco) throws SQLException {
        String sql = "SELECT 1 FROM Endereco WHERE idEndereco = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEndereco);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeLogradouroCep(String cep) throws SQLException {
        String sql = "SELECT 1 FROM Logradouro WHERE CEP = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cep);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void upsertUF(String siglaUF, String nomeUF) throws SQLException {
        String check = "SELECT 1 FROM UF WHERE siglaUF = ?";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, siglaUF);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return;
            }
        }
        String insert = "INSERT INTO UF (siglaUF, nomeUF) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, siglaUF);
            ps.setString(2, nomeUF);
            ps.executeUpdate();
        }
    }

    public int upsertBairro(String nomeBairro) throws SQLException {
        String sel = "SELECT idBairro FROM Bairro WHERE nomeBairro = ?";
        try (PreparedStatement ps = conn.prepareStatement(sel)) {
            ps.setString(1, nomeBairro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        String ins = "INSERT INTO Bairro (nomeBairro) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nomeBairro);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(sel)) {
            ps.setString(1, nomeBairro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Falha ao upsert Bairro");
    }

    public int upsertCidade(String nomeCidade, String siglaUF) throws SQLException {
        String sel = "SELECT idCidade FROM Cidade WHERE nomeCidade = ? AND UF_siglaUF = ?";
        try (PreparedStatement ps = conn.prepareStatement(sel)) {
            ps.setString(1, nomeCidade);
            ps.setString(2, siglaUF);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        String ins = "INSERT INTO Cidade (nomeCidade, UF_siglaUF) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nomeCidade);
            ps.setString(2, siglaUF);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(sel)) {
            ps.setString(1, nomeCidade);
            ps.setString(2, siglaUF);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Falha ao upsert Cidade");
    }

    public void ensureSiglaLogradouro(String sigla, String nomeSigla) throws SQLException {
        String sel = "SELECT 1 FROM SiglaLogradouro WHERE siglaLogradouro = ?";
        try (PreparedStatement ps = conn.prepareStatement(sel)) {
            ps.setString(1, sigla);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return;
            }
        }
        String ins = "INSERT INTO SiglaLogradouro (siglaLogradouro, nomeSigla) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(ins)) {
            ps.setString(1, sigla);
            ps.setString(2, nomeSigla);
            ps.executeUpdate();
        }
    }

    public void upsertLogradouro(String cep, String nomeLogradouro, String siglaLogradouro, int idBairro, int idCidade) throws SQLException {
        String check = "SELECT 1 FROM Logradouro WHERE CEP = ?";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, cep);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return;
            }
        }
        String ins = "INSERT INTO Logradouro (CEP, nomeLogradouro, SiglaLogradouro_siglaLogradouro, idBairro, idCidade) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(ins)) {
            ps.setString(1, cep);
            ps.setString(2, nomeLogradouro);
            ps.setString(3, siglaLogradouro);
            ps.setInt(4, idBairro);
            ps.setInt(5, idCidade);
            ps.executeUpdate();
        }
    }
}
