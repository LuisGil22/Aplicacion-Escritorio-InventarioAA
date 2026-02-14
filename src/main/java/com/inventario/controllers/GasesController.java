package com.inventario.controllers;

import com.inventario.models.Gas;
import com.inventario.utils.ExcelManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.apache.poi.ss.usermodel.*;


public class GasesController {

    @FXML
    private TableView<Gas> tablaGases;

    @FXML
    private TableColumn<Gas, String> colGas;
    @FXML
    private TableColumn<Gas, String> colDescripcion;

    private MainAppController mainAppController;

    public void setMainAppController(MainAppController controller){
        this.mainAppController = controller;
    }

    public void initialize(){
        colGas.setCellValueFactory(new PropertyValueFactory<>("gas"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        cargarDatos();
    }

    private void cargarDatos(){
        ObservableList<Gas>gases = FXCollections.observableArrayList();
        var excelFile = ExcelManager.leerHoja("PARAM_GASES");
         for(int i=1; i<excelFile.size() ; i++){
             var fila = excelFile.get(i);
             if(!fila.isEmpty()){
                 String gas = fila.get(0).trim();
                 if(!gas.isEmpty()){
                     String desc = (fila.size() > 1) ? fila.get(1).trim() : "";
                     gases.add(new Gas(gas,desc));
                 }
             }
         }
         tablaGases.setItems(gases);
    }

    @FXML
    public void onAddGases(){
        Dialog<Gas>dialog = crearDialogo(null);
        dialog.showAndWait().ifPresent(gas -> {
            tablaGases.getItems().add(gas);
            ExcelManager.añadirFila("PARAM_GASES", gas.getGas(), gas.getDescripcion());
        });
    }

    @FXML
    public void onEditGases(){
        Gas seleccionado = tablaGases.getSelectionModel().getSelectedItem();
        if(seleccionado == null){
            mainAppController.showAlert("Selecciona un gas para modificar.");
            return;
        }
        Dialog<Gas>dialog = crearDialogo(seleccionado);
        dialog.showAndWait().ifPresent(nuevo -> {
            ExcelManager.modificarFila("PARAM_GASES",
                    new String[]{seleccionado.getGas(), seleccionado.getDescripcion()},
                    new String[]{nuevo.getGas(), nuevo.getDescripcion()});
                    seleccionado.setGas(nuevo.getGas());
                    seleccionado.setDescripcion(nuevo.getDescripcion());
                    tablaGases.refresh();
        });
    }

    @FXML
    public void onDeleteGases(){
        Gas seleccionado = tablaGases.getSelectionModel().getSelectedItem();
        if(seleccionado == null){
            mainAppController.showAlert("Selecciona un gas para eliminar.");
            return;
        }

        String gasAEliminar = seleccionado.getGas();
        if(ExcelManager.existParametroEnCondensadoras(gasAEliminar,ExcelManager.Columnas.GAS)){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Esta acción no está permitida");
            alert.setHeaderText("No se puede eliminar este gas");
            alert.setContentText("El gas '" +gasAEliminar+ "' no se puede eliminar porque está siendo usada en la hoja 'Condensadoras'.");
            alert.showAndWait();
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar que lo quieres eliminar");
        confirmacion.setHeaderText("¿Estas seguro que deseas eliminarlo?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if(respuesta == ButtonType.OK){
                tablaGases.getItems().remove(seleccionado);
                ExcelManager.eliminarFila("PARAM_GASES", seleccionado.getGas(), seleccionado.getDescripcion());
            }
        });
    }

    private Dialog<Gas>crearDialogo(Gas gas){
        Dialog<Gas>dialog = new Dialog<>();
        dialog.setTitle(gas == null ? "➕ Añadir Gas" : "✏️ Modificar Gas");
        dialog.setHeaderText(null);

        TextField gasField = new TextField();
        TextField descField = new TextField();

        gasField.setPromptText("Gas");
        descField.setPromptText("Descripcion");

        if(gas != null){
            gasField.setText(gas.getGas());
            descField.setText(gas.getDescripcion());
        }

        VBox vBox = new VBox(10, new Label("Gas:"),gasField, new Label("Descripción:"), descField);
        vBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(vBox);
        ButtonType añadirBoton = new ButtonType(gas == null? "Añadir":"Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(añadirBoton,ButtonType.CANCEL);

        dialog.setResultConverter(botonDialog -> {
            if(botonDialog == añadirBoton) {
                return new Gas(gasField.getText(), descField.getText());
            }
            return null;
        });
        return dialog;
    }
}
