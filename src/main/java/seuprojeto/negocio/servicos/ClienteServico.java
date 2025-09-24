// Pacote: seuprojeto.servico
package seuprojeto.negocio.servicos;

import seuprojeto.negocio.bo.*;
import seuprojeto.infra.bd.ConexaoBancoDados;
import seuprojeto.negocio.dao.ClienteDAO;
import seuprojeto.negocio.dao.PessoaDAO;
import seuprojeto.negocio.dao.TelefoneDAO;
import seuprojeto.exception.ValidacaoExcecao;
import seuprojeto.negocio.dao.EmailDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ClienteServico {

    private PessoaDAO pessoaDAO;
    private ClienteDAO clienteDAO;
    private EmailDAO emailDAO;



    public ClienteServico() {
        this.pessoaDAO = new PessoaDAO();
        this.clienteDAO = new ClienteDAO();
        this.emailDAO = new EmailDAO();
    }

    // --- SERVIÇO DE TRANSAÇÃO: CADASTRAR CLIENTE ---
    public void cadastrarCliente(Cliente cliente) throws SQLException, ValidacaoExcecao {
        // 1. Validação
        if (cliente.getPessoa() == null) {
            throw new ValidacaoExcecao("Dados da pessoa não fornecidos.");
        }
        if (cliente.getPessoa().getEmails() == null || cliente.getPessoa().getEmails().isEmpty()) {
            throw new ValidacaoExcecao("É necessário fornecer um Email.");
        }
        if (cliente.getPessoa() instanceof PessoaFisica) {
            PessoaFisica pf = (PessoaFisica) cliente.getPessoa();
            if (pf.getCpf() == null || pf.getCpf().trim().length() != 11) {
                throw new ValidacaoExcecao("CPF inválido.");
            }
        } else if (cliente.getPessoa() instanceof PessoaJuridica) {
            PessoaJuridica pj = (PessoaJuridica) cliente.getPessoa();
            if (pj.getCnpj() == null || pj.getCnpj().trim().length() != 14) {
                throw new ValidacaoExcecao("CNPJ inválido.");
            }
        } else {
            throw new ValidacaoExcecao("Tipo de pessoa inválido para o cliente.");
        }

        if (cliente.getSenhaHash() == null || cliente.getSenhaHash().length() < 3) {
            throw new ValidacaoExcecao("A senha deve ter no mínimo 3 caracteres.");
        }
        if (cliente.getPessoa().getEmails() == null || cliente.getPessoa().getEmails().isEmpty()) {
            throw new ValidacaoExcecao("É necessário fornecer um Email.");
        }
        // 2. Lógica de Transação
        Connection conn = null;
        try {
            conn = ConexaoBancoDados.criarConexao();
            conn.setAutoCommit(false);

            // Passo A: Salva a parte 'Pessoa' e obtém o ID
            Pessoa pessoaSalva = pessoaDAO.salvar(cliente.getPessoa(), conn); // <--- PASSANDO A CONEXÃO
            // o idCliente é o idPessoa
            if (cliente.getPessoa() == null) cliente.setPessoa(pessoaSalva);

            // Passo B: Salva a parte específica (Física ou Jurídica)
            if (pessoaSalva instanceof PessoaFisica) {
                pessoaDAO.salvarPessoaFisica((PessoaFisica) pessoaSalva, conn);
            } else if (pessoaSalva instanceof PessoaJuridica) {
                // pessoaDAO.salvarPessoaJuridica((PessoaJuridica) pessoaSalva, conn);
            }

            // Passo C: Salva a parte 'Cliente'
            clienteDAO.salvar(cliente, conn);
            TelefoneDAO telefoneDAO = new TelefoneDAO(conn);
            if (pessoaSalva.getTelefones() != null) {
                for (Telefone tel : pessoaSalva.getTelefones()) {
                    tel.setIdPessoa(pessoaSalva.getIdPessoa());
                    telefoneDAO.inserir(tel); // método que recebe conn para participar da mesma transação
                }
            }
            // Passo D: Salva os Emails
            for(Email email : pessoaSalva.getEmails()){
                email.setIdPessoa(pessoaSalva.getIdPessoa()); // Garante que o email tem o ID da pessoa
                emailDAO.salvar(email, conn);
            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw new SQLException("Erro ao cadastrar cliente: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    // --- SERVIÇOS DE CONSULTA ---
    public List<Cliente> buscarTodos() throws SQLException {
        return clienteDAO.buscarTodos();
    }

    public Cliente buscarPorId(int id) throws SQLException {
        return clienteDAO.buscarPorId(id);
    }
}
