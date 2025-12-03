package org.example.controllers;

import io.javalin.http.Handler;
import io.javalin.Javalin;
import org.example.models.Animal;
import org.example.models.Producto;
import org.example.services.AnimalService;
import org.example.services.ProductoService;
import org.example.config.DBConfig;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogoController {

    public CatalogoController() {
    }

    public void register(Javalin app) {
        app.get("/api/catalogo", getCatalogo());
    }

    private Handler getCatalogo() {
        return ctx -> {

            final int LIMIT_NOVEDADES = 10;
            final int LIMIT_MAS_VISTOS = 10;

            try (Connection conn = DBConfig.getConnection()) {

                ProductoService productoService = new ProductoService(conn);
                AnimalService animalService = new AnimalService(conn);

                Map<String, Object> novedades = new HashMap<>();
                novedades.put("productos", productoService.getNovedades(LIMIT_NOVEDADES));
                novedades.put("animales", animalService.getNovedades(LIMIT_NOVEDADES));

                Map<String, Object> masVistos = new HashMap<>();
                masVistos.put("productos", productoService.getMasVistos(LIMIT_MAS_VISTOS));
                masVistos.put("animales", animalService.getMasVistos(LIMIT_MAS_VISTOS));

                Map<String, Object> catalogoCompleto = new HashMap<>();
                catalogoCompleto.put("productos", productoService.obtenerTodosRandom());
                catalogoCompleto.put("animales", animalService.obtenerTodosRandom());

                Map<String, Object> respuesta = new HashMap<>();
                respuesta.put("novedades", novedades);
                respuesta.put("masVistos", masVistos);
                respuesta.put("catalogoCompleto", catalogoCompleto);

                ctx.json(respuesta);

            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).json(Map.of("error", "Error al obtener el catálogo: " + e.getMessage()));
            }
        };
    }
}