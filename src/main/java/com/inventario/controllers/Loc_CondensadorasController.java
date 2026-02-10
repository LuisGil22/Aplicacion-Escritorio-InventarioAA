package com.inventario.controllers;

import com.inventario.models.Estado;
import com.inventario.models.Loc_Condensadoras;
import com.inventario.utils.ExcelManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class Loc_CondensadorasController {

    @FXML
    private TableView<Loc_Condensadoras> tablaLoc_Condensadoras;

    @FXML
    private TableColumn<Loc_Condensadoras,String> colLoc_Condensadoras;

    private MainAppController mainController;

    public void setMainController(MainAppController controller) {
        this.mainController = controller;
    }

    @FXML
    public void initialize() {
        colLoc_Condensadoras.setCellValueFactory(new PropertyValueFactory<>("localizacionCondensadoras"));
        cargarDatos();
    }

    public void cargarDatos(){
        ObservableList<Loc_Condensadoras> loc_condensadoras = FXCollections.observableArrayList();
        var excelFile = ExcelManager.leerHoja("PARAM_LOC_COND");
        for (int i = 1; i < excelFile.size(); i++){
            var fila = excelFile.get(i);
            if(!fila.isEmpty() && !fila.get(0).trim().isEmpty()){
                loc_condensadoras.add(new Loc_Condensadoras(fila.get(0)));
            }
        }
        tablaLoc_Condensadoras.setItems(loc_condensadoras);
    }

    @FXML
    public void onAddLoc_Condensadoras(){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo Loc_Condensadoras");
        dialog.setHeaderText("Añadir nuevo Localizador de Condensadoras");
        dialog.setContentText("Loc_Condensadoras:");

        dialog.showAndWait().ifPresent(loc_condensadoras -> {
            if (!loc_condensadoras.trim().isEmpty()) {
                tablaLoc_Condensadoras.getItems().add(new Loc_Condensadoras(loc_condensadoras));
                ExcelManager.añadirFila("PARAM_LOC_COND", loc_condensadoras);
            }
        });
    }

    @FXML
    public void onEditLoc_Condensadoras(){
        Loc_Condensadoras selected = tablaLoc_Condensadoras.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mainController.showAlert("Selecciona un localizador de condensadoras para modificar.");
            return;
        }
        String actualLocCondensadoras = selected.getLocalizacionCondensadoras();
        TextInputDialog dialog = new TextInputDialog(actualLocCondensadoras);
        dialog.setTitle("Editar Localizador de Condensadoras");
        dialog.setHeaderText("Modificar el Localizador de Condensadoras");
        dialog.setContentText("Nuevo Localizador de Condensadoras:");

        dialog.showAndWait().ifPresent(nuevo -> {
            if (!nuevo.trim().isEmpty()) {

                ExcelManager.modificarFila("PARAM_LOC_COND", new String[]{actualLocCondensadoras},new String[]{nuevo});
                selected.setLocalizacionCondensadoras(nuevo);
                tablaLoc_Condensadoras.refresh();
            }
        });
    }

    @FXML
    public void onDeleteLoc_Condensadoras(){
        Loc_Condensadoras selected = tablaLoc_Condensadoras.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mainController.showAlert("Selecciona un Localizador de Condensadoras para eliminar.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar este Localizador de Condensadoras?");
        alert.setContentText("Esta acción no se puede deshacer.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                tablaLoc_Condensadoras.getItems().remove(selected);
                ExcelManager.eliminarFila("PARAM_LOC_COND", selected.getLocalizacionCondensadoras());
            }
        });
    }

}
