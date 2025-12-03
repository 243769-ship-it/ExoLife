package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.example.models.Animal;
import org.example.services.AnimalService;
import org.example.services.CloudinaryService;
import org.example.services.HistorialVistasService;
import org.example.services.RecomendacionService;
import org.example.config.DBConfig;
import org.example.filters.JwtMiddleware;
import java.sql.Connection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnimalController {

    private final CloudinaryService cloudinaryService;
    private final HistorialVistasService historialService = new HistorialVistasService();
    private final RecomendacionService recomendacionService = new RecomendacionService();
    private static final int ROL_ADMIN_ID = 1;

    public AnimalController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    public void register(Javalin app) {
        app.get("/api/v1/animales", this::getAllAnimales);
        app.get("/api/v1/animales/{id}", this::getAnimalById);

        app.post("/api/v1/animales", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.createAnimal(ctx);
        });

        app.put("/api/v1/animales/{id}", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.updateAnimal(ctx);
        });

        app.delete("/api/v1/animales/{id}", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.deleteAnimal(ctx);
        });
    }

    private Long getUserIdFromCtx(Context ctx) {
        Object uid = ctx.attribute("userId");
        if (uid instanceof Integer) return ((Integer) uid).longValue();
        if (uid instanceof Long) return (Long) uid;
        return null;
    }

    private void getAllAnimales(Context ctx) {
        try (Connection conn = DBConfig.getConnection()) {
            AnimalService animalService = new AnimalService(conn);
            ctx.json(animalService.obtenerTodosRandom());
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void getAnimalById(Context ctx) {
        try (Connection conn = DBConfig.getConnection()) {
            AnimalService animalService = new AnimalService(conn);
            Long id = Long.parseLong(ctx.pathParam("id"));
            Animal animal = animalService.obtenerPorId(id);

            if (animal != null) {
                // Registrar historial
                try {
                    Long usuarioId = getUserIdFromCtx(ctx);
                    if (usuarioId != null) {
                        Integer animalIdInt = animal.getAnimal_id().intValue();
                        historialService.registrarVista(usuarioId, null, animalIdInt);
                    }
                } catch (Exception ignored) {}

                // Incluir recomendaciones (opcional, si el front lo espera)
                try {
                    // No hace falta meterlas en el objeto animal si el front las pide aparte,
                    // pero aqui se podrian obtener.
                } catch (Exception ignored) {}

                ctx.json(animal);
            } else {
                ctx.status(404).result("Animal no encontrado");
            }
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void createAnimal(Context ctx) {
        UploadedFile uploadedFile = ctx.uploadedFile("imagen");
        try (Connection conn = DBConfig.getConnection()) {
            AnimalService service = new AnimalService(conn);
            Animal nuevo;
            List<Integer> recomendacionIds = new ArrayList<>();

            if (uploadedFile != null) {
                // FORM-DATA
                String jsonString = ctx.formParam("data");
                nuevo = ctx.jsonMapper().fromJsonString(jsonString, Animal.class);

                // Extraer checklist
                Map<String, Object> rawMap = ctx.jsonMapper().fromJsonString(jsonString, Map.class);
                if (rawMap.containsKey("recomendaciones")) {
                    List<?> list = (List<?>) rawMap.get("recomendaciones");
                    for (Object o : list) {
                        if(o instanceof Number) recomendacionIds.add(((Number) o).intValue());
                    }
                }

                // Subir imagen
                if (uploadedFile.content().available() > 0) {
                    Path temp = Files.createTempFile("animal-", uploadedFile.filename());
                    Files.copy(uploadedFile.content(), temp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    var res = cloudinaryService.uploadFile(temp.toFile(), "ecom_exolife_animales");
                    nuevo.setImagen_url((String)res.get("secure_url"));
                    Files.deleteIfExists(temp);
                }
            } else {
                // JSON PURO
                nuevo = ctx.bodyAsClass(Animal.class);
                Map<String, Object> rawMap = ctx.bodyAsClass(Map.class);
                if (rawMap.containsKey("recomendaciones")) {
                    List<?> list = (List<?>) rawMap.get("recomendaciones");
                    for (Object o : list) {
                        if(o instanceof Number) recomendacionIds.add(((Number) o).intValue());
                    }
                }
            }

            if(nuevo.getVistas_contador() == null) nuevo.setVistas_contador(0);

            // 1. Guardar Animal
            Animal creado = service.crearAnimal(nuevo);

            // 2. Guardar Recomendaciones AUTOMATICAMENTE (CORRECCIÓN APLICADA)
            if (!recomendacionIds.isEmpty() && creado.getAnimal_id() != null) {
                try {
                    recomendacionService.syncRecomendacionesParaAnimal(creado.getAnimal_id(), recomendacionIds);
                    System.out.println("✅ Recomendaciones guardadas para animal ID: " + creado.getAnimal_id());
                } catch (Exception e) {
                    System.err.println("⚠️ Error guardando recomendaciones: " + e.getMessage());
                }
            }

            ctx.status(201).json(creado);

        } catch(Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void updateAnimal(Context ctx) {
        try (Connection conn = DBConfig.getConnection()) {
            Long id = Long.parseLong(ctx.pathParam("id"));

            // Leemos el body como MAPA para sacar checklist
            Map<String, Object> bodyMap = ctx.bodyAsClass(Map.class);
            Animal datosNuevos = ctx.jsonMapper().fromJsonString(ctx.body(), Animal.class);

            AnimalService service = new AnimalService(conn);
            Animal existente = service.obtenerPorId(id);

            if (existente == null) {
                ctx.status(404).result("Animal no encontrado");
                return;
            }

            if(datosNuevos.getNombre() != null) existente.setNombre(datosNuevos.getNombre());
            if(datosNuevos.getDescripcion() != null) existente.setDescripcion(datosNuevos.getDescripcion());
            if(datosNuevos.getPrecio() != null) existente.setPrecio(datosNuevos.getPrecio());
            if(datosNuevos.getStock() != null) existente.setStock(datosNuevos.getStock());

            existente.setAnimal_id(id.intValue());

            if(service.actualizarAnimal(existente)) {
                // 3. ACTUALIZAR RECOMENDACIONES (Si vienen)
                if (bodyMap.containsKey("recomendaciones")) {
                    List<?> list = (List<?>) bodyMap.get("recomendaciones");
                    List<Integer> recomendacionIds = new ArrayList<>();
                    for (Object o : list) {
                        if(o instanceof Number) recomendacionIds.add(((Number) o).intValue());
                    }
                    try {
                        recomendacionService.syncRecomendacionesParaAnimal(existente.getAnimal_id(), recomendacionIds);
                    } catch (Exception e) {
                        System.err.println("⚠️ Error actualizando recomendaciones: " + e.getMessage());
                    }
                }
                ctx.status(200).json(existente);
            } else {
                ctx.status(400).result("No se pudo actualizar el animal");
            }

        } catch(Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void deleteAnimal(Context ctx) {
        try (Connection conn = DBConfig.getConnection()) {
            Long id = Long.parseLong(ctx.pathParam("id"));
            AnimalService service = new AnimalService(conn);
            if(service.eliminarAnimal(id)) {
                ctx.status(200).result("Animal eliminado");
            } else {
                ctx.status(404).result("No encontrado o no se pudo eliminar");
            }
        } catch(Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }
}