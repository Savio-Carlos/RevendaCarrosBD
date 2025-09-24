// Pacote: seuprojeto.servico
package seuprojeto.negocio.servicos;

import seuprojeto.negocio.bo.*;
import seuprojeto.infra.bd.ConexaoBancoDados;
import seuprojeto.negocio.dao.ClienteDAO;
import seuprojeto.negocio.dao.PessoaDAO;
import seuprojeto.negocio.dao.TelefoneDAO;
import seuprojeto.excecao.ValidacaoExcecao;
import seuprojeto.negocio.dao.EmailDAO;
import seuprojeto.negocio.validacao.Validador;

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

    public void cadastrarCliente(Cliente cliente) throws SQLException, ValidacaoExcecao {
        if (cliente == null) throw new ValidacaoExcecao("Cliente não informado.");
        if (cliente.getPessoa() == null) throw new ValidacaoExcecao("Dados da pessoa não fornecidos.");
        if (cliente.getSenhaHash() == null || cliente.getSenhaHash().trim().length() < 3) {
            throw new ValidacaoExcecao("A senha deve ter no mínimo 3 caracteres.");
        }

        Pessoa pessoa = cliente.getPessoa();
        java.util.List<Email> emails = pessoa.getEmails();
        if ((emails == null || emails.isEmpty()) && pessoa.getEmail() != null && !pessoa.getEmail().isBlank()) {
            Email e = new Email();
            e.setEmail(pessoa.getEmail());
            emails = java.util.List.of(e);
            pessoa.setEmails(emails);
        }
        if (emails == null || emails.isEmpty()) {
            throw new ValidacaoExcecao("É necessário fornecer um Email.");
        }

        for (Email e : emails) {
            if (e == null || e.getEmail() == null) throw new ValidacaoExcecao("Email inválido.");
            String em = e.getEmail().trim();
            if (!Validador.email(em)) throw new ValidacaoExcecao("Email inválido.");
            e.setEmail(em);
        }

        java.util.List<Telefone> tels = pessoa.getTelefones();
        if ((tels == null || tels.isEmpty()) && pessoa.getTelefone() != null && !pessoa.getTelefone().isBlank()) {
            Telefone t = new Telefone();
            t.setNumeroTelefone(pessoa.getTelefone());
            tels = java.util.List.of(t);
            pessoa.setTelefones(tels);
        }
        if (tels != null) {
            for (Telefone t : tels) {
                if (t == null || t.getNumeroTelefone() == null) throw new ValidacaoExcecao("Telefone inválido.");
                String digits = t.getNumeroTelefone().replaceAll("\\D", "");
                if (!Validador.telefone(digits)) throw new ValidacaoExcecao("Telefone inválido: use DDD+numero");
                t.setNumeroTelefone(digits);
            }
        }

        if (pessoa instanceof PessoaFisica) {
            PessoaFisica pf = (PessoaFisica) pessoa;
            if (pf.getCpf() == null) throw new ValidacaoExcecao("CPF obrigatório.");
            String cpf = pf.getCpf().replaceAll("\\D", "");
            if (!Validador.cpf(cpf)) throw new ValidacaoExcecao("CPF inválido.");
            pf.setCpf(cpf);
        } else if (pessoa instanceof PessoaJuridica) {
            PessoaJuridica pj = (PessoaJuridica) pessoa;
            if (pj.getCnpj() == null) throw new ValidacaoExcecao("CNPJ obrigatório.");
            String cnpj = pj.getCnpj().replaceAll("\\D", "");
            if (!Validador.cnpj(cnpj)) throw new ValidacaoExcecao("CNPJ inválido.");
            pj.setCnpj(cnpj);
        } else {
            throw new ValidacaoExcecao("Tipo de pessoa inválido para o cliente.");
        }

        Connection conn = null;
        try {
            conn = ConexaoBancoDados.criarConexao();
            conn.setAutoCommit(false);

            // Salva a parte pessoa e obtem o id
            Pessoa pessoaSalva = pessoaDAO.salvar(cliente.getPessoa(), conn);
            // o idCliente eh o idPessoa
            if (cliente.getPessoa() == null) cliente.setPessoa(pessoaSalva);

            // salva se e pessoa juridica ou fisica
            if (pessoaSalva instanceof PessoaFisica) {
                pessoaDAO.salvarPessoaFisica((PessoaFisica) pessoaSalva, conn);
            } else if (pessoaSalva instanceof PessoaJuridica) {
                // pessoaDAO.salvarPessoaJuridica((PessoaJuridica) pessoaSalva, conn);
            }

            // salvar o cliente
            clienteDAO.salvar(cliente, conn);
            TelefoneDAO telefoneDAO = new TelefoneDAO(conn);
            if (pessoaSalva.getTelefones() != null) {
                for (Telefone tel : pessoaSalva.getTelefones()) {
                    tel.setIdPessoa(pessoaSalva.getIdPessoa());
                    telefoneDAO.inserir(tel); 
                }
            }
            // salvar os emails
            for(Email email : pessoaSalva.getEmails()){
                email.setIdPessoa(pessoaSalva.getIdPessoa()); 
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

    //consultas
    public List<Cliente> buscarTodos() throws SQLException {
        return clienteDAO.buscarTodos();
    }

    public Cliente buscarPorId(int id) throws SQLException {
        return clienteDAO.buscarPorId(id);
    }
}
