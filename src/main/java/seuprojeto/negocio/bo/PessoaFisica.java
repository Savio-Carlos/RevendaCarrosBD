package seuprojeto.negocio.bo;

import java.util.Date;

public class PessoaFisica extends Pessoa {

    private int idPessoaFisica;
    private String nome;
    private String cpf;
    private Date dataNascimento;

    // Construtor
    public PessoaFisica() {
        // Ao criar uma PessoaFisica, definimos o tipo automaticamente.
        setTipoPessoa('F');
    }

    // --- Getters e Setters ---

    public int getIdPessoaFisica() {
        return idPessoaFisica;
    }

    public void setIdPessoaFisica(int idPessoaFisica) {
        this.idPessoaFisica = idPessoaFisica;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Date getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(Date dataNascimento) {
        this.dataNascimento = dataNascimento;
    }
}
