package com.inventario.controllers;

import com.inventario.models.Estado;
import com.inventario.utils.ExcelManager;
import com.inventario.utils.FilterUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Controlador para la gestión de estados válidos en la hoja PARAM_ESTADO del inventario.
 * <p>
 * Proporciona funcionalidades para:
 * </p>
 * <ul>
 *   <li>Visualizar estados válidos (ACTIVA, BAJA, AVERIADO)</li>
 *   <li>Añadir nuevos estados</li>
 *   <li>Modificar estados existentes</li>
 *   <li>Eliminar estados (con validación de uso en Cassette/Condensadoras)</li>
 *   <li>Filtrar por estado</li>
 * </ul>
 *
 * @author Luis Gil
 */
public class EstadoController {

    /** Campos FXML */
    @FXML private TableView<Estado> tablaEstados;
    @FXML private TableColumn<Estado, String> colEstado;

    @FXML private Button btnFiltroEstado;

    /** Dependencias */
    private MainAppController mainController;
    private ObservableList<Estado>estados;

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
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        cargarDatos();
        noOrdenar();
    }

    /**
     * Metodo para cargar los datos de la hoja PARAM_ESTADO del archivo Excel y mostrarlos en la tabla.
     * Omite filas vacías o con valores nulos.
     */
    private void cargarDatos() {
        estados = FXCollections.observableArrayList();
        var excelFile = ExcelManager.leerHoja("PARAM_ESTADO");
        for (int i = 1; i < excelFile.size(); i++){
            var fila = excelFile.get(i);
            if(!fila.isEmpty() && !fila.get(0).trim().isEmpty()){
                estados.add(new Estado(fila.get(0)));
            }
        }
        tablaEstados.setItems(estados);
    }

    /**
     * Metodo para abrir un diálogo y añadir un nuevo estado.
     * Valida que el valor no esté vacío antes de guardar.
     */
    @FXML
    public void onAdd() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo Estado");
        dialog.setHeaderText("Añadir nuevo valor de estado");
        dialog.setContentText("Estado:");

        dialog.showAndWait().ifPresent(estado -> {
            if (!estado.trim().isEmpty()) {
                tablaEstados.getItems().add(new Estado(estado));
                ExcelManager.añadirFila("PARAM_ESTADO", estado);
            }
        });
    }

    /**
     * Metodo para abrir un diálogo para modificar el estado seleccionado.
     * Valida que haya una selección activa y que el nuevo valor no esté vacío.
     */
    @FXML
    public void onEdit() {
        Estado selected = tablaEstados.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mainController.showAlert("Selecciona un estado para modificar.");
            return;
        }
        String actualEstado = selected.getEstado();
        TextInputDialog dialog = new TextInputDialog(actualEstado);
        dialog.setTitle("Editar Estado");
        dialog.setHeaderText("Modificar valor de estado");
        dialog.setContentText("Nuevo estado:");

        dialog.showAndWait().ifPresent(nuevo -> {
            if (!nuevo.trim().isEmpty()) {
                List<List<String>> datos = ExcelManager.leerHoja("PARAM_ESTADO");
                int index = -1;

                for (int i= 1; i< datos.size(); i++){
                    if(datos.get(i).size() > 0 && datos.get(i).get(0).trim().equals(actualEstado)){
                        index = i;
                        break;
                    }
                }
                if(index != -1) {
                    ExcelManager.modificarFila("PARAM_ESTADO", index, new String[]{nuevo});
                    selected.setEstado(nuevo);
                    tablaEstados.refresh();
                }else{
                    mainController.showAlert("No se encontró el estado en el archivo.");
                }
            }
        });
    }

    /**
     * Metodo para eliminar el estado seleccionado tras confirmación.
     * Verifica que no esté en uso en las hojas Cassette o Condensadoras.
     */
    @FXML
    public void onDelete() {
        Estado selected = tablaEstados.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mainController.showAlert("Selecciona un estado para eliminar.");
            return;
        }

        String estadoAEliminar = selected.getEstado();
        if(ExcelManager.existParametroEnCondensadoras(estadoAEliminar,ExcelManager.Columnas.ESTADO) && ExcelManager.existParametroEnCassettes(estadoAEliminar,ExcelManager.Columnas.ESTADO)){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Esta acción no está permitida");
            alert.setHeaderText("No se puede eliminar este estado");
            alert.setContentText("El estado '" +estadoAEliminar+ "' no se puede eliminar porque está siendo usado en la hoja 'Condensadoras' 0 'Cassette'.");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar este estado?");
        alert.setContentText("Esta acción no se puede deshacer.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                tablaEstados.getItems().remove(selected);
                ExcelManager.eliminarFila("PARAM_ESTADO", selected.getEstado());
            }
        });
    }

    /**
     * Metodo para configurar el filtro de la columna ESTADO.
     */
    @FXML
    private void configurarFiltroEstado(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Estado", Estado::getEstado,btnFiltroEstado,tablaEstados,estados,(ascending) -> {
            ObservableList<Estado> sorted = FXCollections.observableArrayList(estados);
            sorted.sort(Comparator.comparing(
                    Estado::getEstado,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaEstados.setItems(sorted);
        });
    }

    /**
     * Metodo para desactivar la ordenación en la columna de la tabla.
     * Mejora la estabilidad visual al trabajar con datos no ordenados.
     */
    private void noOrdenar(){
        colEstado.setSortable(false);
    }

}
