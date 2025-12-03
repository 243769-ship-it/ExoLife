package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.example.services.CompraService;
import org.example.services.CloudinaryService;
import org.example.filters.JwtMiddleware;
import org.example.models.Compra;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.File;
import java.util.Map;

public class CompraController {

    private final CloudinaryService cloudinaryService;
    private static final int ROL_EMPAQUETADOR_ID = 2;
    private static final int ROL_ADMIN_ID = 1;

    public CompraController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    public void register(Javalin app) {

        app.post("/api/compras/crear-desde-carrito", this::crearCompraDesdeCarrito);
        app.post("/api/compras/{compraId}/comprobante", this::subirComprobante);
        app.get("/api/compras/mi-pedido-activo", this::getMiPedidoActivo);
        app.put("/api/compras/{compraId}/asignar-direccion", this::asignarDireccion);

        // NUEVO ENDPOINT: Historial del Cliente
        app.get("/api/compras/historial", ctx -> {
            JwtMiddleware.ensureAuthenticated(ctx);
            this.getHistorialCliente(ctx);
        });

        app.get("/api/compras/{compraId}/detalles", ctx -> {
            JwtMiddleware.ensureAuthenticated(ctx);
            Integer rolId = ctx.attribute("rolId");
            if (rolId != null && (rolId == ROL_ADMIN_ID || rolId == ROL_EMPAQUETADOR_ID)) {
                this.getDetallesCompra(ctx);
            } else {
                ctx.status(403).result("Acceso denegado");
            }
        });

        app.get("/api/v1/compras/activos", ctx -> {
            JwtMiddleware.ensureAuthenticated(ctx);
            Integer rolId = ctx.attribute("rolId");
            if (rolId != null && (rolId == ROL_ADMIN_ID || rolId == ROL_EMPAQUETADOR_ID)) {
                this.getAllPedidosActivos(ctx);
            } else {
                ctx.status(403).result("Acceso denegado");
            }
        });

        app.post("/api/v1/compras/{compraId}/confirmar", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.adminConfirmarPago(ctx);
        });

        app.post("/api/v1/compras/{compraId}/rechazar", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.adminRechazarPedido(ctx);
        });

        app.post("/api/v1/compras/empaquetar/{compraId}", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_EMPAQUETADOR_ID);
            this.iniciarEmpaquetado(ctx);
        });

        app.post("/api/v1/compras/enviar/{compraId}", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_EMPAQUETADOR_ID);
            this.marcarEnviado(ctx);
        });

        // ACTUALIZADO: Recibe foto de evidencia
        app.post("/api/v1/compras/entregar/{compraId}", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_EMPAQUETADOR_ID);
            this.marcarEntregadoConEvidencia(ctx);
        });
    }

    private void getHistorialCliente(Context ctx) {
        Long userId = getUserIdFromCtx(ctx);
        if (userId == null) { ctx.status(401).result("No autenticado"); return; }
        try {
            CompraService service = new CompraService();
            var historial = service.getHistorial(userId);
            ctx.json(historial);
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void getAllPedidosActivos(Context ctx) {
        try {
            CompraService compraService = new CompraService();
            var listaActivos = compraService.obtenerTodosLosPedidosActivos();
            ctx.status(200).json(listaActivos);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void adminRechazarPedido(Context ctx) {
        try {
            Long compraId = Long.parseLong(ctx.pathParam("compraId"));
            CompraService compraService = new CompraService();
            if (compraService.rechazarCompra(compraId)) {
                ctx.status(200).result("Pedido rechazado correctamente.");
            } else {
                ctx.status(400).result("Error: No se pudo rechazar.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error interno: " + e.getMessage());
        }
    }

    private void adminConfirmarPago(Context ctx) {
        JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
        try {
            Long compraId = ctx.pathParamAsClass("compraId", Long.class).get();
            CompraService compraService = new CompraService();
            boolean ok = compraService.confirmarPagoYAsignarEmpaquetador(compraId);
            if (ok) ctx.status(200).result("Compra confirmada.");
            else ctx.status(400).result("No se pudo confirmar.");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void getDetallesCompra(Context ctx) {
        try {
            Long compraId = Long.parseLong(ctx.pathParam("compraId"));
            CompraService service = new CompraService();
            var detalles = service.obtenerDetallesDePedido(compraId);
            ctx.json(detalles);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error al obtener detalles: " + e.getMessage());
        }
    }

    private Long getUserIdFromCtx(Context ctx) {
        Object uid = ctx.attribute("userId");
        if (uid instanceof Integer) return ((Integer) uid).longValue();
        if (uid instanceof Long) return (Long) uid;
        return null;
    }

    private void asignarDireccion(Context ctx) {
        JwtMiddleware.ensureAuthenticated(ctx);
        Long userId = getUserIdFromCtx(ctx);
        if (userId == null) { ctx.status(401).result("No autenticado"); return; }
        try {
            Long compraId = ctx.pathParamAsClass("compraId", Long.class).get();
            Map<String, Long> request = ctx.bodyAsClass(Map.class);
            Long direccionId = request.get("direccionId");
            if (direccionId == null) { ctx.status(400).result("Se requiere 'direccionId'"); return; }
            CompraService compraService = new CompraService();
            boolean exito = compraService.asignarDireccion(compraId, direccionId, userId);
            if (exito) ctx.status(200).result("Dirección asignada.");
            else ctx.status(404).result("No se pudo asignar.");
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void getMiPedidoActivo(Context ctx) {
        JwtMiddleware.ensureAuthenticated(ctx);
        Long userId = getUserIdFromCtx(ctx);
        if (userId == null) { ctx.status(401).result("No autenticado"); return; }
        try {
            CompraService compraService = new CompraService();
            Compra pedidoActivo = compraService.getPedidoActivo(userId);
            ctx.status(200).json(pedidoActivo);
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void iniciarEmpaquetado(Context ctx) {
        try {
            Long compraId = Long.parseLong(ctx.pathParam("compraId"));
            CompraService compraService = new CompraService();
            if (compraService.iniciarEmpaquetado(compraId)) ctx.status(200).result("Empaquetado iniciado.");
            else ctx.status(400).result("Error al iniciar empaquetado.");
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void marcarEnviado(Context ctx) {
        try {
            Long compraId = Long.parseLong(ctx.pathParam("compraId"));
            CompraService compraService = new CompraService();
            if (compraService.marcarEnviado(compraId)) ctx.status(200).result("Pedido enviado.");
            else ctx.status(400).result("Error al marcar enviado.");
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    // --- MÉTODO NUEVO: MARCAR ENTREGADO CON FOTO ---
    private void marcarEntregadoConEvidencia(Context ctx) {
        try {
            Long compraId = Long.parseLong(ctx.pathParam("compraId"));

            UploadedFile uploaded = ctx.uploadedFile("evidencia");
            if (uploaded == null) {
                ctx.status(400).result("Se requiere una foto de evidencia.");
                return;
            }

            Path temp = Files.createTempFile("evidencia-", uploaded.filename());
            Files.copy(uploaded.content(), temp, StandardCopyOption.REPLACE_EXISTING);
            File tmpFile = temp.toFile();

            var uploadResult = this.cloudinaryService.uploadFile(tmpFile, "ecom_exolife_evidencias");
            String url = (String) uploadResult.get("secure_url");

            CompraService compraService = new CompraService();
            if (compraService.marcarEntregadoConEvidencia(compraId, url)) {
                ctx.status(200).result("Pedido entregado y evidencia guardada.");
            } else {
                ctx.status(400).result("Error al actualizar el estado.");
            }
            try { Files.deleteIfExists(temp); } catch (Exception ignored) {}

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    // Método legacy (lo mantenemos por si acaso, aunque el nuevo lo reemplaza)
    private void marcarEntregado(Context ctx) {
        try {
            Long compraId = Long.parseLong(ctx.pathParam("compraId"));
            CompraService compraService = new CompraService();
            if (compraService.marcarEntregado(compraId)) ctx.status(200).result("Pedido entregado.");
            else ctx.status(400).result("Error al marcar entregado.");
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }

    private void crearCompraDesdeCarrito(Context ctx) {
        JwtMiddleware.ensureAuthenticated(ctx);
        Long userId = getUserIdFromCtx(ctx);
        if (userId == null) { ctx.status(401).result("No autenticado"); return; }

        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            if (!body.containsKey("direccionId")) {
                ctx.status(400).result("Error: Debes seleccionar una dirección (direccionId) para crear el pedido.");
                return;
            }

            Object dirIdObj = body.get("direccionId");
            Long direccionId;
            if (dirIdObj instanceof Integer) {
                direccionId = ((Integer) dirIdObj).longValue();
            } else {
                direccionId = (Long) dirIdObj;
            }

            CompraService compraService = new CompraService();
            var compra = compraService.crearCompraDesdeCarrito(userId, direccionId);
            ctx.status(201).json(compra);

        } catch (Exception e) {
            ctx.status(400).result("Error: " + e.getMessage());
        }
    }

    private void subirComprobante(Context ctx) {
        JwtMiddleware.ensureAuthenticated(ctx);
        Long userId = getUserIdFromCtx(ctx);
        if (userId == null) { ctx.status(401).result("No autenticado"); return; }
        try {
            Long compraId = ctx.pathParamAsClass("compraId", Long.class).get();
            CompraService compraService = new CompraService();
            if (!compraService.compraPerteneceAUsuario(compraId, userId)) {
                ctx.status(403).result("Acceso denegado.");
                return;
            }
            io.javalin.http.UploadedFile uploaded = ctx.uploadedFile("comprobante");
            if (uploaded == null) { ctx.status(400).result("Falta archivo"); return; }
            Path temp = Files.createTempFile("comprobante-", uploaded.filename());
            Files.copy(uploaded.content(), temp, StandardCopyOption.REPLACE_EXISTING);
            File tmpFile = temp.toFile();
            var uploadResult = this.cloudinaryService.uploadFile(tmpFile, "ecom_exolife_comprobantes");
            String url = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            boolean ok = compraService.guardarComprobante(compraId, url, publicId);
            if (ok) ctx.status(200).result("Comprobante subido.");
            else ctx.status(500).result("Error al guardar.");
            try { Files.deleteIfExists(temp); } catch (Exception ignored) {}
        } catch (Exception e) {
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }
}