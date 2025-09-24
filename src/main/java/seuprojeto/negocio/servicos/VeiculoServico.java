// Pacote: seuprojeto.servico
package seuprojeto.negocio.servicos;

import seuprojeto.negocio.bo.Veiculo;
import seuprojeto.infra.bd.ConexaoBancoDados;
import seuprojeto.negocio.dao.VeiculoDAO;
import seuprojeto.exception.ValidacaoExcecao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Year;
import java.util.List;

public class VeiculoServico {

    private VeiculoDAO veiculoDAO;

    public VeiculoServico() {
        this.veiculoDAO = new VeiculoDAO();
    }

    // --- SERVIÇO DE TRANSAÇÃO: CADASTRAR VEÍCULO ---
    public void cadastrarVeiculo(Veiculo veiculo) throws ValidacaoExcecao, SQLException {
        // 1. Validação das Regras de Negócio
        if (veiculo.getNumChassi() == null || veiculo.getNumChassi().trim().length() != 17) {
            throw new ValidacaoExcecao("O número do chassi é obrigatório e deve conter 17 caracteres.");
        }
        if (veiculo.getPlaca() == null || veiculo.getPlaca().trim().length() != 7) {
            throw new ValidacaoExcecao("A placa é obrigatória e deve conter 7 caracteres.");
        }
        if (veiculo.getAnoModelo() <= 1900 || veiculo.getAnoModelo() > Year.now().getValue() + 1) {
            throw new ValidacaoExcecao("Ano do modelo inválido.");
        }
        if (veiculo.getPrecoVeiculo() <= 0) {
            throw new ValidacaoExcecao("O preço do veículo deve ser maior que zero.");
        }
        // ... (outras validações que você já tinha)

        // 2. Lógica de Transação
        Connection conn = null;
        try {
            conn = ConexaoBancoDados.criarConexao();
            conn.setAutoCommit(false); // Inicia a transação

            veiculoDAO.salvar(veiculo, conn);

            conn.commit(); // Finaliza a transação com sucesso

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw new SQLException("Erro ao cadastrar veículo: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    // --- SERVIÇOS DE CONSULTA ---
    public List<Veiculo> buscarTodos() throws SQLException {
        return veiculoDAO.buscarTodos();
    }

    public Veiculo buscarPorChassi(String chassi) throws SQLException {
        return veiculoDAO.buscarPorChassi(chassi);
    }
}
