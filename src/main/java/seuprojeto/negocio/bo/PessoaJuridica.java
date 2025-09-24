package seuprojeto.negocio.bo;

public class PessoaJuridica extends Pessoa {

    private int idPessoaJuridica;
    private String cnpj;
    private String nomeFantasia;
    private String razaoSocial;

    public PessoaJuridica() {
        setTipoPessoa('J');
    }

    public int getIdPessoaJuridica() {
        return idPessoaJuridica;
    }

    public void setIdPessoaJuridica(int idPessoaJuridica) {
        this.idPessoaJuridica = idPessoaJuridica;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }
}
