package com.example.gAZtos.Utils;

import java.util.Date;
import java.util.Optional;

import com.example.gAZtos.Entities.PasswordResetToken;
import com.example.gAZtos.Entities.User;
import com.example.gAZtos.Repositories.PasswordResetTokenRepository;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${jwt.secret}") // Leer la clave secreta desde application.properties
    private String SECRET_KEY;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // Expiración de 1 hora
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String generateEmailToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10)) // Expiración de 1 hora
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public Optional<PasswordResetToken> extractUser(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateTokenRecovery(String token, Optional<PasswordResetToken> user) {
        return (token.equals(user.get().getToken()) && !isTokenExpiredRecovery(user));
    }

    public boolean validateToken(String token, User user) {
        final String username = extractUsername(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private boolean isTokenExpiredRecovery(Optional<PasswordResetToken> user) {
        return user.get().getExpiryDate().before(new Date());
    }
}
