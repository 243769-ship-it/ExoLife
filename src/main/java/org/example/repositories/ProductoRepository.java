package org.example.repositories;

import org.example.models.Producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoRepository {

    private final Connection connection;

    public ProductoRepository(Connection connection) {
        this.connection = connection;
    }

    public List<Producto> findAllRandom() throws SQLException {
        String sql = "SELECT producto_id, nombre, descripcion, precio, stock, categoria_producto_id, vistas_contador, imagen_url " +
                "FROM productos ORDER BY RAND()";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapResultSetToList(rs);
        }
    }

    public Producto findById(Long id) throws SQLException {
        String sql = "SELECT producto_id, nombre, descripcion, precio, stock, categoria_producto_id, vistas_contador, imagen_url FROM productos WHERE producto_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToProducto(rs);
                return null;
            }
        }
    }

    public List<Producto> findByFilters(String q, Integer categoria, String sort, String order, int page, int size) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT producto_id, nombre, descripcion, precio, stock, categoria_producto_id, vistas_contador, imagen_url FROM productos WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.trim().isEmpty()) {
            sb.append(" AND (LOWER(nombre) LIKE ? OR LOWER(descripcion) LIKE ?) ");
            String like = "%" + q.toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }
        if (categoria != null) {
            sb.append(" AND categoria_producto_id = ? ");
            params.add(categoria);
        }
        String orderBy = "producto_id";
        if (sort != null) {
            if (sort.equalsIgnoreCase("vistas")) orderBy = "vistas_contador";
            else if (sort.equalsIgnoreCase("precio")) orderBy = "precio";
        }
        if (!"asc".equalsIgnoreCase(order)) order = "desc";
        sb.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        int limit = size > 0 ? size : 20;
        int offset = (Math.max(page, 1) - 1) * limit;
        sb.append(" LIMIT ? OFFSET ? ");
        params.add(limit);
        params.add(offset);
        try (PreparedStatement ps = connection.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String) ps.setString(i + 1, (String) p);
                else if (p instanceof Integer) ps.setInt(i + 1, (Integer) p);
                else if (p instanceof Long) ps.setLong(i + 1, (Long) p);
                else ps.setObject(i + 1, p);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSetToList(rs);
            }
        }
    }

    public Producto insert(Producto producto) throws SQLException {
        String sql = "INSERT INTO productos (nombre, descripcion, precio, stock, categoria_producto_id, vistas_contador, imagen_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getDescripcion());
            ps.setBigDecimal(3, producto.getPrecio());
            ps.setObject(4, producto.getStock(), Types.INTEGER);
            ps.setObject(5, producto.getCategoria_producto_id(), Types.INTEGER);
            ps.setObject(6, producto.getVistas_contador(), Types.INTEGER);
            ps.setString(7, producto.getImagen_url());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    producto.setProducto_id(keys.getInt(1));
                }
            }
            return producto;
        }
    }

    public void incrementarVistas(Long productoId) throws SQLException {
        String sql = "UPDATE productos SET vistas_contador = COALESCE(vistas_contador,0) + 1 WHERE producto_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, productoId);
            ps.executeUpdate();
        }
    }

    public boolean decrementStock(Long productoId, int cantidad) throws SQLException {
        String sql = "UPDATE productos SET stock = stock - ? WHERE producto_id = ? AND stock >= ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setLong(2, productoId);
            ps.setInt(3, cantidad);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public void incrementStock(Long productoId, int cantidad) throws SQLException {
        String sql = "UPDATE productos SET stock = stock + ? WHERE producto_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setLong(2, productoId);
            ps.executeUpdate();
        }
    }

    private List<Producto> mapResultSetToList(ResultSet rs) throws SQLException {
        List<Producto> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSetToProducto(rs));
        }
        return list;
    }

    private Producto mapResultSetToProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setProducto_id(rs.getInt("producto_id"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecio(rs.getBigDecimal("precio"));
        int stock = rs.getInt("stock");
        if (rs.wasNull()) p.setStock(null); else p.setStock(stock);
        int cat = rs.getInt("categoria_producto_id");
        if (rs.wasNull()) p.setCategoria_producto_id(null); else p.setCategoria_producto_id(cat);
        int vistas = rs.getInt("vistas_contador");
        if (rs.wasNull()) p.setVistas_contador(null); else p.setVistas_contador(vistas);
        p.setImagen_url(rs.getString("imagen_url"));
        return p;
    }

    public List<Producto> findNovedades(int limit) throws SQLException {
        String sql = "SELECT * FROM productos ORDER BY producto_id DESC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSetToList(rs);
            }
        }
    }

    public List<Producto> findMasVistos(int limit) throws SQLException {
        String sql = "SELECT * FROM productos ORDER BY vistas_contador DESC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSetToList(rs);
            }
        }
    }

    // --- NUEVO: MÉTODOS UPDATE Y DELETE ---
    public boolean update(Producto p) throws SQLException {
        String sql = "UPDATE productos SET nombre = ?, descripcion = ?, precio = ?, stock = ?, categoria_producto_id = ?, imagen_url = ? WHERE producto_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setBigDecimal(3, p.getPrecio());
            ps.setObject(4, p.getStock(), java.sql.Types.INTEGER);
            ps.setObject(5, p.getCategoria_producto_id(), java.sql.Types.INTEGER);
            ps.setString(6, p.getImagen_url());
            ps.setInt(7, p.getProducto_id()); // El ID para el WHERE
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM productos WHERE producto_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}