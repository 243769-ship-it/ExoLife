package org.example.services;

import io.jsonwebtoken.*;
import org.example.models.Usuario;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class JWTUtil {

    private static final String SECRET_KEY = "wKe8p8zR7T!zD*G-KaPdSgVkYp3s6v9y$B&E(H+MbQeThWmZq4t7w!z%C*F)J@Nc";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 3; // 3 Horas

    private static final Key SIGNATURE_KEY = new SecretKeySpec(
            SECRET_KEY.getBytes(), SignatureAlgorithm.HS256.getJcaName()
    );

    public static String generateToken(Usuario user) {

        Integer userId = user.getId() != null ? user.getId().intValue() : null;

        // Obtenemos ambos: el Nombre (Texto) y el ID (Número)
        String userRolName = user.getRol();
        Integer userRolId = user.getRol_id();

        // Validaciones por si vienen nulos
        if (userRolName == null) userRolName = "INVITADO";
        if (userRolId == null) userRolId = 0;

        Date expirationDate = new Date(System.currentTimeMillis() + EXPIRATION_TIME);

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(user.getEmail())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(expirationDate)
                // --- AQUÍ ESTÁ LA MAGIA: MANDAMOS LOS DOS ---
                .claim("userId", userId)
                .claim("rol", userRolName)  // Para que el Frontend sepa pintar botones
                .claim("rolId", userRolId)  // Para que el Backend (Middleware) deje pasar
                // -------------------------------------------
                .signWith(SIGNATURE_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Jws<Claims> validateToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(SIGNATURE_KEY)
                .build()
                .parseClaimsJws(token);
    }
}