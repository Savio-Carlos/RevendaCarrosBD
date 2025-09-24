package seuprojeto.negocio.dao;

import seuprojeto.negocio.bo.Garantia;
import seuprojeto.negocio.bo.StatusGarantia;
import seuprojeto.infra.bd.ConexaoBancoDados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GarantiaDAO {

    public Garantia buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM Garantia g " +
                "LEFT JOIN statusGarantia sg ON g.idStatusGarantia = sg.idstatusGarantia " +
                "WHERE g.idGarantia = ?";

        try (Connection conn = ConexaoBancoDados.criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return resultSetParaGarantia(rs);
                }
            }
        }
        return null;
    }

    private Garantia resultSetParaGarantia(ResultSet rs) throws SQLException {
        Garantia garantia = new Garantia();
        garantia.setIdGarantia(rs.getInt("idGarantia"));
        garantia.setDataInicio(rs.getDate("dataInicio"));
        garantia.setKmLimiteRevisao(rs.getInt("kmLimiteRevisao"));
        garantia.setDataExpiracaoGarantia(rs.getString("dataExpiracaoGarantia"));
        garantia.setIdStatusGarantia(rs.getInt("idStatusGarantia"));

         StatusGarantia status = new StatusGarantia();
         status.setIdStatusGarantia(rs.getInt("idstatusGarantia"));
         status.setStatusGarantia(rs.getString("statusGarantia"));
         garantia.setStatus(status);

        return garantia;
    }
}
