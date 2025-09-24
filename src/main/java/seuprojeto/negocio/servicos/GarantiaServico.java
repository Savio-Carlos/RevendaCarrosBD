// Pacote: seuprojeto.servico
package seuprojeto.negocio.servicos;

import seuprojeto.negocio.bo.Garantia;
import seuprojeto.negocio.dao.GarantiaDAO;
import seuprojeto.excecao.ValidacaoExcecao;

import java.sql.SQLException;

public class GarantiaServico {

    private GarantiaDAO garantiaDAO;

    public GarantiaServico() {
        this.garantiaDAO = new GarantiaDAO();
    }

    public Garantia buscarPorId(int id) throws SQLException, ValidacaoExcecao {
        if (id <= 0) {
            throw new ValidacaoExcecao("O ID da garantia deve ser um número positivo.");
        }
        return garantiaDAO.buscarPorId(id);
    }
}
