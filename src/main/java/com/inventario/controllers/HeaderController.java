package com.inventario.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class HeaderController {

    @FXML
    private ImageView logoView;

    @FXML
    public Button btnMenuMovil;

    @FXML
    public Button exitButton;

    @FXML
    public Label tituloApp;

    @FXML
    public Region spacer;



    @FXML
    private void initialize() {
        try{
            Image logo = new Image(getClass().getResourceAsStream("/imagenes/logo.png"));
            if (!logo.isError()) {
                logoView.setImage(logo);
            }
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo cargar el logo");
        }

        /**System.out.println("=== HeaderController.initialize() ===");
        System.out.println("tituloApp = " + tituloApp);
        System.out.println("exitButton = " + exitButton);
        System.out.println("btnMenuMovil = " + btnMenuMovil);**/
    }

    @FXML
    private void toggleSidebarMovil() {
        if (MainAppController.INSTANCE != null) {
            MainAppController.INSTANCE.toggleSidebarMovil();
        }
    }

    @FXML
    private void onExitButtonClick() {
        if (MainAppController.INSTANCE != null) {
            MainAppController.INSTANCE.salir();
        }
    }




}
