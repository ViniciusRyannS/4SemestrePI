package br.com.julio.pi.backoffice_users.util;

public final class CpfValidator {

    private CpfValidator() {}

    public static String clean(String s) {
        return s == null ? null : s.replaceAll("\\D", "");
    }

    public static boolean isValid(String cpf) {
        if (cpf == null) return false;
        cpf = clean(cpf);
        if (cpf.length() != 11) return false;

       
        boolean allEqual = cpf.chars().distinct().count() == 1;
        if (allEqual) return false;

        try {
            
            int sum = 0;
            for (int i = 0, peso = 10; i < 9; i++, peso--) {
                sum += (cpf.charAt(i) - '0') * peso;
            }
            int d1 = 11 - (sum % 11);
            if (d1 >= 10) d1 = 0;

            sum = 0;
            for (int i = 0, peso = 11; i < 10; i++, peso--) {
                sum += (cpf.charAt(i) - '0') * peso;
            }
            int d2 = 11 - (sum % 11);
            if (d2 >= 10) d2 = 0;

            return d1 == (cpf.charAt(9) - '0') && d2 == (cpf.charAt(10) - '0');
        } catch (Exception e) {
            return false;
        }
    }
}
