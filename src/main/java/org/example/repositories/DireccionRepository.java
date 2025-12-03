package org.example.repositories;

import org.example.config.DBConfig;
import org.example.models.Direccion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DireccionRepository {

    private Direccion mapResultSetToDireccion(ResultSet rs) throws SQLException {
        Direccion d = new Direccion();
        d.setDireccion_ID(rs.getLong("direccion_ID"));
        d.setUsuario_id(rs.getLong("usuario_id"));
        d.setCalle(rs.getString("calle"));
        d.setNumero(rs.getString("numero"));
        d.setCiudad(rs.getString("ciudad"));
        d.setEstado(rs.getString("estado"));
        d.setCp(rs.getString("cp"));
        return d;
    }

    public List<Direccion> findByUsuarioId(Long usuarioId) throws SQLException {
        List<Direccion> direcciones = new ArrayList<>();
        String sql = "SELECT * FROM direcciones WHERE usuario_id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    direcciones.add(mapResultSetToDireccion(rs));
                }
            }
        }
        return direcciones;
    }

    public Direccion save(Direccion direccion) throws SQLException {
        // CORRECCIÓN: Se eliminó "telefono" y el séptimo signo de interrogación "?"
        // Ahora solo hay 6 parámetros, que coinciden con los 6 ps.set... de abajo.
        String sql = "INSERT INTO direcciones (usuario_id, calle, numero, ciudad, estado, cp) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, direccion.getUsuario_id());
            ps.setString(2, direccion.getCalle());
            ps.setString(3, direccion.getNumero());
            ps.setString(4, direccion.getCiudad());
            ps.setString(5, direccion.getEstado());
            ps.setString(6, direccion.getCp());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    direccion.setDireccion_ID(rs.getLong(1));
                }
            }
        }
        return direccion;
    }

    public boolean delete(Long direccionId, Long usuarioId) throws SQLException {
        String sql = "DELETE FROM direcciones WHERE direccion_ID = ? AND usuario_id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, direccionId);
            ps.setLong(2, usuarioId);
            return ps.executeUpdate() > 0;
        }
    }

    public Direccion findByIdAndUsuarioId(Long direccionId, Long usuarioId) throws SQLException {
        String sql = "SELECT * FROM direcciones WHERE direccion_ID = ? AND usuario_id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, direccionId);
            ps.setLong(2, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToDireccion(rs);
            }
        }
        return null;
    }

    // Método para Admin (ver cualquier dirección por ID)
    public Direccion findById(Long direccionId) throws SQLException {
        String sql = "SELECT * FROM direcciones WHERE direccion_ID = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, direccionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToDireccion(rs);
            }
        }
        return null;
    }
}