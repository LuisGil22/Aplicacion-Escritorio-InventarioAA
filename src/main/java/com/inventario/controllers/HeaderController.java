package com.inventario.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

/**
 * Controlador para la cabecera de la aplicación de inventario.
 * <p>
 * Gestiona los elementos visuales y funcionales de la barra superior:
 * </p>
 * <ul>
 *     <li>Logo de la aplicación</li>
 *     <li>Botón de menú móvil (para navegación en pantallas pequeñas)</li>
 *     <li>Botón de salida de la aplicación</li>
 *     <li>Título dinámico de la aplicación</li>
 *     <li>Espaciador para alineación de elementos</li>
 * </ul>
 *
 * @author Luis Gil
 */
public class HeaderController {

    /** Campos FXML*/
    @FXML private ImageView logoView;
    @FXML public Button btnMenuMovil;
    @FXML public Button exitButton;
    @FXML public Label tituloApp;
    @FXML public Region spacer;

    /**
     * Metodo para inicializar el controlador al cargar la vista FXML.
     * Carga el logo de la aplicación desde los recursos y lo asigna al ImageView.
     * Si no se encuentra el logo, se muestra un mensaje de error en la consola.
     */
    @FXML
    private void initialize() {
        try{
            Image logo = new Image(getClass().getResourceAsStream("/imagenes/logo.png"));
            if (!logo.isError()) {
                logoView.setImage(logo);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el logo");
        }
    }

    /**
     * Metodo para manejar el evento de clic en el botón de menú móvil.
     * Delega la acción al controlador principal de la aplicación.
     */
    @FXML
    private void toggleSidebarMovil() {
        if (MainAppController.INSTANCE != null) {
            MainAppController.INSTANCE.toggleSidebarMovil();
        }
    }

    /**
     * Metodo para manejar el evento de clic en el botón de salida.
     * Delega la acción al controlador principal de la aplicación.
     */
    @FXML
    private void onExitButtonClick() {
        if (MainAppController.INSTANCE != null) {
            MainAppController.INSTANCE.salir();
        }
    }
}
