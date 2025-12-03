package org.example.repositories;

import org.example.config.DBConfig;
import org.example.models.Usuario;
import org.example.models.Rol;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UsuarioRepository {

    private final RolRepository rolRepository = new RolRepository();

    public Usuario findByEmail(String email) throws SQLException {

        String sql = "SELECT usuario_id, nombre, correo, contraseña, rol_id FROM usuarios WHERE correo = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
            }
        }
        return null;
    }

    public Usuario save(Usuario usuario) throws SQLException {

        String sql = "INSERT INTO usuarios (nombre, correo, contraseña, rol_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Integer rolId = usuario.getRol_id();

            if (rolId == null) {
                throw new SQLException("El rol_id no está establecido en el objeto Usuario.");
            }

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getPasswordHash());
            stmt.setInt(4, rolId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("La creación del usuario falló, no se insertaron filas.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    usuario.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("La creación del usuario falló, no se obtuvo el ID.");
                }
            }
        }
        return usuario;
    }

    public Usuario findById(Long usuarioId) throws SQLException {
        String sql = "SELECT usuario_id, nombre, correo, contraseña, rol_id FROM usuarios WHERE usuario_id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
            }
        }
        return null;
    }

    public Usuario update(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nombre = ?, correo = ? WHERE usuario_id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setLong(3, usuario.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La actualización del usuario falló.");
            }
        }
        return usuario;
    }

    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario user = new Usuario();
        user.setId(rs.getLong("usuario_id"));
        user.setNombre(rs.getString("nombre"));
        user.setEmail(rs.getString("correo"));
        user.setPasswordHash(rs.getString("contraseña"));
        int rolId = rs.getInt("rol_id");
        user.setRol_id(rolId);
        return user;
    }
}