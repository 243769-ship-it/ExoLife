package org.example.filters;

import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.http.ForbiddenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.example.services.JWTUtil;

public class JwtMiddleware {

    // Definición de Roles (Ids numéricos)
    public static final int ROL_ADMIN = 1;
    public static final int ROL_EMPAQUETADOR = 2;
    public static final int ROL_USUARIO = 3;

    private static Jws<Claims> validateAndSetAttributes(Context ctx) {
        String authHeader = ctx.header("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedResponse("Acceso denegado: Token no proporcionado o formato incorrecto (Bearer).");
        }

        String token = authHeader.substring(7);

        try {
            Jws<Claims> claimsJws = JWTUtil.validateToken(token);
            Claims body = claimsJws.getBody();

            // Extraemos el ID de usuario
            ctx.attribute("userId", body.get("userId", Integer.class));

            // Extraemos el ID numérico del rol (Fundamental para el ensureRole)
            // Nota: Si JWTUtil no lo manda, esto será null y fallará más abajo (seguro)
            ctx.attribute("rolId", body.get("rolId", Integer.class));

            return claimsJws;
        } catch (JwtException e) {
            throw new UnauthorizedResponse("Acceso denegado: Token inválido o expirado.");
        }
    }

    public static void ensureAuthenticated(Context ctx) {
        validateAndSetAttributes(ctx);
    }

    public static void ensureRole(Context ctx, int requiredRoleId) {
        // 1. Validar Token
        validateAndSetAttributes(ctx);

        // 2. Obtener el rol numérico que guardamos en el atributo
        Integer userRoleId = ctx.attribute("rolId");

        // 3. Comparar números (Ej: Si userRoleId es 1 y required es 1 -> Pasa)
        if (userRoleId == null || userRoleId.intValue() != requiredRoleId) {
            System.out.println("Acceso Denegado. Rol requerido: " + requiredRoleId + ", Rol usuario: " + userRoleId);
            throw new ForbiddenResponse("Acceso denegado: Rol insuficiente para esta operación.");
        }
    }
}