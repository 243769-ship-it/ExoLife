package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.filters.JwtMiddleware;
import org.example.services.HistorialVistasService;

public class HistorialVistasController {

    private final HistorialVistasService service = new HistorialVistasService();

    public HistorialVistasController() {
    }

    public void register(Javalin app) {

        app.get("/api/v1/historial/recientes", ctx -> {
            JwtMiddleware.ensureAuthenticated(ctx);
            this.getMisVistasRecientes(ctx);
        });
    }

    private Long getUserIdFromCtx(Context ctx) {
        Object uid = ctx.attribute("userId");
        if (uid instanceof Integer) return ((Integer) uid).longValue();
        if (uid instanceof Long) return (Long) uid;
        return null;
    }

    private void getMisVistasRecientes(Context ctx) {
        try {
            Long usuarioId = getUserIdFromCtx(ctx);
            if (usuarioId == null) {
                ctx.status(401).result("Token inválido o no proporciona userId");
                return;
            }

            var historial = service.getVistasRecientes(usuarioId);
            ctx.json(historial);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error al obtener el historial: " + e.getMessage());
        }
    }
}