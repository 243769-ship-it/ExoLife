package org.example.services;

import org.example.config.DBConfig;
import org.example.models.ProductoAnimal;
import org.example.repositories.ProductoAnimalRepository;
import java.sql.Connection;
import java.util.List;

public class RecomendacionService {

    // --- LÓGICA PARA ANIMALES ---

    public void syncRecomendacionesParaAnimal(Integer animalId, List<Integer> productoIds) {
        if (animalId == null) return;
        try (Connection conn = DBConfig.getConnection()) {
            ProductoAnimalRepository repo = new ProductoAnimalRepository(conn);
            repo.deleteAllByAnimalId(animalId); // Limpiar

            if (productoIds != null && !productoIds.isEmpty()) {
                for (Integer prodId : productoIds) {
                    if (prodId != null) {
                        ProductoAnimal pa = new ProductoAnimal();
                        pa.setAnimal_id(animalId);
                        pa.setProducto_id(prodId);
                        repo.save(pa);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getRecomendaciones(Integer animalId) {
        try (Connection conn = DBConfig.getConnection()) {
            ProductoAnimalRepository repo = new ProductoAnimalRepository(conn);
            return repo.findProductosIdByAnimalId(animalId);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // --- LÓGICA PARA PRODUCTOS (ESTO ES LO QUE TE FALTABA) ---

    public void syncAnimalesParaProducto(Integer productoId, List<Integer> animalIds) {
        if (productoId == null) return;
        try (Connection conn = DBConfig.getConnection()) {
            ProductoAnimalRepository repo = new ProductoAnimalRepository(conn);
            repo.deleteAllByProductoId(productoId); // Limpiar relaciones viejas

            if (animalIds != null && !animalIds.isEmpty()) {
                for (Integer aniId : animalIds) {
                    if (aniId != null) {
                        ProductoAnimal pa = new ProductoAnimal();
                        pa.setProducto_id(productoId);
                        pa.setAnimal_id(aniId);
                        repo.save(pa);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getAnimalesRecomendados(Integer productoId) {
        try (Connection conn = DBConfig.getConnection()) {
            ProductoAnimalRepository repo = new ProductoAnimalRepository(conn);
            return repo.findAnimalesIdByProductoId(productoId);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}