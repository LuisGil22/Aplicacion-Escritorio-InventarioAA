package com.inventario.controllers;

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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("ALL")
public class Loc_CondensadorasController {

    /** Campos FXML */
    @FXML private TableView<Loc_Condensadoras> tablaLoc_Condensadoras;
    @FXML private TableColumn<Loc_Condensadoras,String> colLoc_Condensadoras;
    @FXML private Button btnFiltroLocCondensadoras;

    /** Dependencias */
    private MainAppController mainController;
    private ObservableList<Loc_Condensadoras> loc_condensadoras;

    /**
     * Metodo para establecer la dependencia con el controlador principal de la aplicación.
     *
     * @param controller instancia del controlador principal
     */
    public void setMainController(MainAppController controller) {
        this.mainController = controller;
    }

    /**
     * Metodo para inicializar el controlador al cargar la vista FXML.
     * Configura columnas, carga datos y desactiva ordenación.
     */
    @FXML
    public void initialize() {
        colLoc_Condensadoras.setCellValueFactory(new PropertyValueFactory<>("localizacionCondensadoras"));
        cargarDatos();
        noOrdenar();
    }

    /**
     * Metodo para cargar los datos de la hoja PARAM_LOC_COND del archivo Excel y mostrarlos en la tabla.
     * Omite filas vacías o con valores nulos.
     */
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

    /**
     * Metodo para abrir un diálogo y añadir una nueva localizacion de condensadora.
     * Valida que el valor no esté vacío antes de guardar.
     */
    @FXML
    public void onAddLoc_Condensadoras(){
        Dialog<Loc_Condensadoras>dialog = crearDialogo(null);
        dialog.showAndWait().ifPresent(loc_condensadoras -> {
            tablaLoc_Condensadoras.getItems().add(loc_condensadoras);
            ExcelManager.añadirFila("PARAM_LOC_COND", loc_condensadoras.getLocalizacionCondensadoras());
        });
    }

    /**
     * Metodo para abrir un diálogo para modificar la localizacion de condensadora seleccionada.
     * Valida que haya una selección activa y que el nuevo valor no esté vacío.
     */
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

    /**
     * Metodo para eliminar la localizacion de condensadora seleccionada tras confirmación.
     * Verifica que no esté en uso en la hoja Condensadoras.
     */
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

    /**
     * Metodo para configurar el filtro de la columna LOCALIZACION_CONDENSADORAS.
     */
    @FXML
    private void configurarFiltroLocCondensadoras(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Localización Condensadoras", Loc_Condensadoras::getLocalizacionCondensadoras,btnFiltroLocCondensadoras,tablaLoc_Condensadoras,loc_condensadoras,(ascending) -> {
            ObservableList<Loc_Condensadoras> sorted = FXCollections.observableArrayList(loc_condensadoras);
            sorted.sort(Comparator.comparing(
                    Loc_Condensadoras::getLocalizacionCondensadoras,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaLoc_Condensadoras.setItems(sorted);
        });
    }

    /**
     * Metodo para desactivar la ordenación en la columna de la tabla.
     * Mejora la estabilidad visual al trabajar con datos no ordenados.
     */
    private void noOrdenar(){
        colLoc_Condensadoras.setSortable(false);
    }

    /**
     * Metodo para crear un diálogo personalizado para añadir o modificar una localización de condensadora.
     *
     * @param loc_condensadoras localización a modificar (null para nuevo registro)
     * @return diálogo configurado
     */
    private Dialog<Loc_Condensadoras>crearDialogo(Loc_Condensadoras loc_condensadoras){
        Dialog<Loc_Condensadoras>dialog = new Dialog<>();
        dialog.setTitle(loc_condensadoras == null ? "Añadir localizacion de Condensadora" : "Modificar Localizacion de condensadora");
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
