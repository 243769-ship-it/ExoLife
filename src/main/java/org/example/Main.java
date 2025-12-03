package org.example;

import io.javalin.Javalin;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.example.controllers.*;
import org.example.routes.AuthRouter;
import org.example.services.*;
import org.example.repositories.*;

/**
 * Punto de entrada principal de la API REST "ExoLife".
 * Configura el servidor Javalin, el middleware de seguridad (CORS y JWT),
 * e inyecta las dependencias necesarias en los controladores.
 */
public class Main {

    /**
     * Método principal que inicializa el servidor y registra las rutas.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {

        // Inicialización del servidor en el puerto 8080
        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
        }).start(8080);

        System.out.println("API ExoLife iniciada en http://localhost:8080");

        // Configuración Global de CORS
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });

        /*
         * Middleware de Seguridad:
         * Intercepta rutas bajo /api/* para validar el token JWT.
         * Extrae el userId y rol del token y los inyecta en el contexto.
         */
        app.before("/api/*", ctx -> {
            if (ctx.method().equals("OPTIONS")) return;

            String authHeader = ctx.header("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    Jws<Claims> claims = JWTUtil.validateToken(token);
                    Claims body = claims.getBody();

                    Integer userId = body.get("userId", Integer.class);
                    String rol = body.get("rol", String.class);
                    Integer rolId = body.get("rolId", Integer.class);

                    ctx.attribute("userId", userId);
                    ctx.attribute("rol", rol);
                    ctx.attribute("rolId", rolId);

                } catch (Exception e) {
                    System.out.println("Token inválido en middleware: " + e.getMessage());
                }
            }
        });

        // Manejo de peticiones OPTIONS (Pre-flight)
        app.options("/*", ctx -> ctx.status(200));

        // Inyección de Dependencias
        CloudinaryService cloudinaryService = new CloudinaryService();
        CarritoRepository carritoRepo = new CarritoRepository();
        UsuarioRepository usuarioRepo = new UsuarioRepository();

        // --- REGISTRO DE CONTROLADORES ---
        new AuthRouter().register(app);
        new ProductoController(cloudinaryService).register(app);
        new PagoController(cloudinaryService).register(app);
        new CatalogoController().register(app);
        new CarritoController(carritoRepo).register(app);
        new CompraController(cloudinaryService).register(app);
        new MetricasController().register(app);
        new DireccionController().register(app);
        new AnimalController(cloudinaryService).register(app);
        new RecomendacionController().register(app);
        new EmpaquetadorController().register(app);
        new AdminController().register(app);
        new UsuarioController().register(app);
        new HistorialVistasController().register(app);

        // Tarea de mantenimiento al inicio: Limpieza de compras expiradas
        try {
            CompraService tempCompraService = new CompraService();
            int cleaned = tempCompraService.cleanupExpiredCompras();
            System.out.println("Compras expiradas limpiadas: " + cleaned);
        } catch (Exception e) {
            System.err.println("No se pudo limpiar compras expiradas al iniciar: " + e.getMessage());
        }

        // Ruta base de prueba
        app.get("/", ctx -> ctx.result("API principal funcionando."));

        // Manejo global de excepciones (500 Internal Server Error)
        app.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(500).result("Error interno del servidor: " + e.getMessage());
        });
    }
}