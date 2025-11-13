package br.com.julio.pi.backoffice_users.config;

import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    record Err(String detail) {}

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Err> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Err(ex.getMessage()));
    }

    @ExceptionHandler({NoSuchElementException.class})
    public ResponseEntity<Err> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Err(ex.getMessage()));
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<Err> handleConflict(DataIntegrityViolationException ex) {
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new Err("Registro já existe ou viola restrição de dados."));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Err> handleValidation(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new Err("Dados inválidos."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Err> handleAny(Exception ex) {
        log.error("Erro não tratado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Err("Falha interna. Veja logs do servidor."));
    }
}
