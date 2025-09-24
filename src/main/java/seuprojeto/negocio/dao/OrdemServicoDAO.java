package seuprojeto.negocio.dao;

import seuprojeto.negocio.bo.OrdemServico;
import seuprojeto.negocio.bo.ServicoRealizado;
import seuprojeto.infra.bd.ConexaoBancoDados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OrdemServicoDAO {

    public OrdemServico salvar(OrdemServico os, Connection conn) throws SQLException {
        String sql = "INSERT INTO OrdemServico (dataAbertura, dataFechamento, descricaoServico, quilometragemEntrada, valorTotal, numChassiVeiculo, idCliente, idFuncionario) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDate(1, new java.sql.Date(os.getDataAbertura().getTime()));

            if (os.getDataFechamento() != null) {
                pstmt.setDate(2, new java.sql.Date(os.getDataFechamento().getTime()));
            } else {
                pstmt.setNull(2, java.sql.Types.DATE);
            }

            pstmt.setString(3, os.getDescricaoServico());
            pstmt.setInt(4, os.getQuilometragemEntrada());
            pstmt.setDouble(5, os.getValorTotal());
            pstmt.setString(6, os.getNumChassiVeiculo());
            pstmt.setInt(7, os.getIdCliente());
            pstmt.setInt(8, os.getIdFuncionario());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    os.setIdOrdemServico(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Falha ao obter o ID da Ordem de Serviço.");
                }
            }
        }
        return os;
    }

    public void salvarServicoRealizado(ServicoRealizado item, Connection conn) throws SQLException {
        String sql = "INSERT INTO ServicoRealizado (quantidade, precoUnitario, idPeca, idServico, idOrdemServico) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, item.getQuantidade());
            pstmt.setDouble(2, item.getPrecoUnitario());

            if (item.getIdPeca() > 0) {
                pstmt.setInt(3, item.getIdPeca());
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }

            if (item.getIdServico() > 0) {
                pstmt.setInt(4, item.getIdServico());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }

            pstmt.setInt(5, item.getIdOrdemServico());

            pstmt.executeUpdate();
        }
    }
}
