// br/com/julio/pi/backoffice_users/service/SenhaService.java
package br.com.julio.pi.backoffice_users.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SenhaService {
  private static final Logger log = LoggerFactory.getLogger(SenhaService.class);
  private final PasswordEncoder encoder;

  public SenhaService(PasswordEncoder encoder){
    this.encoder = encoder;
  }

  /** Gera hash (BCrypt). */
  public String gerarHash(String raw){
    String h = encoder.encode(raw);
    log.debug("Gerando hash (len={}): {}", h.length(), h);
    return h;
  }

  /** Compara senha pura com hash (nome novo). */
  public boolean verificar(String raw, String hash){
    boolean ok = encoder.matches(raw, hash);
    log.debug("Comparando senha: {}", ok ? "OK" : "FALHOU");
    return ok;
  }

  /** Alias para compatibilidade com código existente. */
  public boolean validarSenha(String raw, String hash){
    return verificar(raw, hash);
  }
}
