package com.inventario.controllers;

import com.inventario.models.Model_Cassette;
import com.inventario.models.Model_Condensadora;
import com.inventario.utils.ExcelManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class Model_CondensadorasController {

    @FXML
    private TableView<Model_Condensadora> tablaModel_Cond;

    @FXML
    private TableColumn<Model_Condensadora,String> colModelo;

    @FXML
    private TableColumn<Model_Condensadora,String> colDescripcion;

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
        ObservableList<Model_Condensadora> modelCondensadoras = FXCollections.observableArrayList();
        var excelFile = ExcelManager.leerHoja("PARAM_MODELOS_COND");
        for(int i=1; i<excelFile.size() ; i++){
            var fila = excelFile.get(i);
            if(fila.size()>= 2){
                String modelo = fila.get(0).trim();
                String desc = fila.get(1).trim();
                if(!modelo.isEmpty() && !desc.isEmpty()){
                    modelCondensadoras.add(new Model_Condensadora(modelo,desc));
                }
            }
        }
        tablaModel_Cond.setItems(modelCondensadoras);
    }

    @FXML
    public void onAddModel_Cond(){
        Dialog<Model_Condensadora> dialog = crearDialogo(null);
        dialog.showAndWait().ifPresent(modelCondensadora -> {
            tablaModel_Cond.getItems().add(modelCondensadora);
            ExcelManager.añadirFila("PARAM_MODELOS_COND", modelCondensadora.getModelo(), modelCondensadora.getDescripcion());
        });
    }

    @FXML
    public void onEditModel_Cond(){
        Model_Condensadora seleccionada = tablaModel_Cond.getSelectionModel().getSelectedItem();
        if(seleccionada == null){
            mainAppController.showAlert("Selecciona un modelo de condensadora para modificar.");
            return;
        }
        Dialog<Model_Condensadora>dialog = crearDialogo(seleccionada);
        dialog.showAndWait().ifPresent(nueva -> {
            ExcelManager.modificarFila("PARAM_MODELOS_COND",
                    new String[]{seleccionada.getModelo(), seleccionada.getDescripcion()},
                    new String[]{nueva.getModelo(), nueva.getDescripcion()});
            seleccionada.setModelo(nueva.getModelo());
            seleccionada.setDescripcion(nueva.getDescripcion());
            tablaModel_Cond.refresh();
        });
    }

    @FXML
    public void onDeleteModel_Cond(){
        Model_Condensadora seleccionada = tablaModel_Cond.getSelectionModel().getSelectedItem();
        if(seleccionada == null){
            mainAppController.showAlert("Selecciona un modelo de condensadora para eliminar.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar que lo quieres eliminar");
        confirmacion.setHeaderText("¿Estas seguro que deseas eliminarlo?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if(respuesta == ButtonType.OK){
                tablaModel_Cond.getItems().remove(seleccionada);
                ExcelManager.eliminarFila("PARAM_MODELOS_COND", seleccionada.getModelo(), seleccionada.getDescripcion());
            }
        });
    }

    private Dialog<Model_Condensadora>crearDialogo(Model_Condensadora modelCondensadora){
        Dialog<Model_Condensadora>dialog = new Dialog<>();
        dialog.setTitle(modelCondensadora == null ? "➕ Añadir Modelo" : "✏️ Modificar Modelo");
        dialog.setHeaderText(null);

        TextField modeloField = new TextField();
        TextField descField = new TextField();

        modeloField.setPromptText("Modelo");
        descField.setPromptText("Descripcion");

        if(modelCondensadora != null){
            modeloField.setText(modelCondensadora.getModelo());
            descField.setText(modelCondensadora.getDescripcion());
        }

        VBox vBox = new VBox(10,new Label("Modelo: "), modeloField, new Label("Descripcion: ", descField));
        vBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(vBox);
        ButtonType añadirBoton = new ButtonType(modelCondensadora == null? "Añadir":"Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(añadirBoton,ButtonType.CANCEL);

        dialog.setResultConverter(botonDialog -> {
            if(botonDialog == añadirBoton) {
                return new Model_Condensadora(modeloField.getText(), descField.getText());
            }
            return null;
        });
        return dialog;
    }
}
