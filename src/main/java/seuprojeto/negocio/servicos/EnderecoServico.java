package seuprojeto.negocio.servicos;

import seuprojeto.excecao.ValidacaoExcecao;
import seuprojeto.infra.bd.ConexaoBancoDados;
import seuprojeto.negocio.bo.Endereco;
import seuprojeto.negocio.dao.EnderecoDAO;
import seuprojeto.negocio.validacao.Validador;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class EnderecoServico {

    public Endereco inserir(Endereco payload, Map<String,String> seedInfo) throws SQLException, ValidacaoExcecao {
        if (payload == null) throw new ValidacaoExcecao("Payload de endereco ausente");

        if (payload.getLogradouroCEP() != null) {
            payload.setLogradouroCEP(payload.getLogradouroCEP().replaceAll("\\D", ""));
        }
        if (payload.getLogradouroCEP() == null || !Validador.cep(payload.getLogradouroCEP())) {
            throw new ValidacaoExcecao("CEP invalido: envie 8 digitos (somente numeros)");
        }
        if (payload.getNumeroEndereco() == null || payload.getNumeroEndereco().isEmpty()) {
            throw new ValidacaoExcecao("Numero do endereco obrigatorio");
        }

        try (Connection conn = ConexaoBancoDados.criarConexao()) {
            EnderecoDAO dao = new EnderecoDAO(conn);

            boolean logradouroExiste = dao.existeLogradouroCep(payload.getLogradouroCEP());
            if (!logradouroExiste) {
                String siglaUF = upperOrDefault(seedInfo.get("uf"), "NA");
                String nomeUF = defaultIfBlank(seedInfo.get("nomeUF"), "N/A");
                String nomeCidade = defaultIfBlank(seedInfo.get("cidade"), "Desconhecida");
                String nomeBairro = defaultIfBlank(seedInfo.get("bairro"), "Centro");
                String nomeLogradouro = defaultIfBlank(seedInfo.get("logradouro"), "Logradouro");
                String siglaLog = defaultIfBlank(seedInfo.get("siglaLog"), "R");

                conn.setAutoCommit(false);
                try {
                    dao.upsertUF(siglaUF, nomeUF);
                    int idCidade = dao.upsertCidade(nomeCidade, siglaUF);
                    int idBairro = dao.upsertBairro(nomeBairro);
                    dao.ensureSiglaLogradouro(siglaLog, siglaLog);
                    dao.upsertLogradouro(payload.getLogradouroCEP(), nomeLogradouro, siglaLog, idBairro, idCidade);
                    conn.commit();
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            }

            return dao.inserir(payload);
        }
    }

    private static String defaultIfBlank(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }
    private static String upperOrDefault(String s, String def) {
        String v = defaultIfBlank(s, def);
        return v.toUpperCase();
    }
}
