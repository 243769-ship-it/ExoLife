package org.example.repositories;

import org.example.models.Compra;
import org.example.models.DetalleCompraItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import org.example.config.DBConfig;

public class CompraRepository {

    private final Connection connection;

    public CompraRepository(Connection connection) {
        this.connection = connection;
    }

    private Compra mapResultSetToCompra(ResultSet rs) throws SQLException {
        Compra compra = new Compra();
        compra.setCompraId(rs.getLong("compra_id"));
        compra.setUsuarioId(rs.getLong("usuario_id"));

        long dirId = rs.getLong("direccion_id");
        if (!rs.wasNull()) {
            compra.setDireccionId(dirId);
        }

        compra.setEstado(rs.getString("estado"));
        compra.setTotal(rs.getDouble("total"));

        try {
            compra.setComprobanteUrl(rs.getString("comprobante_url"));
        } catch (SQLException e) {}

        // NUEVO: Mapeo de evidencia
        try {
            compra.setEvidenciaEntregaUrl(rs.getString("evidencia_entrega_url"));
        } catch (SQLException e) {}

        Timestamp fecha = rs.getTimestamp("fecha");
        if (fecha != null) {
            compra.setFecha(fecha.toLocalDateTime());
        }
        compra.setGuiaEnvioNumero(rs.getString("guia_envio_numero"));
        Timestamp fechaEntrega = rs.getTimestamp("fecha_entrega_confirmada");
        if (fechaEntrega != null) {
            compra.setFechaEntregaConfirmada(fechaEntrega.toLocalDateTime());
        }
        if (rs.getObject("empaquetador_id") != null) {
            compra.setEmpaquetador_id(rs.getLong("empaquetador_id"));
        }
        return compra;
    }

    // --- NUEVO: HISTORIAL COMPLETO ---
    public List<Compra> findHistoryByUserId(Long usuarioId) throws SQLException {
        List<Compra> lista = new ArrayList<>();
        String sql = "SELECT * FROM compras WHERE usuario_id = ? ORDER BY fecha DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToCompra(rs));
                }
            }
        }
        return lista;
    }

    // --- NUEVO: MARCAR ENTREGADO CON EVIDENCIA ---
    public boolean marcarEntregadoConEvidencia(Long compraId, String urlEvidencia) throws SQLException {
        String sql = "UPDATE compras SET estado = 'Entregada', fecha_entrega_confirmada = NOW(), evidencia_entrega_url = ? WHERE compra_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, urlEvidencia);
            ps.setLong(2, compraId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Compra> findAllActiveOrders() throws SQLException {
        List<Compra> lista = new ArrayList<>();
        String sql = "SELECT * FROM compras WHERE estado NOT IN ('Entregada', 'Cancelada', 'Rechazada') ORDER BY fecha ASC";
        try (PreparedStatement ps = this.connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapResultSetToCompra(rs));
            }
        }
        return lista;
    }

    public List<Compra> findByEstado(String estado) throws SQLException {
        List<Compra> lista = new ArrayList<>();
        String sql = "SELECT * FROM compras WHERE estado = ? ORDER BY fecha ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSetToCompra(rs));
                }
            }
        }
        return lista;
    }

    public boolean actualizarEstadoYVoucher(Long compraId, String nuevoEstado, String voucherUrl) throws SQLException {
        String sql = "UPDATE compras SET estado = ?, comprobante_url = ? WHERE compra_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setString(2, voucherUrl);
            ps.setLong(3, compraId);
            return ps.executeUpdate() > 0;
        }
    }

    public Compra guardar(Compra compra) throws SQLException {
        String SQL = "INSERT INTO compras (usuario_id, direccion_id, fecha, estado, total) VALUES (?, ?, NOW(), ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, compra.getUsuarioId());
            if (compra.getDireccionId() != null) {
                pstmt.setLong(2, compra.getDireccionId());
            } else {
                pstmt.setNull(2, Types.BIGINT);
            }
            pstmt.setString(3, compra.getEstado());
            pstmt.setDouble(4, compra.getTotal());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        compra.setCompraId(rs.getLong(1));
                    }
                }
            }
            return compra;
        }
    }

    public Compra findById(Long compraId) throws SQLException {
        String sql = "SELECT * FROM compras WHERE compra_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, compraId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCompra(rs);
                }
            }
        }
        return null;
    }

    public Double getTotalMontoCompra(Long compraId) throws SQLException {
        String sql = "SELECT total FROM compras WHERE compra_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, compraId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return null;
    }

    public boolean actualizarEstadoCompra(Long compraId, String estadoAnterior, String nuevoEstado) throws SQLException {
        String sql = "UPDATE compras SET estado = ? WHERE compra_id = ? AND estado = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setLong(2, compraId);
            ps.setString(3, estadoAnterior);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarEstadoCompra(Long compraId, String nuevoEstado) throws SQLException {
        String sql = "UPDATE compras SET estado = ? WHERE compra_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setLong(2, compraId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Compra> findPendingShipment() throws SQLException {
        List<Compra> compras = new ArrayList<>();
        String sql = "SELECT * FROM compras WHERE estado = 'Pagada' OR estado = 'Empaquetado'";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                compras.add(mapResultSetToCompra(rs));
            }
        }
        return compras;
    }

    public boolean registerShipment(Long compraId, String guiaEnvioNumero) throws SQLException {
        String sql = "UPDATE compras SET estado = 'Enviada', guia_envio_numero = ? WHERE compra_id = ? AND estado IN ('Pagada', 'Empaquetado')";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, guiaEnvioNumero);
            ps.setLong(2, compraId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean confirmDelivery(Long compraId) throws SQLException {
        String sql = "UPDATE compras SET estado = 'Entregada', fecha_entrega_confirmada = NOW() WHERE compra_id = ? AND estado = 'Enviada'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, compraId);
            return ps.executeUpdate() > 0;
        }
    }

    public Compra createFromCarrito(Long usuarioId, Long direccionId, org.example.models.Carrito carrito, Connection conn) throws SQLException {
        System.out.println("🛒 REPO: Creando compra. Usuario: " + usuarioId + " | Dirección ID: " + direccionId);

        double totalCompra = 0.0;
        for (org.example.models.CarritoItem it : carrito.getItems()) {
            totalCompra += (it.getPrecioUnitario() * it.getCantidad());
        }

        String insertCompra = "INSERT INTO compras (usuario_id, direccion_id, fecha, estado, total) VALUES (?, ?, NOW(), 'Creada', ?)";

        try (PreparedStatement ps = conn.prepareStatement(insertCompra, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, usuarioId);
            if (direccionId != null) {
                ps.setLong(2, direccionId);
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setDouble(3, totalCompra);

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long compraId = rs.getLong(1);

                    String insertDetalle = "INSERT INTO detalle_compra (compra_id, producto_id, animal_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement psd = conn.prepareStatement(insertDetalle)) {
                        for (org.example.models.CarritoItem it : carrito.getItems()) {
                            psd.setLong(1, compraId);
                            psd.setObject(2, it.getProductoId());
                            psd.setObject(3, it.getAnimalId());
                            psd.setInt(4, it.getCantidad());
                            psd.setDouble(5, it.getPrecioUnitario());
                            psd.addBatch();
                        }
                        psd.executeBatch();
                    }
                    Compra c = new Compra();
                    c.setCompraId(compraId);
                    c.setUsuarioId(usuarioId);
                    c.setDireccionId(direccionId);
                    c.setEstado("Creada");
                    c.setTotal(totalCompra);
                    return c;
                }
            }
        }
        return null;
    }

    public List<DetalleCompraItem> findItemsByCompraId(Long compraId) throws SQLException {
        List<DetalleCompraItem> items = new ArrayList<>();
        String sqlDetalle = "SELECT producto_id, animal_id, cantidad FROM detalle_compra WHERE compra_id = ?";
        try (PreparedStatement psd = this.connection.prepareStatement(sqlDetalle)) {
            psd.setLong(1, compraId);
            try (ResultSet rsd = psd.executeQuery()) {
                while (rsd.next()) {
                    items.add(new DetalleCompraItem(
                            (Long) rsd.getObject("producto_id"),
                            (Long) rsd.getObject("animal_id"),
                            rsd.getInt("cantidad")
                    ));
                }
            }
        }
        return items;
    }

    public int cleanupExpiredAndRestoreStock() throws SQLException {
        int total = 0;
        String sqlSelect = "SELECT c.compra_id FROM compras c WHERE c.estado = 'Creada' AND c.fecha < DATE_SUB(NOW(), INTERVAL 24 HOUR)";
        try (PreparedStatement ps = this.connection.prepareStatement(sqlSelect);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long compraId = rs.getLong("compra_id");
                String sqlDetalle = "SELECT producto_id, animal_id, cantidad FROM detalle_compra WHERE compra_id = ?";
                try (PreparedStatement psd = this.connection.prepareStatement(sqlDetalle)) {
                    psd.setLong(1, compraId);
                    try (ResultSet rsd = psd.executeQuery()) {
                        while (rsd.next()) {
                            int cant = rsd.getInt("cantidad");
                            if (rsd.getObject("producto_id") != null) {
                                long pid = rsd.getLong("producto_id");
                                String up = "UPDATE productos SET stock = stock + ? WHERE producto_id = ?";
                                try (PreparedStatement pup = this.connection.prepareStatement(up)) {
                                    pup.setInt(1, cant);
                                    pup.setLong(2, pid);
                                    pup.executeUpdate();
                                }
                            } else if (rsd.getObject("animal_id") != null) {
                                long aid = rsd.getLong("animal_id");
                                String up = "UPDATE animales SET stock = stock + ? WHERE animal_id = ?";
                                try (PreparedStatement pup = this.connection.prepareStatement(up)) {
                                    pup.setInt(1, cant);
                                    pup.setLong(2, aid);
                                    pup.executeUpdate();
                                }
                            }
                        }
                    }
                }
                String upc = "UPDATE compras SET estado = 'Cancelada' WHERE compra_id = ?";
                try (PreparedStatement psc = this.connection.prepareStatement(upc)) {
                    psc.setLong(1, compraId);
                    psc.executeUpdate();
                }
                total++;
            }
        }
        return total;
    }

    public Long selectNextEmpaquetador() throws SQLException {
        String q = "SELECT u.usuario_id, COUNT(c.compra_id) as cnt FROM usuarios u LEFT JOIN compras c ON u.usuario_id = c.empaquetador_id AND c.estado IN ('Pagada','Empaquetado','Enviada') WHERE u.rol_id = ? GROUP BY u.usuario_id ORDER BY cnt ASC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setInt(1, 2);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("usuario_id");
            }
        }
        return null;
    }

    public boolean compraPerteneceAUsuario(Long compraId, Long usuarioId) throws SQLException {
        String sql = "SELECT compra_id FROM compras WHERE compra_id = ? AND usuario_id = ? AND estado IN ('Creada','Pendiente de confirmar')";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, compraId);
            ps.setLong(2, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean guardarComprobante(Long compraId, String url, String publicId) throws SQLException {
        String sql = "UPDATE compras SET comprobante_url = ?, comprobante_public_id = ?, estado = 'Pendiente de confirmar' WHERE compra_id = ? AND estado = 'Creada'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, url);
            ps.setString(2, publicId);
            ps.setLong(3, compraId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean asignarEmpaquetador(Long compraId, Long empaquetadorId) throws SQLException {
        String sql = "UPDATE compras SET empaquetador_id = ?, estado = 'Pagada' WHERE compra_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, empaquetadorId);
            ps.setLong(2, compraId);
            return ps.executeUpdate() > 0;
        }
    }

    public Compra findActiveOrderByUserId(Long usuarioId) throws SQLException {
        String sql = "SELECT * FROM compras WHERE usuario_id = ? " +
                "AND estado NOT IN ('Entregada', 'Cancelada') " + // (Rechazada sí se muestra)
                "ORDER BY fecha DESC LIMIT 1";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCompra(rs);
                }
            }
        }
        return null;
    }

    public boolean asignarDireccion(Long compraId, Long direccionId, Long usuarioId) throws SQLException {
        String sql = "UPDATE compras SET direccion_id = ? " +
                "WHERE compra_id = ? AND usuario_id = ? AND estado = 'Creada'";
        try (PreparedStatement ps = this.connection.prepareStatement(sql)) {
            ps.setLong(1, direccionId);
            ps.setLong(2, compraId);
            ps.setLong(3, usuarioId);
            return ps.executeUpdate() > 0;
        }
    }
}