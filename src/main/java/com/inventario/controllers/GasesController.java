package com.inventario.controllers;

import com.inventario.models.Estado;
import com.inventario.models.Gas;
import com.inventario.utils.ExcelManager;
import com.inventario.utils.FilterUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;


public class GasesController {

    @FXML
    private TableView<Gas> tablaGases;

    @FXML
    private TableColumn<Gas, String> colGas;
    @FXML
    private TableColumn<Gas, String> colPcg_Pca;
    @FXML
    private TableColumn<Gas, String> colFechaCaducidad;
    @FXML
    private TableColumn<Gas, String> colFechaRevision;
    @FXML
    private TableColumn<Gas, String> colObservaciones;

    @FXML
    private Button btnFiltroGas;
    @FXML
    private Button btnFiltroPcg_Pca;
    @FXML
    private Button btnFiltroFechaCad;
    @FXML
    private Button btnFiltroFechaRev;

    private MainAppController mainAppController;
    private ObservableList<Gas>gases;

    public void setMainAppController(MainAppController controller){
        this.mainAppController = controller;
    }

    public void initialize(){
        confColumnas();
        cargarDatos();
        noOrdenar();
    }

    public void confColumnas(){
        colGas.setCellValueFactory(new PropertyValueFactory<>("gas"));
        colPcg_Pca.setCellValueFactory(new PropertyValueFactory<>("pcg_pca"));
        colFechaCaducidad.setCellValueFactory(new PropertyValueFactory<>("fechaCaducidad"));
        colFechaRevision.setCellValueFactory(new PropertyValueFactory<>("fechaRevision"));
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
    }

    private void cargarDatos(){
        gases = FXCollections.observableArrayList();
        List<List<String>> datos = ExcelManager.leerHoja("PARAM_GASES");
         for(int i=1; i<datos.size() ; i++){
             List<String> fila = datos.get(i);
             if (fila.isEmpty()) continue;
             while (fila.size() < 5){
                 fila.add("");
             }
             String gas = fila.get(0).trim();
             if(gas.isEmpty()) continue;

             try {
                 Double pcgPca = parseDouble(fila.get(1));
                 String fechaCad = fila.get(2).trim();
                 String fechaRev = fila.get(3).trim();
                 String observaciones = fila.get(4).trim();

                 gases.add(new Gas(gas, pcgPca, fechaCad, fechaRev, observaciones));
             } catch (Exception e) {
                 System.err.println("Error al cargar gas en fila " + i);
             }

         }
         tablaGases.setItems(gases);
    }

    @FXML
    public void onAddGases(){
        /**Dialog<Gas>dialog = crearDialogo(null);
        dialog.showAndWait().ifPresent(gas -> {
            tablaGases.getItems().add(gas);
            ExcelManager.añadirFila("PARAM_GASES", gas.getGas(), String.valueOf(gas.getPcg_pca()),gas.getFechaCaducidad(),gas.getFechaRevision(),gas.getObservaciones());
        });**/
        Stage stage = new Stage();
        stage.setTitle("Añadir Nuevo Gas");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(tablaGases.getScene().getWindow());

        TextField textGas = new TextField();
        TextField textPcgPca = new TextField();
        DatePicker dateCaducidad = new DatePicker();
        DatePicker dateRevision = new DatePicker();
        TextField textObservacion = new TextField();
        textObservacion.setPrefHeight(60);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8); grid.setPadding(new Insets(15));
        int row = 0;

        grid.add(new Label("Gas:"), 0, row); grid.add(textGas, 1, row++);
        grid.add(new Label("PCG/PCA:"), 0, row); grid.add(textPcgPca, 1, row++);
        grid.add(new Label("Fecha Caducidad:"), 0, row); grid.add(dateCaducidad, 1, row++);
        grid.add(new Label("Fecha Revisión:"), 0, row); grid.add(dateRevision, 1, row++);
        grid.add(new Label("Observaciones:"), 0, row); grid.add(textObservacion, 1, row++);

        Button btnGuardar = new Button("Guardar");
        Button btnCancelar = new Button("Cancelar");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        btnGuardar.setOnAction(e -> {
            String gas = textGas.getText().trim();
            if (gas.isEmpty()) {
                mainAppController.showAlert("El campo 'Gas' es obligatorio.");
                return;
            }
            Double pcgPca = parseDouble(textPcgPca.getText());
            String fechaCad = dateCaducidad.getValue() != null ? dateCaducidad.getValue().format(fmt) : "";
            String fechaRev = dateRevision.getValue() != null ? dateRevision.getValue().format(fmt) : "";
            String obs = textObservacion.getText().trim();

            try{
                List<String> filaNueva = Arrays.asList(
                        gas,
                        pcgPca != null ? String.valueOf(pcgPca) : "",
                        fechaCad,
                        fechaRev,
                        obs
                );
                ExcelManager.añadirFila("PARAM_GASES", filaNueva.toArray(new String[0]));
                cargarDatos();
                stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                mainAppController.showAlert("Error al guardar.");
            }
        });

        btnCancelar.setOnAction(e -> stage.close());
        HBox botones = new HBox(10, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER);
        grid.add(botones, 1, ++row);

        stage.setScene(new Scene(grid, 400, 350));
        stage.show();
    }

    @FXML
    public void onEditGases(){
        Gas seleccionado = tablaGases.getSelectionModel().getSelectedItem();
        if(seleccionado == null){
            mainAppController.showAlert("Selecciona un gas para modificar.");
            return;
        }
       /** Dialog<Gas>dialog = crearDialogo(seleccionado);
        dialog.showAndWait().ifPresent(nuevo -> {
            List<List<String>> datos = ExcelManager.leerHoja("PARAM_GASES");
            int index = -1;
            for (int i = 1; i < datos.size(); i++) {
                List<String> fila = datos.get(i);
                // Comparar por la primera columna (GAS)
                if (fila.size() > 0 && fila.get(0).trim().equals(seleccionado.getGas())) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                // Usar modificarFilaPorIndice
                ExcelManager.modificarFila("PARAM_GASES", index, new String[]{nuevo.getGas(), nuevo.getPcg_pca() != null ? String.valueOf(nuevo.getPcg_pca()) : "", nuevo.getFechaCaducidad(), nuevo.getFechaRevision(), nuevo.getObservaciones()}
                );
                seleccionado.setGas(nuevo.getGas());
                seleccionado.setPcg_pca(nuevo.getPcg_pca());
                seleccionado.setFechaCaducidad(nuevo.getFechaCaducidad());
                seleccionado.setFechaRevision(nuevo.getFechaRevision());
                seleccionado.setObservaciones(nuevo.getObservaciones());
                tablaGases.refresh();
            } else {
                mainAppController.showAlert("No se encontró el gas en el archivo.");
            }
        });**/
        Stage stage = new Stage();
        stage.setTitle("Modificar Gas");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(tablaGases.getScene().getWindow());

        TextField textGas = new TextField(seleccionado.getGas());
        TextField textPcgPca = new TextField(seleccionado.getPcg_pca() != null ? String.valueOf(seleccionado.getPcg_pca()) : "");
        DatePicker dateCaducidad = new DatePicker();
        DatePicker dateRevision = new DatePicker();
        TextField textObservacion = new TextField(seleccionado.getObservaciones());

        setDatePicker(dateCaducidad, seleccionado.getFechaCaducidad());
        setDatePicker(dateRevision, seleccionado.getFechaRevision());

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8); grid.setPadding(new Insets(15));
        int row = 0;

        grid.add(new Label("Gas:"), 0, row); grid.add(textGas, 1, row++);
        grid.add(new Label("PCG/PCA:"), 0, row); grid.add(textPcgPca, 1, row++);
        grid.add(new Label("Fecha Caducidad:"), 0, row); grid.add(dateCaducidad, 1, row++);
        grid.add(new Label("Fecha Revisión:"), 0, row); grid.add(dateRevision, 1, row++);
        grid.add(new Label("Observaciones:"), 0, row); grid.add(textObservacion, 1, row++);

        Button btnGuardar = new Button("Guardar");
        Button btnCancelar = new Button("Cancelar");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        btnGuardar.setOnAction(e -> {
            String gas = textGas.getText().trim();
            if (gas.isEmpty()) {
                mainAppController.showAlert("El campo 'Gas' es obligatorio.");
                return;
            }
            Double pcgPca = parseDouble(textPcgPca.getText());
            String fechaCad = dateCaducidad.getValue() != null ? dateCaducidad.getValue().format(fmt) : "";
            String fechaRev = dateRevision.getValue() != null ? dateRevision.getValue().format(fmt) : "";
            String obs = textObservacion.getText().trim();

            List<List<String>> datos = ExcelManager.leerHoja("PARAM_GASES");
            int index = -1;

            for (int i = 1; i < datos.size(); i++){
                if (datos.get(i).size() > 0 && datos.get(i).get(0).trim().equals(seleccionado.getGas())) {
                    index = i;
                    break;
                }
            }

            if(index != -1){
                List<String> filaNueva = Arrays.asList(
                        gas,
                        pcgPca != null ? String.valueOf(pcgPca) : "",
                        fechaCad,
                        fechaRev,
                        obs
                );
                ExcelManager.modificarFila("PARAM_GASES", index , filaNueva.toArray(new String[0]));
                cargarDatos();
                stage.close();
            }else{
                mainAppController.showAlert("No se encontro el Gas en el archivo.");
            }
        });

        btnCancelar.setOnAction(e -> stage.close());
        HBox botones = new HBox(10, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER);
        grid.add(botones, 1, ++row);

        stage.setScene(new Scene(grid, 400, 350));
        stage.show();
    }

    @FXML
    public void onDeleteGases(){
        Gas seleccionado = tablaGases.getSelectionModel().getSelectedItem();
        if(seleccionado == null){
            mainAppController.showAlert("Selecciona un gas para eliminar.");
            return;
        }

        String gasAEliminar = seleccionado.getGas();
        if(ExcelManager.existParametroEnCondensadoras(gasAEliminar,ExcelManager.Columnas.GAS)){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Esta acción no está permitida");
            alert.setHeaderText("No se puede eliminar este gas");
            alert.setContentText("El gas '" +gasAEliminar+ "' no se puede eliminar porque está siendo usada en la hoja 'Condensadoras'.");
            alert.showAndWait();
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar que lo quieres eliminar");
        confirmacion.setHeaderText("¿Estas seguro que deseas eliminarlo?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if(respuesta == ButtonType.OK){
                tablaGases.getItems().remove(seleccionado);
                ExcelManager.eliminarFila("PARAM_GASES", gasAEliminar);
            }
        });
    }

    /**private Dialog<Gas>crearDialogo(Gas gas){
        Dialog<Gas>dialog = new Dialog<>();
        dialog.setTitle(gas == null ? "➕ Añadir Gas" : "✏️ Modificar Gas");
        dialog.setHeaderText(null);

        TextField gasField = new TextField();
        TextField descField = new TextField();

        gasField.setPromptText("Gas");
        descField.setPromptText("Descripcion");

        if(gas != null){
            gasField.setText(gas.getGas());
            descField.setText(gas.getDescripcion());
        }

        VBox vBox = new VBox(10, new Label("Gas:"),gasField, new Label("Descripción:"), descField);
        vBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(vBox);
        ButtonType añadirBoton = new ButtonType(gas == null? "Añadir":"Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(añadirBoton,ButtonType.CANCEL);

        dialog.setResultConverter(botonDialog -> {
            if(botonDialog == añadirBoton) {
                return new Gas(gasField.getText(), descField.getText());
            }
            return null;
        });
        return dialog;
    }**/

    private Double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(s.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setDatePicker(DatePicker datePicker, String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) return;
        try {
            LocalDate d = LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            datePicker.setValue(d);
        } catch (Exception ignored) {}
    }

    @FXML
    private void configurarFiltroGas(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Gas", Gas::getGas,btnFiltroGas,tablaGases,gases);
    }

    @FXML
    private void configurarFiltroPcg_Pca(){
        FilterUtils.abrirFiltroGenerico("Filtrar por PCG/PCA", gas -> gas.getPcg_pca() != null ? String.valueOf(gas.getPcg_pca()): "", btnFiltroPcg_Pca,tablaGases,gases);
    }

    @FXML
    private void configurarFiltroFechaCad(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Fecha de Caducidad", Gas::getFechaCaducidad, btnFiltroFechaCad,tablaGases,gases);
    }

    @FXML
    private void configurarFiltroFechaRev(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Fecha de Revision", Gas::getFechaRevision, btnFiltroFechaRev,tablaGases,gases);
    }

    private void noOrdenar(){
        colGas.setSortable(false);
        colPcg_Pca.setSortable(false);
        colFechaCaducidad.setSortable(false);
        colFechaRevision.setSortable(false);
        colObservaciones.setSortable(false);
    }
}
