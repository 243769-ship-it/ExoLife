package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * Clase de configuración para la conexión a la base de datos MySQL.
 * Implementa el patrón Singleton para el DataSource y utiliza HikariCP
 * para el manejo eficiente del pool de conexiones.
 *
 * @author TuNombre
 * @version 1.0
 */
public class DBConfig {

    /** Instancia única del DataSource gestionada por HikariCP. */
    private static DataSource dataSource;

    /**
     * Obtiene y configura el DataSource de la aplicación.
     * Si no existe, inicializa la configuración de HikariCP apuntando al servidor AWS EC2.
     *
     * <p>Configuraciones clave:</p>
     * <ul>
     * <li>Pool Size: 10 conexiones máximas.</li>
     * <li>Timeout: 30 segundos.</li>
     * <li>Driver: MySQL Connector/J.</li>
     * </ul>
     *
     * @return El objeto {@link DataSource} configurado.
     */
    public static DataSource getDataSource() {
        if (dataSource == null) {
            try {
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl("jdbc:mysql://52.91.121.15/ExoLife?serverTimezone=UTC");
                config.setUsername("c2-user");
                config.setPassword("261106");

                config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                config.setMaximumPoolSize(10);
                config.setConnectionTimeout(30000);
                config.setMinimumIdle(2);
                config.setIdleTimeout(600000);
                config.setMaxLifetime(1800000);

                dataSource = new HikariDataSource(config);
                System.out.println("✅HikariCP configurado para 'ExoLife' en AWS EC2.");

            } catch (Exception e) {
                System.err.println("❌ERROR: No se pudo conectar a la base de datos en AWS EC2.");
                e.printStackTrace();
            }
        }
        return dataSource;
    }

    /**
     * Obtiene una conexión activa desde el pool de conexiones.
     *
     * @return Un objeto {@link Connection} listo para ejecutar sentencias SQL.
     * @throws SQLException Si hay errores al obtener la conexión o el DataSource es nulo.
     */
    public static Connection getConnection() throws SQLException {
        DataSource ds = getDataSource();
        if (ds == null) {
            throw new SQLException("La fuente de datos (DataSource) no está inicializada. Verifique la conexión a AWS EC2.");
        }
        return ds.getConnection();
    }
}