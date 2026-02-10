package com.inventario;

import com.inventario.controllers.MainAppController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
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

    public static void main(String[] args) {
        launch(); // Lanza la app JavaFX
    }
}