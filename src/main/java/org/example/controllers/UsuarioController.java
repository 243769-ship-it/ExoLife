package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.filters.JwtMiddleware;
import org.example.models.Usuario;
import org.example.services.UsuarioService;
import java.util.Map;

public class UsuarioController {

    private final UsuarioService usuarioService = new UsuarioService();

    public UsuarioController() {
    }

    public void register(Javalin app) {

        app.get("/api/perfil", ctx -> {
            JwtMiddleware.ensureAuthenticated(ctx);
            this.getMiPerfil(ctx);
        });

        app.put("/api/perfil", ctx -> {
            JwtMiddleware.ensureAuthenticated(ctx);
            this.updateMiPerfil(ctx);
        });

        // --- NUEVA RUTA: Obtener info de un usuario por ID (Para Admin) ---
        app.get("/api/usuarios/{id}", ctx -> {
            JwtMiddleware.ensureAuthenticated(ctx);
            this.getUsuarioPorId(ctx);
        });
    }

    private Long getUserIdFromCtx(Context ctx) {
        Object uid = ctx.attribute("userId");
        if (uid instanceof Integer) return ((Integer) uid).longValue();
        if (uid instanceof Long) return (Long) uid;
        return null;
    }

    private void getMiPerfil(Context ctx) {
        try {
            Long usuarioId = getUserIdFromCtx(ctx);
            if (usuarioId == null) {
                ctx.status(401).result("Token inválido o no proporciona userId");
                return;
            }

            Usuario perfil = usuarioService.getPerfil(usuarioId);
            ctx.status(200).json(perfil);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(404).json(Map.of("error", e.getMessage()));
        }
    }

    private void updateMiPerfil(Context ctx) {
        try {
            Long usuarioId = getUserIdFromCtx(ctx);
            if (usuarioId == null) {
                ctx.status(401).result("Token inválido o no proporciona userId");
                return;
            }

            Usuario datosNuevos = ctx.bodyAsClass(Usuario.class);

            Usuario perfilActualizado = usuarioService.updatePerfil(usuarioId, datosNuevos);

            perfilActualizado.setPasswordHash(null);

            ctx.status(200).json(perfilActualizado);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    // --- MÉTODO NUEVO ---
    private void getUsuarioPorId(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            // Llamamos al nuevo método del servicio
            Usuario usuario = usuarioService.obtenerUsuarioPorId(id);

            if (usuario != null) {
                ctx.json(usuario);
            } else {
                ctx.status(404).result("Usuario no encontrado");
            }
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }
}