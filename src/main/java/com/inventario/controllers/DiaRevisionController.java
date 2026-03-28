package com.inventario.controllers;

import com.inventario.models.DiaRevision;
import com.inventario.utils.ExcelManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controlador para la gestión del parámetro de días entre revisiones.
 * <p>
 * Permite modificar el valor en la hoja PARAM_DIAS_REVISION del archivo Excel.
 * </p>
 *
 * @author Luis Gil
 */
public class DiaRevisionController {
    /** Campos FXML */
    @FXML private TableView<DiaRevision> tablaDiasRevision;
    @FXML private TableColumn<DiaRevision, Integer> colDias;

    /** Dependencias */
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
    public void initialize(){
        colDias.setCellValueFactory(new PropertyValueFactory<>("dias"));
        cargarDatosDiaRevision();
    }

    /**
     * Carga el valor de días desde la hoja PARAM_DIAS_REVISION y lo muestra en la tabla.
     */
    private void cargarDatosDiaRevision(){
        diasData = FXCollections.observableArrayList();
        int dias = ExcelManager.getDiasRevision();
        diasData.add(new DiaRevision(dias));
        tablaDiasRevision.setItems(diasData);
    }

    /**
     * Abre un diálogo para modificar el número de días y actualiza el Excel.
     */
    @FXML
    private void onModificarDias(){
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Modificar días entre revisiones");
        dialog.setHeaderText("Introduce el nuevo número de días:");

        TextField inputField = new TextField();
        inputField.setPromptText("Ej: 365");
        inputField.setText(String.valueOf(diasData.get(0).getDias()));

        dialog.getDialogPane().setContent(inputField);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    return Integer.parseInt(inputField.getText().trim());
                } catch (NumberFormatException e) {
                    mainController.showAlert("Por favor, introduce un número válido.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(nuevoValor -> {
            if (nuevoValor != null && nuevoValor > 0) {
                // Actualizar en Excel (sobrescribir fila 1)
                ExcelManager.añadirFila("PARAM_DIAS_REVISION", String.valueOf(nuevoValor));

                // Actualizar modelo y tabla
                diasData.get(0).setDias(nuevoValor);

                mainController.showAlert("Parámetro actualizado correctamente.");
            } else {
                mainController.showAlert("El número de días debe ser mayor que 0.");
            }
        });
    }

}
