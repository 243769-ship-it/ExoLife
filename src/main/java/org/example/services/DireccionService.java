package org.example.services;

import org.example.models.Direccion;
import org.example.repositories.DireccionRepository;
import java.util.List;

public class DireccionService {

    private final DireccionRepository direccionRepository = new DireccionRepository();

    public List<Direccion> getDireccionesPorUsuario(Long usuarioId) throws Exception {
        return direccionRepository.findByUsuarioId(usuarioId);
    }

    public Direccion crearDireccion(Direccion direccion, Long usuarioId) throws Exception {
        if (direccion.getCalle() == null || direccion.getCiudad() == null) {
            throw new Exception("Faltan datos obligatorios de la dirección.");
        }
        direccion.setUsuario_id(usuarioId);
        return direccionRepository.save(direccion);
    }

    public boolean eliminarDireccion(Long direccionId, Long usuarioId) throws Exception {
        return direccionRepository.delete(direccionId, usuarioId);
    }

    // --- NUEVO MÉTODO PARA OBTENER UNA SOLA ---
    public Direccion obtenerPorId(Long id) throws Exception {
        return direccionRepository.findById(id);
    }
}