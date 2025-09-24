package seuprojeto.excecao;

public class ValidacaoExcecao extends RuntimeException {
    public ValidacaoExcecao(String message) { super(message); }
    public ValidacaoExcecao(String message, Throwable cause) { super(message, cause); }
}