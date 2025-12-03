package org.example.services;

import org.example.models.Producto;
import org.example.repositories.ProductoRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(Connection connection) {
        this.productoRepository = new ProductoRepository(connection);
    }

    public List<Producto> obtenerPorFiltros(String q, Integer categoria, String sort, String order, int page, int size) throws Exception {
        try {
            return productoRepository.findByFilters(q, categoria, sort, order, page, size);
        } catch (SQLException e) {
            throw new Exception("Fallo al buscar productos por filtros. " + e.getMessage(), e);
        }
    }

    public Producto obtenerPorId(Long id) throws Exception {
        try {
            return productoRepository.findById(id);
        } catch (SQLException e) {
            throw new Exception("Fallo al buscar producto por id. " + e.getMessage(), e);
        }
    }

    public void incrementarVistas(Long id) throws Exception {
        try {
            productoRepository.incrementarVistas(id);
        } catch (SQLException e) {
            throw new Exception("Fallo al incrementar vistas.", e);
        }
    }

    public Producto crearProducto(Producto p) throws Exception {
        try {
            if (p.getVistas_contador() == null) p.setVistas_contador(0);
            return productoRepository.insert(p);
        } catch (SQLException e) {
            throw new Exception("Fallo al crear producto. " + e.getMessage(), e);
        }
    }
    public List<Producto> obtenerTodosRandom() throws Exception {
        try {
            return productoRepository.findAllRandom();
        } catch (SQLException e) {
            throw new Exception("Fallo al obtener todos los productos.", e);
        }
    }

    public List<Producto> getNovedades(int limit) throws Exception {
        try {
            return productoRepository.findNovedades(limit);
        } catch (SQLException e) {
            throw new Exception("Fallo al obtener novedades de productos.", e);
        }
    }

    public List<Producto> getMasVistos(int limit) throws Exception {
        try {
            return productoRepository.findMasVistos(limit);
        } catch (SQLException e) {
            throw new Exception("Fallo al obtener productos más vistos.", e);
        }
    }

    // --- NUEVOS: Update y Delete ---
    public boolean actualizarProducto(Producto p) throws Exception {
        try {
            return productoRepository.update(p);
        } catch (SQLException e) {
            throw new Exception("Fallo al actualizar producto. " + e.getMessage(), e);
        }
    }

    public boolean eliminarProducto(Long id) throws Exception {
        try {
            return productoRepository.deleteById(id);
        } catch (SQLException e) {
            throw new Exception("Fallo al eliminar producto. " + e.getMessage(), e);
        }
    }
}