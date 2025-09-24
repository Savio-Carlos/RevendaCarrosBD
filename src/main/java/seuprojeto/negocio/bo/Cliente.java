// Pacote: seuprojeto.classe
package seuprojeto.negocio.bo;

import java.util.ArrayList;
import java.util.List;

public class Cliente {

    private int idCliente;
    private String senhaHash;
    private String email; // opcional: facilitar exibição no topo
    private List<Telefone> telefones; // manter compatibilidade com resposta antiga

    // Pessoa associada (PF ou PJ)
    private Pessoa pessoa;

    public Cliente() { }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Pessoa getPessoa() { return pessoa; }
    public void setPessoa(Pessoa pessoa) { this.pessoa = pessoa; }

    public List<Telefone> getTelefones() { return telefones; }
    public void setTelefones(List<Telefone> telefones) { this.telefones = telefones; }
    public void addTelefone(Telefone telefone) {
        if (this.telefones == null) this.telefones = new ArrayList<>();
        this.telefones.add(telefone);
    }
}
