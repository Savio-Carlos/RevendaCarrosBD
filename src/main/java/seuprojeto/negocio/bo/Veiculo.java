package seuprojeto.negocio.bo;

public class Veiculo {

    private String numChassi;
    private String placa;
    private String marcaCarro;
    private String modeloVeiculo;
    private int anoModelo;
    private int quilometragem;
    private String cor;
    private double precoVeiculo;
    private String descricao;
    private String fotos;
    private int idtipoCombustivel; // Chave estrangeira
    private int idGarantia;        // Chave estrangeira
    private int idStatusVeiculo;   // Chave estrangeira

    public Veiculo() {
    }

    public String getNumChassi() {
        return numChassi;
    }

    public void setNumChassi(String numChassi) {
        this.numChassi = numChassi;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarcaCarro() {
        return marcaCarro;
    }

    public void setMarcaCarro(String marcaCarro) {
        this.marcaCarro = marcaCarro;
    }

    public String getModeloVeiculo() {
        return modeloVeiculo;
    }

    public void setModeloVeiculo(String modeloVeiculo) {
        this.modeloVeiculo = modeloVeiculo;
    }

    public int getAnoModelo() {
        return anoModelo;
    }

    public void setAnoModelo(int anoModelo) {
        this.anoModelo = anoModelo;
    }

    public int getQuilometragem() {
        return quilometragem;
    }

    public void setQuilometragem(int quilometragem) {
        this.quilometragem = quilometragem;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public double getPrecoVeiculo() {
        return precoVeiculo;
    }

    public void setPrecoVeiculo(double precoVeiculo) {
        this.precoVeiculo = precoVeiculo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getFotos() {
        return fotos;
    }

    public void setFotos(String fotos) {
        this.fotos = fotos;
    }

    public int getIdtipoCombustivel() {
        return idtipoCombustivel;
    }

    public void setIdtipoCombustivel(int idtipoCombustivel) {
        this.idtipoCombustivel = idtipoCombustivel;
    }

    public int getIdGarantia() {
        return idGarantia;
    }

    public void setIdGarantia(int idGarantia) {
        this.idGarantia = idGarantia;
    }

    public int getIdStatusVeiculo() {
        return idStatusVeiculo;
    }

    public void setIdStatusVeiculo(int idStatusVeiculo) {
        this.idStatusVeiculo = idStatusVeiculo;
    }
}
