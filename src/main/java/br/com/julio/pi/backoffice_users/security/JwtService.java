package br.com.julio.pi.backoffice_users.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final Key key;
    private final long expirationMinutes;

    public JwtService(
            @Value("${app.jwt.secret:change-me-please-change-me-please-change-me-please-123456}") String secret,
            @Value("${app.jwt.expiration-minutes:240}") long expirationMinutes
    ) {
        // chave HMAC a partir do secret
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    /** Gera um JWT assinado (HS256) com subject e claims extras. */
    public String generateToken(String subject, Map<String, Object> claims){
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationMinutes * 60);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Faz o parse do JWT (assinado) e retorna o Jws<Claims>. */
    public Jws<Claims> parse(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(stripBearer(token));
    }

    /**
     * Valida assinatura e expiração. Se inválido, lança JwtException.
     * Se válido, retorna os Claims do token.
     */
    public Claims validate(String token) throws JwtException {
        Jws<Claims> jws = parse(token);
        Claims claims = jws.getBody();

        Date exp = claims.getExpiration();
        if (exp != null && exp.before(new Date())) {
            throw new JwtException("Token expirado");
        }
        return claims;
    }

    /** Remove prefixo "Bearer " se vier no header. */
    private static String stripBearer(String token){
        if (token == null) return null;
        String t = token.trim();
        if (t.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return t.substring(7);
        }
        return t;
    }

    public long getExpirationMinutes() { return expirationMinutes; }
}

