package seuprojeto.negocio.dao;

import seuprojeto.negocio.bo.*;
import seuprojeto.infra.bd.ConexaoBancoDados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class ClienteDAO {

    public Cliente salvar(Cliente cliente, Connection conn) throws SQLException {
        String sql = "INSERT INTO Cliente (idCliente, Senha_Hash) VALUES (?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // idCliente é o id da Pessoa associada
                if (cliente.getPessoa() == null) {
                    throw new SQLException("Pessoa nao definida no Cliente ao salvar.");
                }
                pstmt.setInt(1, cliente.getPessoa().getIdPessoa());
            pstmt.setString(2, cliente.getSenhaHash());
            pstmt.executeUpdate();
                cliente.setIdCliente(cliente.getPessoa().getIdPessoa());
        }
        return cliente;
    }

    public Cliente buscarPorId(int id) throws SQLException {
        String sql = baseSelectSql() + " WHERE c.idCliente = ?";

        try (Connection conn = ConexaoBancoDados.criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return resultSetParaCliente(rs, conn);
                }
            }
        }
        return null;
    }

    public List<Cliente> buscarTodos() throws SQLException {
        String sql = baseSelectSql();

        Map<Integer, Cliente> mapa = new HashMap<>();
        try (Connection conn = ConexaoBancoDados.criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int idCliente = rs.getInt("idCliente");
                Cliente cliente = mapa.get(idCliente);
                if (cliente == null) {
                    cliente = resultSetParaCliente(rs, conn);
                    mapa.put(idCliente, cliente);
                }

                // telefones vindos do LEFT JOIN Telefone (pode repetir linhas)
                int idTel = rs.getInt("idTelefone");
                if (!rs.wasNull()) {
                    Telefone tel = new Telefone();
                    tel.setIdTelefone(idTel);
                    tel.setNumeroTelefone(rs.getString("numeroTelefone"));
                    tel.setDdd(rs.getInt("ddd"));
                    tel.setDdi(rs.getInt("ddi"));
                        tel.setIdPessoa(cliente.getPessoa().getIdPessoa());
                    cliente.addTelefone(tel);
                }
            }
        }

        return new ArrayList<>(mapa.values());
    }

    private String baseSelectSql() {
    return "SELECT " +
                "c.idCliente AS idCliente, c.Senha_Hash AS Senha_Hash, " +
                "p.idPessoa AS idPessoa, p.TipoPessoa AS TipoPessoa, p.idEndereco AS idEndereco, p.complementoEndereco AS pessoaComplemento, p.telefone AS telefone, p.email AS email, " +
                "pf.idPessoaFisica AS idPessoaFisica, pf.nome AS nomePF, pf.CPF AS cpfPF, pf.dataNascimento AS dataNascimento, " +
                "pj.idPessoaJuridica AS idPessoaJuridica, pj.CNPJ AS CNPJ, pj.nomeFantasia AS nomeFantasia, pj.RazaoSocial AS RazaoSocial, " +
        "e.numeroEndereco AS numeroEndereco, e.complementoEndereco AS enderecoComplemento, e.Logradouro_CEP AS cep, " +
        "l.nomeLogradouro AS nomeLogradouro, b.nomeBairro AS nomeBairro, cdd.nomeCidade AS nomeCidade, u.siglaUF AS siglaUF, " +
                "t.idTelefone AS idTelefone, t.numeroTelefone AS numeroTelefone, t.ddd AS ddd, t.ddi AS ddi " +
                "FROM Cliente c " +
                "JOIN Pessoa p ON c.idCliente = p.idPessoa " +
                "LEFT JOIN PessoaFisica pf ON p.idPessoa = pf.idPessoaFisica " +
                "LEFT JOIN PessoaJuridica pj ON p.idPessoa = pj.idPessoaJuridica " +
        "LEFT JOIN Endereco e ON p.idEndereco = e.idEndereco " +
        "LEFT JOIN Logradouro l ON e.Logradouro_CEP = l.CEP " +
        "LEFT JOIN Bairro b ON l.idBairro = b.idBairro " +
        "LEFT JOIN Cidade cdd ON l.idCidade = cdd.idCidade " +
        "LEFT JOIN UF u ON cdd.UF_siglaUF = u.siglaUF " +
                "LEFT JOIN Telefone t ON p.idPessoa = t.idPessoa";
    }

    private Cliente resultSetParaCliente(ResultSet rs, Connection conn) throws SQLException {
        Cliente cliente = new Cliente();

        int idCliente = rs.getInt("idCliente");
        cliente.setIdCliente(idCliente);
        cliente.setSenhaHash(rs.getString("Senha_Hash"));

    int idPessoa = rs.getInt("idPessoa");

        String tp = rs.getString("TipoPessoa");
        char tipoPessoa = (tp != null && !tp.isEmpty()) ? tp.charAt(0) : 0;

    String email = rs.getString("email");
        if (email == null || email.isEmpty()) {
            try {
                EmailDAO emailDAO = new EmailDAO();
                String primary = emailDAO.getPrimaryEmail(conn, idPessoa);
                if (primary != null && !primary.isEmpty()) {
                    email = primary;
                }
            } catch (SQLException ignored) { }
        }
        if (email != null && !email.isEmpty()) {
            cliente.setEmail(email);
        }

    String numeroEnderecoStr = rs.getString("numeroEndereco");
    String enderecoComplemento = rs.getString("enderecoComplemento");
    String pessoaComplemento = rs.getString("pessoaComplemento");
    String cepStr = rs.getString("cep");
    String nomeLogradouro = rs.getString("nomeLogradouro");
    String nomeBairro = rs.getString("nomeBairro");
    String nomeCidade = rs.getString("nomeCidade");
    String siglaUF = rs.getString("siglaUF");

        if (tipoPessoa == 'F') {
            PessoaFisica pf = new PessoaFisica();
            pf.setIdPessoaFisica(rs.getInt("idPessoaFisica"));
            pf.setIdPessoa(idPessoa);
            pf.setTipoPessoa(tipoPessoa);
            pf.setNome(rs.getString("nomePF"));
            pf.setCpf(rs.getString("cpfPF"));
            pf.setDataNascimento(rs.getDate("dataNascimento"));

            // idEndereco vindo da tabela Pessoa
            int idEnd = rs.getInt("idEndereco");
            if (!rs.wasNull()) {
                pf.setIdEndereco(idEnd);
            } else {
                pf.setIdEndereco(0);
            }

            if (numeroEnderecoStr != null) {
                try {
                    pf.setNroEndereco(Integer.parseInt(numeroEnderecoStr));
                } catch (NumberFormatException ex) {
                    pf.setNroEndereco(0);
                }
            } else {
                pf.setNroEndereco(0);
            }
            if (pessoaComplemento != null && !pessoaComplemento.isEmpty()) {
                pf.setComplementoEndereco(pessoaComplemento);
            } else {
                pf.setComplementoEndereco(enderecoComplemento);
            }
            // também popular campos simples para o contrato
            pf.setEmail(email);
            String telNum = rs.getString("telefone");
            pf.setTelefone(telNum);
            if (cepStr != null) pf.setCep(cepStr);
            if (nomeLogradouro != null) pf.setLogradouro(nomeLogradouro);
            if (nomeBairro != null) pf.setBairro(nomeBairro);
            if (nomeCidade != null) pf.setCidade(nomeCidade);
            if (siglaUF != null) pf.setUf(siglaUF);
            cliente.setPessoa(pf);

        } else if (tipoPessoa == 'J') {
            PessoaJuridica pj = new PessoaJuridica();
            pj.setIdPessoaJuridica(rs.getInt("idPessoaJuridica"));
            pj.setIdPessoa(idPessoa);
            pj.setTipoPessoa(tipoPessoa);
            pj.setCnpj(rs.getString("CNPJ"));
            pj.setNomeFantasia(rs.getString("nomeFantasia"));
            pj.setRazaoSocial(rs.getString("RazaoSocial"));

            if (numeroEnderecoStr != null) {
                try {
                    pj.setNroEndereco(Integer.parseInt(numeroEnderecoStr));
                } catch (NumberFormatException ex) {
                    pj.setNroEndereco(0);
                }
            } else {
                pj.setNroEndereco(0);
            }
            if (pessoaComplemento != null && !pessoaComplemento.isEmpty()) {
                pj.setComplementoEndereco(pessoaComplemento);
            } else {
                pj.setComplementoEndereco(enderecoComplemento);
            }
            pj.setEmail(email);
            String telNum = rs.getString("telefone");
            pj.setTelefone(telNum);
            if (cepStr != null) pj.setCep(cepStr);
            if (nomeLogradouro != null) pj.setLogradouro(nomeLogradouro);
            if (nomeBairro != null) pj.setBairro(nomeBairro);
            if (nomeCidade != null) pj.setCidade(nomeCidade);
            if (siglaUF != null) pj.setUf(siglaUF);
            cliente.setPessoa(pj);
        } else {
            cliente.setPessoa(null);
        }

        return cliente;
    }
}
