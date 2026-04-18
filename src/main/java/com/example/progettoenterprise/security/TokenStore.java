package com.example.progettoenterprise.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

// Classe che gestisce i JWT
@Component
public class TokenStore {

    // Recupera la chiave dal file .env tramite application.properties
    @Value( "${jwt.secret}")
    private String secretKey;

    // Metodo che crea il token
    public String createToken(Map<String, Object> claims) throws JOSEException {
        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS); // Momento della creazione
        Instant notBefore = issuedAt; // Momento dal quale il token è valido
        Instant expiration = issuedAt.plus(24, ChronoUnit.HOURS); // Scadenza del token

        // Costruzione del token
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        for(String entry : claims.keySet())
            builder.claim(entry, claims.get(entry));

        JWTClaimsSet claimsSet = builder.issueTime(Date.from(issuedAt))
                .notBeforeTime(Date.from(notBefore))
                .expirationTime(Date.from(expiration)).build();

        // Creazione payload, intestazione e firma del token (crittografia simmetrica)
        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), payload);
        jwsObject.sign(new MACSigner(secretKey.getBytes()));

        return jwsObject.serialize();
    }

    // Verifica se il token è strutturalmente valido e non contraffatto
    public boolean verifyToken(String token) throws JOSEException, ParseException {
        try {
            getUser(token); // Se non lancia eccezzioni, il token è valido
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    // Valida il token e restituisce lo username
    public String getUser(String token) throws JOSEException, ParseException {

        // Analizza il token
        SignedJWT signedJWT = SignedJWT.parse(token);

        // Crea un verifier per verificare la firma del token
        JWSVerifier jwsVerifier = new MACVerifier(secretKey.getBytes());

        // Verifica che il token sia valido, non contraffatto e non scaduto
        if (signedJWT.verify(jwsVerifier) &&
                new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime()) &&
                new Date().after(signedJWT.getJWTClaimsSet().getNotBeforeTime())) {
            return signedJWT.getJWTClaimsSet().getStringClaim("username");
        }

        // Token non valido o scaduto
        throw new RuntimeException("Token scaduto o non valido");
    }

    // Recupera il token dall'intestazione della richiesta HTTP
    public String getToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.replace("Bearer ", "");
        }
        return null;
    }
}
