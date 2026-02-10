package com.inventario.controllers;

import com.inventario.models.Marca;
import com.inventario.models.Model_Cassette;
import com.inventario.utils.ExcelManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class Model_CassettesController {

    @FXML
    private TableView<Model_Cassette> tablaModel_Cas;

    @FXML
    private TableColumn<Model_Cassette,String> colModelo;

    @FXML
    private TableColumn<Model_Cassette,String> colDescripcion;

    private MainAppController mainAppController;

    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    public void initialize(){
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        cargarDatos();
    }

    private void cargarDatos(){
        ObservableList<Model_Cassette> modelCassettes = FXCollections.observableArrayList();
        var excelFile = ExcelManager.leerHoja("PARAM_MODELOS_CAS");
        for(int i=1; i<excelFile.size() ; i++){
            var fila = excelFile.get(i);
            if(fila.size()>= 2){
                String modelo = fila.get(0).trim();
                String desc = fila.get(1).trim();
                if(!modelo.isEmpty() && !desc.isEmpty()){
                    modelCassettes.add(new Model_Cassette(modelo,desc));
                }
            }
        }
        tablaModel_Cas.setItems(modelCassettes);
    }

    @FXML
    public void onAddModel_Cas(){
        Dialog<Model_Cassette> dialog = crearDialogo(null);
        dialog.showAndWait().ifPresent(modelCassette -> {
            tablaModel_Cas.getItems().add(modelCassette);
            ExcelManager.añadirFila("PARAM_MODELOS_CAS", modelCassette.getModelo(), modelCassette.getDescripcion());
        });
    }

    @FXML
    public void onEditModel_Cas(){
        Model_Cassette seleccionado = tablaModel_Cas.getSelectionModel().getSelectedItem();
        if(seleccionado == null){
            mainAppController.showAlert("Selecciona un modelo de cassette para modificar.");
            return;
        }
        Dialog<Model_Cassette>dialog = crearDialogo(seleccionado);
        dialog.showAndWait().ifPresent(nuevo -> {
            ExcelManager.modificarFila("PARAM_MODELOS_CAS",
                    new String[]{seleccionado.getModelo(), seleccionado.getDescripcion()},
                    new String[]{nuevo.getModelo(), nuevo.getDescripcion()});
            seleccionado.setModelo(nuevo.getModelo());
            seleccionado.setDescripcion(nuevo.getDescripcion());
            tablaModel_Cas.refresh();
        });
    }

    @FXML
    public void onDeleteModel_Cas(){
        Model_Cassette seleccionado = tablaModel_Cas.getSelectionModel().getSelectedItem();
        if(seleccionado == null){
            mainAppController.showAlert("Selecciona un modelo de cassette para eliminar.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar que lo quieres eliminar");
        confirmacion.setHeaderText("¿Estas seguro que deseas eliminarlo?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if(respuesta == ButtonType.OK){
                tablaModel_Cas.getItems().remove(seleccionado);
                ExcelManager.eliminarFila("PARAM_MODELOS_CAS", seleccionado.getModelo(), seleccionado.getDescripcion());
            }
        });
    }

    private Dialog<Model_Cassette>crearDialogo(Model_Cassette modelCassette){
        Dialog<Model_Cassette>dialog = new Dialog<>();
        dialog.setTitle(modelCassette == null ? "➕ Añadir Modelo" : "✏️ Modificar Modelo");
        dialog.setHeaderText(null);

        TextField modeloField = new TextField();
        TextField descField = new TextField();

        modeloField.setPromptText("Modelo");
        descField.setPromptText("Descripcion");

        if(modelCassette != null){
            modeloField.setText(modelCassette.getModelo());
            descField.setText(modelCassette.getDescripcion());
        }

        VBox vBox = new VBox(10,new Label("Modelo: "), modeloField, new Label("Descripcion: ", descField));
        vBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(vBox);
        ButtonType añadirBoton = new ButtonType(modelCassette == null? "Añadir":"Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(añadirBoton,ButtonType.CANCEL);

        dialog.setResultConverter(botonDialog -> {
            if(botonDialog == añadirBoton) {
                return new Model_Cassette(modeloField.getText(), descField.getText());
            }
            return null;
        });
        return dialog;
    }
}
