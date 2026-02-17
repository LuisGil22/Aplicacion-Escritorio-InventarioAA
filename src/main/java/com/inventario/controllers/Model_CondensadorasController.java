package com.inventario.controllers;

import com.inventario.models.Model_Cassette;
import com.inventario.models.Model_Condensadora;
import com.inventario.utils.ExcelManager;
import com.inventario.utils.FilterUtils;
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

    @FXML
    private Button btnFiltroModelo;

    private MainAppController mainAppController;
    private ObservableList<Model_Condensadora> modelCondensadoras;

    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    public void initialize(){
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        cargarDatos();
        noOrdenar();
    }

    private void cargarDatos(){
        modelCondensadoras = FXCollections.observableArrayList();
        var excelFile = ExcelManager.leerHoja("PARAM_MODELOS_COND");
        for(int i=1; i<excelFile.size() ; i++){
            var fila = excelFile.get(i);
            if(!fila.isEmpty()){
                String modelo = fila.get(0).trim();
                if(!modelo.isEmpty()){
                    String desc = (fila.size() > 1) ? fila.get(1).trim() : "";
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

        String modeloAEliminar = seleccionada.getModelo();
        if(ExcelManager.existParametroEnCondensadoras(modeloAEliminar,ExcelManager.Columnas.MODELO)){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Esta acción no está permitida");
            alert.setHeaderText("No se puede eliminar este modelo");
            alert.setContentText("El modelo '" +modeloAEliminar+ "' no se puede eliminar porque está siendo usada en la hoja 'Condensadoras'.");
            alert.showAndWait();
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

    @FXML
    private void configurarFiltroModelo(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Modelo", Model_Condensadora::getModelo,btnFiltroModelo,tablaModel_Cond,modelCondensadoras);
    }

    private void noOrdenar(){
        colModelo.setSortable(false);
    }
}
