package org.example.services;

import org.example.config.DBConfig;
import org.example.models.Carrito;
import org.example.models.CarritoItem;
import org.example.models.Compra;
import org.example.repositories.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class CompraService {

    public boolean rechazarCompra(Long compraId) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            CompraRepository compraRepository = new CompraRepository(conn);
            return compraRepository.actualizarEstadoCompra(compraId, "Rechazada");
        }
    }

    public List<Map<String, Object>> obtenerDetallesDePedido(Long compraId) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            DetalleCompraRepository detalleRepo = new DetalleCompraRepository(conn);
            return detalleRepo.obtenerDetallesExtendidos(compraId);
        }
    }

    public List<Compra> obtenerTodosLosPedidosActivos() throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            CompraRepository compraRepository = new CompraRepository(conn);
            return compraRepository.findAllActiveOrders();
        }
    }

    // --- NUEVO: OBTENER HISTORIAL COMPLETO ---
    public List<Compra> getHistorial(Long usuarioId) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            CompraRepository compraRepository = new CompraRepository(conn);
            return compraRepository.findHistoryByUserId(usuarioId);
        }
    }

    // --- NUEVO: MARCAR ENTREGADO CON EVIDENCIA ---
    public boolean marcarEntregadoConEvidencia(Long compraId, String urlEvidencia) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            CompraRepository compraRepository = new CompraRepository(conn);
            return compraRepository.marcarEntregadoConEvidencia(compraId, urlEvidencia);
        }
    }

    public Compra crearCompraDesdeCarrito(Long usuarioId, Long direccionId) throws Exception {
        try (Connection conn = DBConfig.getConnection()) {
            try {
                conn.setAutoCommit(false);

                DireccionRepository direccionRepo = new DireccionRepository();
                var dir = direccionRepo.findByIdAndUsuarioId(direccionId, usuarioId);
                if (dir == null) {
                    throw new Exception("La dirección no es válida o no pertenece al usuario.");
                }

                CarritoRepository carritoRepository = new CarritoRepository();
                ProductoRepository productoRepository = new ProductoRepository(conn);
                AnimalRepository animalRepository = new AnimalRepository(conn);
                CompraRepository compraRepository = new CompraRepository(conn);

                Carrito carrito = carritoRepository.findByUsuarioId(usuarioId);
                if (carrito == null || carrito.getItems().isEmpty()) {
                    throw new Exception("Carrito vacío");
                }

                for (CarritoItem it : carrito.getItems()) {
                    if (it.getProductoId() != null) {
                        var prod = productoRepository.findById(it.getProductoId());
                        if (prod == null) throw new Exception("Producto no existe: " + it.getProductoId());
                        if (prod.getStock() == null || prod.getStock() < it.getCantidad()) {
                            throw new Exception("Stock insuficiente para producto " + it.getProductoId());
                        }
                    } else if (it.getAnimalId() != null) {
                        var animal = animalRepository.findById(it.getAnimalId());
                        if (animal == null) throw new Exception("Animal no existe: " + it.getAnimalId());
                        if (animal.getStock() == null || animal.getStock() < it.getCantidad()) {
                            throw new Exception("Stock insuficiente para animal " + it.getAnimalId());
                        }
                    }
                }

                for (CarritoItem it : carrito.getItems()) {
                    if (it.getProductoId() != null) {
                        boolean stockActualizado = productoRepository.decrementStock(it.getProductoId(), it.getCantidad());
                        if (!stockActualizado) {
                            throw new Exception("Stock de producto no se pudo actualizar para: " + it.getProductoId());
                        }
                    } else if (it.getAnimalId() != null) {
                        boolean stockActualizado = animalRepository.decrementStock(it.getAnimalId(), it.getCantidad());
                        if (!stockActualizado) {
                            throw new Exception("Stock de animal no se pudo actualizar para: " + it.getAnimalId());
                        }
                    }
                }

                Compra created = compraRepository.createFromCarrito(usuarioId, direccionId, carrito, conn);
                carritoRepository.clearCarrito(carrito.getCarritoId());
                conn.commit();
                return created;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int cleanupExpiredCompras() throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            CompraRepository compraRepository = new CompraRepository(conn);
            return compraRepository.cleanupExpiredAndRestoreStock();
        }
    }

    public boolean compraPerteneceAUsuario(Long compraId, Long usuarioId) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            CompraRepository compraRepository = new CompraRepository(conn);
            return compraRepository.compraPerteneceAUsuario(compraId, usuarioId);
        }
    }

    public boolean guardarComprobante(Long compraId, String url, String publicId) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            CompraRepository compraRepository = new CompraRepository(conn);
            return compraRepository.guardarComprobante(compraId, url, publicId);
        }
    }

    public boolean confirmarPagoYAsignarEmpaquetador(Long compraId) throws Exception {
        try (Connection conn = DBConfig.getConnection()) {
            CompraRepository compraRepository = new CompraRepository(conn);
            boolean updated = compraRepository.actualizarEstadoCompra(compraId, "Pagada");
            if (!updated) return false;
            Long empaquetadorId = compraRepository.selectNextEmpaquetador();
            if (empaquetadorId != null) {
                return compraRepository.asignarEmpaquetador(compraId, empaquetadorId);
            } else {
                return true;
            }
        }
    }

    public boolean iniciarEmpaquetado(Long compraId) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            CompraRepository compraRepository = new CompraRepository(conn);
            return compraRepository.actualizarEstadoCompra(compraId, "Empaquetado");
        }
    }

    public boolean marcarEnviado(Long compraId) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            CompraRepository compraRepository = new CompraRepository(conn);
            return compraRepository.actualizarEstadoCompra(compraId, "Enviado");
        }
    }

    public boolean marcarEntregado(Long compraId) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            CompraRepository compraRepository = new CompraRepository(conn);
            return compraRepository.actualizarEstadoCompra(compraId, "Entregado");
        }
    }

    public Compra getPedidoActivo(Long usuarioId) throws SQLException {
        try (Connection conn = DBConfig.getConnection()) {
            CompraRepository compraRepository = new CompraRepository(conn);
            return compraRepository.findActiveOrderByUserId(usuarioId);
        }
    }

    public boolean asignarDireccion(Long compraId, Long direccionId, Long usuarioId) throws Exception {
        try (Connection conn = DBConfig.getConnection()) {
            DireccionRepository direccionRepo = new DireccionRepository();
            var direccion = direccionRepo.findByIdAndUsuarioId(direccionId, usuarioId);
            if (direccion == null) {
                throw new Exception("La dirección no es válida o no pertenece a este usuario.");
            }
            CompraRepository compraRepository = new CompraRepository(conn);
            return compraRepository.asignarDireccion(compraId, direccionId, usuarioId);
        }
    }
}