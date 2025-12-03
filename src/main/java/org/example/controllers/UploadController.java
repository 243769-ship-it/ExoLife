package org.example.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.example.services.CloudinaryService;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class UploadController {

    private final CloudinaryService cloudinaryService;

    public UploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    public void register(Javalin app) {
        app.post("/api/v1/upload/test", this::handleFileUpload);
    }

    private void handleFileUpload(Context ctx) throws Exception {

        UploadedFile uploadedFile = ctx.uploadedFile("file");
        if (uploadedFile == null) {
            ctx.status(400).result("No se encontró el archivo 'file' en la petición.");
            return;
        }

        Path tempPath = null;
        File tempFile = null;

        try {
            tempPath = Files.createTempFile("upload-", uploadedFile.filename());
            Files.copy(uploadedFile.content(), tempPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            tempFile = tempPath.toFile();
            Map uploadResult = cloudinaryService.uploadFile(tempFile, "ecom_exolife_test");
            String imageUrl = (String) uploadResult.get("secure_url");

            ctx.status(201).json(Map.of(
                    "message", "Archivo subido con éxito a Cloudinary",
                    "url", imageUrl
            ));

        } catch (Exception e) {
            System.err.println("Error subiendo a Cloudinary: " + e.getMessage());
            ctx.status(500).result("Error interno al procesar la subida del archivo: " + e.getMessage());
        } finally {
            if (tempPath != null) {
                try {
                    Files.deleteIfExists(tempPath);
                } catch (java.io.IOException e) {
                    System.err.println("Advertencia: No se pudo eliminar el archivo temporal: " + tempPath);
                }
            }
        }
    }
}