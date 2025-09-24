package seuprojeto.negocio.bo;

import java.util.Date;

public class Venda {

    private int idVenda;
    private Date dataVenda;
    private double precoVendaVeiculo;
    private String formaPagamento;

    // Chaves estrangeiras
    private int idCliente;
    private int idFuncionario;
    private String numChassiVeiculo;

    public Venda() {
    }

    public int getIdVenda() {
        return idVenda;
    }

    public void setIdVenda(int idVenda) {
        this.idVenda = idVenda;
    }

    public Date getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(Date dataVenda) {
        this.dataVenda = dataVenda;
    }

    public double getPrecoVendaVeiculo() {
        return precoVendaVeiculo;
    }

    public void setPrecoVendaVeiculo(double precoVendaVeiculo) {
        this.precoVendaVeiculo = precoVendaVeiculo;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdFuncionario() {
        return idFuncionario;
    }

    public void setIdFuncionario(int idFuncionario) {
        this.idFuncionario = idFuncionario;
    }

    public String getNumChassiVeiculo() {
        return numChassiVeiculo;
    }

    public void setNumChassiVeiculo(String numChassiVeiculo) {
        this.numChassiVeiculo = numChassiVeiculo;
    }
}
