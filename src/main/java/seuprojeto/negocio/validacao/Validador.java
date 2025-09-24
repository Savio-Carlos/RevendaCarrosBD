package seuprojeto.negocio.validacao;

public final class Validador {
    private Validador() {}

    // Email simples
    public static boolean email(String email) {
        return email != null && email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // Telefone BR: 10 ou 11 dígitos (DDD + numero)
    public static boolean telefone(String digits) {
        return digits != null && digits.matches("\\d{10,11}");
    }

    // CEP: 8 dígitos
    public static boolean cep(String digits) {
        return digits != null && digits.matches("\\d{8}");
    }

    // CPF: 11 dígitos com DV
    public static boolean cpf(String digits) {
        if (digits == null || !digits.matches("\\d{11}")) return false;
        if (digits.chars().distinct().count() == 1) return false;
        int d1 = dvCPF(digits.substring(0, 9));
        int d2 = dvCPF(digits.substring(0, 9) + d1);
        return digits.equals(digits.substring(0,9) + d1 + d2);
    }
    private static int dvCPF(String base) {
        int soma = 0, peso = base.length() + 1;
        for (char c : base.toCharArray()) soma += (c - '0') * peso--;
        int mod = soma % 11; return (mod < 2) ? 0 : 11 - mod;
    }

    // CNPJ: 14 dígitos com DV
    public static boolean cnpj(String digits) {
        if (digits == null || !digits.matches("\\d{14}")) return false;
        if (digits.chars().distinct().count() == 1) return false;
        int d1 = dvCNPJ(digits.substring(0, 12));
        int d2 = dvCNPJ(digits.substring(0, 12) + d1);
        return digits.equals(digits.substring(0,12) + d1 + d2);
    }
    private static int dvCNPJ(String base) {
        int[] pesos = {5,4,3,2,9,8,7,6,5,4,3,2};
        if (base.length() == 13) pesos = new int[]{6,5,4,3,2,9,8,7,6,5,4,3,2};
        int soma = 0; for (int i=0; i<base.length(); i++) soma += (base.charAt(i)-'0') * pesos[i + (pesos.length - base.length())];
        int mod = soma % 11; return (mod < 2) ? 0 : 11 - mod;
    }

    // VIN: 17 caracteres, sem I/O/Q
    public static boolean vin(String vin) {
        return vin != null && vin.matches("[A-HJ-NPR-Z0-9]{17}");
    }

    // Placa BR: antigo (ABC1234) ou Mercosul (ABC1D23)
    public static boolean placa(String p) {
        if (p == null) return false;
        return p.matches("[A-Z]{3}[0-9]{4}") || p.matches("[A-Z]{3}[0-9][A-Z][0-9]{2}");
    }
}
