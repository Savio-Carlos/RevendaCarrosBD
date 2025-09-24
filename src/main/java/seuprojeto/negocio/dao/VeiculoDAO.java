package seuprojeto.negocio.dao;

import seuprojeto.negocio.bo.Veiculo;
import seuprojeto.infra.bd.ConexaoBancoDados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VeiculoDAO {

    public Veiculo salvar(Veiculo veiculo, Connection conn) throws SQLException {
        String sql = "INSERT INTO Veiculo (numChassi, placa, marcaCarro, modeloVeiculo, anoModelo, quilometragem, cor, precoVeiculo, descricao, fotos, idtipoCombustivel, idGarantia, idStatusVeiculo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, veiculo.getNumChassi());
            pstmt.setString(2, veiculo.getPlaca());
            pstmt.setString(3, veiculo.getMarcaCarro());
            pstmt.setString(4, veiculo.getModeloVeiculo());
            pstmt.setInt(5, veiculo.getAnoModelo());
            pstmt.setInt(6, veiculo.getQuilometragem());
            pstmt.setString(7, veiculo.getCor());
            pstmt.setDouble(8, veiculo.getPrecoVeiculo());
            pstmt.setString(9, veiculo.getDescricao());
            pstmt.setString(10, veiculo.getFotos());
            pstmt.setInt(11, veiculo.getIdtipoCombustivel());
            pstmt.setInt(12, veiculo.getIdGarantia());
            pstmt.setInt(13, veiculo.getIdStatusVeiculo());

            pstmt.executeUpdate();
        }
        return veiculo;
    }

    public void marcarComoVendido(String numChassi, Connection conn) throws SQLException {
        String sql = "UPDATE Veiculo SET idStatusVeiculo = ? WHERE numChassi = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, 2);
            pstmt.setString(2, numChassi);
            pstmt.executeUpdate();
        }
    }

    public Veiculo buscarPorChassi(String chassi) throws SQLException {
        String sql = "SELECT * FROM Veiculo WHERE numChassi = ?";
        try (Connection conn = ConexaoBancoDados.criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, chassi);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return resultSetParaVeiculo(rs);
                }
            }
        }
        return null;
    }

    public List<Veiculo> buscarTodos() throws SQLException {
        String sql = "SELECT * FROM Veiculo";
        List<Veiculo> veiculos = new ArrayList<>();
        try (Connection conn = ConexaoBancoDados.criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                veiculos.add(resultSetParaVeiculo(rs));
            }
        }
        return veiculos;
    }

    private Veiculo resultSetParaVeiculo(ResultSet rs) throws SQLException {
        Veiculo veiculo = new Veiculo();
        veiculo.setNumChassi(rs.getString("numChassi"));
        veiculo.setPlaca(rs.getString("placa"));
        veiculo.setMarcaCarro(rs.getString("marcaCarro"));
        veiculo.setModeloVeiculo(rs.getString("modeloVeiculo"));
        veiculo.setAnoModelo(rs.getInt("anoModelo"));
        veiculo.setQuilometragem(rs.getInt("quilometragem"));
        veiculo.setCor(rs.getString("cor"));
        veiculo.setPrecoVeiculo(rs.getDouble("precoVeiculo"));
        veiculo.setDescricao(rs.getString("descricao"));
        veiculo.setFotos(rs.getString("fotos"));
        veiculo.setIdGarantia(rs.getInt("idGarantia"));
        veiculo.setIdStatusVeiculo(rs.getInt("idStatusVeiculo"));

        return veiculo;
    }
}
