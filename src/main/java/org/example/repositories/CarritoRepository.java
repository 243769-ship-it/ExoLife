package org.example.repositories;

import org.example.config.DBConfig;
import org.example.models.Carrito;
import org.example.models.CarritoItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarritoRepository {

    public Carrito findByUsuarioId(Long usuarioId) throws SQLException {
        String sql = "SELECT carrito_id, usuario_id FROM carritos WHERE usuario_id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Carrito c = new Carrito();
                    c.setCarritoId(rs.getLong("carrito_id"));
                    c.setUsuarioId(rs.getLong("usuario_id"));
                    c.setItems(findItemsByCarritoId(c.getCarritoId(), conn));
                    return c;
                }
            }
        }
        return null;
    }

    public Carrito createCarrito(Long usuarioId) throws SQLException {
        String sql = "INSERT INTO carritos (usuario_id) VALUES (?)";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, usuarioId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Carrito c = new Carrito();
                    c.setCarritoId(rs.getLong(1));
                    c.setUsuarioId(usuarioId);
                    return c;
                }
            }
        }
        return null;
    }

    public List<CarritoItem> findItemsByCarritoId(Long carritoId, Connection conn) throws SQLException {
        List<CarritoItem> items = new ArrayList<>();
        String sql = "SELECT item_id, carrito_id, producto_id, animal_id, cantidad, precio_unitario FROM carrito_items WHERE carrito_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, carritoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CarritoItem it = new CarritoItem();
                    it.setItemId(rs.getLong("item_id"));
                    it.setCarritoId(rs.getLong("carrito_id"));

                    Object pObj = rs.getObject("producto_id");
                    Object aObj = rs.getObject("animal_id");

                    it.setProductoId(pObj instanceof Number ? ((Number) pObj).longValue() : null);
                    it.setAnimalId(aObj instanceof Number ? ((Number) aObj).longValue() : null);

                    it.setCantidad(rs.getInt("cantidad"));
                    it.setPrecioUnitario(rs.getDouble("precio_unitario"));
                    items.add(it);
                }
            }
        }
        return items;
    }

    public List<CarritoItem> findItemsByCarritoId(Long carritoId) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            return findItemsByCarritoId(carritoId, conn);
        }
    }

    public CarritoItem addItem(Long carritoId, Long productoId, Long animalId, int cantidad, double precioUnitario) throws SQLException {
        String sql = "INSERT INTO carrito_items (carrito_id, producto_id, animal_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, carritoId);

            if (productoId != null) {
                ps.setLong(2, productoId);
            } else {
                ps.setNull(2, Types.BIGINT);
            }

            if (animalId != null) {
                ps.setLong(3, animalId);
            } else {
                ps.setNull(3, Types.BIGINT);
            }

            ps.setInt(4, cantidad);
            ps.setDouble(5, precioUnitario);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    CarritoItem it = new CarritoItem();
                    it.setItemId(rs.getLong(1));
                    it.setCarritoId(carritoId);
                    it.setProductoId(productoId);
                    it.setAnimalId(animalId);
                    it.setCantidad(cantidad);
                    it.setPrecioUnitario(precioUnitario);
                    return it;
                }
            }
        }
        return null;
    }

    public boolean removeItem(Long itemId) throws SQLException {
        String sql = "DELETE FROM carrito_items WHERE item_id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, itemId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean clearCarrito(Long carritoId) throws SQLException {
        String sql = "DELETE FROM carrito_items WHERE carrito_id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, carritoId);
            return ps.executeUpdate() >= 0;
        }
    }

    public boolean updateCantidad(Long itemId, int nuevaCantidad) throws SQLException {
        String sql = "UPDATE carrito_items SET cantidad = ? WHERE item_id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nuevaCantidad);
            ps.setLong(2, itemId);
            return ps.executeUpdate() > 0;
        }
    }

    // --- NUEVO MÉTODO PARA LÓGICA INTELIGENTE ---
    public CarritoItem findItemByProductOrAnimal(Long carritoId, Long productoId, Long animalId) throws SQLException {
        // Busca si ya existe un item con ese producto O ese animal en este carrito
        String sql = "SELECT * FROM carrito_items WHERE carrito_id = ? AND (producto_id = ? OR animal_id = ?)";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, carritoId);

            if (productoId != null) ps.setLong(2, productoId);
            else ps.setNull(2, Types.BIGINT);

            if (animalId != null) ps.setLong(3, animalId);
            else ps.setNull(3, Types.BIGINT);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CarritoItem it = new CarritoItem();
                    it.setItemId(rs.getLong("item_id"));
                    it.setCarritoId(rs.getLong("carrito_id"));
                    it.setCantidad(rs.getInt("cantidad"));
                    it.setPrecioUnitario(rs.getDouble("precio_unitario"));

                    Object pObj = rs.getObject("producto_id");
                    it.setProductoId(pObj instanceof Number ? ((Number) pObj).longValue() : null);

                    Object aObj = rs.getObject("animal_id");
                    it.setAnimalId(aObj instanceof Number ? ((Number) aObj).longValue() : null);

                    return it;
                }
            }
        }
        return null;
    }
}