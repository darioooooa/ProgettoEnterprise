package com.example.progettoenterprise.exception;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.dto.ErroreDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


// Classe che ascolta tutti i controller e gestisce gli errori
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageLang messageLang;

    // Gestisce gli errori di validazione
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroreDTO> handleValidationErrors(MethodArgumentNotValidException e) {
        String messaggio = (e.getBindingResult().getFieldError() != null)
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : messageLang.getMessage("request.validation.generic");
        return buildResponse(HttpStatus.BAD_REQUEST, messaggio);
    }

    // Gestisce quando una risorsa non esiste
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErroreDTO> handleNotFound(EntityNotFoundException e) {
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // Gestisce dati non validi o duplicati
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroreDTO> handleIllegalArgument(IllegalArgumentException e) {
        return buildResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    // Gestisce errori di autenticazione
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroreDTO> handleAuthException(AuthenticationException e) {
        return buildResponse(HttpStatus.UNAUTHORIZED, messageLang.getMessage("auth.login.invalid"));
    }

    // Gestisce permessi negati
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroreDTO> handleAccessDenied(AccessDeniedException e) {
        return buildResponse(HttpStatus.FORBIDDEN, messageLang.getMessage("auth.access.denied"));
    }

    // Gestisce il caso in cui il body della richiesta sia assente o malformato
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroreDTO> handleReadableException(HttpMessageNotReadableException e) {
        return buildResponse(HttpStatus.BAD_REQUEST, messageLang.getMessage("request.body.malformed"));
    }

    // Gestisce tutti gli altri errori
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroreDTO> handleGenericException(Exception e) {
        // Logga l'errore sulla console per il debug (solo in fase di sviluppo)
        e.printStackTrace();
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, messageLang.getMessage("server.internal.error"));
    }
    // Metodo che costruisce la risposta HTTP con l'errore
    private ResponseEntity<ErroreDTO> buildResponse(HttpStatus status, String messaggio) {
        ErroreDTO errore = new ErroreDTO(status.value(), messaggio, System.currentTimeMillis());
        return new ResponseEntity<>(errore, status);
    }
}
