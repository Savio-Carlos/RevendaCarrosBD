package seuprojeto.negocio.dao;

import seuprojeto.negocio.bo.Email;
import seuprojeto.negocio.bo.Pessoa;
import seuprojeto.negocio.bo.PessoaFisica;
import seuprojeto.negocio.bo.PessoaJuridica;
import seuprojeto.negocio.bo.Telefone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

public class PessoaDAO {

    public Pessoa salvar(Pessoa pessoa, Connection conn) throws SQLException {
        // Atualizado para refletir o novo schema (idEndereco, telefone, email na tabela Pessoa)
        String sql = "INSERT INTO Pessoa (TipoPessoa, idEndereco, complementoEndereco, telefone, email) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // TipoPessoa é obrigatório
            pstmt.setString(1, String.valueOf(pessoa.getTipoPessoa()));

            // idEndereco (nullable)
            int idEndereco = pessoa.getIdEndereco();
            if (idEndereco > 0) {
                // validar existência para evitar erro de FK
                boolean enderecoOk = false;
                try {
                    seuprojeto.negocio.dao.EnderecoDAO endDAO = new seuprojeto.negocio.dao.EnderecoDAO(conn);
                    enderecoOk = endDAO.existePorId(idEndereco);
                } catch (SQLException ignored) { }
                if (enderecoOk) {
                    pstmt.setInt(2, idEndereco);
                } else {
                    pstmt.setNull(2, Types.INTEGER);
                }
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }

            // complementoEndereco (nullable)
            pstmt.setString(3, pessoa.getComplementoEndereco());

            // telefone (opcional): usa campo direto ou o primeiro da lista de telefones
            String telefoneStr = pessoa.getTelefone();
            if ((telefoneStr == null || telefoneStr.isEmpty())) {
                List<Telefone> tels = pessoa.getTelefones();
                if (tels != null && !tels.isEmpty() && tels.get(0) != null) {
                    telefoneStr = tels.get(0).getNumeroTelefone();
                }
            }
            if (telefoneStr != null && !telefoneStr.isEmpty()) {
                pstmt.setString(4, telefoneStr);
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            // email (opcional): usa campo direto ou o primeiro da lista de emails
            String emailStr = pessoa.getEmail();
            if (emailStr == null || emailStr.isEmpty()) {
                List<Email> emails = pessoa.getEmails();
                if (emails != null && !emails.isEmpty() && emails.get(0) != null) {
                    emailStr = emails.get(0).getEmail();
                }
            }
            if (emailStr != null && !emailStr.isEmpty()) {
                pstmt.setString(5, emailStr);
            } else {
                pstmt.setNull(5, Types.VARCHAR);
            }

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pessoa.setIdPessoa(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Falha ao obter o ID da pessoa, nenhuma linha foi criada.");
                }
            }
        }
        return pessoa;
    }

    public PessoaFisica salvarPessoaFisica(PessoaFisica pessoaFisica, Connection conn) throws SQLException {
        // Este método espera uma transação ativa controlada pela camada de Serviço.
        String sql = "INSERT INTO PessoaFisica (idPessoaFisica, nome, CPF, dataNascimento) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pessoaFisica.getIdPessoa()); // Usa o mesmo ID da tabela Pessoa
            pstmt.setString(2, pessoaFisica.getNome());
            pstmt.setString(3, pessoaFisica.getCpf());
            if (pessoaFisica.getDataNascimento() != null) {
                pstmt.setDate(4, new java.sql.Date(pessoaFisica.getDataNascimento().getTime()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }

            pstmt.executeUpdate();
        }
        return pessoaFisica;
    }

    public PessoaJuridica salvarPessoaJuridica(PessoaJuridica pessoaJuridica, Connection conn) throws SQLException {
        // Este método espera uma transação ativa controlada pela camada de Serviço.
        String sql = "INSERT INTO PessoaJuridica (idPessoaJuridica, CNPJ, nomeFantasia, RazaoSocial) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pessoaJuridica.getIdPessoa()); // Usa o mesmo ID da tabela Pessoa
            pstmt.setString(2, pessoaJuridica.getCnpj());
            pstmt.setString(3, pessoaJuridica.getNomeFantasia());
            pstmt.setString(4, pessoaJuridica.getRazaoSocial());

            pstmt.executeUpdate();
        }
        return pessoaJuridica;
    }
}
