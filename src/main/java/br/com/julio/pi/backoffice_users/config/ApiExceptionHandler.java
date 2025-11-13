package br.com.julio.pi.backoffice_users.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> onRse(ResponseStatusException ex){
        return ResponseEntity.status(ex.getStatusCode())
                .body(java.util.Map.of("detail", ex.getReason()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> onAny(Exception ex){
        ex.printStackTrace(); // deve resolver com stacktrace em console (deus queira)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("detail","Falha interna. Veja logs do servidor."));
    }
}
