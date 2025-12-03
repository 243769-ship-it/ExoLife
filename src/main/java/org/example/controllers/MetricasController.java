package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.config.DBConfig;
import org.example.filters.JwtMiddleware;
import org.example.services.MetricasService;
import java.sql.Connection;

public class MetricasController {

    private static final int ROL_ADMIN_ID = 1;

    public MetricasController() {
    }

    public void register(Javalin app) {
        app.get("/api/admin/metricas/ventas-diarias", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.getVentasDiarias(ctx);
        });

        app.get("/api/admin/metricas/mas-vendidos", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.getMasVendidos(ctx);
        });

        app.get("/api/admin/metricas/ventas-mes", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.getVentasMensuales(ctx);
        });

        app.get("/api/admin/metricas/kpis", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.getKpis(ctx);
        });

        app.get("/api/admin/metricas/productos-mas-vistos", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.getProductosMasVistos(ctx);
        });

        app.get("/api/admin/metricas/animales-mas-vistos", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.getAnimalesMasVistos(ctx);
        });

        app.get("/api/admin/metricas/stock-bajo", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.getStockBajo(ctx);
        });
    }

    private void getVentasDiarias(Context ctx) {
        try (Connection conn = DBConfig.getConnection()) {
            MetricasService metricasService = new MetricasService(conn);
            var metricas = metricasService.getVentasDiarias();
            ctx.status(200).json(metricas);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error al obtener métricas de ventas: " + e.getMessage());
        }
    }

    private void getMasVendidos(Context ctx) {
        try (Connection conn = DBConfig.getConnection()) {
            MetricasService metricasService = new MetricasService(conn);
            var metricas = metricasService.getMasVendidos();
            ctx.status(200).json(metricas);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error al obtener métricas de más vendidos: " + e.getMessage());
        }
    }

    private void getVentasMensuales(Context ctx) {
        try (Connection conn = DBConfig.getConnection()) {
            MetricasService metricasService = new MetricasService(conn);
            var metricas = metricasService.getVentasMensuales();
            ctx.status(200).json(metricas);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error al obtener métricas de ventas mensuales: " + e.getMessage());
        }
    }

    private void getKpis(Context ctx) {
        try (Connection conn = DBConfig.getConnection()) {
            MetricasService metricasService = new MetricasService(conn);
            var kpis = metricasService.getKpis();
            ctx.status(200).json(kpis);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error al obtener KPIs: " + e.getMessage());
        }
    }

    private void getProductosMasVistos(Context ctx) {
        try (Connection conn = DBConfig.getConnection()) {
            MetricasService metricasService = new MetricasService(conn);
            var metricas = metricasService.getProductosMasVistos();
            ctx.status(200).json(metricas);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error al obtener productos más vistos: " + e.getMessage());
        }
    }

    private void getAnimalesMasVistos(Context ctx) {
        try (Connection conn = DBConfig.getConnection()) {
            MetricasService metricasService = new MetricasService(conn);
            var metricas = metricasService.getAnimalesMasVistos();
            ctx.status(200).json(metricas);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error al obtener animales más vistos: " + e.getMessage());
        }
    }

    private void getStockBajo(Context ctx) {
        try (Connection conn = DBConfig.getConnection()) {
            MetricasService metricasService = new MetricasService(conn);
            var items = metricasService.getStockBajo();
            ctx.status(200).json(items);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error al obtener items con stock bajo: " + e.getMessage());
        }
    }
}