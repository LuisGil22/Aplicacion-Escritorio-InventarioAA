package com.inventario.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;


public class SidebarController {

    @FXML
    private Button tituloParametros;

    @FXML
    private VBox vboxParametros;

    private Button botonSeleccionado;

    private MainAppController mainAppController;

    public void setMainController(MainAppController controller){
        this.mainAppController = controller;
    }


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

    public void clearSeleccion(){
        if(botonSeleccionado != null){
            botonSeleccionado.getStyleClass().remove("selected");
            botonSeleccionado = null;
        }
    }
}
