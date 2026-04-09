package com.inventario;

import com.inventario.controllers.MainAppController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Clase principal de la aplicación de inventario de aire acondicionado.
 * <p>
 * Esta clase es el punto de entrada de la aplicación JavaFX. Se encarga de:
 * </p>
 * <ul>
 *     <li>Configurar la localización y zona horaria por defecto</li>
 *     <li>Cargar la vista principal (mainApp.fxml) que contiene el diseño general de la aplicación</li>
 *     <li>Inicializar el controlador principal (MainAppController) y configurar listeners para ajustes de diseño responsivo</li>
 * </ul>
 *
 * @author Luis Gil
 */
@SuppressWarnings("ALL")
public class MainApp extends Application {

    /**
     * Metodo de inicio de la aplicación JavaFX.
     */
    @Override
    public void start(Stage stage) throws IOException {
        Locale.setDefault(new Locale("es", "ES"));
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Madrid"));
        // Cargamos la VISTA PRINCIPAL (que incluirá header, menú y contenido)
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/views/mainApp.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Inventario Aire Acondicionado");
        stage.setScene(scene);
        stage.show();
        stage.setMaximized(true);

        MainAppController controller = fxmlLoader.getController();
        if (controller != null) {
            //System.out.println("=== MainApp.java: Llamando a ajustarDiseno() ===");
            // Aplicar estado inicial
            controller.ajustarDiseno(stage.getWidth());

            // Escuchar cambios de tamaño
            stage.widthProperty().addListener((obs, oldVal, newVal) -> {
                //System.out.println("=== Cambio de ancho detectado en MainApp.java ===");
                controller.ajustarDiseno(newVal.doubleValue());
            });
        }
    }

    /**
     * Metodo principal que lanza la aplicación JavaFX.
     */
    public static void main(String[] args) {
        launch(); // Lanza la app JavaFX
    }
}