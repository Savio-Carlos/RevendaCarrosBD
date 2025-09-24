package seuprojeto.negocio.bo;

import java.util.ArrayList;
import java.util.List;

public class Pessoa {

    private int idPessoa;
    private char tipoPessoa; // 'F' para Física, 'J' para Jurídica
    private int idEndereco;
    private int nroEndereco;
    private String complementoEndereco;
    private String telefone;
    private String email;
    private String cep; // vindo de Endereco.Logradouro_CEP
    private String logradouro; // Logradouro.nomeLogradouro
    private String bairro; // Bairro.nomeBairro
    private String cidade; // Cidade.nomeCidade
    private String uf; // UF.siglaUF

    private List<Telefone> telefones;
    private List<Email> emails;

    public Pessoa() {
    }

    // --- Getters e Setters ---

    public int getIdPessoa() {
        return idPessoa;
    }

    public void setIdPessoa(int idPessoa) {
        this.idPessoa = idPessoa;
    }

    public char getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(char tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
    }

    public int getIdEndereco() {
        return idEndereco;
    }

    public void setIdEndereco(int idEndereco) {
        this.idEndereco = idEndereco;
    }

    public int getNroEndereco() {
        return nroEndereco;
    }

    public void setNroEndereco(int nroEndereco) {
        this.nroEndereco = nroEndereco;
    }

    public String getComplementoEndereco() {
        return complementoEndereco;
    }

    public void setComplementoEndereco(String complementoEndereco) {
        this.complementoEndereco = complementoEndereco;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public List<Telefone> getTelefones() {
        return telefones;
    }

    public void setTelefones(List<Telefone> telefones) {
        this.telefones = telefones;
    }

    public void addTelefone(Telefone telefone) {
        if (this.telefones == null) {
            this.telefones = new ArrayList<>();
        }
        this.telefones.add(telefone);
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }
}
