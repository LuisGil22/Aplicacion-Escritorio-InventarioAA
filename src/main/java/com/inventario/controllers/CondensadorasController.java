package com.inventario.controllers;

import com.inventario.models.Condensadora;
import com.inventario.utils.ExcelManager;
import com.inventario.utils.FilterUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 ** Controlador para la gestión de condensadoras en la hoja Condensadoras del inventario.
 *  <p>
 *   Proporciona funcionalidades para:
 *  </p>
 *  <ul>
 *     <li>Visualizar y editar condensadoras</li>
 *     <li>Filtrar por cualquier columna mediante el sistema genérico de filtros</li>
 *     <li>Gestionar automáticamente averías al cambiar el estado a AVERIADO/ACTIVA</li>
 *     <li>Validar duplicados (CONDENSADORA + NUM_SECUENCIA)</li>
 *     <li>Prevenir eliminación si la condensadora está en uso en Cassette</li>
 *  </ul>
 *
 *  @author Luis Gil
 */
public class CondensadorasController {

    /* Campos FXML */
    @FXML private TableView<Condensadora> tablaCondensadoras;
    @FXML private TableColumn<Condensadora,String>colCondensadoras;
    @FXML private TableColumn<Condensadora,Integer>colNumSecuencia;
    @FXML private TableColumn<Condensadora,String>colEstado;
    @FXML private TableColumn<Condensadora,String>colMarca;
    @FXML private TableColumn<Condensadora,String>colModelo;
    @FXML private TableColumn<Condensadora,Long>colNumSerie;
    @FXML private TableColumn<Condensadora,String>colLocCondensadoras;
    @FXML private TableColumn<Condensadora,String>colGas;
    @FXML private TableColumn<Condensadora,String>colFechaInstal;
    @FXML private TableColumn<Condensadora, String> colFechaBaja;
    @FXML private TableColumn<Condensadora,String>colFechaRev;
    @FXML private TableColumn<Condensadora,String>colAveria;
    @FXML private TableColumn<Condensadora,String>colObservaciones;


    @FXML private Button btnFiltroCondensadora;
    @FXML private Button btnFiltroNumSecuencia;
    @FXML private Button btnFiltroEstado;
    @FXML private Button btnFiltroMarca;
    @FXML private Button btnFiltroModelo;
    @FXML private Button btnFiltroNumSerie;
    @FXML private Button btnFiltroLocCondensadoras;
    @FXML private Button btnFiltroGas;
    @FXML private Button btnFiltroFechaInst;
    @FXML private Button btnFiltroFechaBaja;
    @FXML private Button btnFiltroFechaRev;

    /** Dependencias  */
    private MainAppController mainAppController;
    private ObservableList<Condensadora> allDatos;

    /**
     * Establece la dependencia con el controlador principal de la aplicación.
     *
     * @param mainAppController instancia del controlador principal
     */
    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    /**
     * Método que inicializa el controlador al cargar la vista FXML.
     * Configura columnas, carga datos y desactiva ordenación.
     */
    @FXML
    public void initialize(){
        confColumnas();
        cargarDatos();
        noOrdenar();
    }

    /**
     * Método que configura las columnas de la tabla Condensadoras.
     */
    private void confColumnas(){
        colCondensadoras.setCellValueFactory(new PropertyValueFactory<>("condensadora"));
        colNumSecuencia.setCellValueFactory(new PropertyValueFactory<>("numSecuencia"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colNumSerie.setCellValueFactory(new PropertyValueFactory<>("numSerieCond"));
        colLocCondensadoras.setCellValueFactory(new PropertyValueFactory<>("loc_condensadora"));
        colGas.setCellValueFactory(new PropertyValueFactory<>("gas"));
        colFechaInstal.setCellValueFactory(new PropertyValueFactory<>("fechaInstalacion"));
        colFechaBaja.setCellValueFactory(new PropertyValueFactory<>("fechaBaja"));
        colFechaRev.setCellValueFactory(new PropertyValueFactory<>("fechaRevision"));
        colAveria.setCellValueFactory(new PropertyValueFactory<>("averia"));
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
    }

    /**
     * Método para cargar los datos de la hoja Condensadoras del archivo Excel y los muestra en la tabla.
     * Omite filas vacías y valida el formato de los datos antes de crear objetos Condensadora.
     */
    private void cargarDatos() {
        allDatos = FXCollections.observableArrayList();
        List<List<String>> datos = ExcelManager.leerHoja("Condensadoras");

        for (int i = 1; i < datos.size(); i++) {
            List<String> fila = datos.get(i);
            //System.out.println("Procesando fila " + i  + ": " + fila);

            if (fila.isEmpty()) continue;


            if (fila.size() < 13) {
                while (fila.size() < 13) {
                    fila.add("");
                }
            }

            String condensadora = fila.get(0).trim();
            String numSecuenciaStr = fila.get(1).trim();
            String estado = fila.get(2).trim();
            String marca = fila.get(3).trim();
            String modelo = fila.get(4).trim();
            String numSerieStr = fila.get(5).trim();
            String loc = fila.get(6).trim();
            String gas = fila.get(7).trim();
            String fechaInst = fila.get(8).trim();
            String fechaBaja = fila.get(9).trim();
            String fechaRev = fila.get(10).trim();
            String averia = fila.get(11).trim();
            String observaciones = fila.get(12).trim();


            if (condensadora.isEmpty() && estado.isEmpty()) {
                continue;
            }
            try {

                Integer numSecuencia = (Integer) (numSecuenciaStr.trim().isEmpty() ? 1 : parseInt(numSecuenciaStr));
                Long numSerie = parseLong(numSerieStr);

                allDatos.add(new Condensadora(
                        condensadora,
                        numSecuencia,
                        estado,
                        marca,
                        modelo,
                        numSerie,
                        loc,
                        gas,
                        fechaInst,
                        fechaBaja,
                        fechaRev,
                        averia,
                        observaciones
                ));
            }catch (Exception e){
                //System.err.println("Error fila " + i + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        tablaCondensadoras.setItems(allDatos);
        //System.out.println("Cargados " + allDatos.size() + " registros de Condensadoras");

        Platform.runLater(() -> {
            Node scrollNode = tablaCondensadoras.lookup(".scroll-pane");
            if (scrollNode instanceof ScrollPane sp) {
                sp.setHvalue(0.0);
            }
        });
    }

    /**
     * Métodos para configurar los filtros en las columnas de la tabla Condensadoras.
     */
    @FXML
    private void configurarFiltroCondensadora(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Condensadora", Condensadora::getCondensadora,btnFiltroCondensadora,tablaCondensadoras,allDatos);
    }

    @FXML
    private void configurarFiltroNumSecuencia(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Numero de Secuencia", item -> String.valueOf(item.getNumSecuencia()),btnFiltroNumSecuencia,tablaCondensadoras,allDatos);
    }

    @FXML
    private void configurarFiltroEstado(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Estado", Condensadora::getEstado,btnFiltroEstado,tablaCondensadoras,allDatos);
    }

    @FXML
    private void configurarFiltroMarca(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Marca", Condensadora::getMarca,btnFiltroMarca,tablaCondensadoras,allDatos);
    }

    @FXML
    private void configurarFiltroModelo(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Modelo", Condensadora::getModelo,btnFiltroModelo,tablaCondensadoras,allDatos);
    }

    @FXML
    private void configurarFiltroNumSerie(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Numero de Serie", item -> String.valueOf(item.getNumSerieCond()),btnFiltroNumSerie,tablaCondensadoras,allDatos);
    }

    @FXML
    private void configurarFiltroLocCondensadoras(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Localización de Condensadora", Condensadora::getLoc_condensadora,btnFiltroLocCondensadoras,tablaCondensadoras,allDatos);
    }

    @FXML
    private void configurarFiltroGas(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Gas", Condensadora::getGas,btnFiltroGas,tablaCondensadoras,allDatos);
    }

    @FXML
    private void configurarFiltroFechaInst(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Fecha de Instalación", Condensadora::getFechaInstalacion,btnFiltroFechaInst,tablaCondensadoras,allDatos);
    }

    @FXML
    private void configurarFiltroFechaBaja(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Fecha de Baja", Condensadora::getFechaBaja,btnFiltroFechaBaja,tablaCondensadoras,allDatos);
    }

    @FXML
    private void configurarFiltroFechaRev(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Fecha de Revisión", Condensadora::getFechaRevision,btnFiltroFechaRev,tablaCondensadoras,allDatos);
    }

    /**
     * Metodo para convertir un String a entero de forma segura.
     * Devuelve 1 si el valor es nulo, vacío o no es numérico.
     *
     * @param dato String a convertir
     * @return valor entero (mínimo 1)
     */
    private int parseInt(String dato) {
        try {
            return dato == null || dato.trim().isEmpty() ? 1 : Integer.parseInt(dato.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    /**
     * Metodo para convertir un String a long de forma segura.
     * Devuelve 0L si el valor es nulo, vacío o no es numérico.
     *
     * @param dato String a convertir
     * @return valor long
     */
    private long parseLong(String dato){
        try {
            return dato.isEmpty()? 0L : Long.parseLong(dato);
        }catch (NumberFormatException e){
            return 0L;
        }
    }

    /**
     * Metodo para abrir el formulario y añadir una nueva condensadora.
     */
    @FXML
    private void onAddCond(){
        onAddForm(null);
    }

    /**
     * Metodo para abrir el formulario para añadir o modificar condensadoras.
     * Precarga datos si se está modificando un registro existente.
     *
     * @param editar si es null se abre formulario para añadir condensadora y
     *               si no es null para modificar condensadora seleccionada.
     */
    private void onAddForm(Condensadora editar){
        Stage stage = new Stage();
        stage.setTitle(editar == null ? "Añadir Nueva Condensadora" : "Modificar Condensadora");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(tablaCondensadoras.getScene().getWindow());

        /**Campos de texto*/
        TextField textCondensadora = new TextField();
        Label avisoRellenar = new Label("campo Obligarorio");
        avisoRellenar.setTextFill(Color.RED);
        avisoRellenar.setVisible(false);
        if (editar != null) {
            textCondensadora.setText(editar.getCondensadora());
            textCondensadora.setEditable(false);
        }else{
            textCondensadora.textProperty().addListener((obs, oldText, newText) -> {
                if (!newText.equals(newText.toUpperCase())) {
                    textCondensadora.setText(newText.toUpperCase());
                }
            });
        }


        ComboBox<String> comboEstado = new ComboBox<>(FXCollections.observableArrayList(cargarParametrosExcel("PARAM_ESTADO")));
        ComboBox<String> comboMarca = new ComboBox<>(FXCollections.observableArrayList(cargarParametrosExcel("PARAM_MARCAS")));
        ComboBox<String> comboModelo = new ComboBox<>(FXCollections.observableArrayList(cargarParametrosExcel("PARAM_MODELOS_COND")));
        TextField textNumSerie = new TextField();
        ComboBox<String> comLocCondensadora = new ComboBox<>(FXCollections.observableArrayList(cargarParametrosExcel("PARAM_LOC_COND")));
        ComboBox<String> comboGas = new ComboBox<>(FXCollections.observableArrayList(cargarParametrosExcel("PARAM_GASES")));
        DatePicker dateFechaInst = new DatePicker();
        DatePicker dateFechaBaja = new DatePicker();
        DatePicker dateFechaRev = new DatePicker();
        TextField textAveria = new TextField();
        TextField textObservacion = new TextField();
        textObservacion.setPrefHeight(60);

        /** precargar los datos para modificar.*/

        if(editar != null){

            if(comboEstado.getItems().contains(editar.getEstado())){
                comboEstado.setValue(editar.getEstado());
            }
            if(comboMarca.getItems().contains(editar.getMarca())){
                comboMarca.setValue(editar.getMarca());
            }
            if(comboModelo.getItems().contains(editar.getModelo())){
                comboModelo.setValue(editar.getModelo());
            }
            if (editar.getNumSerieCond() != null) {
                textNumSerie.setText(String.valueOf(editar.getNumSerieCond()));
            }
            if(comLocCondensadora.getItems().contains(editar.getLoc_condensadora())){
                comLocCondensadora.setValue(editar.getLoc_condensadora());
            }
            if(comboGas.getItems().contains(editar.getGas())){
                comboGas.setValue(editar.getGas());
            }
            setDatePicker(dateFechaInst, editar.getFechaInstalacion());
            setDatePicker(dateFechaBaja, editar.getFechaBaja());
            setDatePicker(dateFechaRev, editar.getFechaRevision());
            textAveria.setText(editar.getAveria());
            textObservacion.setText(editar.getObservaciones());
        }

        /** Creacion del formulario.*/
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(8);
        gridPane.setPadding(new Insets(15));

        int row = 0;

        gridPane.add(new Label("Condensadora: "), 0 , row);
        gridPane.add(textCondensadora, 1, row);
        gridPane.add(avisoRellenar, 2, row++);
        gridPane.add(new Label("Estado: "), 0, row);
        gridPane.add(comboEstado, 1, row++);
        gridPane.add(new Label("Marca: "), 0, row);
        gridPane.add(comboMarca, 1, row++);
        gridPane.add(new Label("Modelo: "), 0, row);
        gridPane.add(comboModelo, 1, row++);
        gridPane.add(new Label("num.Serie: "), 0 , row);
        gridPane.add(textNumSerie, 1, row++);
        gridPane.add(new Label("Loc-Condensadora: "), 0, row);
        gridPane.add(comLocCondensadora, 1, row++);
        gridPane.add(new Label("Gas: "), 0, row);
        gridPane.add(comboGas, 1, row++);
        gridPane.add(new Label("Fecha Instalación: "), 0 , row);
        gridPane.add(dateFechaInst, 1, row++);
        gridPane.add(new Label("Fecha Baja: "), 0 , row);
        gridPane.add(dateFechaBaja, 1, row++);
        gridPane.add(new Label("Fecha Revisión: "), 0 , row);
        gridPane.add(dateFechaRev, 1, row++);
        gridPane.add(new Label("Averia: "), 0 , row);
        gridPane.add(textAveria, 1, row++);
        gridPane.add(new Label("Observación: "), 0 , row);
        gridPane.add(textObservacion, 1, row++);

        /** Creacion de los botones guardar y cancelar.*/
        Button btnGuardar = new Button("Guardar");
        Button btnCancelar = new Button("Cancelar");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        btnGuardar.setOnAction(e ->{
            String condensadora = textCondensadora.getText().trim().toUpperCase();
            if(condensadora.isEmpty()){
                avisoRellenar.setVisible(true);
                //mainAppController.showAlert("El campo Condensadora es obligatorio.");
                return;
            }

            Long numSerie = null;
            if (!textNumSerie.getText().trim().isEmpty()) {
                try {
                    numSerie = Long.parseLong(textNumSerie.getText().trim());
                } catch (NumberFormatException ex) {
                    mainAppController.showAlert("El Nº Serie debe ser un número válido.");
                    return;
                }
            }
            /** Formatear fecha o texto.*/
            String fechaInst = dateFechaInst.getValue() != null? dateFechaInst.getValue().format(formatter) : "";
            String fechaBaja = dateFechaBaja.getValue() != null? dateFechaBaja.getValue().format(formatter) : "";
            String fechaRev = dateFechaRev.getValue() != null? dateFechaRev.getValue().format(formatter) : "";
            String estado = comboEstado.getValue() != null? comboEstado.getValue() : "";

            try {
                int numSecuencia = 1;
                if(editar == null){
                    for(Condensadora cond : allDatos){
                        if(cond.getCondensadora().equals(condensadora)){
                            mainAppController.showAlert("La condensadora " + condensadora + " ya existe, Pero puedes sustituirla \n clicando en el boton Sustituir");
                            return;
                            /**if(cond.getNumSecuencia() >= numSecuencia){
                                numSecuencia = cond.getNumSecuencia() + 1;
                            }*/
                        }
                    }
                }else{
                    numSecuencia = editar.getNumSecuencia();
                }
                String numSerieExcel = numSerie != null ? String.valueOf(numSerie) : "";

                List<String>filaNueva = Arrays.asList(
                        condensadora,
                        String.valueOf(numSecuencia),
                        estado,
                        comboMarca.getValue() != null? comboMarca.getValue() : "",
                        comboModelo.getValue() != null? comboModelo.getValue() : "",
                        numSerieExcel,
                        comLocCondensadora.getValue() != null? comLocCondensadora.getValue() : "",
                        comboGas.getValue() != null? comboGas.getValue() : "",
                        fechaInst,
                        fechaBaja,
                        fechaRev,
                        "",
                        textObservacion.getText().trim()
                );
                boolean esNuevo = (editar == null);
                int indexExcel = -1;

                if(esNuevo){
                    ExcelManager.añadirFilaOrdenada("Condensadoras", filaNueva.toArray(new String[0]));
                    //cargarDatos();
                }else{
                    List<List<String>> datos = ExcelManager.leerHoja("Condensadoras");

                    for (int i = 1; i < datos.size(); i++) {
                        List<String> fila = datos.get(i);
                        if (fila.size() > 1 && fila.get(0).trim().equals(editar.getCondensadora()) && fila.get(1).trim().equals(String.valueOf(editar.getNumSecuencia()))){
                            indexExcel = i;
                            break;
                        }
                    }
                    if (indexExcel != -1){
                        ExcelManager.modificarFila("Condensadoras", indexExcel, filaNueva.toArray(new String[0]));
                        //cargarDatos();
                    }
                }

                /** Gestión Automática de Averias.*/
                String estadoAnterior = null;
                if (!esNuevo) {
                    List<List<String>> datosCond = ExcelManager.leerHoja("Condensadoras");

                    for (int i = 1; i < datosCond.size(); i++) {
                        List<String> fila = datosCond.get(i);
                        //System.out.println("Comparando: '" + fila.get(0) + "' vs '" + condensadora + "'");
                        if (fila.size() > 0 && fila.get(0).trim().equals(textCondensadora) ){
                            estadoAnterior = fila.get(2).trim(); // columna ESTADO
                            break;
                        }
                    }
                    if(estadoAnterior == null || estadoAnterior.isEmpty()){
                        estadoAnterior = editar.getEstado();
                    }
                }

                String numAveria = "";
                String estadoActual = comboEstado.getValue() != null ? comboEstado.getValue() : "";
                //System.out.println(" estadoAnterior = '" + estadoAnterior + "'");
                //System.out.println(" estadoActual = '" + estadoActual + "'");
                /** Si cambia de ACTIVA a AVERIADO crea una averia.*/
                if ("ACTIVA".equals(estadoAnterior) && "AVERIADO".equals(estadoActual)) {
                    //System.out.println("¡ENTRA EN EL IF!");
                    List<List<String>> averias = ExcelManager.leerHoja("AVERIAS");
                    int maxNum = 0;
                    for (int i = 1; i < averias.size(); i++) {
                        if (!averias.get(i).isEmpty()) {
                            String numStr = averias.get(i).get(0).trim();
                            try {
                                int n = Integer.parseInt(numStr);
                                if (n > maxNum) maxNum = n;
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                    numAveria = String.format("%04d", maxNum + 1);
                    String planta = "";
                    String localizacion = comLocCondensadora.getValue() != null ? comLocCondensadora.getValue() : "";
                    String observaciones = textObservacion.getText().trim();
                    ExcelManager.registrarAveriaAutomaticamente("CONDENSADORA",condensadora,planta, localizacion, observaciones, "Condensadoras", numAveria);
                }

                /** Si cambia de AVERIADO a ACTIVA la columna estado marca REPARADO.*/
                if ("AVERIADO".equals(estadoAnterior) && "ACTIVA".equals(estadoActual)) {
                    ExcelManager.marcarAveriaComoReparada("CONDENSADORA", condensadora);
                }

                Condensadora actualizada = new Condensadora(
                        condensadora,
                        //spiNumSecuencia.getValue(),
                        numSecuencia,
                        estadoActual,
                        comboMarca.getValue() != null ? comboMarca.getValue() : "",
                        comboModelo.getValue() != null ? comboModelo.getValue() : "",
                        numSerie,
                        comLocCondensadora.getValue() != null ? comLocCondensadora.getValue() : "",
                        comboGas.getValue() != null ? comboGas.getValue() : "",
                        fechaInst,
                        fechaBaja,
                        fechaRev,
                        numAveria,
                        textObservacion.getText().trim()
                );
                if (esNuevo) {
                    //allDatos.add(actualizada);
                } else {
                    int pos = allDatos.indexOf(editar);
                    if (pos != -1) {
                        allDatos.set(pos, actualizada);
                    }
                }
                stage.close();

                new Thread(() -> {
                    try {
                        ExcelManager.calcularYActualizarRevisionIndividual("CONDENSADORA", condensadora, fechaInst); //  Primero escribe en Excel
                        Platform.runLater(() -> {
                            cargarDatos(); //  Luego recarga la tabla
                            tablaCondensadoras.refresh();

                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();

               /** Platform.runLater(() -> {
                    cargarDatos();
                    new Thread(() -> {
                        try {
                            ExcelManager.calcularYActualizarTodasLasRevisiones();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                    tablaCondensadoras.refresh();
                    stage.close();
                });*/

            } catch (Exception ex) {
                ex.printStackTrace();
                mainAppController.showAlert("Error al guardar los cambios.");

            }
        });
        btnCancelar.setOnAction(e -> stage.close());

        HBox botones = new HBox(10, btnGuardar,btnCancelar);
        botones.setAlignment(Pos.CENTER);
        gridPane.add(botones, 1, ++row);

        Scene scene = new Scene(gridPane,500,600);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Metodo para establecer la fecha en un DatePicker a partir de un String con formato dd/MM/yyyy.
     *
     * @param picker DatePicker a configurar
     * @param fechaStr fecha en formato String (dd/MM/yyyy)
     */
    private void setDatePicker(DatePicker picker, String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) return;
        try {
            LocalDate d = LocalDate.parse(fechaStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            picker.setValue(d);
        } catch (Exception ignored) {}
    }

    /**
     * Metodo para verificar si existe una condensadora duplicada con la misma clave compuesta (CONDENSADORA + NUM_SECUENCIA).
     *
     * @param condensadora código de la condensadora
     * @param numSecuencia número de secuencia
     * @return true si existe duplicado, false en caso contrario
     */
    private boolean existeCondensadoraDuplicada(String condensadora, int numSecuencia) {
        for (Condensadora c : allDatos) {
            if (c.getCondensadora().equals(condensadora) && c.getNumSecuencia() == numSecuencia) {
                return true;
            }
        }
        return false;
    }

    /**
     * Metodo para abrir el formulario y modificar la condensadora seleccionada.
     * Valida que haya una selección activa antes de abrir el formulario.
     */
    @FXML
    public void onEditCond(){
        Condensadora seleccionada = tablaCondensadoras.getSelectionModel().getSelectedItem();
        if(seleccionada == null){
            mainAppController.showAlert("Selecciona una condensadora para modificar.");
            return;
        }

        onAddForm(seleccionada);
    }

    /**
     * Metodo para eliminar la condensadora seleccionada tras confirmación.
     * Verifica que no esté en uso en la hoja Cassette antes de eliminar.
     */
    @FXML
    public void onDeleteCond(){
        Condensadora selected = tablaCondensadoras.getSelectionModel().getSelectedItem();
        if(selected == null){
            mainAppController.showAlert("Selecciona una condensadora para poder eliminarla");
            return;
        }
        /** Verificar si está en uso en Cassette.*/
        if (ExcelManager.existParametroEnCassettes(selected.getCondensadora(), "CONDENSADORA")) {
            mainAppController.showAlert("No se puede eliminar: esta condensadora está en uso en la hoja 'Cassette'.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar esta condensadora?");
        alert.setContentText("Esta acción no se puede deshacer.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                tablaCondensadoras.getItems().remove(selected);
                ExcelManager.eliminarFila("Condensadoras", selected.getCondensadora());
            }
        });
    }

    /**
     * Metodo para sustituir la condensadora seleccionada tras confirmación,
     * por otra creada con las mismas propiedades más significativas.
     * El estado de la sustituida será BAJA con la fecha actual en FECHA_BAJA.
     * El estado de la nueva será ACTIVA con fecha actual en FECHA_INSTALACION.
     */
    @FXML
    public void onSustituirCond(){
        Condensadora selected = tablaCondensadoras.getSelectionModel().getSelectedItem();
        if(selected == null){
            mainAppController.showAlert("Selecciona una condensadora para poder sustituirla");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar sustitución");
        alert.setHeaderText("Estas seguro de querer sustituir esta Condensadora?");
        alert.setContentText("Se dara de baja esta condensadora y se activara una nueva");
        alert.showAndWait().ifPresent(response -> {
            if(response == ButtonType.OK){
                try{
                    int secuenciaPorDefecto = 1;
                    for(Condensadora cond : allDatos){
                        if(cond.getCondensadora().equals(selected.getCondensadora())){
                            if(cond.getNumSecuencia() > secuenciaPorDefecto){
                                secuenciaPorDefecto = cond.getNumSecuencia();
                            }
                        }
                    }
                    int secuenciaNueva = secuenciaPorDefecto + 1;

                    /** Crear nueva Condensadora */
                    String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    List<String> filaNueva = Arrays.asList(
                        selected.getCondensadora(),
                        String.valueOf(secuenciaNueva),
                        "ACTIVA",
                        selected.getMarca(),
                        selected.getModelo(),
                        "",
                        selected.getLoc_condensadora(),
                        selected.getGas(),
                        fechaActual,
                        "",
                        "",
                        "",
                        ""
                    );
                    ExcelManager.añadirFilaOrdenada("Condensadoras", filaNueva.toArray(new String[0]));

                    /** Actualizar Condensadora seleccionada */
                    List<List<String>> hojaCond = ExcelManager.leerHoja("Condensadoras");
                    for(int i = 1; i < hojaCond.size(); i++){
                        List<String> filaCond = hojaCond.get(i);
                        if(filaCond.size() > 0 && filaCond.get(0).trim().equals(selected.getCondensadora())){
                            while(filaCond.size() < 10){
                                filaCond.add("");
                            }
                            filaCond.set(2, "BAJA");
                            filaCond.set(9, fechaActual);
                            ExcelManager.modificarFila("Condensadoras", i, filaCond.toArray(new String[0]));
                            break;
                        }
                    }
                    cargarDatos();
                    Platform.runLater(() -> {
                        tablaCondensadoras.refresh();
                    });
                    mainAppController.showAlert("Condensadora sustituida con éxito");
                } catch (Exception e) {
                    e.printStackTrace();
                    mainAppController.showAlert("Error al sustituir la Condensadora.");
                }
            }
        });

    }

    /**
     * Metodo para cargar los parámetros de una hoja específica del Excel (ej. PARAM_ESTADO, PARAM_MARCAS).
     * Omite valores vacíos y "en blanco".
     *
     * @param hoja nombre de la hoja de parámetros
     * @return lista de valores válidos
     */
    private List<String> cargarParametrosExcel(String hoja){
        List<String> lista = new ArrayList<>();
        try{
             List<List<String>> datos = ExcelManager.leerHoja(hoja);
            for(int i = 1; i<datos.size();i++){
                List<String> fila = datos.get(i);
                if(!fila.isEmpty() && fila.get(0) != null){
                    String valor = fila.get(0).trim();
                    if(!valor.isEmpty() && !valor.equalsIgnoreCase("en blanco")) {
                        lista.add(valor);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error al cargar: " + hoja);
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Metodo para desactivar la ordenación en todas las columnas de la tabla.
     * Mejora la estabilidad visual al trabajar con datos no ordenados.
     */
    private void noOrdenar(){
        //colCondensadoras.setSortable(false);
        colNumSecuencia.setSortable(false);
        colEstado.setSortable(false);
        colMarca.setSortable(false);
        colModelo.setSortable(false);
        colNumSerie.setSortable(false);
        colLocCondensadoras.setSortable(false);
        colGas.setSortable(false);
        colFechaInstal.setSortable(false);
        colFechaBaja.setSortable(false);
        colFechaRev.setSortable(false);
        colAveria.setSortable(false);
        colObservaciones.setSortable(false);
    }
}
