package seuprojeto.negocio.bo;

public class Telefone {

    private int idTelefone;
    private String numeroTelefone;
    private int ddd;
    private int ddi;
    private int idPessoa; // Chave estrangeira

    public Telefone() {
    }

    // Getters e Setters
    public int getIdTelefone() { return idTelefone; }
    public void setIdTelefone(int idTelefone) { this.idTelefone = idTelefone; }
    public String getNumeroTelefone() { return numeroTelefone; }
    public void setNumeroTelefone(String numeroTelefone) { this.numeroTelefone = numeroTelefone; }
    public int getDdd() { return ddd; }
    public void setDdd(int ddd) { this.ddd = ddd; }
    public int getDdi() { return ddi; }
    public void setDdi(int ddi) { this.ddi = ddi; }
    public int getIdPessoa() { return idPessoa; }
    public void setIdPessoa(int idPessoa) { this.idPessoa = idPessoa; }
}
