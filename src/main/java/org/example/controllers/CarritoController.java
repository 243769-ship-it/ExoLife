package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.models.Carrito;
import org.example.models.CarritoItem;
import org.example.repositories.AnimalRepository;
import org.example.repositories.CarritoRepository;
import org.example.repositories.ProductoRepository;
import org.example.config.DBConfig;
import java.sql.Connection;
import java.util.Map;

public class CarritoController {

    private final CarritoRepository carritoRepository;

    public CarritoController(CarritoRepository carritoRepository) {
        this.carritoRepository = carritoRepository;
    }

    public void register(Javalin app) {
        app.post("/api/carrito", this::createCarritoIfNotExists);
        app.post("/api/carrito/items", this::addItemToCarrito);
        app.get("/api/carrito", this::getCarritoByUser);
        app.delete("/api/carrito/items/{itemId}", this::removeItem);
        app.patch("/api/carrito/items/{itemId}", this::updateItemQuantity);
    }

    private Long getUserIdFromCtx(Context ctx) {
        Object uid = ctx.attribute("userId");
        if (uid == null) return null;
        if (uid instanceof Integer) return ((Integer) uid).longValue();
        if (uid instanceof Long) return (Long) uid;
        return null;
    }

    public static class AddItemRequest {
        public Long itemId;
        public String tipo;
        public Integer cantidad;
        public AddItemRequest() {}
    }

    private void createCarritoIfNotExists(Context ctx) throws Exception {
        Long userId = getUserIdFromCtx(ctx);
        if (userId == null) { ctx.status(401).result("No autenticado"); return; }
        Carrito c = carritoRepository.findByUsuarioId(userId);
        if (c == null) { c = carritoRepository.createCarrito(userId); }
        ctx.json(c);
    }

    // --- MÉTODO ACTUALIZADO CON VALIDACIÓN DE STOCK ---
    private void addItemToCarrito(Context ctx) {
        Long userId = getUserIdFromCtx(ctx);
        if (userId == null) { ctx.status(401).result("No autenticado"); return; }

        AddItemRequest req;
        try {
            req = ctx.bodyAsClass(AddItemRequest.class);
        } catch (Exception e) {
            ctx.status(400).result("JSON inválido."); return;
        }

        if (req.itemId == null || req.tipo == null) {
            ctx.status(400).result("itemId (del producto/animal) y tipo son obligatorios"); return;
        }
        int cantidadSolicitada = (req.cantidad == null || req.cantidad <= 0) ? 1 : req.cantidad;

        try (Connection conn = DBConfig.getConnection()) {
            Long productoId = null;
            Long animalId = null;
            double precioUnitario = 0.0;
            int stockDisponible = 0; // NUEVO: Variable para guardar el stock real

            // 1. Validar existencia del producto/animal, obtener precio Y STOCK
            if ("producto".equalsIgnoreCase(req.tipo)) {
                ProductoRepository productoRepo = new ProductoRepository(conn);
                var producto = productoRepo.findById(req.itemId);
                if (producto == null) { ctx.status(404).result("Producto no existe"); return; }

                productoId = producto.getProducto_id().longValue();
                precioUnitario = producto.getPrecio().doubleValue();
                // Leemos el stock
                stockDisponible = (producto.getStock() != null) ? producto.getStock() : 0;

            } else if ("animal".equalsIgnoreCase(req.tipo)) {
                AnimalRepository animalRepo = new AnimalRepository(conn);
                var animal = animalRepo.findById(req.itemId);
                if (animal == null) { ctx.status(404).result("Animal no existe"); return; }

                animalId = animal.getAnimal_id().longValue();
                precioUnitario = animal.getPrecio().doubleValue();
                // Leemos el stock
                stockDisponible = (animal.getStock() != null) ? animal.getStock() : 0;

            } else {
                ctx.status(400).result("Tipo inválido. Use 'producto' o 'animal'.");
                return;
            }

            // --- NUEVO: VALIDACIÓN INICIAL DE STOCK ---
            if (stockDisponible < cantidadSolicitada) {
                ctx.status(400).result("Stock insuficiente. Solo quedan " + stockDisponible + " unidades disponibles.");
                return;
            }
            // ------------------------------------------

            // 2. Obtener o crear el carrito
            Carrito c = carritoRepository.findByUsuarioId(userId);
            if (c == null) c = carritoRepository.createCarrito(userId);

            // 3. VERIFICAR SI YA EXISTE (Upsert con validación de suma)
            CarritoItem itemExistente = carritoRepository.findItemByProductOrAnimal(c.getCarritoId(), productoId, animalId);

            if (itemExistente != null) {
                // Si ya existe, calculamos cuánto tendría en total
                int nuevaCantidadTotal = itemExistente.getCantidad() + cantidadSolicitada;

                // NUEVO: Validamos que la suma total no supere el stock
                if (nuevaCantidadTotal > stockDisponible) {
                    ctx.status(400).result("No puedes agregar más. Ya tienes " + itemExistente.getCantidad() + " en el carrito y el stock máximo es " + stockDisponible);
                    return;
                }

                carritoRepository.updateCantidad(itemExistente.getItemId(), nuevaCantidadTotal);
                itemExistente.setCantidad(nuevaCantidadTotal);
                ctx.status(200).json(itemExistente);
            } else {
                // Si no existe, lo CREAMOS (ya validamos el stock inicial arriba)
                CarritoItem newItem = carritoRepository.addItem(c.getCarritoId(), productoId, animalId, cantidadSolicitada, precioUnitario);
                ctx.status(201).json(newItem);
            }

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error interno: " + e.getMessage());
        }
    }

    private void getCarritoByUser(Context ctx) throws Exception {
        Long userId = getUserIdFromCtx(ctx);
        if (userId == null) { ctx.status(401).result("No autenticado"); return; }
        Carrito c = carritoRepository.findByUsuarioId(userId);
        if (c == null) {
            Carrito carritoVacio = new Carrito();
            carritoVacio.setUsuarioId(userId);
            ctx.json(carritoVacio);
            return;
        }
        ctx.json(c);
    }

    private void removeItem(Context ctx) throws Exception {
        Long userId = getUserIdFromCtx(ctx);
        if (userId == null) { ctx.status(401).result("No autenticado"); return; }
        Long itemId = ctx.pathParamAsClass("itemId", Long.class).get();
        boolean ok = carritoRepository.removeItem(itemId);
        if (ok) ctx.status(200).result("Eliminado");
        else ctx.status(404).result("No encontrado");
    }

    private void updateItemQuantity(Context ctx) {
        Long userId = getUserIdFromCtx(ctx);
        if (userId == null) { ctx.status(401).result("No autenticado"); return; }
        try {
            Long itemId = ctx.pathParamAsClass("itemId", Long.class).get();
            Map<String, Integer> body = ctx.bodyAsClass(Map.class);

            if (!body.containsKey("cantidad")) {
                ctx.status(400).result("Debes enviar 'cantidad'");
                return;
            }

            int nuevaCantidad = body.get("cantidad");
            if (nuevaCantidad <= 0) {
                // Si mandan 0 o menos, mejor eliminamos el item
                carritoRepository.removeItem(itemId);
                ctx.status(200).result("Item eliminado por cantidad 0");
                return;
            }

            boolean ok = carritoRepository.updateCantidad(itemId, nuevaCantidad);
            if (ok) ctx.status(200).result("Cantidad actualizada");
            else ctx.status(404).result("Item no encontrado");

        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }
}