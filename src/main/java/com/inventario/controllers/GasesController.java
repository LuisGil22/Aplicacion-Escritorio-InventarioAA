package com.inventario.controllers;

import com.inventario.models.Gas;
import com.inventario.utils.ExcelManager;
import com.inventario.utils.FilterUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Controlador para la gestión de gases en la hoja PARAM_GASES del inventario.
 * <p>
 * Proporciona funcionalidades para:
 * </p>
 * <ul>
 *   <li>Visualizar gases registrados</li>
 *   <li>Añadir nuevos gases</li>
 *   <li>Modificar gases existentes</li>
 *   <li>Eliminar gases (con validación de uso en Condensadoras)</li>
 *   <li>Filtrar por cualquier columna</li>
 * </ul>
 *
 * @author Luis Gil
 */
public class GasesController {

    /** Campos FXML */
    @FXML private TableView<Gas> tablaGases;
    @FXML private TableColumn<Gas, String> colGas;
    @FXML private TableColumn<Gas, String> colPcg_Pca;
    @FXML private TableColumn<Gas, String> colFechaCaducidad;
    @FXML private TableColumn<Gas, String> colFechaRevision;
    @FXML private TableColumn<Gas, String> colObservaciones;

    @FXML private Button btnFiltroGas;
    @FXML private Button btnFiltroPcg_Pca;
    @FXML private Button btnFiltroFechaCad;
    @FXML private Button btnFiltroFechaRev;

    /** Dependencias */
    private MainAppController mainAppController;
    private ObservableList<Gas>gases;

    /**
     * Metodo para establecer la dependencia con el controlador principal de la aplicación.
     *
     * @param controller instancia del controlador principal
     */
    public void setMainAppController(MainAppController controller){
        this.mainAppController = controller;
    }

    /**
     * Metodo para inicializar el controlador al cargar la vista FXML.
     * Configura columnas, carga datos y desactiva ordenación.
     */
    public void initialize(){
        confColumnas();
        cargarDatos();
        noOrdenar();

        /** Configura el estilo y comportamiento de la columna
         *  de observaciones para permitir texto multilínea
         */
        if(colObservaciones != null) {
            colObservaciones.getStyleClass().add("col-observaciones");
            colObservaciones.setCellFactory(column -> new TableCell<Gas, String>() {
                private final Text text = new Text();
                {text.wrappingWidthProperty().bind(colObservaciones.widthProperty().subtract(10)); // Restar padding
                    text.setTextOrigin(VPos.TOP);}
                @Override
                protected void updateItem(String item, boolean empty) {
                    //System.out.println("Actualizando celda observaciones: " + item);
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        text.setText(item);
                        setGraphic(text);
                        setText(null);
                    }

                }
            });
            if (!colObservaciones.getStyleClass().contains("col-observaciones")) {
                colObservaciones.getStyleClass().add("col-observaciones");
            }
        }
    }

    /**
     * Metodo para configurar las columnas de la tabla Gas.
     */
    public void confColumnas(){
        colGas.setCellValueFactory(new PropertyValueFactory<>("gas"));
        colPcg_Pca.setCellValueFactory(new PropertyValueFactory<>("pcg_pca"));
        colFechaCaducidad.setCellValueFactory(new PropertyValueFactory<>("fechaCaducidad"));
        colFechaRevision.setCellValueFactory(new PropertyValueFactory<>("fechaRevision"));
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
    }

    /**
     * Metodo para cargar los datos de la hoja PARAM_GASES del archivo Excel y mostrarlos en la tabla.
     * Omite filas vacías o con valores nulos.
     * Valida el formato de los datos antes de crear objetos Gas.
     */
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

    /**
     * Metodo para abrir el formulario y añadir un nuevo gas.
     */
    @FXML
    public void onAddGases(){

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
                int newIndex = ExcelManager.obtenerIndiceFilaPorCodigo("PARAM_GASES", gas);
                if (newIndex != -1) {
                    ExcelManager.actualizarCeldaObservacionConEstilo("PARAM_GASES", newIndex, 4, obs);
                }
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

    /**
     * Metodo para abrir el formulario y modificar el gas seleccionado.
     * Valida que haya una selección activa antes de abrir el formulario.
     */
    @FXML
    public void onEditGases(){
        Gas seleccionado = tablaGases.getSelectionModel().getSelectedItem();
        if(seleccionado == null){
            mainAppController.showAlert("Selecciona un gas para modificar.");
            return;
        }

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
                int newIndex = ExcelManager.obtenerIndiceFilaPorCodigo("PARAM_GASES", gas);
                if (newIndex != -1) {
                    ExcelManager.actualizarCeldaObservacionConEstilo("PARAM_GASES", newIndex, 4, obs);
                }
                cargarDatos();
                tablaGases.refresh();
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

    /**
     * Metodo para eliminar el gas seleccionado tras confirmación.
     * Verifica que no esté en uso en la hoja Condensadoras.
     */
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

    /**
     * Metodo para convertir un String a Double de forma segura.
     * Limpia caracteres no numéricos y maneja coma decimal.
     *
     * @param s String a convertir
     * @return valor Double o null si no es válido
     */
    private Double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(s.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Metodo para establecer la fecha en un DatePicker a partir de un String con formato dd/MM/yyyy.
     *
     * @param datePicker DatePicker a configurar
     * @param fecha fecha en formato String (dd/MM/yyyy)
     */
    private void setDatePicker(DatePicker datePicker, String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) return;
        try {
            LocalDate d = LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            datePicker.setValue(d);
        } catch (Exception ignored) {}
    }

    /** Metodos para configurar los filtros de las columnas en la tabla Gas.*/
    @FXML
    private void configurarFiltroGas(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Gas", Gas::getGas,btnFiltroGas,tablaGases,gases,(ascending) -> {
            ObservableList<Gas> sorted = FXCollections.observableArrayList(gases);
            sorted.sort(Comparator.comparing(
                    Gas::getGas,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaGases.setItems(sorted);
        });
    }

    @FXML
    private void configurarFiltroPcg_Pca(){
        FilterUtils.abrirFiltroGenerico("Filtrar por PCG/PCA", gas -> gas.getPcg_pca() != null ? String.valueOf(gas.getPcg_pca()): "", btnFiltroPcg_Pca,tablaGases,gases,(ascending) -> {
            ObservableList<Gas> sorted = FXCollections.observableArrayList(gases);
            sorted.sort(Comparator.comparing(
                    item -> String.valueOf(item.getPcg_pca()),
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaGases.setItems(sorted);
        });
    }

    @FXML
    private void configurarFiltroFechaCad(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Fecha de Caducidad", Gas::getFechaCaducidad, btnFiltroFechaCad,tablaGases,gases,(ascending) -> {
            ObservableList<Gas> sorted = FXCollections.observableArrayList(gases);
            sorted.sort(Comparator.comparing(
                    Gas::getFechaCaducidad,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaGases.setItems(sorted);
        });
    }

    @FXML
    private void configurarFiltroFechaRev(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Fecha de Revision", Gas::getFechaRevision, btnFiltroFechaRev,tablaGases,gases,(ascending) -> {
            ObservableList<Gas> sorted = FXCollections.observableArrayList(gases);
            sorted.sort(Comparator.comparing(
                    Gas::getFechaRevision,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaGases.setItems(sorted);
        });
    }

    /**
     * Desactiva la ordenación en todas las columnas de la tabla.
     * Mejora la estabilidad visual al trabajar con datos no ordenados.
     */
    private void noOrdenar(){
        colGas.setSortable(false);
        colPcg_Pca.setSortable(false);
        colFechaCaducidad.setSortable(false);
        colFechaRevision.setSortable(false);
        colObservaciones.setSortable(false);
    }

    /**
     * Metodo para abrir el diálogo de observaciones al hacer clic en una celda de la columna de observaciones.
     * Permite editar las observaciones y guarda los cambios tanto en memoria como en el archivo Excel.
     */
    @FXML
    private void abrirObservacionesGas() {
        Gas selected = tablaGases.getSelectionModel().getSelectedItem();
        if (selected == null){
            mainAppController.showAlert("Selecciona un Gas para crear las observaciones");
            return;
        }
        //System.out.println("Buscando en Excel -> Hoja: PARAM_GASES, Codigo: " + selected.getGas());
        mainAppController.abrirDialogoObservaciones(
                "Observaciones - GAS " + selected.getGas(),
                selected.getObservaciones(),
                nuevaObs -> {
                    selected.setObservaciones(nuevaObs);

                    int indiceFila = ExcelManager.obtenerIndiceFilaPorCodigo(
                            "PARAM_GASES",
                            selected.getGas()
                    );

                    if (indiceFila != -1) {
                        ExcelManager.actualizarCeldaObservacionConEstilo("PARAM_GASES", indiceFila, 4, nuevaObs);
                    }else{
                        System.err.println("Error: No se encontró la fila en Excel para " + selected.getGas());
                    }
                    tablaGases.refresh();
                }
        );
    }
}
