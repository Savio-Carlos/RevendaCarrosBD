package seuprojeto.negocio.bo;

import java.util.Date;

public class VendaResumo {
    private int idVenda;
    private Date dataVenda;
    private double precoVendaVeiculo;
    private String formaPagamento;

    private int clienteId;
    private String clienteNome;

    private int funcionarioId;
    private String funcionarioNome;

    private String numChassiVeiculo;
    private String veiculoModelo;
    private String veiculoPlaca;
    private String veiculoMarca;

    public int getIdVenda() { return idVenda; }
    public void setIdVenda(int idVenda) { this.idVenda = idVenda; }

    public Date getDataVenda() { return dataVenda; }
    public void setDataVenda(Date dataVenda) { this.dataVenda = dataVenda; }

    public double getPrecoVendaVeiculo() { return precoVendaVeiculo; }
    public void setPrecoVendaVeiculo(double precoVendaVeiculo) { this.precoVendaVeiculo = precoVendaVeiculo; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }

    public int getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(int funcionarioId) { this.funcionarioId = funcionarioId; }

    public String getFuncionarioNome() { return funcionarioNome; }
    public void setFuncionarioNome(String funcionarioNome) { this.funcionarioNome = funcionarioNome; }

    public String getNumChassiVeiculo() { return numChassiVeiculo; }
    public void setNumChassiVeiculo(String numChassiVeiculo) { this.numChassiVeiculo = numChassiVeiculo; }

    public String getVeiculoModelo() { return veiculoModelo; }
    public void setVeiculoModelo(String veiculoModelo) { this.veiculoModelo = veiculoModelo; }

    public String getVeiculoPlaca() { return veiculoPlaca; }
    public void setVeiculoPlaca(String veiculoPlaca) { this.veiculoPlaca = veiculoPlaca; }

    public String getVeiculoMarca() { return veiculoMarca; }
    public void setVeiculoMarca(String veiculoMarca) { this.veiculoMarca = veiculoMarca; }
}
