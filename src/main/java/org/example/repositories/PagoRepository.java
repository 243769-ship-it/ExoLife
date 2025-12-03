package org.example.repositories;

import org.example.models.Pago;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class PagoRepository {

    private final Connection connection;

    public PagoRepository(Connection connection) {
        this.connection = connection;
    }

    public Pago guardar(Pago pago) throws SQLException {
        String SQL = "INSERT INTO pagos (compra_id, monto, comprobante_url, fecha_pago, estado) VALUES (?, ?, ?, NOW(), ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, pago.getCompraId());
            pstmt.setDouble(2, pago.getMonto());
            pstmt.setString(3, pago.getComprobante_url());
            pstmt.setString(4, pago.getEstado());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        pago.setPagoId(rs.getLong(1));
                    }
                }
            }
            return pago;
        }
    }

    public Pago buscarPorId(Long id) throws SQLException {
        String SQL = "SELECT * FROM pagos WHERE pago_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(SQL)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Pago pago = new Pago();
                    pago.setPagoId(rs.getLong("pago_id"));
                    pago.setCompraId(rs.getLong("compra_id"));
                    pago.setMonto(rs.getDouble("monto"));
                    // ✅ CORRECCIÓN 3: El campo en la BD es 'comprobante_url'
                    pago.setComprobante_url(rs.getString("comprobante_url"));
                    pago.setEstado(rs.getString("estado"));
                    return pago;
                }
            }
        }
        return null;
    }

    public boolean actualizarEstado(Long pagoId, String nuevoEstado) throws SQLException {
        String SQL = "UPDATE pagos SET estado = ? WHERE pago_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(SQL)) {

            pstmt.setString(1, nuevoEstado);
            pstmt.setLong(2, pagoId);

            return pstmt.executeUpdate() > 0;

        }
    }
}