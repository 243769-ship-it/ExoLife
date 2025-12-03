package org.example.repositories;

import org.example.config.DBConfig;
import org.example.models.Producto;
import org.example.models.Animal; // Asegúrate de importar tu modelo Animal
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecomendacionRepository {

    // --- LÓGICA ANIMAL -> PRODUCTOS (Ya la tenías) ---
    public void syncRecomendaciones(Integer animalId, List<Integer> productoIds, Connection conn) throws SQLException {
        String sqlDelete = "DELETE FROM producto_animal WHERE animal_id = ?";
        try (PreparedStatement psDelete = conn.prepareStatement(sqlDelete)) {
            psDelete.setInt(1, animalId);
            psDelete.executeUpdate();
        }

        if (productoIds != null && !productoIds.isEmpty()) {
            String sqlInsert = "INSERT INTO producto_animal (animal_id, producto_id) VALUES (?, ?)";
            try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                for (Integer productoId : productoIds) {
                    psInsert.setInt(1, animalId);
                    psInsert.setInt(2, productoId);
                    psInsert.addBatch();
                }
                psInsert.executeBatch();
            }
        }
    }

    public List<Producto> findRecomendacionesPorAnimal(Integer animalId) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.producto_id, p.nombre, p.precio, p.imagen_url FROM productos p " +
                "JOIN producto_animal pa ON p.producto_id = pa.producto_id " +
                "WHERE pa.animal_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, animalId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto p = new Producto();
                    p.setProducto_id(rs.getInt("producto_id"));
                    p.setNombre(rs.getString("nombre"));
                    p.setPrecio(rs.getBigDecimal("precio"));
                    p.setImagen_url(rs.getString("imagen_url"));
                    productos.add(p);
                }
            }
        }
        return productos;
    }

    // --- LÓGICA PRODUCTO -> ANIMALES (NUEVO) ---
    public void syncAnimalesParaProducto(Integer productoId, List<Integer> animalIds, Connection conn) throws SQLException {
        // 1. Borrar relaciones existentes para este producto
        String sqlDelete = "DELETE FROM producto_animal WHERE producto_id = ?";
        try (PreparedStatement psDelete = conn.prepareStatement(sqlDelete)) {
            psDelete.setInt(1, productoId);
            psDelete.executeUpdate();
        }

        // 2. Insertar nuevas
        if (animalIds != null && !animalIds.isEmpty()) {
            String sqlInsert = "INSERT INTO producto_animal (producto_id, animal_id) VALUES (?, ?)";
            try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                for (Integer animalId : animalIds) {
                    psInsert.setInt(1, productoId); // Primero el producto
                    psInsert.setInt(2, animalId);   // Segundo el animal
                    psInsert.addBatch();
                }
                psInsert.executeBatch();
            }
        }
    }

    public List<Animal> findAnimalesPorProducto(Integer productoId) throws SQLException {
        List<Animal> animales = new ArrayList<>();
        // Traemos datos básicos del animal para mostrarlos en la ficha del producto
        String sql = "SELECT a.animal_id, a.nombre, a.precio, a.imagen_url FROM animales a " +
                "JOIN producto_animal pa ON a.animal_id = pa.animal_id " +
                "WHERE pa.producto_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Animal a = new Animal();
                    a.setAnimal_id(rs.getInt("animal_id"));
                    a.setNombre(rs.getString("nombre"));
                    a.setPrecio(rs.getBigDecimal("precio"));
                    a.setImagen_url(rs.getString("imagen_url"));
                    animales.add(a);
                }
            }
        }
        return animales;
    }
}