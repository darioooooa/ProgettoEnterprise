package com.example.progettoenterprise.exception;

import com.example.progettoenterprise.dto.ErroreDTO;
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
public class GlobalExceptionHandler {

    // Gestisce gli errori di validazione
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroreDTO> handleValidationErrors(MethodArgumentNotValidException e) {
        String messaggio = (e.getBindingResult().getFieldError() != null)
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "Errore di validazione";
        return buildResponse(HttpStatus.BAD_REQUEST, messaggio);
    }

    // Gestisce le eccezzioni lanciate nel service
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErroreDTO> handleRuntimeException(RuntimeException e) {
        return buildResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    // Getsisce errori di autenticazione
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroreDTO> handleAuthException(AuthenticationException e) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Credenziali non valide");
    }

    // Gestisce permessi negati
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroreDTO> handleAccessDenied(AccessDeniedException e) {
        return buildResponse(HttpStatus.FORBIDDEN, "Non hai i permessi per eseguire questa azione");
    }

    // Gestisce tutti gli altri errori
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroreDTO> handleGenericException(Exception e) {
        // Logga l'errore sulla console per il debug
        e.printStackTrace();
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno del server");
    }

    // Gestisce il caso in cui il body della richiesta sia assente o malformato
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroreDTO> handleReadableException(HttpMessageNotReadableException e) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Il corpo della richiesta è assente o non valido");
    }

    // Metodo che costruisce la risposta HTTP con l'errore
    private ResponseEntity<ErroreDTO> buildResponse(HttpStatus status, String messaggio) {
        ErroreDTO errore = new ErroreDTO(status.value(), messaggio, System.currentTimeMillis());
        return new ResponseEntity<>(errore, status);
    }
}
