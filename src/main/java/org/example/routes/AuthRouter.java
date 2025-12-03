package org.example.routes;

import io.javalin.Javalin;
import org.example.controllers.LoginController;
import org.example.controllers.RegisterController;

public class AuthRouter {

    private final RegisterController registerController = new RegisterController();
    private final LoginController loginController = new LoginController();

    public void register(Javalin app) {

        // Rutas
        app.post("/api/v1/auth/registro", registerController::registrar);
        app.post("/api/v1/auth/login", loginController::login);
        app.post("/api/v1/admin/crear-empaquetador", registerController::registrarEmpaquetador);
    }
}