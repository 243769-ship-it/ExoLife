package org.example.repositories;

import org.example.config.DBConfig;
import org.example.models.Rol;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RolRepository {

    public Rol findByNombre(String nombre) throws SQLException {
        String sql = "SELECT rol_id, nombre FROM ROLES WHERE nombre = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Rol rol = new Rol();
                    rol.setRol_id(rs.getInt("rol_id"));
                    rol.setNombre(rs.getString("nombre"));
                    return rol;
                }
            }
        }
        return null;
    }

    public Rol findById(int id) throws SQLException {
        String sql = "SELECT rol_id, nombre FROM ROLES WHERE rol_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Rol rol = new Rol();
                    rol.setRol_id(rs.getInt("rol_id"));
                    rol.setNombre(rs.getString("nombre"));
                    return rol;
                }
            }
        }
        return null;
    }
}