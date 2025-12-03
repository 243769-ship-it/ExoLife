package org.example.controllers;

import io.javalin.http.Context;
import org.example.models.Usuario;
import org.example.services.UsuarioService;
import org.example.filters.JwtMiddleware;

public class RegisterController {

    private final UsuarioService usuarioService = new UsuarioService();

    private static final int ROL_CLIENTE = 3;
    private static final int ROL_ADMIN_ID = 1;

    public void registrar(Context ctx) {
        try {
            Usuario nuevoUsuario = ctx.bodyAsClass(Usuario.class);

            if (nuevoUsuario.getEmail() == null || nuevoUsuario.getPasswordHash() == null) {
                ctx.status(400).result("Error: El correo y la contraseña son obligatorios.");
                return;
            }

            nuevoUsuario.setRol_id(ROL_CLIENTE);
            Usuario usuarioRegistrado = usuarioService.registrarUsuario(nuevoUsuario);
            ctx.status(201).json(usuarioRegistrado);

        } catch (Exception e) {
            System.err.println("Error en el Controller de Registro: " + e.getMessage());
            ctx.status(400).result("Error al registrar: " + e.getMessage());
        }
    }

    public void registrarEmpaquetador(Context ctx) {
        JwtMiddleware.ensureRole(ctx, ROL_ADMIN_ID);

        try {
            Usuario nuevoEmpaquetador = ctx.bodyAsClass(Usuario.class);

            if (nuevoEmpaquetador.getEmail() == null || nuevoEmpaquetador.getPasswordHash() == null || nuevoEmpaquetador.getNombre() == null) {
                ctx.status(400).result("Error: Nombre, correo y contraseña son obligatorios.");
                return;
            }

            Usuario creado = usuarioService.crearEmpaquetador(nuevoEmpaquetador);

            creado.setPasswordHash(null);

            ctx.status(201).json(creado);

        } catch (Exception e) {
            ctx.status(400).result("Error al crear empaquetador: " + e.getMessage());
        }
    }
}