package com.example.progettoenterprise.exception;

import com.example.progettoenterprise.config.i18n.MessageLang;
import com.example.progettoenterprise.dto.ErroreDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;


// Classe che ascolta tutti i controller e gestisce gli errori
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageLang messageLang;

    // Gestisce gli errori di validazione (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroreDTO handleValidationErrors(WebRequest request, MethodArgumentNotValidException e) {
        // Concatena gli errori di validazione in una singola stringa
        String messaggio = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, request, messaggio);
    }

    // Tipo di parametro errato (400)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroreDTO handleTypeMismatch(WebRequest request, MethodArgumentTypeMismatchException e) {
        String messaggio = String.format("Il parametro '%s' dovrebbe essere di tipo %s",
                e.getName(), Objects.requireNonNull(e.getRequiredType()).getSimpleName());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, request, messaggio);
    }

    // Gestisce quando una risorsa non esiste (404)
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroreDTO handleNotFound(WebRequest request, EntityNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, request, e.getMessage());
    }

    // Metodo non supporatto (405)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErroreDTO handleMethodNotSupported(WebRequest request, HttpRequestMethodNotSupportedException e) {
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, request, e.getMessage());
    }

    // Gestisce dati non validi o conflitti (409)
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroreDTO handleIllegalArgument(WebRequest request, IllegalArgumentException e) {
        return buildErrorResponse(HttpStatus.CONFLICT, request, e.getMessage());
    }

    // Gestisce errori di autenticazione (401)
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErroreDTO handleAuthException(WebRequest request, AuthenticationException e) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, request, messageLang.getMessage("auth.login.invalid"));
    }

    // Gestisce permessi negati (403)
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErroreDTO handleAccessDenied(WebRequest request, AccessDeniedException e) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, request, messageLang.getMessage("auth.access.denied"));
    }

    // Gestisce il caso in cui il body della richiesta sia assente o malformato (400)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroreDTO handleReadableException(WebRequest request, HttpMessageNotReadableException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, request, messageLang.getMessage("request.body.malformed"));
    }

    // Gestisce tutti gli altri errori (500)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErroreDTO handleGenericException(WebRequest request, Exception e) {
        log.error("ERRORE NON GESTITO: ", e); // Logga l'intera eccezione per il debug
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, messageLang.getMessage("server.internal.error"));
    }

    private ErroreDTO buildErrorResponse(HttpStatus status, WebRequest request, String messaggio) {
        // Estrae l'uri dalla richiesta
        HttpServletRequest httpRequest = (HttpServletRequest) request.resolveReference("request");
        String uri = (httpRequest != null) ? httpRequest.getRequestURI() : "unknown";

        ErroreDTO errore = new ErroreDTO(
                status.value(),
                messaggio,
                uri,
                LocalDateTime.now()
        );

        // Registra l'errore nel log del server
        log.error("Errore {} presso {}: {}", status.value(), uri, messaggio);

        return errore;
    }
}
