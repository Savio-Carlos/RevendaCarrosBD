package seuprojeto.negocio.bo;

import java.util.Date;
import java.util.List;

public class OrdemServico {

    private int idOrdemServico;
    private Date dataAbertura;
    private Date dataFechamento;
    private String descricaoServico;
    private int quilometragemEntrada;
    private double valorTotal;

    // Chaves estrangeiras
    private String numChassiVeiculo;
    private int idCliente;
    private int idFuncionario;


    private List<ServicoRealizado> servicosRealizados;

    public OrdemServico() {
    }


    public int getIdOrdemServico() {
        return idOrdemServico;
    }

    public void setIdOrdemServico(int idOrdemServico) {
        this.idOrdemServico = idOrdemServico;
    }

    public Date getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(Date dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public Date getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(Date dataFechamento) {
        this.dataFechamento = dataFechamento;
    }

    public String getDescricaoServico() {
        return descricaoServico;
    }

    public void setDescricaoServico(String descricaoServico) {
        this.descricaoServico = descricaoServico;
    }

    public int getQuilometragemEntrada() {
        return quilometragemEntrada;
    }

    public void setQuilometragemEntrada(int quilometragemEntrada) {
        this.quilometragemEntrada = quilometragemEntrada;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getNumChassiVeiculo() {
        return numChassiVeiculo;
    }

    public void setNumChassiVeiculo(String numChassiVeiculo) {
        this.numChassiVeiculo = numChassiVeiculo;
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

    public List<ServicoRealizado> getServicosRealizados() {
        return servicosRealizados;
    }

    public void setServicosRealizados(List<ServicoRealizado> servicosRealizados) {
        this.servicosRealizados = servicosRealizados;
    }
}
