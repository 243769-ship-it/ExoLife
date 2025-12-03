package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.filters.JwtMiddleware;
import org.example.models.Direccion;
import org.example.services.DireccionService;

public class DireccionController {

    private final DireccionService direccionService = new DireccionService();
    private static final int ROL_ADMIN_ID = 1;

    public DireccionController() {
    }

    public void register(Javalin app) {
        app.get("/api/direcciones", this::getMisDirecciones);
        app.get("/api/direcciones/{id}", this::getDireccionPorId);
        app.post("/api/direcciones", this::crearNuevaDireccion);
        app.delete("/api/direcciones/{id}", this::eliminarDireccion);
    }

    private Long getUserIdFromCtx(Context ctx) {
        Object uid = ctx.attribute("userId");
        if (uid instanceof Integer) return ((Integer) uid).longValue();
        if (uid instanceof Long) return (Long) uid;
        return null;
    }

    private void getMisDirecciones(Context ctx) throws Exception {
        JwtMiddleware.ensureAuthenticated(ctx);
        Long userId = getUserIdFromCtx(ctx);
        if (userId == null) {
            ctx.status(401).result("Token inválido o no proporciona userId");
            return;
        }
        ctx.json(direccionService.getDireccionesPorUsuario(userId));
    }

    private void getDireccionPorId(Context ctx) {
        JwtMiddleware.ensureAuthenticated(ctx);
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Direccion dir = direccionService.obtenerPorId(id);
            if (dir != null) {
                ctx.json(dir);
            } else {
                ctx.status(404).result("Dirección no encontrada");
            }
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void crearNuevaDireccion(Context ctx) throws Exception {
        JwtMiddleware.ensureAuthenticated(ctx);
        Long userId = getUserIdFromCtx(ctx);
        if (userId == null) {
            ctx.status(401).result("Token inválido");
            return;
        }

        Direccion nuevaDireccion = ctx.bodyAsClass(Direccion.class);

        try {
            Direccion creada = direccionService.crearDireccion(nuevaDireccion, userId);
            ctx.status(201).json(creada);
        } catch (Exception e) {
            ctx.status(400).result(e.getMessage());
        }
    }

    private void eliminarDireccion(Context ctx) throws Exception {
        JwtMiddleware.ensureAuthenticated(ctx);
        Long userId = getUserIdFromCtx(ctx);
        if (userId == null) {
            ctx.status(401).result("Token inválido");
            return;
        }

        Long direccionId = ctx.pathParamAsClass("id", Long.class).get();

        if (direccionService.eliminarDireccion(direccionId, userId)) {
            ctx.status(200).result("Dirección eliminada");
        } else {
            ctx.status(404).result("No se pudo eliminar");
        }
    }
}