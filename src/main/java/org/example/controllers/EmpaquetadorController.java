package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.filters.JwtMiddleware;
import org.example.services.EmpaquetadorService;

public class EmpaquetadorController {

    private final EmpaquetadorService service = new EmpaquetadorService();
    private static final int ROL_EMPAQUETADOR_ID = 2;

    public EmpaquetadorController() {

    }

    public void register(Javalin app) {
        app.get("/api/empaquetador/pedidos", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_EMPAQUETADOR_ID);
            this.getMiColaDeTrabajo(ctx);
        });
    }

    private Long getUserIdFromCtx(Context ctx) {
        Object uid = ctx.attribute("userId");
        if (uid instanceof Integer) return ((Integer) uid).longValue();
        if (uid instanceof Long) return (Long) uid;
        return null;
    }

    private void getMiColaDeTrabajo(Context ctx) {
        try {
            Long empaquetadorId = getUserIdFromCtx(ctx);
            if (empaquetadorId == null) {
                ctx.status(401).result("Token inválido o no proporciona userId");
                return;
            }

            var pedidos = service.getWorkQueue(empaquetadorId);
            ctx.json(pedidos);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error al obtener la cola de trabajo: " + e.getMessage());
        }
    }
}