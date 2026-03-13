package com.inventario.controllers;

import com.inventario.models.Ubicacion_Cassette;
import com.inventario.utils.ExcelManager;
import com.inventario.utils.FilterUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

/**
 * Controlador para la gestión de ubicaciones de cassettes en la hoja PARAM_UBICACIONES_CASSETTES del inventario.
 * <p>
 * Proporciona funcionalidades para:
 * </p>
 * <ul>
 *   <li>Visualizar ubicaciones registradas</li>
 *   <li>Añadir nuevas ubicaciones</li>
 *   <li>Modificar ubicaciones existentes</li>
 *   <li>Eliminar ubicaciones (con validación de uso en Cassette)</li>
 *   <li>Filtrar por nombre de ubicación</li>
 * </ul>
 *
 * @author Luis Gil
 */
public class UbicacionCassetteController {

    /** Campos FXML */
    @FXML private TableView<Ubicacion_Cassette> tablaUbicacionCassettes;
    @FXML private TableColumn<Ubicacion_Cassette,String> colUbicacionCas;
    @FXML private Button btnFiltroUbicacionCas;

    /** Dependencias */
    private MainAppController mainAppController;
    private ObservableList<Ubicacion_Cassette> ubicacionCassettes;

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
    @FXML
    public void initialize(){
        colUbicacionCas.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        cargarDatos();
        noOrdenar();
    }

    /**
     * Metodo para cargar los datos de la hoja PARAM_UBICACIONES_CASSETTES del archivo Excel y mostrarlos en la tabla.
     * Omite filas vacías o con valores nulos.
     */
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

    /**
     * Metodo para abrir un diálogo y añadir una nueva Ubicación de Cassette.
     * Válida que el valor no esté vacío antes de guardar.
     */
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

    /**
     * Metodo para abrir un diálogo para modificar la Ubicación de Cassette seleccionada.
     * Válida que haya una selección activa y que el nuevo valor no esté vacío.
     */
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

    /**
     * Metodo para eliminar la Ubicación de Cassette seleccionada tras confirmación.
     * Verifíca que no esté en uso en las hojas Cassette o Condensadoras.
     */
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

    /**
     * Metodo para configurar el filtro de la columna NOMBRE.
     */
    @FXML
    private void configurarFiltroUbicacionCas(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Nombre", Ubicacion_Cassette::getNombre,btnFiltroUbicacionCas,tablaUbicacionCassettes,ubicacionCassettes);
    }

    /**
     * Metodo para desactivar la ordenación en la columna de la tabla.
     * Mejora la estabilidad visual al trabajar con datos no ordenados.
     */
    private void noOrdenar(){
        colUbicacionCas.setSortable(false);
    }

}
