package org.example.repositories;

import org.example.models.MetricaItemVendido;
import org.example.models.MetricaItemVisto;
import org.example.models.MetricaKpi;
import org.example.models.MetricaVentaDiaria;
import org.example.models.MetricaStockBajo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MetricasRepository {

    private final Connection connection;

    public MetricasRepository(Connection connection) {
        this.connection = connection;
    }

    public List<MetricaVentaDiaria> findVentasDiarias() throws SQLException {
        List<MetricaVentaDiaria> metricas = new ArrayList<>();
        String sql = "SELECT DATE(c.fecha) as fecha_dia, SUM(c.total) as total_vendido " +
                "FROM compras c " +
                "WHERE c.estado IN ('Pagada', 'Empaquetado', 'Enviada', 'Entregado', 'Entregada') " +
                "GROUP BY fecha_dia " +
                "ORDER BY fecha_dia DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                metricas.add(new MetricaVentaDiaria(
                        rs.getString("fecha_dia"),
                        rs.getDouble("total_vendido")
                ));
            }
        }
        return metricas;
    }

    public List<MetricaItemVendido> findMasVendidos() throws SQLException {
        List<MetricaItemVendido> metricas = new ArrayList<>();
        String sql =
                "(SELECT p.producto_id AS id, p.nombre AS nombre, SUM(dc.cantidad) as total_vendido, 'Producto' as tipo " +
                        "FROM detalle_compra dc " +
                        "JOIN productos p ON dc.producto_id = p.producto_id " +
                        "JOIN compras c ON dc.compra_id = c.compra_id " +
                        "WHERE c.estado IN ('Pagada', 'Empaquetado', 'Enviada', 'Entregado', 'Entregada') " +
                        "GROUP BY p.producto_id, p.nombre) " +
                        "UNION ALL " +
                        "(SELECT a.animal_id AS id, a.nombre AS nombre, SUM(dc.cantidad) as total_vendido, 'Animal' as tipo " +
                        "FROM detalle_compra dc " +
                        "JOIN animales a ON dc.animal_id = a.animal_id " +
                        "JOIN compras c ON dc.compra_id = c.compra_id " +
                        "WHERE c.estado IN ('Pagada', 'Empaquetado', 'Enviada', 'Entregado', 'Entregada') " +
                        "GROUP BY a.animal_id, a.nombre) " +
                        "ORDER BY total_vendido DESC " +
                        "LIMIT 10";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                metricas.add(new MetricaItemVendido(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getInt("total_vendido"),
                        rs.getString("tipo")
                ));
            }
        }
        return metricas;
    }

    public List<MetricaVentaDiaria> findVentasMensuales() throws SQLException {
        List<MetricaVentaDiaria> metricas = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(c.fecha, '%Y-%m') as fecha_mes, SUM(c.total) as total_vendido " +
                "FROM compras c " +
                "WHERE c.estado IN ('Pagada', 'Empaquetado', 'Enviada', 'Entregado', 'Entregada') " +
                "GROUP BY fecha_mes " +
                "ORDER BY fecha_mes DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                metricas.add(new MetricaVentaDiaria(
                        rs.getString("fecha_mes"),
                        rs.getDouble("total_vendido")
                ));
            }
        }
        return metricas;
    }

    public MetricaKpi findKpis() throws SQLException {
        long totalClientes = 0;
        long totalPedidosPendientes = 0;
        long totalProductos = 0;
        long totalAnimales = 0;

        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM usuarios WHERE rol_id = 3");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) totalClientes = rs.getLong(1);
        }

        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM compras WHERE estado IN ('Pagada', 'Empaquetado', 'Enviada')");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) totalPedidosPendientes = rs.getLong(1);
        }
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM productos");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) totalProductos = rs.getLong(1);
        }
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM animales");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) totalAnimales = rs.getLong(1);
        }
        return new MetricaKpi(totalClientes, totalPedidosPendientes, totalProductos, totalAnimales);
    }

    public List<MetricaItemVisto> findProductosMasVistos() throws SQLException {
        List<MetricaItemVisto> metricas = new ArrayList<>();
        String sql = "SELECT nombre, vistas_contador FROM productos ORDER BY vistas_contador DESC LIMIT 10";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                metricas.add(new MetricaItemVisto(
                        rs.getString("nombre"),
                        rs.getInt("vistas_contador")
                ));
            }
        }
        return metricas;
    }

    public List<MetricaItemVisto> findAnimalesMasVistos() throws SQLException {
        List<MetricaItemVisto> metricas = new ArrayList<>();
        String sql = "SELECT nombre, vistas_contador FROM animales ORDER BY vistas_contador DESC LIMIT 10";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                metricas.add(new MetricaItemVisto(
                        rs.getString("nombre"),
                        rs.getInt("vistas_contador")
                ));
            }
        }
        return metricas;
    }

    public List<MetricaStockBajo> findStockBajo(int limiteStock) throws SQLException {
        List<MetricaStockBajo> items = new ArrayList<>();
        String sql =
                "(SELECT producto_id AS id, nombre, stock, 'Producto' as tipo " +
                        "FROM productos " +
                        "WHERE stock <= ?) " +
                        "UNION ALL " +
                        "(SELECT animal_id AS id, nombre, stock, 'Animal' as tipo " +
                        "FROM animales " +
                        "WHERE stock <= ?) " +
                        "ORDER BY stock ASC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limiteStock);
            ps.setInt(2, limiteStock);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new MetricaStockBajo(
                            rs.getLong("id"),
                            rs.getString("nombre"),
                            rs.getInt("stock"),
                            rs.getString("tipo")
                    ));
                }
            }
        }
        return items;
    }
}