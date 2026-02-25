package com.inventario.controllers;

import com.inventario.models.Estado;
import com.inventario.models.Gas;
import com.inventario.models.Loc_Condensadoras;
import com.inventario.utils.ExcelManager;
import com.inventario.utils.FilterUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.List;

public class Loc_CondensadorasController {

    @FXML
    private TableView<Loc_Condensadoras> tablaLoc_Condensadoras;

    @FXML
    private TableColumn<Loc_Condensadoras,String> colLoc_Condensadoras;
    @FXML
    private Button btnFiltroLocCondensadoras;

    private MainAppController mainController;
    private ObservableList<Loc_Condensadoras> loc_condensadoras;

    public void setMainController(MainAppController controller) {
        this.mainController = controller;
    }

    @FXML
    public void initialize() {
        colLoc_Condensadoras.setCellValueFactory(new PropertyValueFactory<>("localizacionCondensadoras"));
        cargarDatos();
        noOrdenar();
    }

    public void cargarDatos(){
        loc_condensadoras = FXCollections.observableArrayList();
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
        Dialog<Loc_Condensadoras>dialog = crearDialogo(null);
        dialog.showAndWait().ifPresent(loc_condensadoras -> {
            tablaLoc_Condensadoras.getItems().add(loc_condensadoras);
            ExcelManager.añadirFila("PARAM_LOC_COND", loc_condensadoras.getLocalizacionCondensadoras());
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
                List<List<String>> datos = ExcelManager.leerHoja("PARAM_LOC_COND");
                int index = -1;

                for (int i= 1; i< datos.size(); i++){
                    if(datos.get(i).size() > 0 && datos.get(i).get(0).trim().equals(actualLocCondensadoras)){
                        index = i;
                        break;
                    }
                }
                if(index != -1) {
                    ExcelManager.modificarFila("PARAM_LOC_COND", index, new String[]{nuevo});
                    selected.setLocalizacionCondensadoras(nuevo);
                    tablaLoc_Condensadoras.refresh();
                }else{
                    mainController.showAlert("No se encontró la Localizacion Condensadora en el archivo.");
                }
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

        String locAEliminar = selected.getLocalizacionCondensadoras();
        if(ExcelManager.existParametroEnCondensadoras(locAEliminar, ExcelManager.Columnas.LOCALIZACION_CONDENSADORA)){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Esta acción no está permitida");
            alert.setHeaderText("No se puede eliminar esta localización");
            alert.setContentText("La localización '" +locAEliminar+ "' no se puede eliminar porque está siendo usada en la hoja 'Condensadoras'.");
            alert.showAndWait();
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

    @FXML
    private void configurarFiltroLocCondensadoras(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Localización Condensadoras", Loc_Condensadoras::getLocalizacionCondensadoras,btnFiltroLocCondensadoras,tablaLoc_Condensadoras,loc_condensadoras);
    }

    private void noOrdenar(){
        colLoc_Condensadoras.setSortable(false);
    }

    private Dialog<Loc_Condensadoras>crearDialogo(Loc_Condensadoras loc_condensadoras){
        Dialog<Loc_Condensadoras>dialog = new Dialog<>();
        dialog.setTitle(loc_condensadoras == null ? "➕ Añadir localizacion de Condensadora" : "✏️ Modificar Localizacion de condensadora");
        dialog.setHeaderText(null);

        TextField localizacionCondensadorasField = new TextField();


        localizacionCondensadorasField.setPromptText("Localizacion de Condensadora");


        if(loc_condensadoras != null){
            localizacionCondensadorasField.setText(loc_condensadoras.getLocalizacionCondensadoras());

        }

        VBox vBox = new VBox(10, new Label("Localización Condensadora:"),localizacionCondensadorasField);
        vBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(vBox);
        ButtonType añadirBoton = new ButtonType(loc_condensadoras == null? "Añadir":"Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(añadirBoton,ButtonType.CANCEL);

        dialog.setResultConverter(botonDialog -> {
            if(botonDialog == añadirBoton) {
                return new Loc_Condensadoras(localizacionCondensadorasField.getText());
            }
            return null;
        });
        return dialog;
    }

}
