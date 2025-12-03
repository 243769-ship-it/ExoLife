package org.example.services;

import org.example.models.Animal;
import org.example.repositories.AnimalRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AnimalService {
    private final AnimalRepository animalRepository;

    public AnimalService(Connection connection) {
        this.animalRepository = new AnimalRepository(connection);
    }

    public List<Animal> obtenerTodosRandom() throws Exception {
        try {
            return animalRepository.findAllRandom();
        } catch (SQLException e) {
            throw new Exception("Fallo al obtener animales.", e);
        }
    }

    public Animal obtenerPorId(Long id) throws Exception {
        try {
            Animal animal = animalRepository.findById(id);
            if (animal != null) {
                animalRepository.incrementarVistas(id);
            }
            return animal;
        } catch (SQLException e) {
            throw new Exception("Fallo al buscar animal por id. " + e.getMessage(), e);
        }
    }

    public Animal crearAnimal(Animal animal) throws Exception {
        try {
            if (animal.getVistas_contador() == null) animal.setVistas_contador(0);
            return animalRepository.insert(animal);
        } catch (SQLException e) {
            throw new Exception("Fallo al crear animal. " + e.getMessage(), e);
        }
    }

    public boolean actualizarStock(Long animalId, int nuevoStock) throws Exception {
        if (nuevoStock < 0) {
            throw new Exception("El stock no puede ser negativo.");
        }
        try {
            return animalRepository.updateStock(animalId, nuevoStock);
        } catch (SQLException e) {
            throw new Exception("Fallo al actualizar stock. " + e.getMessage(), e);
        }
    }

    public boolean eliminarAnimal(Long animalId) throws Exception {
        try {
            return animalRepository.deleteById(animalId);
        } catch (SQLException e) {
            throw new Exception("Fallo al eliminar animal. " + e.getMessage(), e);
        }
    }

    public List<Animal> getNovedades(int limit) throws Exception {
        try {
            return animalRepository.findNovedades(limit);
        } catch (SQLException e) {
            throw new Exception("Fallo al obtener novedades de animales.", e);
        }
    }

    public List<Animal> getMasVistos(int limit) throws Exception {
        try {
            return animalRepository.findMasVistos(limit);
        } catch (SQLException e) {
            throw new Exception("Fallo al obtener animales más vistos.", e);
        }
    }

    // --- NUEVO: Método update general ---
    public boolean actualizarAnimal(Animal animal) throws Exception {
        try {
            return animalRepository.update(animal);
        } catch (SQLException e) {
            throw new Exception("Fallo al actualizar el animal. " + e.getMessage(), e);
        }
    }
}