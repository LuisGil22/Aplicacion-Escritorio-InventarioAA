package com.inventario.controllers;

import com.inventario.models.Gas;
import com.inventario.models.Marca;
import com.inventario.utils.ExcelManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class MarcaController {

    @FXML
    private TableView<Marca> tablaMarca;

    @FXML
    private TableColumn<Marca,String> colMarca;

    @FXML
    private TableColumn<Marca,String> colDescripcion;

    private MainAppController mainAppController;

    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    public void initialize(){
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        cargarDatos();
    }

    private void cargarDatos(){
        ObservableList<Marca> marcas = FXCollections.observableArrayList();
        var excelFile = ExcelManager.leerHoja("PARAM_MARCAS");
        for(int i=1; i<excelFile.size() ; i++){
            var fila = excelFile.get(i);
            if(fila.size()>= 2){
                String marca = fila.get(0).trim();
                String desc = fila.get(1).trim();
                if(!marca.isEmpty() && !desc.isEmpty()){
                    marcas.add(new Marca(marca,desc));
                }
            }
        }
        tablaMarca.setItems(marcas);
    }

    @FXML
    public void onAddMarca(){
        Dialog<Marca> dialog = crearDialogo(null);
        dialog.showAndWait().ifPresent(marca -> {
            tablaMarca.getItems().add(marca);
            ExcelManager.añadirFila("PARAM_MARCAS", marca.getMarca(), marca.getDescripcion());
        });
    }

    @FXML
    public void onEditMarca(){
        Marca seleccionada = tablaMarca.getSelectionModel().getSelectedItem();
        if(seleccionada == null){
            mainAppController.showAlert("Selecciona una marca para modificar.");
            return;
        }
        Dialog<Marca>dialog = crearDialogo(seleccionada);
        dialog.showAndWait().ifPresent(nueva -> {
            ExcelManager.modificarFila("PARAM_MARCAS",
                    new String[]{seleccionada.getMarca(), seleccionada.getDescripcion()},
                    new String[]{nueva.getMarca(), nueva.getDescripcion()});
            seleccionada.setMarca(nueva.getMarca());
            seleccionada.setDescripcion(nueva.getDescripcion());
            tablaMarca.refresh();
        });
    }

    @FXML
    public void onDeleteMarca(){
        Marca seleccionada = tablaMarca.getSelectionModel().getSelectedItem();
        if(seleccionada == null){
            mainAppController.showAlert("Selecciona una marca para eliminar.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar que la quieres eliminar");
        confirmacion.setHeaderText("¿Estas seguro que deseas eliminarla?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if(respuesta == ButtonType.OK){
                tablaMarca.getItems().remove(seleccionada);
                ExcelManager.eliminarFila("PARAM_MARCAS", seleccionada.getMarca(), seleccionada.getDescripcion());
            }
        });
    }

    private Dialog<Marca>crearDialogo(Marca marca){
        Dialog<Marca>dialog = new Dialog<>();
        dialog.setTitle(marca == null ? "➕ Añadir Marca" : "✏️ Modificar Marca");
        dialog.setHeaderText(null);

        TextField marcaField = new TextField();
        TextField descField = new TextField();

        marcaField.setPromptText("Marca");
        descField.setPromptText("Descripcion");

        if(marca != null){
            marcaField.setText(marca.getMarca());
            descField.setText(marca.getDescripcion());
        }

        VBox vBox = new VBox(10,new Label("Marca: "), marcaField, new Label("Descripcion: ", descField));
        vBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(vBox);
        ButtonType añadirBoton = new ButtonType(marca == null? "Añadir":"Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(añadirBoton,ButtonType.CANCEL);

        dialog.setResultConverter(botonDialog -> {
            if(botonDialog == añadirBoton) {
                return new Marca(marcaField.getText(), descField.getText());
            }
            return null;
        });
        return dialog;
    }
}
