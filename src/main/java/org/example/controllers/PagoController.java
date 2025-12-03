package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.services.PagoService;
import org.example.models.Pago;
import org.example.services.CloudinaryService;
import io.javalin.http.UploadedFile;
import org.example.filters.JwtMiddleware;
import org.example.config.DBConfig;
import org.example.repositories.CompraRepository;
import org.example.repositories.PagoRepository;
import org.example.repositories.ProductoRepository;
import org.example.repositories.AnimalRepository; // <-- ¡¡¡NUEVO IMPORT!!!
import java.sql.Connection;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.io.IOException;

public class PagoController {

    private final CloudinaryService cloudinaryService;
    private static final int ROL_ADMIN_ID = 1;

    public PagoController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    public void register(Javalin app) {
        app.post("/api/v1/pagos/voucher", this::subirVoucher);

        app.post("/api/v1/pagos/confirmar/{pagoId}", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.confirmarPago(ctx);
        });

        app.post("/api/v1/pagos/rechazar/{pagoId}", ctx -> {
            JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);
            this.rechazarPago(ctx);
        });
    }

    private void subirVoucher(Context ctx) {
        Long compraId;
        double monto;

        try {
            compraId = ctx.formParamAsClass("compraId", Long.class).check(id -> id > 0, "Compra ID inválida").get();
            monto = ctx.formParamAsClass("monto", Double.class).check(m -> m > 0.0, "Monto inválido").get();
        } catch (Exception e) {
            ctx.status(400).result("Error en los datos: Compra ID y Monto deben ser números válidos y mayores a cero.");
            return;
        }

        UploadedFile uploadedFile = ctx.uploadedFile("comprobante");
        if (uploadedFile == null) {
            ctx.status(400).result("Debe adjuntar el archivo comprobante en el campo 'comprobante'.");
            return;
        }
        if (!uploadedFile.filename().toLowerCase().endsWith(".png")) {
            ctx.status(400).result("Error: El comprobante debe ser un archivo PNG.");
            return;
        }

        Path tempPath = null;
        try {
            tempPath = Files.createTempFile("voucher-", uploadedFile.filename());
            Files.copy(uploadedFile.content(), tempPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            File tempFile = tempPath.toFile();
            Map uploadResult = cloudinaryService.uploadFile(tempFile, "ecom_exolife_vouchers");
            String comprobanteUrl = (String) uploadResult.get("secure_url");

            try (Connection conn = DBConfig.getConnection()) {
                PagoRepository pagoRepo = new PagoRepository(conn);
                CompraRepository compraRepo = new CompraRepository(conn);
                ProductoRepository productoRepo = new ProductoRepository(conn);
                AnimalRepository animalRepo = new AnimalRepository(conn);
                PagoService pagoService = new PagoService(pagoRepo, compraRepo, productoRepo, animalRepo);
                Pago pagoRegistrado = pagoService.registrarVoucher(compraId, monto, comprobanteUrl);
                if (pagoRegistrado != null) {
                    ctx.status(201).json(pagoRegistrado);
                } else {
                    ctx.status(500).result("Error al registrar el pago en la base de datos.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error interno al procesar el archivo o la base de datos: " + e.getMessage());
        } finally {
            if (tempPath != null) {
                try {
                    Files.deleteIfExists(tempPath);
                } catch (IOException e) {
                    System.err.println("Advertencia: No se pudo eliminar el archivo temporal del voucher: " + tempPath);
                }
            }
        }
    }

    private void confirmarPago(Context ctx) {
        try {
            Long pagoId = Long.parseLong(ctx.pathParam("pagoId"));

            try (Connection conn = DBConfig.getConnection()) {
                PagoRepository pagoRepo = new PagoRepository(conn);
                CompraRepository compraRepo = new CompraRepository(conn);
                ProductoRepository productoRepo = new ProductoRepository(conn);
                AnimalRepository animalRepo = new AnimalRepository(conn);
                PagoService pagoService = new PagoService(pagoRepo, compraRepo, productoRepo, animalRepo);
                if (pagoService.procesarVerificacion(pagoId, true)) {
                    ctx.status(200).result("Pago verificado y Compra actualizada a 'Pagada'.");
                } else {
                    ctx.status(500).result("Error al confirmar el pago o actualizar la Compra.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error interno al confirmar el pago: " + e.getMessage());
        }
    }

    private void rechazarPago(Context ctx) {
        try {
            Long pagoId = Long.parseLong(ctx.pathParam("pagoId"));

            try (Connection conn = DBConfig.getConnection()) {
                PagoRepository pagoRepo = new PagoRepository(conn);
                CompraRepository compraRepo = new CompraRepository(conn);
                ProductoRepository productoRepo = new ProductoRepository(conn);
                AnimalRepository animalRepo = new AnimalRepository(conn);

                PagoService pagoService = new PagoService(pagoRepo, compraRepo, productoRepo, animalRepo);

                if (pagoService.procesarVerificacion(pagoId, false)) {
                    ctx.status(200).result("Pago rechazado. Stock restaurado.");
                } else {
                    ctx.status(500).result("Error al rechazar el pago.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error interno al rechazar el pago: " + e.getMessage());
        }
    }
}