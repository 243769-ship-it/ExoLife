package org.example.controllers;

import io.javalin.http.Context;
import org.example.models.Usuario;
import org.example.services.JWTUtil;
import org.example.services.UsuarioService;

public class LoginController {

    private final UsuarioService usuarioService = new UsuarioService();

    public void login(Context ctx) {
        try {
            Usuario request = ctx.bodyAsClass(Usuario.class);

            Usuario usuario = usuarioService.login(request.getEmail(), request.getPasswordHash());

            String token = JWTUtil.generateToken(usuario);

            ctx.status(200).json(new Object() {
                public String getToken() { return token; }

                public String getRol() { return usuario.getRol(); }

                public String getNombre() { return usuario.getNombre(); }
            });

        } catch (Exception e) {
            System.err.println("Fallo en el Login: " + e.getMessage());
            ctx.status(401).result(e.getMessage());
        }
    }
}