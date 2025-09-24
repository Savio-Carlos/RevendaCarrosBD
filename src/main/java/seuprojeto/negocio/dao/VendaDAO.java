package seuprojeto.negocio.dao;

import seuprojeto.negocio.bo.Venda;
import seuprojeto.negocio.bo.VendaResumo;
import seuprojeto.infra.bd.ConexaoBancoDados;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VendaDAO {

    public Venda salvar(Venda venda, Connection conn) throws SQLException {
        String sql = "INSERT INTO Venda (dataVenda, precoVendaVeiculo, formaPagamento, idCliente, idFuncionario, numChassiVeiculo) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (venda.getDataVenda() != null) {
                pstmt.setDate(1, new java.sql.Date(venda.getDataVenda().getTime()));
            } else {
                pstmt.setDate(1, new java.sql.Date(System.currentTimeMillis()));
            }
            pstmt.setDouble(2, venda.getPrecoVendaVeiculo());
            pstmt.setString(3, venda.getFormaPagamento());
            pstmt.setInt(4, venda.getIdCliente());
            pstmt.setInt(5, venda.getIdFuncionario());
            pstmt.setString(6, venda.getNumChassiVeiculo());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    venda.setIdVenda(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Falha ao obter o ID da venda, nenhuma linha foi criada.");
                }
            }
        }
        return venda;
    }

    public Venda buscarPorId(int id) throws SQLException {
    String sql = "SELECT * FROM Venda WHERE idVenda = ?";
        try (Connection conn = ConexaoBancoDados.criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Venda venda = new Venda();
                    venda.setIdVenda(rs.getInt("idVenda"));
                    venda.setDataVenda(rs.getDate("dataVenda"));
                    venda.setPrecoVendaVeiculo(rs.getDouble("precoVendaVeiculo"));
                    venda.setFormaPagamento(rs.getString("formaPagamento"));
                    venda.setIdCliente(rs.getInt("idCliente"));
                    venda.setIdFuncionario(rs.getInt("idFuncionario"));
                    venda.setNumChassiVeiculo(rs.getString("numChassiVeiculo"));
                    return venda;
                }
            }
        }
        return null;
    }

    public List<Venda> buscarTodos() throws SQLException {
        String sql = "SELECT * FROM Venda ORDER BY idVenda DESC";
        List<Venda> vendas = new ArrayList<>();
        try (Connection conn = ConexaoBancoDados.criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Venda venda = new Venda();
                venda.setIdVenda(rs.getInt("idVenda"));
                venda.setDataVenda(rs.getDate("dataVenda"));
                venda.setPrecoVendaVeiculo(rs.getDouble("precoVendaVeiculo"));
                venda.setFormaPagamento(rs.getString("formaPagamento"));
                venda.setIdCliente(rs.getInt("idCliente"));
                venda.setIdFuncionario(rs.getInt("idFuncionario"));
                venda.setNumChassiVeiculo(rs.getString("numChassiVeiculo"));
                vendas.add(venda);
            }
        }
        return vendas;
    }

    public List<VendaResumo> buscarTodosResumo() throws SQLException {
        String sql =
            "SELECT v.idVenda, v.dataVenda, v.precoVendaVeiculo, v.formaPagamento, " +
            "       v.idCliente, COALESCE(pfCli.nome, pjCli.RazaoSocial, 'Cliente') AS clienteNome, " +
            "       v.idFuncionario, pfFun.nome AS funcionarioNome, " +
            "       v.numChassiVeiculo, ve.modeloVeiculo, ve.placa, ve.marcaCarro " +
            "FROM Venda v " +
            "JOIN Cliente c ON v.idCliente = c.idCliente " +
            "JOIN Pessoa pcli ON c.idCliente = pcli.idPessoa " +
            "LEFT JOIN PessoaFisica pfCli ON pcli.idPessoa = pfCli.idPessoaFisica " +
            "LEFT JOIN PessoaJuridica pjCli ON pcli.idPessoa = pjCli.idPessoaJuridica " +
            "JOIN Funcionario f ON v.idFuncionario = f.idFuncionario " +
            "JOIN PessoaFisica pfFun ON f.idFuncionario = pfFun.idPessoaFisica " +
            "JOIN Veiculo ve ON v.numChassiVeiculo = ve.numChassi " +
            "ORDER BY v.idVenda DESC";

        List<VendaResumo> out = new ArrayList<>();
        try (Connection conn = ConexaoBancoDados.criarConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(mapResumo(rs));
            }
        }
        return out;
    }

    public VendaResumo buscarResumoPorId(int id) throws SQLException {
        String sql =
            "SELECT v.idVenda, v.dataVenda, v.precoVendaVeiculo, v.formaPagamento, " +
            "       v.idCliente, COALESCE(pfCli.nome, pjCli.RazaoSocial, 'Cliente') AS clienteNome, " +
            "       v.idFuncionario, pfFun.nome AS funcionarioNome, " +
            "       v.numChassiVeiculo, ve.modeloVeiculo, ve.placa, ve.marcaCarro " +
            "FROM Venda v " +
            "JOIN Cliente c ON v.idCliente = c.idCliente " +
            "JOIN Pessoa pcli ON c.idCliente = pcli.idPessoa " +
            "LEFT JOIN PessoaFisica pfCli ON pcli.idPessoa = pfCli.idPessoaFisica " +
            "LEFT JOIN PessoaJuridica pjCli ON pcli.idPessoa = pjCli.idPessoaJuridica " +
            "JOIN Funcionario f ON v.idFuncionario = f.idFuncionario " +
            "JOIN PessoaFisica pfFun ON f.idFuncionario = pfFun.idPessoaFisica " +
            "JOIN Veiculo ve ON v.numChassiVeiculo = ve.numChassi " +
            "WHERE v.idVenda = ?";

        try (Connection conn = ConexaoBancoDados.criarConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResumo(rs);
            }
        }
        return null;
    }

    private VendaResumo mapResumo(ResultSet rs) throws SQLException {
        VendaResumo vr = new VendaResumo();
        vr.setIdVenda(rs.getInt("idVenda"));
        vr.setDataVenda(rs.getDate("dataVenda"));
        vr.setPrecoVendaVeiculo(rs.getDouble("precoVendaVeiculo"));
        vr.setFormaPagamento(rs.getString("formaPagamento"));
        vr.setClienteId(rs.getInt("idCliente"));
        vr.setClienteNome(rs.getString("clienteNome"));
        vr.setFuncionarioId(rs.getInt("idFuncionario"));
        vr.setFuncionarioNome(rs.getString("funcionarioNome"));
        vr.setNumChassiVeiculo(rs.getString("numChassiVeiculo"));
        vr.setVeiculoModelo(rs.getString("modeloVeiculo"));
        vr.setVeiculoPlaca(rs.getString("placa"));
        vr.setVeiculoMarca(rs.getString("marcaCarro"));
        return vr;
    }
}
