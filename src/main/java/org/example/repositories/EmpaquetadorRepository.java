package org.example.repositories;

import org.example.config.DBConfig;
import org.example.models.Compra;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmpaquetadorRepository {

    public List<Compra> findWorkQueueByEmpaquetadorId(Long empaquetadorId) throws SQLException {
        List<Compra> compras = new ArrayList<>();
        // El SQL ya estaba bien, trae todo (*)
        String sql = "SELECT * FROM compras WHERE empaquetador_id = ? AND estado IN ('Pagada', 'Empaquetado') ORDER BY fecha ASC";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, empaquetadorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Compra c = new Compra();
                    c.setCompraId(rs.getLong("compra_id"));
                    c.setUsuarioId(rs.getLong("usuario_id"));

                    // --- ESTA ES LA LÍNEA QUE FALTABA ---
                    // Leemos la columna de la BD y la metemos al objeto
                    long dirId = rs.getLong("direccion_id");
                    if (!rs.wasNull()) {
                        c.setDireccionId(dirId);
                    }
                    // ------------------------------------

                    c.setEstado(rs.getString("estado"));
                    c.setTotal(rs.getDouble("total"));

                    // Agregué la fecha también porque es útil para ordenar
                    java.sql.Timestamp fecha = rs.getTimestamp("fecha");
                    if (fecha != null) {
                        c.setFecha(fecha.toLocalDateTime());
                    }

                    compras.add(c);
                }
            }
        }
        return compras;
    }
}