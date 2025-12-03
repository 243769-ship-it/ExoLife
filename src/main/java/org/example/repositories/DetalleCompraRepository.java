package org.example.repositories;

import org.example.config.DBConfig;
import org.example.models.DetalleCompra;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetalleCompraRepository {

    private final Connection connection;

    public DetalleCompraRepository(Connection connection) {
        this.connection = connection;
    }

    public DetalleCompra guardar(DetalleCompra detalle) {
        String SQL = "INSERT INTO detalle_compra (compra_id, item_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, detalle.getCompraId());
            pstmt.setLong(2, detalle.getItemId());
            pstmt.setInt(3, detalle.getCantidad());
            pstmt.setDouble(4, detalle.getPrecioUnitario());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (var rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        detalle.setDetalleId(rs.getLong(1));
                    }
                }
            }
            return detalle;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<Map<String, Object>> obtenerDetallesExtendidos(Long compraId) throws SQLException {
        List<Map<String, Object>> detalles = new ArrayList<>();

        String sql = "SELECT dc.cantidad, " +
                "       COALESCE(p.nombre, a.nombre) as nombre_item, " +
                "       COALESCE(p.imagen_url, a.imagen_url) as imagen, " +
                "       CASE WHEN p.producto_id IS NOT NULL THEN 'Producto' ELSE 'Animal' END as tipo " +
                "FROM detalle_compra dc " +
                "LEFT JOIN productos p ON dc.producto_id = p.producto_id " +
                "LEFT JOIN animales a ON dc.animal_id = a.animal_id " +
                "WHERE dc.compra_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, compraId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("nombre", rs.getString("nombre_item"));
                    item.put("cantidad", rs.getInt("cantidad"));
                    item.put("tipo", rs.getString("tipo"));
                    item.put("imagen", rs.getString("imagen"));
                    detalles.add(item);
                }
            }
        }
        return detalles;
    }
}