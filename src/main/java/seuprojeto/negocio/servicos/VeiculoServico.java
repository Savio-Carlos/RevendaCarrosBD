// Pacote: seuprojeto.servico
package seuprojeto.negocio.servicos;

import seuprojeto.negocio.bo.Veiculo;
import seuprojeto.infra.bd.ConexaoBancoDados;
import seuprojeto.negocio.dao.VeiculoDAO;
import seuprojeto.excecao.ValidacaoExcecao;
import seuprojeto.negocio.validacao.Validador;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Year;
import java.util.List;

public class VeiculoServico {

    private VeiculoDAO veiculoDAO;

    public VeiculoServico() {
        this.veiculoDAO = new VeiculoDAO();
    }

    // cadastrar veiculo
    public void cadastrarVeiculo(Veiculo veiculo) throws ValidacaoExcecao, SQLException {
        if (veiculo == null) {
            throw new ValidacaoExcecao("Dados do veículo não fornecidos.");
        }
        // Chassi
        String chassi = veiculo.getNumChassi();
        if (chassi == null || chassi.isBlank()) {
            throw new ValidacaoExcecao("Número do chassi é obrigatório.");
        }
        chassi = chassi.toUpperCase().replaceAll("\\s+", "");
        if (!Validador.vin(chassi)) {
            throw new ValidacaoExcecao("Chassi invalido: deve ter 17 caracteres (A-H,J-N,P,R-Z,0-9)");
        }
        veiculo.setNumChassi(chassi);

        // Placa
        String placa = veiculo.getPlaca();
        if (placa == null || placa.isBlank()) {
            throw new ValidacaoExcecao("Placa é obrigatória.");
        }
        placa = placa.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (!Validador.placa(placa)) {
            throw new ValidacaoExcecao("Placa invalida: use ABC1234 ou ABC1D23");
        }
        veiculo.setPlaca(placa);

        // Preço e ano
        if (veiculo.getPrecoVeiculo() <= 0) {
            throw new ValidacaoExcecao("O preço do veículo deve ser maior que zero.");
        }
        int currentYear = Year.now().getValue();
        if (veiculo.getAnoModelo() < 1950 || veiculo.getAnoModelo() > currentYear + 1) {
            throw new ValidacaoExcecao("Ano do modelo inválido.");
        }

        // logica de Transação
        Connection conn = null;
        try {
            conn = ConexaoBancoDados.criarConexao();
            conn.setAutoCommit(false);

            veiculoDAO.salvar(veiculo, conn);

            conn.commit();

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

    public List<Veiculo> buscarTodos() throws SQLException {
        return veiculoDAO.buscarTodos();
    }

    public Veiculo buscarPorChassi(String chassi) throws SQLException {
        return veiculoDAO.buscarPorChassi(chassi);
    }
}
