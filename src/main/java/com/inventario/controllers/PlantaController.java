package com.inventario.controllers;

import com.inventario.models.Planta;
import com.inventario.utils.ExcelManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class PlantaController {

    @FXML
    private TableView<Planta> tablaPlantas;

    @FXML
    private TableColumn<Planta,String> colPlanta;

    @FXML
    private TableColumn<Planta,String> colDescripcion;

    private MainAppController mainAppController;

    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    public void initialize(){
        colPlanta.setCellValueFactory(new PropertyValueFactory<>("planta"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        cargarDatos();
    }

    private void cargarDatos(){
        ObservableList<Planta> plantas = FXCollections.observableArrayList();
        var excelFile = ExcelManager.leerHoja("PARAM_PLANTAS");
        for(int i=1; i<excelFile.size() ; i++){
            var fila = excelFile.get(i);
            if(fila.size()>= 2){
                String planta = fila.get(0).trim();
                String desc = fila.get(1).trim();
                if(!planta.isEmpty() && !desc.isEmpty()){
                    plantas.add(new Planta(planta,desc));
                }
            }
        }
        tablaPlantas.setItems(plantas);
    }

    @FXML
    public void onAddPlanta(){
        Dialog<Planta> dialog = crearDialogo(null);
        dialog.showAndWait().ifPresent(planta -> {
            tablaPlantas.getItems().add(planta);
            ExcelManager.añadirFila("PARAM_PLANTAS", planta.getPlanta(), planta.getDescripcion());
        });
    }

    @FXML
    public void onEditPlanta(){
        Planta seleccionada = tablaPlantas.getSelectionModel().getSelectedItem();
        if(seleccionada == null){
            mainAppController.showAlert("Selecciona una planta para modificar.");
            return;
        }
        Dialog<Planta>dialog = crearDialogo(seleccionada);
        dialog.showAndWait().ifPresent(nueva -> {
            ExcelManager.modificarFila("PARAM_PLANTAS",
                    new String[]{seleccionada.getPlanta(), seleccionada.getDescripcion()},
                    new String[]{nueva.getPlanta(), nueva.getDescripcion()});
            seleccionada.setPlanta(nueva.getPlanta());
            seleccionada.setDescripcion(nueva.getDescripcion());
            tablaPlantas.refresh();
        });
    }

    @FXML
    public void onDeletePlanta(){
        Planta seleccionada = tablaPlantas.getSelectionModel().getSelectedItem();
        if(seleccionada == null){
            mainAppController.showAlert("Selecciona una planta para eliminar.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar que la quieres eliminar");
        confirmacion.setHeaderText("¿Estas seguro que deseas eliminarla?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if(respuesta == ButtonType.OK){
                tablaPlantas.getItems().remove(seleccionada);
                ExcelManager.eliminarFila("PARAM_PLANTAS", seleccionada.getPlanta(), seleccionada.getDescripcion());
            }
        });
    }

    private Dialog<Planta>crearDialogo(Planta planta){
        Dialog<Planta>dialog = new Dialog<>();
        dialog.setTitle(planta == null ? "➕ Añadir Gas" : "✏️ Modificar Gas");
        dialog.setHeaderText(null);

        TextField plantaField = new TextField();
        TextField descField = new TextField();

        plantaField.setPromptText("Planta");
        descField.setPromptText("Descripcion");

        if(planta != null){
            plantaField.setText(planta.getPlanta());
            descField.setText(planta.getDescripcion());
        }

        VBox vBox = new VBox(10, new Label("Planta: "),plantaField, new Label("Descripción: "), descField);
        vBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(vBox);
        ButtonType añadirBoton = new ButtonType(planta == null? "Añadir":"Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(añadirBoton,ButtonType.CANCEL);

        dialog.setResultConverter(botonDialog -> {
            if(botonDialog == añadirBoton) {
                return new Planta(plantaField.getText(), descField.getText());
            }
            return null;
        });
        return dialog;
    }
}
