package org.example.repositories;

import org.example.config.DBConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistorialVistasRepository {

    public void saveVista(Long usuarioId, Integer productoId, Integer animalId) throws SQLException {
        String sql = "INSERT INTO historial_vistas (usuario_id, producto_id, animal_id, fecha_vista) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            if (productoId != null) ps.setInt(2, productoId); else ps.setNull(2, java.sql.Types.INTEGER);
            if (animalId != null) ps.setInt(3, animalId); else ps.setNull(3, java.sql.Types.INTEGER);
            ps.executeUpdate();
        }
    }

    // AHORA DEVUELVE UNA LISTA DE MAPAS CON LOS DATOS REALES
    public List<Map<String, Object>> findRecientes(Long usuarioId, int limit) throws SQLException {
        List<Map<String, Object>> items = new ArrayList<>();

        // Esta consulta busca el nombre, precio e imagen dependiendo de si es animal o producto
        String sql = "SELECT " +
                "   h.producto_id, p.nombre as p_nombre, p.imagen_url as p_img, p.precio as p_precio, " +
                "   h.animal_id,   a.nombre as a_nombre, a.imagen_url as a_img, a.precio as a_precio, " +
                "   MAX(h.fecha_vista) as ultima_vista " +
                "FROM historial_vistas h " +
                "LEFT JOIN productos p ON h.producto_id = p.producto_id " +
                "LEFT JOIN animales a ON h.animal_id = a.animal_id " +
                "WHERE h.usuario_id = ? " +
                "GROUP BY h.producto_id, h.animal_id " +
                "ORDER BY ultima_vista DESC " +
                "LIMIT ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, usuarioId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();

                    int pId = rs.getInt("producto_id");
                    if (!rs.wasNull()) {
                        // ES UN PRODUCTO
                        item.put("type", "producto");
                        item.put("id", pId);
                        item.put("nombre", rs.getString("p_nombre"));
                        item.put("imagen_url", rs.getString("p_img"));
                        item.put("precio", rs.getBigDecimal("p_precio"));
                    } else {
                        // ES UN ANIMAL
                        item.put("type", "animal");
                        item.put("id", rs.getInt("animal_id"));
                        item.put("nombre", rs.getString("a_nombre"));
                        item.put("imagen_url", rs.getString("a_img"));
                        item.put("precio", rs.getBigDecimal("a_precio"));
                    }

                    items.add(item);
                }
            }
        }
        return items;
    }
}