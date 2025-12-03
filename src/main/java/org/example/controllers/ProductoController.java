package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.example.models.Producto;
import org.example.services.ProductoService;
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

public class ProductoController {

    private final CloudinaryService cloudinaryService;
    private final HistorialVistasService historialService = new HistorialVistasService();
    private final RecomendacionService recomendacionService = new RecomendacionService();
    private static final int ROL_ADMIN_ID = 1;

    public ProductoController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    public void register(Javalin app) {
        app.get("/api/v1/productos/random", this::getRandomProductos);
        app.get("/api/v1/productos", this::getAllProductos);
        app.get("/api/v1/productos/{id}", this::getProductoById);

        // Endpoint auxiliar para ver animales de un producto (opcional, pero util)
        app.get("/api/v1/productos/{id}/animales", ctx -> {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            ctx.json(recomendacionService.getAnimalesRecomendados(id));
        });

        app.post("/api/v1/productos", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.createProducto(ctx);
        });

        app.put("/api/v1/productos/{id}", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.updateProducto(ctx);
        });

        app.delete("/api/v1/productos/{id}", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.deleteProducto(ctx);
        });
    }

    private Long getUserIdFromCtx(Context ctx) {
        Object uid = ctx.attribute("userId");
        if (uid instanceof Integer) return ((Integer) uid).longValue();
        if (uid instanceof Long) return (Long) uid;
        return null;
    }

    private void getRandomProductos(Context ctx) {
        try (Connection conn = DBConfig.getConnection()) {
            ProductoService productoService = new ProductoService(conn);
            List<Producto> random = productoService.obtenerTodosRandom();
            ctx.json(random);
        } catch (Exception e) {
            ctx.status(500).result("Error obteniendo productos random: " + e.getMessage());
        }
    }

    private void getAllProductos(Context ctx) {
        try (Connection conn = DBConfig.getConnection()) {
            ProductoService productoService = new ProductoService(conn);
            String q = ctx.queryParam("q");
            List<Producto> productos = productoService.obtenerPorFiltros(q, null, null, "desc", 1, 100);
            ctx.json(productos);
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void getProductoById(Context ctx) {
        try (Connection conn = DBConfig.getConnection()) {
            ProductoService productoService = new ProductoService(conn);
            Long id = Long.parseLong(ctx.pathParam("id"));
            Producto producto = productoService.obtenerPorId(id);

            if (producto == null) {
                ctx.status(404).result("Producto no encontrado");
                return;
            }

            try { productoService.incrementarVistas(id); } catch (Exception ignored) {}

            try {
                Long usuarioId = getUserIdFromCtx(ctx);
                if (usuarioId != null) {
                    Integer prodIdInt = producto.getProducto_id().intValue();
                    historialService.registrarVista(usuarioId, prodIdInt, null);
                }
            } catch (Exception ignored) {}

            ctx.json(producto);
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void createProducto(Context ctx) {
        UploadedFile uploadedFile = ctx.uploadedFile("imagen");
        try (Connection conn = DBConfig.getConnection()) {
            ProductoService productoService = new ProductoService(conn);
            Producto nuevoProducto;
            List<Integer> animalIds = new ArrayList<>();

            if (uploadedFile != null) {
                String jsonString = ctx.formParam("data");
                if (jsonString == null) { ctx.status(400).result("Falta JSON data"); return; }

                nuevoProducto = ctx.jsonMapper().fromJsonString(jsonString, Producto.class);

                Map<String, Object> rawMap = ctx.jsonMapper().fromJsonString(jsonString, Map.class);
                if (rawMap.containsKey("animales")) {
                    List<?> list = (List<?>) rawMap.get("animales");
                    for (Object o : list) if(o instanceof Number) animalIds.add(((Number) o).intValue());
                }

                if (uploadedFile.content().available() > 0) {
                    Path temp = Files.createTempFile("producto-", uploadedFile.filename());
                    Files.copy(uploadedFile.content(), temp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    var res = cloudinaryService.uploadFile(temp.toFile(), "ecom_exolife_productos");
                    nuevoProducto.setImagen_url((String)res.get("secure_url"));
                    Files.deleteIfExists(temp);
                }
            } else {
                nuevoProducto = ctx.bodyAsClass(Producto.class);
                Map<String, Object> rawMap = ctx.bodyAsClass(Map.class);
                if (rawMap.containsKey("animales")) {
                    List<?> list = (List<?>) rawMap.get("animales");
                    for (Object o : list) if(o instanceof Number) animalIds.add(((Number) o).intValue());
                }
            }

            if(nuevoProducto.getVistas_contador() == null) nuevoProducto.setVistas_contador(0);

            Producto creado = productoService.crearProducto(nuevoProducto);

            // 1. Guardar Animales relacionados AUTOMATICAMENTE (CORRECCIÓN APLICADA)
            if (!animalIds.isEmpty() && creado.getProducto_id() != null) {
                try {
                    recomendacionService.syncAnimalesParaProducto(creado.getProducto_id(), animalIds);
                    System.out.println("✅ Animales relacionados guardados para producto ID: " + creado.getProducto_id());
                } catch (Exception e) {
                    System.err.println("⚠️ Error guardando animales relacionados: " + e.getMessage());
                }
            }

            ctx.status(201).json(creado);

        } catch(Exception e) {
            ctx.status(500).result("Error creando: " + e.getMessage());
        }
    }

    private void updateProducto(Context ctx) {
        try (Connection conn = DBConfig.getConnection()) {
            Long id = Long.parseLong(ctx.pathParam("id"));

            // Leemos el body como MAPA para sacar checklist de animales
            Map<String, Object> bodyMap = ctx.bodyAsClass(Map.class);
            Producto datosNuevos = ctx.jsonMapper().fromJsonString(ctx.body(), Producto.class);

            ProductoService service = new ProductoService(conn);
            Producto existente = service.obtenerPorId(id);

            if (existente == null) {
                ctx.status(404).result("Producto no encontrado");
                return;
            }

            if (datosNuevos.getNombre() != null) existente.setNombre(datosNuevos.getNombre());
            if (datosNuevos.getPrecio() != null) existente.setPrecio(datosNuevos.getPrecio());
            if (datosNuevos.getStock() != null) existente.setStock(datosNuevos.getStock());
            if (datosNuevos.getDescripcion() != null) existente.setDescripcion(datosNuevos.getDescripcion());
            if (datosNuevos.getCategoria_producto_id() != null) existente.setCategoria_producto_id(datosNuevos.getCategoria_producto_id());

            existente.setProducto_id(id.intValue());

            if(service.actualizarProducto(existente)) {
                // 3. ACTUALIZAR RELACIONES (Si vienen)
                if (bodyMap.containsKey("animales")) {
                    List<?> list = (List<?>) bodyMap.get("animales");
                    List<Integer> animalIds = new ArrayList<>();
                    for (Object o : list) if(o instanceof Number) animalIds.add(((Number) o).intValue());
                    try {
                        recomendacionService.syncAnimalesParaProducto(existente.getProducto_id(), animalIds);
                    } catch (Exception e) {
                        System.err.println("⚠️ Error actualizando relaciones de animales: " + e.getMessage());
                    }
                }
                ctx.json(existente);
            } else {
                ctx.status(400).result("No se pudo actualizar");
            }

        } catch (Exception e) {
            ctx.status(500).result("Error actualizando: " + e.getMessage());
        }
    }

    private void deleteProducto(Context ctx) {
        try (Connection conn = DBConfig.getConnection()) {
            Long id = Long.parseLong(ctx.pathParam("id"));
            ProductoService service = new ProductoService(conn);

            if(service.eliminarProducto(id)) {
                ctx.status(200).result("Producto eliminado");
            } else {
                ctx.status(404).result("No encontrado o no se pudo eliminar");
            }
        } catch (Exception e) {
            ctx.status(500).result("Error eliminando: " + e.getMessage());
        }
    }
}