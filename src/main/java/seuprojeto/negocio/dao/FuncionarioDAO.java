package seuprojeto.negocio.dao;

import seuprojeto.negocio.bo.FuncionarioResumo;
import seuprojeto.infra.bd.ConexaoBancoDados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FuncionarioDAO {

    public List<FuncionarioResumo> listarResumido() throws SQLException {
        String sql = "SELECT f.idFuncionario, pf.nome " +
                "FROM Funcionario f " +
                "JOIN PessoaFisica pf ON f.idFuncionario = pf.idPessoaFisica " +
                "WHERE f.status = TRUE " +
                "ORDER BY pf.nome";
        List<FuncionarioResumo> lista = new ArrayList<>();
        try (Connection conn = ConexaoBancoDados.criarConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                FuncionarioResumo fr = new FuncionarioResumo();
                fr.setIdFuncionario(rs.getInt("idFuncionario"));
                fr.setNome(rs.getString("nome"));
                lista.add(fr);
            }
        }
        return lista;
    }
}
