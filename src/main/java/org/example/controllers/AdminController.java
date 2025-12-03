package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.config.DBConfig;
import org.example.filters.JwtMiddleware;
import org.example.models.Compra;
import org.example.models.Usuario;
import org.example.repositories.CompraRepository;
import org.example.services.UsuarioService;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class AdminController {

    private final UsuarioService usuarioService = new UsuarioService();
    private static final int ROL_ADMIN_ID = 1;

    public AdminController() {
    }

    public void register(Javalin app) {
        // Endpoint existente
        app.post("/api/admin/crear-empaquetador", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.crearEmpaquetador(ctx);
        });

        // --- NUEVO ENDPOINT PARA VER PEDIDOS PENDIENTES DE PAGO/CONFIRMACIÓN ---
        app.get("/api/admin/pedidos/pendientes", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.getPedidosPendientesConfirmacion(ctx);
        });
    }

    private void crearEmpaquetador(Context ctx) {
        try {
            Usuario nuevoEmpaquetador = ctx.bodyAsClass(Usuario.class);
            Usuario creado = usuarioService.crearEmpaquetador(nuevoEmpaquetador);
            creado.setPasswordHash(null);

            ctx.status(201).json(creado);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(400).json(Map.of("error", e.getMessage()));
        }
    }

    // --- LÓGICA DEL NUEVO ENDPOINT ---
    private void getPedidosPendientesConfirmacion(Context ctx) {
        try (Connection conn = DBConfig.getConnection()) {
            CompraRepository compraRepo = new CompraRepository(conn);

            // Buscamos exactamente ese estado que deja el pago con voucher
            List<Compra> pendientes = compraRepo.findByEstado("Pendiente de confirmar");

            ctx.json(pendientes);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error al obtener pedidos pendientes: " + e.getMessage());
        }
    }
}