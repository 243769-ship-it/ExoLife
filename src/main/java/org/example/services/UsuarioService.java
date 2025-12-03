package org.example.services;

import org.example.models.Usuario;
import org.example.models.Rol;
import org.example.repositories.UsuarioRepository;
import org.example.repositories.RolRepository;
import java.sql.SQLException;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private static final int ROL_CLIENTE = 3;
    private static final int ROL_EMPAQUETADOR = 2;

    public Usuario registrarUsuario(Usuario usuario) throws Exception {
        if (usuario.getRol_id() == null) {
            usuario.setRol_id(ROL_CLIENTE);
        }
        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            throw new Exception("El correo ya está registrado.");
        }
        String hashContraseña = SecurityUtil.hashPassword(usuario.getPasswordHash());
        usuario.setPasswordHash(hashContraseña);
        return usuarioRepository.save(usuario);
    }

    public Usuario login(String email, String rawPassword) throws Exception {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            throw new Exception("Credenciales inválidas: Correo o contraseña incorrectos.");
        }
        if (SecurityUtil.verifyPassword(rawPassword, usuario.getPasswordHash())) {
            return usuario;
        } else {
            throw new Exception("Credenciales inválidas: Correo o contraseña incorrectos.");
        }
    }

    public Usuario crearEmpaquetador(Usuario usuario) throws Exception {
        if (usuario.getEmail() == null || usuario.getPasswordHash() == null || usuario.getNombre() == null) {
            throw new Exception("El nombre, correo y contraseña son obligatorios.");
        }
        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            throw new Exception("El correo ya está registrado.");
        }
        usuario.setRol_id(ROL_EMPAQUETADOR);
        String hashContraseña = SecurityUtil.hashPassword(usuario.getPasswordHash());
        usuario.setPasswordHash(hashContraseña);
        return usuarioRepository.save(usuario);
    }

    public Usuario getPerfil(Long usuarioId) throws Exception {
        Usuario usuario = usuarioRepository.findById(usuarioId);
        if (usuario == null) {
            throw new Exception("Usuario no encontrado.");
        }
        usuario.setPasswordHash(null);
        return usuario;
    }

    public Usuario updatePerfil(Long usuarioId, Usuario datosNuevos) throws Exception {
        Usuario usuarioActual = usuarioRepository.findById(usuarioId);
        if (usuarioActual == null) {
            throw new Exception("Usuario no encontrado.");
        }

        if (datosNuevos.getEmail() != null && !datosNuevos.getEmail().isEmpty()) {
            Usuario emailExistente = usuarioRepository.findByEmail(datosNuevos.getEmail());
            if (emailExistente != null && !emailExistente.getId().equals(usuarioId)) {
                throw new Exception("El nuevo correo ya está en uso por otra cuenta.");
            }
            usuarioActual.setEmail(datosNuevos.getEmail());
        }

        if (datosNuevos.getNombre() != null && !datosNuevos.getNombre().isEmpty()) {
            usuarioActual.setNombre(datosNuevos.getNombre());
        }

        return usuarioRepository.update(usuarioActual);
    }

    // --- NUEVO MÉTODO: Obtener usuario por ID (para el Admin) ---
    public Usuario obtenerUsuarioPorId(Long id) throws Exception {
        Usuario u = usuarioRepository.findById(id);
        if (u != null) {
            u.setPasswordHash(null); // Seguridad: Borramos la contraseña antes de enviarla
        }
        return u;
    }
}