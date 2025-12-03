package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.filters.JwtMiddleware;
import org.example.services.RecomendacionService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecomendacionController {

    private final RecomendacionService service = new RecomendacionService();
    private static final int ROL_ADMIN_ID = 1;

    public RecomendacionController() {
    }

    public void register(Javalin app) {

        app.get("/api/v1/animales/{id}/recomendaciones", this::getRecomendacionesParaAnimal);
        app.put("/api/v1/admin/animales/{id}/recomendaciones", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.syncRecomendaciones(ctx);
        });
    }

    private void getRecomendacionesParaAnimal(Context ctx) {
        try {
            Integer animalId = ctx.pathParamAsClass("id", Integer.class).get();
            var recomendaciones = service.getRecomendaciones(animalId);
            ctx.json(recomendaciones);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error al obtener recomendaciones: " + e.getMessage());
        }
    }

    private void syncRecomendaciones(Context ctx) {
        try {
            Integer animalId = ctx.pathParamAsClass("id", Integer.class).get();

            Map<String, List<Object>> request = ctx.bodyAsClass(Map.class);
            List<Object> idsObject = request.get("productoIds");

            if (idsObject == null) {
                ctx.status(400).result("JSON inválido. Se esperaba: { \"productoIds\": [...] }");
                return;
            }

            List<Integer> productoIds = idsObject.stream()
                    .map(obj -> ((Number) obj).intValue())
                    .collect(Collectors.toList());

            service.syncRecomendacionesParaAnimal(animalId, productoIds);
            ctx.status(200).result("Recomendaciones sincronizadas.");

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error al sincronizar recomendaciones: " + e.getMessage());
        }
    }
}