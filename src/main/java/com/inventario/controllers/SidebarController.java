package com.inventario.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * Controlador para la barra lateral (sidebar) de la aplicación de inventario.
 * <p>
 * Gestiona:
 * </p>
 * <ul>
 *   <li>La expansión/colapso del menú de parámetros</li>
 *   <li>La selección visual de botones de menú</li>
 *   <li>La navegación entre vistas mediante {@link MainAppController#loadView(String)}</li>
 * </ul>
 *
 * @author Luis Gil
 */
public class SidebarController {

    /** Campos FXML */
    @FXML private Button tituloParametros;
    @FXML private VBox vboxParametros;

    /** Dependencias */
    private Button botonSeleccionado;
    private MainAppController mainAppController;

    /**
     * Metodo para establecer la dependencia con el controlador principal de la aplicación.
     *
     * @param controller instancia del controlador principal
     */
    public void setMainController(MainAppController controller){
        this.mainAppController = controller;
    }

    /**
     * Metodo para alternar la visibilidad del menú de parámetros.
     * <p>
     * Si está visible → lo colapsa y cambia el texto a "▼ PARÁMETROS".<br>
     * Si está oculto → lo expande y cambia el texto a "▲ PARÁMETROS".
     * </p>
     */
    @FXML
    public void btnParametros(){
        boolean visible = vboxParametros.isVisible();
        if(visible){
            tituloParametros.setText("▼ PARÁMETROS");
            tituloParametros.getStyleClass().remove("abierto");
            vboxParametros.setVisible(false);
        }else{
            tituloParametros.setText("▲ PARÁMETROS");
            tituloParametros.getStyleClass().add("abierto");
            vboxParametros.setVisible(true);
        }

    }

    /**
     * Metodo para manejar el evento de clic en cualquier botón del menú.
     * <p>
     * Realiza las siguientes acciones:
     * </p>
     * <ol>
     *   <li>Desmarca el botón previamente seleccionado</li>
     *   <li>Marca visualmente el botón actual como seleccionado</li>
     *   <li>Carga la vista correspondiente en el contenedor principal</li>
     * </ol>
     *
     * @param event evento de acción generado por el clic
     */
    @FXML
    public void ClickMenu(ActionEvent event){
        Button boton = (Button) event.getSource();
        String menu = boton.getText();
        if(botonSeleccionado != null){
            botonSeleccionado.getStyleClass().remove("selected");
        }
        boton.getStyleClass().add("selected");
        botonSeleccionado = boton;

        if(mainAppController != null){
            mainAppController.loadView(menu);
        }

    }

    /**
     * Metodo para limpiar la selección visual del botón actualmente marcado.
     * <p>
     * Útil al volver a la pantalla de inicio o al cargar una vista no asociada a un botón.
     * </p>
     */
    public void clearSeleccion(){
        if(botonSeleccionado != null){
            botonSeleccionado.getStyleClass().remove("selected");
            botonSeleccionado = null;
        }
    }
}
