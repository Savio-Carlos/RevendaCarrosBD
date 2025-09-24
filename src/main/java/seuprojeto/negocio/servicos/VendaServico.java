// Pacote: seuprojeto.servico
package seuprojeto.negocio.servicos;

import seuprojeto.negocio.bo.Venda;
import seuprojeto.infra.bd.ConexaoBancoDados;
import seuprojeto.negocio.dao.VendaDAO;
import seuprojeto.negocio.dao.VeiculoDAO; // Importe o VeiculoDAO
import seuprojeto.excecao.ValidacaoExcecao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class VendaServico {

    private VendaDAO vendaDAO;
    private VeiculoDAO veiculoDAO; // DAO para consultar o veículo

    public VendaServico() {
        this.vendaDAO = new VendaDAO();
        this.veiculoDAO = new VeiculoDAO(); // Instancie o DAO
    }

    public void venderCarro(Venda venda) throws SQLException, ValidacaoExcecao {
        if (venda == null) throw new ValidacaoExcecao("Venda nula");
        if (venda.getIdCliente() <= 0) throw new ValidacaoExcecao("Cliente inválido.");
        if (venda.getIdFuncionario() <= 0) throw new ValidacaoExcecao("Funcionário inválido.");
        if (venda.getNumChassiVeiculo() == null || venda.getNumChassiVeiculo().trim().isEmpty()) {
            throw new ValidacaoExcecao("Chassi do veículo inválido.");
        }
        if (venda.getPrecoVendaVeiculo() <= 0) throw new ValidacaoExcecao("Preço de venda inválido.");

        venda.setDataVenda(new Date());

        Connection conn = null;
        try {
            conn = ConexaoBancoDados.criarConexao();
            conn.setAutoCommit(false);

            // Verifica existência do Cliente
            if (!existeRegistro(conn, "SELECT 1 FROM Cliente WHERE idCliente = ?", venda.getIdCliente())) {
                throw new ValidacaoExcecao("Cliente não encontrado.");
            }
            // Verifica existência do Funcionário
            if (!existeRegistro(conn, "SELECT 1 FROM Funcionario WHERE idFuncionario = ?", venda.getIdFuncionario())) {
                throw new ValidacaoExcecao("Funcionário não encontrado.");
            }

            // Trava o veículo e verifica disponibilidade na mesma transação
            Integer status = obterStatusVeiculoForUpdate(conn, venda.getNumChassiVeiculo());
            if (status == null) {
                throw new ValidacaoExcecao("Veículo com o chassi informado não existe.");
            }
            if (status != 1) { // 1 = Disponível
                throw new ValidacaoExcecao("Este veículo não está disponível para venda.");
            }

            // Salva a venda
            vendaDAO.salvar(venda, conn);

            // Marca como vendido
            veiculoDAO.marcarComoVendido(venda.getNumChassiVeiculo(), conn);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    private boolean existeRegistro(Connection conn, String sql, int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Integer obterStatusVeiculoForUpdate(Connection conn, String chassi) throws SQLException {
        String sql = "SELECT idStatusVeiculo FROM Veiculo WHERE numChassi = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chassi);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                return null;
            }
        }
    }

    // --- Consultas ---
    public Venda buscarPorId(int id) throws SQLException {
        return vendaDAO.buscarPorId(id);
    }

    public java.util.List<Venda> buscarTodos() throws SQLException {
        return vendaDAO.buscarTodos();
    }

    // Resumo enriquecido
    public java.util.List<seuprojeto.negocio.bo.VendaResumo> buscarTodosResumo() throws SQLException {
        return vendaDAO.buscarTodosResumo();
    }

    public seuprojeto.negocio.bo.VendaResumo buscarResumoPorId(int id) throws SQLException {
        return vendaDAO.buscarResumoPorId(id);
    }
}
