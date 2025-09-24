package seuprojeto.negocio.bo;

public class ServicoRealizado {

    private int idServicosRealizados;
    private double quantidade;
    private double precoUnitario;

    private int idPeca;
    private int idServico;
    private int idOrdemServico;

    public ServicoRealizado() {
    }

    public int getIdServicosRealizados() {
        return idServicosRealizados;
    }

    public void setIdServicosRealizados(int idServicosRealizados) {
        this.idServicosRealizados = idServicosRealizados;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(double precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public int getIdPeca() {
        return idPeca;
    }

    public void setIdPeca(int idPeca) {
        this.idPeca = idPeca;
    }

    public int getIdServico() {
        return idServico;
    }

    public void setIdServico(int idServico) {
        this.idServico = idServico;
    }

    public int getIdOrdemServico() {
        return idOrdemServico;
    }

    public void setIdOrdemServico(int idOrdemServico) {
        this.idOrdemServico = idOrdemServico;
    }
}
