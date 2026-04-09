package com.inventario.controllers;

import com.inventario.models.DiaRevision;
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
import java.util.Optional;

/**
 * Controlador para la gestión del parámetro de días entre revisiones.
 * <p>
 * Permite modificar el valor en la hoja PARAM_DIAS_REVISION del archivo Excel.
 * </p>
 *
 * @author Luis Gil
 */
public class DiaRevisionController {
    /**
     * Campos FXML
     */
    @FXML
    private TableView<DiaRevision> tablaDiasRevision;
    @FXML
    private TableColumn<DiaRevision, Integer> colDias;

    @FXML private Button btnFiltroDias;

    /**
     * Dependencias
     */
    private MainAppController mainController;
    private ObservableList<DiaRevision> diasData;

    /**
     * Metodo para establecer la dependencia con el controlador principal de la aplicación.
     *
     * @param controller instancia del controlador principal
     */
    public void setMainAppController(MainAppController controller) {
        this.mainController = controller;
    }

    /**
     * Metodo para inicializar el controlador al cargar la vista FXML.
     * Configura columnas, carga datos y desactiva ordenación.
     */
    @FXML
    public void initialize() {
        colDias.setCellValueFactory(new PropertyValueFactory<>("dias"));
        cargarDatosDiaRevision();
    }

    /**
     * Carga el valor de días desde la hoja PARAM_DIAS_REVISION y lo muestra en la tabla.
     */
    private void cargarDatosDiaRevision() {
        diasData = FXCollections.observableArrayList();
        var excelFile = ExcelManager.leerHoja("PARAM_DIAS_REVISION");
        for (int i = 1; i < excelFile.size(); i++) {
            List<String> fila = excelFile.get(i);
            if (!fila.isEmpty() && fila.get(0) != null) {
                diasData.add(new DiaRevision(Integer.parseInt(fila.get(0))));
            }
        }
        tablaDiasRevision.setItems(diasData);

    }

    /**
     * Abre un diálogo para añadir el número de días y actualiza el Excel.
     */

    @FXML
    private void onAddDias() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Añadir Día de Revisión");
        dialog.setHeaderText("Introduce un número de días (ej. 90, 180, 365)");
        dialog.setContentText("Días:");

        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isPresent()) {
            String input = resultado.get().trim();
            if (input.isEmpty()) {
                mainController.showAlert("El campo no puede estar vacío.");
                return;
            }

            try {
                int dias = Integer.parseInt(input);
                if (dias <= 0) {
                    mainController.showAlert("El número de días debe ser mayor que 0.");
                    return;
                }

                // Añadir a la hoja PARAM_DIAS_REVISION
                ExcelManager.añadirFilaOrdenada("PARAM_DIAS_REVISION", String.valueOf(dias));

                // Recargar la tabla
                cargarDatosDiaRevision();

                mainController.showAlert("Día de revisión añadido correctamente: " + dias);
            } catch (NumberFormatException e) {
                mainController.showAlert("Por favor, introduce un número válido.");
            }
        }
    }

    /**
     * Abre un diálogo para modificar el número de días y actualiza el Excel.
     */
    @FXML
    private void onEditDias() {
        DiaRevision selected = tablaDiasRevision.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mainController.showAlert("Selecciona un valor para modificar.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getDias()));
        dialog.setTitle("Modificar días entre revisiones");
        dialog.setHeaderText("Introduce el nuevo número de días:");
        dialog.setContentText("Dias: ");

        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isPresent()) {
            String input = resultado.get().trim();
            if (input.isEmpty()) {
                mainController.showAlert("El campo no puede estar vacío.");
                return;
            }
            try {
                int diaNuevo = Integer.parseInt(input);
                if (diaNuevo <= 0) {
                    mainController.showAlert("El numero debe ser mayor que 0.");
                    return;
                }

                List<List<String>> datos = ExcelManager.leerHoja("PARAM_DIAS_REVISION");
                Boolean encontrado = false;

                for (int i = 1; i < datos.size(); i++) {
                    List<String> fila = datos.get(i);
                    if (!fila.isEmpty() && !fila.get(0).trim().isEmpty()) {
                        int valorActual = Integer.parseInt(fila.get(0).trim());
                        if (valorActual == selected.getDias()) {
                            // Actualizar la fila
                            fila.set(0, String.valueOf(diaNuevo));
                            ExcelManager.modificarFila("PARAM_DIAS_REVISION", i, fila.toArray(new String[0]));
                            encontrado = true;
                            break;
                        }
                    }
                }

                if (!encontrado) {
                    mainController.showAlert("No se encontró el valor seleccionado en el archivo.");
                    return;
                }

                cargarDatosDiaRevision();
                mainController.showAlert("Valor actualizado correctamente.");

            } catch (NumberFormatException e) {
                mainController.showAlert("Por favor, introduce un número válido.");
            }

        }
    }

    /**
     * Abre un diálogo de confirmación para eliminar el número de días seleccionado y actualiza el Excel.
     */
    @FXML
    private void onDeleteDias(){
        DiaRevision selected = tablaDiasRevision.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mainController.showAlert("Selecciona un valor para modificar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar este valor de la tabla DIAS?");
        confirm.setContentText("Esta acción no se puede deshacer.");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int diasAEliminar = selected.getDias();
                List<List<String>> datos = ExcelManager.leerHoja("PARAM_DIAS_REVISION");

                boolean encontrado = false;
                for (int i = 1; i < datos.size(); i++){
                    List<String> fila = datos.get(i);
                    if (!fila.isEmpty() && !fila.get(0).trim().isEmpty()) {
                        try {
                            int valor = Integer.parseInt(fila.get(0).trim());
                            if (valor == diasAEliminar) {
                                // Eliminar la fila del Excel
                                ExcelManager.eliminarFilaPorIndice("PARAM_DIAS_REVISION", i);
                                encontrado = true;
                                break;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }

                if (!encontrado) {
                    mainController.showAlert("No se encontró el valor seleccionado en el archivo.");
                    return;
                }

                cargarDatosDiaRevision();
                mainController.showAlert("Valor eliminado correctamente.");

            }catch (NumberFormatException e) {
                mainController.showAlert("Error al eliminar el valor.");
            }
        }
    }

    /**
     * Configura el botón de filtro para ordenar la tabla por días.
     * Utiliza la utilidad FilterUtils para mostrar un menú de ordenación.
     */
    @FXML
    private void configurarFiltroDias(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Dias", item -> String.valueOf(item.getDias()), btnFiltroDias,tablaDiasRevision,diasData,(ascending) -> {
            ObservableList<DiaRevision> sorted = FXCollections.observableArrayList(diasData);
            sorted.sort(Comparator.comparing(
                    item -> String.valueOf(item.getDias()),
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaDiasRevision.setItems(sorted);
        });
    }
}