package seuprojeto.negocio.bo;

public class Servico {

    private int idServico;
    private String nomeServico;
    private String descricaoServico;
    private double precoMaoDeObra;

    public Servico() {
    }

    // --- Getters e Setters ---

    public int getIdServico() {
        return idServico;
    }

    public void setIdServico(int idServico) {
        this.idServico = idServico;
    }

    public String getNomeServico() {
        return nomeServico;
    }

    public void setNomeServico(String nomeServico) {
        this.nomeServico = nomeServico;
    }

    public String getDescricaoServico() {
        return descricaoServico;
    }

    public void setDescricaoServico(String descricaoServico) {
        this.descricaoServico = descricaoServico;
    }

    public double getPrecoMaoDeObra() {
        return precoMaoDeObra;
    }

    public void setPrecoMaoDeObra(double precoMaoDeObra) {
        this.precoMaoDeObra = precoMaoDeObra;
    }
}
