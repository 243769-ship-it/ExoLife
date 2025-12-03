package org.example.repositories;

import org.example.models.Animal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnimalRepository {

    private final Connection connection;

    public AnimalRepository(Connection connection) {
        this.connection = connection;
    }

    private Animal mapResultSetToAnimal(ResultSet rs) throws SQLException {
        Animal a = new Animal();
        a.setAnimal_id(rs.getInt("animal_id"));
        a.setNombre(rs.getString("nombre"));
        a.setDescripcion(rs.getString("descripcion"));
        a.setPrecio(rs.getBigDecimal("precio"));
        int stock = rs.getInt("stock");
        if (rs.wasNull()) a.setStock(null); else a.setStock(stock);
        int vistas = rs.getInt("vistas_contador");
        if (rs.wasNull()) a.setVistas_contador(null); else a.setVistas_contador(vistas);
        a.setImagen_url(rs.getString("imagen_url"));
        return a;
    }

    private List<Animal> mapResultSetToList(ResultSet rs) throws SQLException {
        List<Animal> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSetToAnimal(rs));
        }
        return list;
    }

    public List<Animal> findAllRandom() throws SQLException {
        String sql = "SELECT * FROM animales ORDER BY RAND()";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return mapResultSetToList(rs);
        }
    }

    public Animal findById(Long id) throws SQLException {
        String sql = "SELECT * FROM animales WHERE animal_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToAnimal(rs);
                return null;
            }
        }
    }

    public Animal insert(Animal animal) throws SQLException {
        String sql = "INSERT INTO animales (nombre, descripcion, precio, stock, vistas_contador, imagen_url) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, animal.getNombre());
            ps.setString(2, animal.getDescripcion());
            ps.setBigDecimal(3, animal.getPrecio());
            ps.setObject(4, animal.getStock(), Types.INTEGER);
            ps.setObject(5, animal.getVistas_contador(), Types.INTEGER);
            ps.setString(6, animal.getImagen_url());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    animal.setAnimal_id(keys.getInt(1));
                }
            }
            return animal;
        }
    }

    public boolean updateStock(Long animalId, int nuevoStock) throws SQLException {
        String sql = "UPDATE animales SET stock = ? WHERE animal_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, nuevoStock);
            ps.setLong(2, animalId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Long animalId) throws SQLException {
        String sql = "DELETE FROM animales WHERE animal_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, animalId);
            return ps.executeUpdate() > 0;
        }
    }

    public void incrementarVistas(Long animalId) throws SQLException {
        String sql = "UPDATE animales SET vistas_contador = COALESCE(vistas_contador,0) + 1 WHERE animal_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, animalId);
            ps.executeUpdate();
        }
    }

    public List<Animal> findNovedades(int limit) throws SQLException {
        String sql = "SELECT * FROM animales ORDER BY animal_id DESC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSetToList(rs);
            }
        }
    }

    public List<Animal> findMasVistos(int limit) throws SQLException {
        String sql = "SELECT * FROM animales ORDER BY vistas_contador DESC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSetToList(rs);
            }
        }
    }

    public boolean decrementStock(Long animalId, int cantidad) throws SQLException {
        String sql = "UPDATE animales SET stock = stock - ? WHERE animal_id = ? AND stock >= ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setLong(2, animalId);
            ps.setInt(3, cantidad);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public void incrementStock(Long animalId, int cantidad) throws SQLException {
        String sql = "UPDATE animales SET stock = stock + ? WHERE animal_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setLong(2, animalId);
            ps.executeUpdate();
        }
    }

    // --- NUEVO: MÉTODO UPDATE GENERAL ---
    public boolean update(Animal animal) throws SQLException {
        String sql = "UPDATE animales SET nombre = ?, descripcion = ?, precio = ?, stock = ?, imagen_url = ? WHERE animal_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, animal.getNombre());
            ps.setString(2, animal.getDescripcion());
            ps.setBigDecimal(3, animal.getPrecio());
            ps.setObject(4, animal.getStock(), Types.INTEGER);
            ps.setString(5, animal.getImagen_url());
            ps.setInt(6, animal.getAnimal_id()); // El ID para el WHERE
            return ps.executeUpdate() > 0;
        }
    }
}