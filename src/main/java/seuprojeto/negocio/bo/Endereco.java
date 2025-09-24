package seuprojeto.negocio.bo;

public class Endereco {
    private int idEndereco;
    private String logradouroCEP; // Logradouro_CEP
    private String numeroEndereco;
    private String complementoEndereco;
    private String referencia;

    public int getIdEndereco() { return idEndereco; }
    public void setIdEndereco(int idEndereco) { this.idEndereco = idEndereco; }
    public String getLogradouroCEP() { return logradouroCEP; }
    public void setLogradouroCEP(String logradouroCEP) { this.logradouroCEP = logradouroCEP; }
    public String getNumeroEndereco() { return numeroEndereco; }
    public void setNumeroEndereco(String numeroEndereco) { this.numeroEndereco = numeroEndereco; }
    public String getComplementoEndereco() { return complementoEndereco; }
    public void setComplementoEndereco(String complementoEndereco) { this.complementoEndereco = complementoEndereco; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
}
