package com.inventario.controllers;

import com.inventario.models.Estado;
import com.inventario.utils.ExcelManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.poi.ss.usermodel.*;
import java.io.*;

public class EstadoController {

    @FXML
    private TableView<Estado> tablaEstados;

    @FXML
    private TableColumn<Estado, String> colEstado;


    // Referencia al controlador principal
    private MainAppController mainController;

    public void setMainController(MainAppController controller) {
        this.mainController = controller;
    }

    @FXML
    public void initialize() {
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        cargarDatos();
    }

    private void cargarDatos() {
        ObservableList<Estado> estados = FXCollections.observableArrayList();
        var excelFile = ExcelManager.leerHoja("PARAM_ESTADO");
        for (int i = 1; i < excelFile.size(); i++){
            var fila = excelFile.get(i);
            if(!fila.isEmpty() && !fila.get(0).trim().isEmpty()){
                estados.add(new Estado(fila.get(0)));
            }
        }
        tablaEstados.setItems(estados);
    }

    @FXML
    public void onAdd() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo Estado");
        dialog.setHeaderText("Añadir nuevo valor de estado");
        dialog.setContentText("Estado:");

        dialog.showAndWait().ifPresent(estado -> {
            if (!estado.trim().isEmpty()) {
                tablaEstados.getItems().add(new Estado(estado));
                ExcelManager.añadirFila("PARAM_ESTADO", estado);
            }
        });
    }

    @FXML
    public void onEdit() {
        Estado selected = tablaEstados.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mainController.showAlert("Selecciona un estado para modificar.");
            return;
        }
        String actualEstado = selected.getEstado();
        TextInputDialog dialog = new TextInputDialog(actualEstado);
        dialog.setTitle("Editar Estado");
        dialog.setHeaderText("Modificar valor de estado");
        dialog.setContentText("Nuevo estado:");

        dialog.showAndWait().ifPresent(nuevo -> {
            if (!nuevo.trim().isEmpty()) {

                ExcelManager.modificarFila("PARAM_ESTADO", new String[]{actualEstado},new String[]{nuevo});
                selected.setEstado(nuevo);
                tablaEstados.refresh();
            }
        });
    }

    @FXML
    public void onDelete() {
        Estado selected = tablaEstados.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mainController.showAlert("Selecciona un estado para eliminar.");
            return;
        }

        String estadoAEliminar = selected.getEstado();
        if(ExcelManager.existEstadoEnCondensadoras(estadoAEliminar)){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Esta acción no está permitida");
            alert.setHeaderText("No se puede eliminar este estado");
            alert.setContentText("El estado '" +estadoAEliminar+ "' no se puede eliminar porque está siendo usado en la hoja 'Condensadoras'.");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar este estado?");
        alert.setContentText("Esta acción no se puede deshacer.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                tablaEstados.getItems().remove(selected);
                ExcelManager.eliminarFila("PARAM_ESTADO", selected.getEstado());
            }
        });
    }

}
