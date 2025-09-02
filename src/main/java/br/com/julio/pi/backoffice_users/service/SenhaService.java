package br.com.julio.pi.backoffice_users.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SenhaService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String gerarHash(String senha) {
        return encoder.encode(senha);
    }

    public boolean validarSenha(String senhaDigitada, String hash) {
        return encoder.matches(senhaDigitada, hash);
    }
}

