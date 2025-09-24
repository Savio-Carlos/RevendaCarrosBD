package seuprojeto.negocio.bo;

public class Email {

    private int idEmail;
    private String email;
    private int idPessoa; // Chave estrangeira

    public Email() {
    }

    // Getters e Setters
    public int getIdEmail() { return idEmail; }
    public void setIdEmail(int idEmail) { this.idEmail = idEmail; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getIdPessoa() { return idPessoa; }
    public void setIdPessoa(int idPessoa) { this.idPessoa = idPessoa; }
}
