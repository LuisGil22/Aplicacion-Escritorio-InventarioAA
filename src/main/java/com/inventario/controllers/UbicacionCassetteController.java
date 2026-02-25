package com.inventario.controllers;

import com.inventario.models.Estado;
import com.inventario.models.Ubicacion_Cassette;
import com.inventario.utils.ExcelManager;
import com.inventario.utils.FilterUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class UbicacionCassetteController {

    @FXML
    private TableView<Ubicacion_Cassette> tablaUbicacionCassettes;

    @FXML
    private TableColumn<Ubicacion_Cassette,String> colUbicacionCas;

    @FXML
    private Button btnFiltroUbicacionCas;

    private MainAppController mainAppController;
    private ObservableList<Ubicacion_Cassette> ubicacionCassettes;

    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    @FXML
    public void initialize(){
        colUbicacionCas.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        cargarDatos();
        noOrdenar();
    }

    private void cargarDatos() {
        ubicacionCassettes = FXCollections.observableArrayList();
        var excelFile = ExcelManager.leerHoja("PARAM_UBICACIONES_CASSETTES");
        for (int i = 1; i < excelFile.size(); i++){
            var fila = excelFile.get(i);
            if(!fila.isEmpty() && !fila.get(0).trim().isEmpty()){
                ubicacionCassettes.add(new Ubicacion_Cassette(fila.get(0)));
            }
        }
        tablaUbicacionCassettes.setItems(ubicacionCassettes);
    }

    @FXML
    public void onAddUbicacionCas(){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nueva Ubicacion de cassette");
        dialog.setHeaderText("Añadir nueva Ubicacion de cassette");
        dialog.setContentText("Ubicacion: ");

        dialog.showAndWait().ifPresent(ubicacion -> {
            if (!ubicacion.trim().isEmpty()) {
                tablaUbicacionCassettes.getItems().add(new Ubicacion_Cassette(ubicacion));
                ExcelManager.añadirFila("PARAM_UBICACIONES_CASSETTES", ubicacion);
            }
        });
    }

    @FXML
    public void onEditUbicacionCas(){
        Ubicacion_Cassette selected = tablaUbicacionCassettes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mainAppController.showAlert("Selecciona un nombre para modificar.");
            return;
        }
        String actualUbicacion = selected.getNombre();
        TextInputDialog dialog = new TextInputDialog(actualUbicacion);
        dialog.setTitle("Editar Ubicacion de cassette");
        dialog.setHeaderText("Modificar Ubicacion de cassette");
        dialog.setContentText("Nueva Ubicacion: ");

        dialog.showAndWait().ifPresent(nueva -> {
            if (!nueva.trim().isEmpty()) {
                List<List<String>> datos = ExcelManager.leerHoja("PARAM_UBICACIONES_CASSETTES");
                int index = -1;

                for (int i= 1; i< datos.size(); i++){
                    if(datos.get(i).size() > 0 && datos.get(i).get(0).trim().equals(actualUbicacion)){
                        index = i;
                        break;
                    }
                }
                if(index != -1) {
                    ExcelManager.modificarFila("PARAM_UBICACIONES_CASSETTES", index, new String[]{nueva});
                    selected.setNombre(nueva);
                    tablaUbicacionCassettes.refresh();
                }else{
                    mainAppController.showAlert("No se encontró la Ubicacion en el archivo.");
                }
            }
        });
    }

    @FXML
    public void onDeleteUbicacionCas(){
        Ubicacion_Cassette selected = tablaUbicacionCassettes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mainAppController.showAlert("Selecciona un nombre para eliminar.");
            return;
        }

        String ubicacionAEliminar = selected.getNombre();
        if(ExcelManager.existParametroEnCassettes(ubicacionAEliminar,"NOMBRE")){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Esta acción no está permitida");
            alert.setHeaderText("No se puede eliminar este nombre");
            alert.setContentText("El nombre '" +ubicacionAEliminar+ "' no se puede eliminar porque está siendo usado en la hoja 'Cassette'.");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar este nombre?");
        alert.setContentText("Esta acción no se puede deshacer.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                tablaUbicacionCassettes.getItems().remove(selected);
                ExcelManager.eliminarFila("PARAM_UBICACIONES_CASSETTES", selected.getNombre());
            }
        });
    }

    @FXML
    private void configurarFiltroUbicacionCas(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Nombre", Ubicacion_Cassette::getNombre,btnFiltroUbicacionCas,tablaUbicacionCassettes,ubicacionCassettes);
    }

    private void noOrdenar(){
        colUbicacionCas.setSortable(false);
    }

}
