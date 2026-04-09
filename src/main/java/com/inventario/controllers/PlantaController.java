package com.inventario.controllers;

import com.inventario.models.Planta;
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

/**
 * Controlador para la gestión de plantas en la hoja PARAM_PLANTAS del inventario.
 * <p>
 * Proporciona funcionalidades para:
 * </p>
 * <ul>
 *   <li>Visualizar plantas registradas</li>
 *   <li>Añadir nuevas plantas</li>
 *   <li>Modificar plantas existentes</li>
 *   <li>Eliminar plantas (con validación de uso en Cassette)</li>
 *   <li>Filtrar por planta o descripción</li>
 * </ul>
 *
 * @author Luis Gil
 */
@SuppressWarnings("ALL")
public class PlantaController {

    /** Campos FXML */
    @FXML private TableView<Planta> tablaPlantas;
    @FXML private TableColumn<Planta,String> colPlanta;
    @FXML private TableColumn<Planta,String> colDescripcion;
    @FXML private Button btnFiltroPlanta;

    /** Dependencias */
    private MainAppController mainAppController;
    private ObservableList<Planta> plantas;

    /**
     * Metodo para establecer la dependencia con el controlador principal de la aplicación.
     *
     * @param mainAppController instancia del controlador principal
     */
    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    /**
     * Metodo para inicializar el controlador al cargar la vista FXML.
     * Configura columnas, carga datos y desactiva ordenación.
     */
    public void initialize(){
        colPlanta.setCellValueFactory(new PropertyValueFactory<>("planta"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        cargarDatos();
        noOrdenar();
    }

    /**
     * Metodo para cargar los datos de la hoja PARAM_PLANTAS del archivo Excel y mostrarlos en la tabla.
     * Omite filas vacías o con valores nulos.
     */
    private void cargarDatos(){
        plantas = FXCollections.observableArrayList();
        var excelFile = ExcelManager.leerHoja("PARAM_PLANTAS");
        for(int i=1; i<excelFile.size() ; i++){
            var fila = excelFile.get(i);
            if(!fila.isEmpty()){
                String planta = fila.get(0).trim();
                if(!planta.isEmpty()){
                    String desc = (fila.size() > 1) ? fila.get(1).trim() : "";
                    plantas.add(new Planta(planta,desc));
                }
            }
        }
        tablaPlantas.setItems(plantas);
    }

    /**
     * Metodo para abrir un diálogo y añadir una nueva Planta.
     * Valida que el valor no esté vacío antes de guardar.
     */
    @FXML
    public void onAddPlanta(){
        Dialog<Planta> dialog = crearDialogo(null);
        dialog.showAndWait().ifPresent(planta -> {
            tablaPlantas.getItems().add(planta);
            ExcelManager.añadirFila("PARAM_PLANTAS", planta.getPlanta(), planta.getDescripcion());
        });
    }

    /**
     * Metodo para abrir un diálogo para modificar la Planta seleccionada.
     * Valida que haya una selección activa y que el nuevo valor no esté vacío.
     */
    @FXML
    public void onEditPlanta(){
        Planta seleccionada = tablaPlantas.getSelectionModel().getSelectedItem();
        if(seleccionada == null){
            mainAppController.showAlert("Selecciona una planta para modificar.");
            return;
        }
        Dialog<Planta>dialog = crearDialogo(seleccionada);
        dialog.showAndWait().ifPresent(nueva -> {
            List<List<String>> datos = ExcelManager.leerHoja("PARAM_PLANTAS");
            int index = -1;
            for (int i = 1; i < datos.size(); i++) {
                List<String> fila = datos.get(i);

                if (fila.size() > 0 && fila.get(0).trim().equals(seleccionada.getPlanta())) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {

                ExcelManager.modificarFila("PARAM_PLANTAS", index, new String[]{nueva.getPlanta(), nueva.getDescripcion()}
                );
                seleccionada.setPlanta(nueva.getPlanta());
                seleccionada.setDescripcion(nueva.getDescripcion());
                tablaPlantas.refresh();
            } else {
                mainAppController.showAlert("No se encontró la planta en el archivo.");
            }
        });
    }

    /**
     * Metodo para eliminar la Planta seleccionada tras confirmación.
     * Verifica que no esté en uso en la hoja Cassette.
     */
    @FXML
    public void onDeletePlanta(){
        Planta seleccionada = tablaPlantas.getSelectionModel().getSelectedItem();
        if(seleccionada == null){
            mainAppController.showAlert("Selecciona una planta para eliminar.");
            return;
        }

        String plantaAEliminar = seleccionada.getPlanta();
        if(ExcelManager.existParametroEnCassettes(plantaAEliminar,"PLANTA")){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Esta acción no está permitida");
            alert.setHeaderText("No se puede eliminar esta planta");
            alert.setContentText("La planta '" +plantaAEliminar+ "' no se puede eliminar porque está siendo usada en la hoja 'Cassette'.");
            alert.showAndWait();
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar que la quieres eliminar");
        confirmacion.setHeaderText("¿Estas seguro que deseas eliminarla?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if(respuesta == ButtonType.OK){
                tablaPlantas.getItems().remove(seleccionada);
                ExcelManager.eliminarFila("PARAM_PLANTAS", plantaAEliminar);
            }
        });
    }

    /**
     * Metodo para crear un diálogo personalizado para añadir o modificar una planta.
     *
     * @param planta planta a modificar (null para nuevo registro)
     * @return diálogo configurado
     */
    private Dialog<Planta>crearDialogo(Planta planta){
        Dialog<Planta>dialog = new Dialog<>();
        dialog.setTitle(planta == null ? "➕ Añadir Planta" : "✏️ Modificar Planta");
        dialog.setHeaderText(null);

        TextField plantaField = new TextField();
        TextField descField = new TextField();

        plantaField.setPromptText("Planta");
        descField.setPromptText("Descripcion");

        if(planta != null){
            plantaField.setText(planta.getPlanta());
            descField.setText(planta.getDescripcion());
        }

        VBox vBox = new VBox(10, new Label("Planta: "),plantaField, new Label("Descripción: "), descField);
        vBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(vBox);
        ButtonType añadirBoton = new ButtonType(planta == null? "Añadir":"Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(añadirBoton,ButtonType.CANCEL);

        dialog.setResultConverter(botonDialog -> {
            if(botonDialog == añadirBoton) {
                return new Planta(plantaField.getText(), descField.getText());
            }
            return null;
        });
        return dialog;
    }

    /**
     * Metodo para configurar el filtro de la columna PLANTA.
     */
    @FXML
    private void configurarFiltroPlanta(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Planta", Planta::getPlanta,btnFiltroPlanta,tablaPlantas,plantas,(ascending) -> {
            ObservableList<Planta> sorted = FXCollections.observableArrayList(plantas);
            sorted.sort(Comparator.comparing(
                    Planta::getPlanta,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaPlantas.setItems(sorted);
        });
    }

    /**
     * Metodo para desactivar la ordenación en la columna de la tabla.
     * Mejora la estabilidad visual al trabajar con datos no ordenados.
     */
    private void noOrdenar(){
        colPlanta.setSortable(false);
        colDescripcion.setSortable(false);
    }
}
