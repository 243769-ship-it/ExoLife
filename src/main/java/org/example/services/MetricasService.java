package org.example.services;

import org.example.models.MetricaItemVendido;
import org.example.models.MetricaItemVisto;
import org.example.models.MetricaKpi;
import org.example.models.MetricaVentaDiaria;
import org.example.models.MetricaStockBajo;
import org.example.repositories.MetricasRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MetricasService {

    private final MetricasRepository metricasRepository;

    public MetricasService(Connection connection) {
        this.metricasRepository = new MetricasRepository(connection);
    }

    public List<MetricaVentaDiaria> getVentasDiarias() throws SQLException {
        return metricasRepository.findVentasDiarias();
    }

    public List<MetricaItemVendido> getMasVendidos() throws SQLException {
        return metricasRepository.findMasVendidos();
    }

    public List<MetricaVentaDiaria> getVentasMensuales() throws SQLException {
        return metricasRepository.findVentasMensuales();
    }

    public MetricaKpi getKpis() throws SQLException {
        return metricasRepository.findKpis();
    }

    public List<MetricaItemVisto> getProductosMasVistos() throws SQLException {
        return metricasRepository.findProductosMasVistos();
    }

    public List<MetricaItemVisto> getAnimalesMasVistos() throws SQLException {
        return metricasRepository.findAnimalesMasVistos();
    }

    public List<MetricaStockBajo> getStockBajo() throws SQLException {
        int limiteStock = 5;
        return metricasRepository.findStockBajo(limiteStock);
    }
}