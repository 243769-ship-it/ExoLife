package org.example.services;

import org.example.repositories.HistorialVistasRepository;
import java.util.List;
import java.util.Map;

public class HistorialVistasService {

    private final HistorialVistasRepository repository = new HistorialVistasRepository();

    public void registrarVista(Long usuarioId, Integer productoId, Integer animalId) throws Exception {
        try {
            repository.saveVista(usuarioId, productoId, animalId);
        } catch (Exception e) {
            // Loguear error pero no detener la app
            System.err.println("Error guardando vista: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getVistasRecientes(Long usuarioId) throws Exception {
        try {
            // Traemos los últimos 10
            return repository.findRecientes(usuarioId, 10);
        } catch (Exception e) {
            throw new Exception("Error obteniendo historial", e);
        }
    }
}