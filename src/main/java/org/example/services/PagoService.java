package org.example.services;

import org.example.models.Pago;
import org.example.models.DetalleCompraItem;
import org.example.repositories.PagoRepository;
import org.example.repositories.CompraRepository;
import org.example.repositories.ProductoRepository;
import org.example.repositories.AnimalRepository;

import java.sql.SQLException;
import java.util.List;

public class PagoService {

    private final PagoRepository pagoRepository;
    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;
    private final AnimalRepository animalRepository;

    public PagoService(PagoRepository pagoRepository, CompraRepository compraRepository, ProductoRepository productoRepository, AnimalRepository animalRepository) {
        this.pagoRepository = pagoRepository;
        this.compraRepository = compraRepository;
        this.productoRepository = productoRepository;
        this.animalRepository = animalRepository;
    }

    public Pago registrarVoucher(Long compraId, double monto, String comprobanteUrl) throws Exception {
        if (comprobanteUrl == null || comprobanteUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("La URL del comprobante no puede estar vacía.");
        }
        Pago nuevoPago = new Pago(compraId, monto, comprobanteUrl);
        try {
            Pago pagoGuardado = pagoRepository.guardar(nuevoPago);

            compraRepository.actualizarEstadoYVoucher(compraId, "Pendiente de confirmar", comprobanteUrl);

            return pagoGuardado;
        } catch (SQLException e) {
            System.err.println("Error al guardar pago en la base de datos: " + e.getMessage());
            throw new Exception("Fallo al registrar el pago debido a un error de base de datos.", e);
        }
    }

    public boolean procesarVerificacion(Long pagoId, boolean esVerificado) throws Exception {

        String estadoPago = esVerificado ? "Verificado" : "Rechazado";

        try {
            boolean pagoActualizado = pagoRepository.actualizarEstado(pagoId, estadoPago);

            if (!pagoActualizado) {
                throw new Exception("No se pudo actualizar el estado del pago con ID: " + pagoId);
            }

            Pago pago = pagoRepository.buscarPorId(pagoId);
            if (pago == null) {
                throw new Exception("El pago (ID: " + pagoId + ") no fue encontrado después de actualizarse.");
            }
            Long compraId = pago.getCompraId();

            if (esVerificado) {
                boolean compraActualizada = compraRepository.actualizarEstadoCompra(compraId, "Pagada");

                if (!compraActualizada) {
                    throw new Exception("El pago se confirmó, pero no se pudo actualizar el estado de la compra asociada.");
                }
                Long empaquetadorId = compraRepository.selectNextEmpaquetador();
                if(empaquetadorId != null) {
                    compraRepository.asignarEmpaquetador(compraId, empaquetadorId);
                }

            } else {
                compraRepository.actualizarEstadoCompra(compraId, "Rechazada");

                List<DetalleCompraItem> items = compraRepository.findItemsByCompraId(compraId);
                if (items.isEmpty()) {
                    System.err.println("Advertencia: Se rechazó pago " + pagoId + " de compra " + compraId + " pero no se encontraron items para restaurar.");
                }

                for (DetalleCompraItem item : items) {
                    if (item.getProductoId() != null) {
                        productoRepository.incrementStock(item.getProductoId(), item.getCantidad());
                    } else if (item.getAnimalId() != null) {
                        animalRepository.incrementStock(item.getAnimalId(), item.getCantidad());
                    }
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Error de BD en la verificación del pago: " + e.getMessage());
            throw new Exception("Fallo en la verificación del pago debido a un error de base de datos.", e);
        }
    }
}