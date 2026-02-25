package com.inventario.controllers;

import com.inventario.models.Gas;
import com.inventario.models.Marca;
import com.inventario.models.Model_Cassette;
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

public class Model_CassettesController {

    @FXML
    private TableView<Model_Cassette> tablaModel_Cas;

    @FXML
    private TableColumn<Model_Cassette,String> colModeloCas;

    @FXML
    private TableColumn<Model_Cassette,String> colDescripcion;

    @FXML
    private Button btnFiltroModeloCas;

    private MainAppController mainAppController;
    private ObservableList<Model_Cassette> modelCassettes;

    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    public void initialize(){
        colModeloCas.setCellValueFactory(new PropertyValueFactory<>("modeloCas"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        cargarDatos();
        noOrdenar();
    }

    private void cargarDatos(){
        modelCassettes = FXCollections.observableArrayList();
        var excelFile = ExcelManager.leerHoja("PARAM_MODELOS_CAS");
        for(int i=1; i<excelFile.size() ; i++){
            var fila = excelFile.get(i);
            if(!fila.isEmpty()){
                String modelo = fila.get(0).trim();
                if(!modelo.isEmpty()){
                    String desc = (fila.size() > 1) ? fila.get(1).trim() : "";
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
            ExcelManager.añadirFila("PARAM_MODELOS_CAS", modelCassette.getModeloCas(), modelCassette.getDescripcion());
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
            List<List<String>> datos = ExcelManager.leerHoja("PARAM_MODELOS_CAS");
            int index = -1;
            for (int i = 1; i < datos.size(); i++) {
                List<String> fila = datos.get(i);
                // Comparar por la primera columna (GAS)
                if (fila.size() > 0 && fila.get(0).trim().equals(seleccionado.getModeloCas())) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                // Usar modificarFilaPorIndice
                ExcelManager.modificarFila("PARAM_MODELOS_CAS", index, new String[]{nuevo.getModeloCas(), nuevo.getDescripcion()}
                );
                seleccionado.setModeloCas(nuevo.getModeloCas());
                seleccionado.setDescripcion(nuevo.getDescripcion());
                tablaModel_Cas.refresh();
            } else {
                mainAppController.showAlert("No se encontró el Modelo de Cassette en el archivo.");
            }
        });
    }

    @FXML
    public void onDeleteModel_Cas(){
        Model_Cassette seleccionado = tablaModel_Cas.getSelectionModel().getSelectedItem();
        if(seleccionado == null){
            mainAppController.showAlert("Selecciona un modelo de cassette para eliminar.");
            return;
        }
        String modeloCasAEliminar = seleccionado.getModeloCas();

        if(ExcelManager.existParametroEnCassettes(modeloCasAEliminar,"MARCA/MODELO")){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Esta acción no está permitida");
            alert.setHeaderText("No se puede eliminar este modelo de cassette");
            alert.setContentText("El modelo de cassette '" +modeloCasAEliminar+ "' no se puede eliminar porque está siendo usada en la hoja 'Cassette'.");
            alert.showAndWait();
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar que lo quieres eliminar");
        confirmacion.setHeaderText("¿Estas seguro que deseas eliminarlo?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if(respuesta == ButtonType.OK){
                tablaModel_Cas.getItems().remove(seleccionado);
                ExcelManager.eliminarFila("PARAM_MODELOS_CAS", modeloCasAEliminar);
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
            modeloField.setText(modelCassette.getModeloCas());
            descField.setText(modelCassette.getDescripcion());
        }

        VBox vBox = new VBox(10,new Label("Modelo: "), modeloField, new Label("Descripcion: "), descField);
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

    @FXML
    private void configurarFiltroModeloCas(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Modelo", Model_Cassette::getModeloCas,btnFiltroModeloCas,tablaModel_Cas,modelCassettes);
    }

    private void noOrdenar(){
        colModeloCas.setSortable(false);
        colDescripcion.setSortable(false);
    }
}
