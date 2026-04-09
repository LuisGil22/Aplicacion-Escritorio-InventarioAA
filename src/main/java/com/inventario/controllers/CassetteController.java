package com.inventario.controllers;

import com.inventario.models.Cassette;
import com.inventario.utils.ExcelManager;
import com.inventario.utils.FilterUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
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
 * Controlador para la gestión de cassettes en la hoja Cassette del inventario.
 * <p>
 * Proporciona funcionalidades para:
 * </p>
 * <ul>
 *   <li>Visualizar y editar cassettes</li>
 *   <li>Filtrar por cualquier columna mediante el sistema genérico de filtros</li>
 *   <li>Gestionar automáticamente averías al cambiar el estado a AVERIADO/ACTIVA</li>
 *   <li>Validar duplicados (NUM_CASSETTE + NUM_SECUENCIA)</li>
 *   <li>Autocompletar localización y gas desde la condensadora seleccionada</li>
 * </ul>
 *
 * @author Luis Gil
 */
@SuppressWarnings("ALL")
public class CassetteController {
    /** Campos FXML */
    @FXML private TableView<Cassette>tablaCassette;
    @FXML private TableColumn<Cassette,String>colNumCassette;
    @FXML private TableColumn<Cassette,Integer>colNumSecuencia;
    @FXML private TableColumn<Cassette,String>colEstado;
    @FXML private TableColumn<Cassette,String>colPlanta;
    @FXML private TableColumn<Cassette,String>colNombre;
    @FXML private TableColumn<Cassette,Double>colPotenciaCalor;
    @FXML private TableColumn<Cassette,Double>colPotenciaFrio;
    @FXML private TableColumn<Cassette,String>colMarcaModelo;
    @FXML private TableColumn<Cassette,String>colNumSerieCas;
    @FXML private TableColumn<Cassette,String>colCondensadora;
    @FXML private TableColumn<Cassette,String>colLocalizacionCondensadora;
    @FXML private TableColumn<Cassette,String>colGas;
    @FXML private TableColumn<Cassette,String>colFechaInst;
    @FXML private TableColumn<Cassette, String> colFechaBaja;
    @FXML private TableColumn<Cassette,String>colFechaRev;
    @FXML private TableColumn<Cassette,String>colAveria;
    @FXML private TableColumn<Cassette,String>colFoto;
    @FXML private TableColumn<Cassette, String> colObservaciones;

    @FXML private Button btnFiltroNumCassette;
    @FXML private Button btnFiltroNumSecuencia;
    @FXML private Button btnFiltroEstado;
    @FXML private Button btnFiltroPlanta;
    @FXML private Button btnFiltroNombre;
    @FXML private Button btnFiltroPotenciaCalor;
    @FXML private Button btnFiltroPotenciaFrio;
    @FXML private Button btnFiltroMarcaModelo;
    @FXML private Button btnFiltroNumSerieCas;
    @FXML private Button btnFiltroCondensadora;
    @FXML private Button btnFiltroLocalizacionCondensadora;
    @FXML private Button btnFiltroGas;
    @FXML private Button btnFiltroFechaInst;
    @FXML private Button btnFiltroFechaBaja;
    @FXML private Button btnFiltroFechaRev;
    @FXML private Button btnFiltroAveria;
    @FXML private Button btnFiltroFoto;
    //@FXML private Button btnFiltroRevision;

    @FXML private Label lblActualizado;

    /** Dependencias */
    private MainAppController mainAppController;
    private ObservableList<Cassette> allDatos;
    private Map<String, String[]> datosCondensadoras = new HashMap<>();

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
     * Carga datos de condensadoras, configura columnas, carga datos y desactiva ordenación.
     */
    @FXML
    public void initialize(){
        cargarDatosCondensadoras();
        configurarColumnasCas();
        cargarDatosCas();
        noOrdenar();
        actualizarFechaEncabezado();
    }

    /**
     * Metodo para cargar los datos de las condensadoras y autocompletar localización y gas.
     * Solo se carga una vez (si ya está cargado, no se recarga).
     */
    private void cargarDatosCondensadoras(){
        if(!datosCondensadoras.isEmpty()){
            return;
        }
        List<List<String>> datos = ExcelManager.leerHoja("Condensadoras");
        for (int i = 1; i < datos.size(); i++){
            List<String> fila = datos.get(i);
            if (fila.size() >= 8) {
                String cond = fila.get(0).trim();
                String loc_cond = fila.get(6).trim(); // columna G
                String gas_cond = fila.get(7).trim(); // columna H
                if (!cond.isEmpty()) {
                    datosCondensadoras.put(cond, new String[]{loc_cond, gas_cond});
                }
            }
        }
    }
    /**
     * Metodo para configurar las columnas de la tabla Cassette.
     */
    private void configurarColumnasCas(){
        colNumCassette.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getNumCassette())
        );
        colNumSecuencia.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getNumSecuencia())
        );
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colPlanta.setCellValueFactory(new PropertyValueFactory<>("planta"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPotenciaCalor.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getPotenciaCalor())
        );
        colPotenciaFrio.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getPotenciaFrio())
        );
        colMarcaModelo.setCellValueFactory(new PropertyValueFactory<>("marcaModelo"));
        colNumSerieCas.setCellValueFactory(new PropertyValueFactory<>("numSerieCas"));
        colCondensadora.setCellValueFactory(new PropertyValueFactory<>("condensadora"));
        colLocalizacionCondensadora.setCellValueFactory(new PropertyValueFactory<>("localizacionCondensadora"));
        colGas.setCellValueFactory(new PropertyValueFactory<>("gas"));
        colFechaInst.setCellValueFactory(new PropertyValueFactory<>("fechaInstalacion"));
        colFechaBaja.setCellValueFactory(new PropertyValueFactory<>("fechaBaja"));
        colFechaRev.setCellValueFactory(new PropertyValueFactory<>("fechaRevision"));
        colAveria.setCellValueFactory(new PropertyValueFactory<>("averia"));
        colFoto.setCellValueFactory(new PropertyValueFactory<>("foto"));
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
    }

    /**
     * Metodo para cargar los datos de la hoja Cassette del archivo Excel y mostrarlos en la tabla.
     * Rellena localización y gas desde Condensadoras si están vacíos.
     */
    private void cargarDatosCas(){
        allDatos = FXCollections.observableArrayList();
        List<List<String>>datos = ExcelManager.leerHoja("Cassette");
        for(int i = 1;i<datos.size();i++){
            List<String>fila = datos.get(i);
            //System.out.println("Procesando fila " + i  + ": " + fila);

            if (fila.isEmpty()) continue;

            if (fila.size() < 18) {
                while (fila.size() < 18) {
                    fila.add("");
                }
            }

            String numCassette = fila.get(0).trim();
            String strNumSecuencia = fila.get(1).trim();
            String estado = fila.get(2).trim();
            String planta = fila.get(3).trim();
            String nombre = fila.get(4).trim();
            Double potenciaCalor =parseDouble(fila.get(5));
            Double potenciaFrio = parseDouble(fila.get(6));
            String marcaModelo = fila.get(7).trim();
            String numSerieCas = fila.get(8).trim();
            String condensadora = fila.get(9).trim();
            String localizacionCondensadora = fila.get(10).trim();
            String gas = fila.get(11).trim();
            String fechaInstalacion = fila.get(12).trim();
            String fechaBaja = fila.get(13).trim();
            String fechaRevision = fila.get(14).trim();
            String averia = fila.get(15).trim();
            String foto = fila.get(16).trim();
            String observaciones = fila.get(17).trim();

            if (numCassette.isEmpty() && estado.isEmpty()) continue;

            /** Rellenar localización y gas desde Condensadoras si están vacíos.*/
            String localizacionExcel = localizacionCondensadora;
            String gasExcel = gas;

            if (localizacionExcel.isEmpty() && !condensadora.isEmpty()) {
                String[] datosCond = datosCondensadoras.get(condensadora);
                if (datosCond != null) {
                    localizacionExcel = datosCond[0]; // localización
                    gasExcel = datosCond[1];        // gas
                }
            }

            try{
                Integer numSecuencia = parseInt(strNumSecuencia);
                allDatos.add(new Cassette(
                        numCassette,numSecuencia,estado,planta,nombre,
                        potenciaCalor,potenciaFrio,marcaModelo,numSerieCas,condensadora,
                        localizacionExcel,gasExcel,fechaInstalacion,fechaBaja,fechaRevision,averia,foto,observaciones
                ));

            } catch (Exception e) {
                //System.err.println("Error fila " + i + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        tablaCassette.setItems(allDatos);

        Platform.runLater(() -> {
            Node scrollNode = tablaCassette.lookup(".scroll-pane");
            if (scrollNode instanceof ScrollPane sp) {
                sp.setHvalue(0.0);
            }
        });

    }

    /** Metodos para configurar el filtro de las columnas de la tabla cassette.*/
    @FXML
    private void configurarFiltroNumCassette(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Num_Cassette",item -> String.valueOf(item.getNumCassette()),btnFiltroNumCassette,tablaCassette,allDatos,(ascending) -> {
            ObservableList<Cassette> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    item -> String.valueOf(item.getNumCassette()),
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaCassette.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroNumSecuencia(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Num_Secuencia",item -> String.valueOf(item.getNumSecuencia()),btnFiltroNumSecuencia,tablaCassette,allDatos,(ascending) -> {
            ObservableList<Cassette> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    item -> String.valueOf(item.getNumSecuencia()),
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaCassette.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroEstado(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Estado",Cassette::getEstado,btnFiltroEstado,tablaCassette,allDatos,(ascending) -> {
            ObservableList<Cassette> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Cassette::getEstado,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaCassette.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroPlanta(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Planta",Cassette::getPlanta,btnFiltroPlanta,tablaCassette,allDatos,(ascending) -> {
            ObservableList<Cassette> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Cassette::getPlanta,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaCassette.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroNombre(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Nombre",Cassette::getNombre,btnFiltroNombre,tablaCassette,allDatos,(ascending) -> {
            ObservableList<Cassette> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Cassette::getNombre,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaCassette.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroPotenciaCalor(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Potencia_Calor",item -> String.valueOf(item.getPotenciaCalor()),btnFiltroPotenciaCalor,tablaCassette,allDatos,(ascending) -> {
            ObservableList<Cassette> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    item -> String.valueOf(item.getPotenciaCalor()),
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaCassette.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroPotenciaFrio(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Potencia_Frio",item -> String.valueOf(item.getPotenciaFrio()),btnFiltroPotenciaFrio,tablaCassette,allDatos,(ascending) -> {
            ObservableList<Cassette> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    item -> String.valueOf(item.getPotenciaFrio()),
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaCassette.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroMarcaModelo(){
        FilterUtils.abrirFiltroGenerico("Filtrar por MarcaModelo",Cassette::getMarcaModelo,btnFiltroMarcaModelo,tablaCassette,allDatos,(ascending) -> {
            ObservableList<Cassette> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Cassette::getMarcaModelo,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaCassette.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroNumSerieCas(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Num_Serie_Cas",Cassette::getNumSerieCas,btnFiltroNumSerieCas,tablaCassette,allDatos,(ascending) -> {
            ObservableList<Cassette> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Cassette::getNumSerieCas,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaCassette.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroCondensadora(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Condensadora",Cassette::getCondensadora,btnFiltroCondensadora,tablaCassette,allDatos,(ascending) -> {
            ObservableList<Cassette> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Cassette::getCondensadora,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaCassette.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroLocalizacionCondensadora(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Localizacion_Condensadora",Cassette::getLocalizacionCondensadora,btnFiltroLocalizacionCondensadora,tablaCassette,allDatos,(ascending) -> {
            ObservableList<Cassette> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Cassette::getLocalizacionCondensadora,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaCassette.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroGas(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Gas",Cassette::getGas,btnFiltroGas,tablaCassette,allDatos,(ascending) -> {
            ObservableList<Cassette> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Cassette::getGas,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaCassette.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroFechaInst(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Fecha_Instalacion",Cassette::getFechaInstalacion,btnFiltroFechaInst,tablaCassette,allDatos,(ascending) -> {
            ObservableList<Cassette> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Cassette::getFechaInstalacion,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaCassette.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroFechaBaja(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Fecha_Baja",Cassette::getFechaBaja,btnFiltroFechaBaja,tablaCassette,allDatos,(ascending) -> {
            ObservableList<Cassette> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Cassette::getFechaBaja,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaCassette.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroFechaRev(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Fecha_Revision",Cassette::getFechaRevision,btnFiltroFechaRev,tablaCassette,allDatos,(ascending) -> {
            ObservableList<Cassette> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Cassette::getFechaRevision,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaCassette.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroAveria(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Averia",Cassette::getAveria,btnFiltroAveria,tablaCassette,allDatos,(ascending) -> {
            ObservableList<Cassette> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Cassette::getAveria,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaCassette.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroFoto(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Foto",Cassette::getFoto,btnFiltroFoto,tablaCassette,allDatos,(ascending) -> {
            ObservableList<Cassette> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Cassette::getFoto,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaCassette.setItems(sorted);
        });
    }

    @FXML
    private void abrirFiltroRevision() {
        // Cargar opciones desde PARAM_DIAS_REVISION

        List<String> opciones = ExcelManager.getOpcionesDiasRevision();


        // Crear ComboBox en un diálogo
        ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(opciones));
        combo.setPromptText("Selecciona días");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Filtro de Revisión Personalizado");
        alert.setHeaderText("Elige el periodo de revisión (días)");
        alert.getDialogPane().setContent(combo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String seleccion = combo.getValue();
            if (seleccion == null || seleccion.trim().isEmpty()) {
                mainAppController.showAlert("Debes seleccionar un valor válido.");
                return;
            }

            int diasRevision;
            try {
                diasRevision = Integer.parseInt(seleccion.trim());
            } catch (NumberFormatException e) {
                mainAppController.showAlert("Valor no numérico: " + seleccion);
                return;
            }

            // Obtener equipos seleccionados o todos

            Cassette selected = tablaCassette.getSelectionModel().getSelectedItem();
            if (selected == null) {
                mainAppController.showAlert("Selecciona una cassette para aplicar el filtro de dias para la próxima revision.");
                return;
            }

            // Aplicar cálculo personalizado
            new Thread(() -> {
                try {

                    if ("ACTIVA".equals(selected.getEstado()) && !selected.getFechaInstalacion().isEmpty()) {
                        LocalDate fi = LocalDate.parse(selected.getFechaInstalacion(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        LocalDate fr = ExcelManager.calcularProximaFechaRevision(fi, diasRevision);
                        LocalDate fd = fr.minusDays(30);

                        // Actualizar FECHA_REVISION en Excel
                        List<List<String>> datosCas = ExcelManager.leerHoja("Cassette");
                        for (int i = 1; i < datosCas.size(); i++) {
                            List<String> fila = datosCas.get(i);
                            if (fila.size() > 1 && selected.getNumCassette().equals(fila.get(0).trim()) && String.valueOf(selected.getNumSecuencia()).equals(fila.get(1).trim())){
                                while (fila.size() <= 18) {
                                    fila.add("");
                                }
                                fila.set(14, fr.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                                fila.set(18, String.valueOf(diasRevision));

                                ExcelManager.modificarFila("Cassette", i, fila.toArray(new String[0]));
                                break;
                            }
                        }

                        // Crear en REVISIONES si está en rango
                        if (!LocalDate.now().isBefore(fd)) {
                            ExcelManager.crearEntradaRevision(
                                    "CASSETTE",
                                    selected.getNumCassette(),
                                    selected.getEstado(),
                                    selected.getPlanta(),
                                    selected.getLocalizacionCondensadora(),
                                    fr,
                                    fd
                            );
                        }
                    }


                    Platform.runLater(() -> {
                        cargarDatosCas();
                        tablaCassette.refresh();
                        mainAppController.showAlert("Revisión personalizada aplicada con éxito (" + diasRevision + " días).");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> mainAppController.showAlert("Error al aplicar revisión personalizada."));
                }
            }).start();
        }
    }

    /**
     * Metodo para abrir el formulario y añadir o modificar cassettes.
     * Precarga datos si se está modificando un registro existente.
     * Autocompleta localización y gas al seleccionar una condensadora.
     *
     * @param editar si es null se abre el formulario para añadir un Cassette y
     *               si no es null se abre el formulario para modificar el Cassette seleccionado.
     */
    private void formularioCas(Cassette editar){
        Stage stage = new Stage();
        stage.setTitle(editar == null ? "Añadir Cassette" : "Modificar Cassette");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(tablaCassette.getScene().getWindow());

        /**Campos de formulario.*/
        TextField textoNumCassette = new TextField();
        Label avisoRellenar = new Label("campo Obligarorio");
        avisoRellenar.setTextFill(Color.RED);
        avisoRellenar.setVisible(false);
        if(editar!=null){
            textoNumCassette.setText(editar.getNumCassette());
            textoNumCassette.setEditable(false);
        }else {
            textoNumCassette.textProperty().addListener((obs, oldText, newText) -> {
                if (!newText.equals(newText.toUpperCase())) {
                    textoNumCassette.setText(newText.toUpperCase());
                }
            });
        }

        ComboBox<String> comboEstado = new ComboBox<>(FXCollections.observableArrayList(cargarParametrosExcel("PARAM_ESTADO")));
        ComboBox<String> comboPlanta = new ComboBox<>(FXCollections.observableArrayList(cargarParametrosExcel("PARAM_PLANTAS")));
        ComboBox<String> comboNombre = new ComboBox<>(FXCollections.observableArrayList(cargarParametrosExcel("PARAM_UBICACIONES_CASSETTES")));
        TextField textoPotenciaCalor= new TextField();
        TextField textoPotenciaFrio= new TextField();
        ComboBox<String> comboMarcaModelo = new ComboBox<>(FXCollections.observableArrayList(cargarParametrosExcel("PARAM_MODELOS_CAS")));
        TextField textoNumSerieCas= new TextField();

        /**Condensadora con autocompletado de Localizacion_Condensadora y Gas.*/
        ComboBox<String> comboCondensadora = new ComboBox<>(FXCollections.observableArrayList(datosCondensadoras.keySet()));
        TextField textoLocalizacionCond = new TextField();
        TextField textoGas = new TextField();
        textoLocalizacionCond.setEditable(false);
        textoGas.setEditable(false);
        comboCondensadora.setOnAction(e -> {
            String condensadora = comboCondensadora.getValue();
            if(condensadora != null && datosCondensadoras.containsKey(condensadora)){
                String[] datos = datosCondensadoras.get(condensadora);
                textoLocalizacionCond.setText(datos[0]);
                textoGas.setText(datos[1]);
            }else{
                textoLocalizacionCond.clear();
                textoGas.clear();
            }
        });

        DatePicker dateInst= new DatePicker();
        DatePicker dateBaja= new DatePicker();
        DatePicker dateRev= new DatePicker();
        TextField textoAveria= new TextField();
        TextField textoFoto= new TextField();
        TextField textoObservaciones= new TextField();
        textoObservaciones.setPrefHeight(60);

        Label lblDiasRev = new Label("Dias próxima revisión: ");
        ComboBox<String>comboDiasRev = new ComboBox<>(FXCollections.observableArrayList(ExcelManager.getOpcionesDiasRevision()));

        comboDiasRev.setPromptText("Selecciona días");
        if (!comboDiasRev.getItems().isEmpty()) {
            comboDiasRev.setValue(comboDiasRev.getItems().get(0));
        }

        /**Precargar datos para modificar.*/
        if(editar != null){

            if(comboEstado.getItems().contains(editar.getEstado())) {
                comboEstado.setValue(editar.getEstado());
            }
            if(comboPlanta.getItems().contains(editar.getPlanta())) {
                comboPlanta.setValue(editar.getPlanta());
            }
            if(comboNombre.getItems().contains(editar.getNombre())) {
                comboNombre.setValue(editar.getNombre());
            }
            if(editar.getPotenciaCalor() != null){
                textoPotenciaCalor.setText(String.valueOf(editar.getPotenciaCalor()));
            }
            if(editar.getPotenciaFrio() != null){
                textoPotenciaFrio.setText(String.valueOf(editar.getPotenciaFrio()));
            }
            if(comboMarcaModelo.getItems().contains(editar.getMarcaModelo())) {
                comboMarcaModelo.setValue(editar.getMarcaModelo());
            }
            textoNumSerieCas.setText(editar.getNumSerieCas());
            if(comboCondensadora.getItems().contains(editar.getCondensadora())){
                comboCondensadora.setValue(editar.getCondensadora());
                String[] datos = datosCondensadoras.get(editar.getCondensadora());
                if(datos != null){
                    textoLocalizacionCond.setText(datos[0]);
                    textoGas.setText(datos[1]);
                }
            }
            parseDatePicker(dateInst, editar.getFechaInstalacion());
            parseDatePicker(dateBaja, editar.getFechaBaja());
            parseDatePicker(dateRev, editar.getFechaRevision());
            textoAveria.setText(editar.getAveria());
            textoFoto.setText(editar.getFoto());
            textoObservaciones.setText(editar.getObservaciones());
        }

        /** Creacion de filas y columnas para la tabla Cassette.*/
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));
        int row = 0;

        grid.add(new Label("Nº Cassette: "), 0, row);
        grid.add(textoNumCassette, 1, row);
        grid.add(avisoRellenar, 2, row++);
        grid.add(new Label("Estado: "), 0, row);
        grid.add(comboEstado, 1, row++);
        grid.add(new Label("Planta: "), 0, row);
        grid.add(comboPlanta, 1, row++);
        grid.add(new Label("Nombre: "), 0, row);
        grid.add(comboNombre, 1, row++);
        grid.add(new Label("Potencia Calor: "), 0, row);
        grid.add(textoPotenciaCalor, 1, row++);
        grid.add(new Label("Potencia Frio: "), 0, row);
        grid.add(textoPotenciaFrio, 1, row++);
        grid.add(new Label("Marca/Modelo: "), 0, row);
        grid.add(comboMarcaModelo, 1, row++);
        grid.add(new Label("Nº Serie Cas: "), 0, row);
        grid.add(textoNumSerieCas, 1, row++);
        grid.add(new Label("Condensadora: "), 0, row);
        grid.add(comboCondensadora, 1, row++);
        grid.add(new Label("Localizacion Cond: "), 0, row);
        grid.add(textoLocalizacionCond, 1, row++);
        grid.add(new Label("Gas: "), 0, row);
        grid.add(textoGas, 1, row++);
        grid.add(new Label("Fecha Instalacion: "), 0, row);
        grid.add(dateInst, 1, row++);
        grid.add(lblDiasRev,0,row);
        grid.add(comboDiasRev,1,row++);
        grid.add(new Label("Fecha Baja: "), 0, row);
        grid.add(dateBaja, 1, row++);
        grid.add(new Label("Fecha Revision: "), 0, row);
        grid.add(dateRev, 1, row++);
        grid.add(new Label("Averia: "), 0, row);
        grid.add(textoAveria, 1, row++);
        grid.add(new Label("Foto: "), 0, row);
        grid.add(textoFoto, 1, row++);
        grid.add(new Label("Observaciones: "), 0, row);
        grid.add(textoObservaciones, 1, row++);

        Button btnGuardar = new Button("Guardar");
        Button btnCancelar = new Button("Cancelar");
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        btnGuardar.setOnAction(e -> {
            String numCassette = textoNumCassette.getText().trim().toUpperCase();
            if(numCassette.isEmpty()){
                avisoRellenar.setVisible(true);
                //mainAppController.showAlert("El campo 'Nº Cassette' es obligatorio.");
                return;
            }
            Double potenciaCalor = parseDouble(textoPotenciaCalor.getText());
            Double potenciaFrio = parseDouble(textoPotenciaFrio.getText());

            String fechaInst = dateInst.getValue() != null ? dateInst.getValue().format(format) : "";
            String fechaBaja = dateBaja.getValue() != null ? dateBaja.getValue().format(format) : "";
            String fechaRev = dateRev.getValue() != null ? dateRev.getValue().format(format) : "";
            String estado = comboEstado.getValue() != null ? comboEstado.getValue() : "";

            int diasSeleccionados = 365;
            String diasSelStr = comboDiasRev.getValue();
            if (diasSelStr != null && !diasSelStr.trim().isEmpty()) {
                try {
                    diasSeleccionados = Integer.parseInt(diasSelStr.trim());
                } catch (NumberFormatException ex) {
                    mainAppController.showAlert("Valor de días no válido. Usando 365.");
                }
            }

            if (fechaInst.isEmpty()) {
                mainAppController.showAlert("Fecha de instalación requerida.");
                return;
            }

            try{
                int numSecuencia = 1;
                if (editar == null) {
                    for (Cassette c : allDatos) {
                        if (c.getNumCassette().equals(numCassette)) {
                            if (c.getNumSecuencia() >= numSecuencia) {
                                numSecuencia = c.getNumSecuencia() + 1;
                            }
                        }
                    }
                }else {
                    numSecuencia = editar.getNumSecuencia();
                }

                LocalDate fi = LocalDate.parse(fechaInst, format);
                LocalDate fr = ExcelManager.calcularProximaFechaRevision(fi, diasSeleccionados);
                String frStr = fr.format(format);

                List<String> filaNueva = Arrays.asList(
                  numCassette,
                  String.valueOf(numSecuencia),
                  estado,
                  comboPlanta.getValue() != null ? comboPlanta.getValue() : "",
                  comboNombre.getValue() != null ? comboNombre.getValue() : "",
                  potenciaCalor != null ? String.valueOf(potenciaCalor) : "",
                  potenciaFrio != null ? String.valueOf(potenciaFrio) : "",
                  comboMarcaModelo.getValue() != null ? comboMarcaModelo.getValue() : "",
                  textoNumSerieCas.getText().trim(),
                  comboCondensadora.getValue() != null ? comboCondensadora.getValue() : "",
                  textoLocalizacionCond.getText().trim(),
                  textoGas.getText().trim(),
                  fechaInst,
                  fechaBaja,
                  frStr,
                  "",
                  textoFoto.getText().trim(),
                  textoObservaciones.getText().trim(),
                  String.valueOf(diasSeleccionados)
                );
                boolean esNuevo = (editar == null);
                int indexExcel = -1;

                if(esNuevo){

                    ExcelManager.añadirFilaOrdenada("Cassette", filaNueva.toArray(new String[0]));

                    cargarDatosCas();

                }else {
                    List<List<String>> datos = ExcelManager.leerHoja("Cassette");

                    for (int i = 1; i < datos.size(); i++) {
                        List<String> fila = datos.get(i);
                        if (fila.size() > 1 &&
                                fila.get(0).trim().equals(editar.getNumCassette()) &&
                                fila.get(1).trim().equals(String.valueOf(editar.getNumSecuencia()))) {
                            while (fila.size() <= 18) fila.add("");
                            fila.set(14, frStr);
                            fila.set(18, String.valueOf(diasSeleccionados));
                            ExcelManager.modificarFila("Cassette", i, filaNueva.toArray(new String[0]));
                            break;
                        }
                    }
                }
                LocalDate fd = fr.minusDays(30);
                if (!LocalDate.now().isBefore(fd)) {
                    String localizacion = textoLocalizacionCond.getText() != null ? textoLocalizacionCond.getText() : "";
                    ExcelManager.crearEntradaRevision(
                            "CASSETTE",
                            numCassette,
                            estado,
                            comboPlanta.getValue() != null ? comboPlanta.getValue() : "",
                            localizacion,
                            fr,
                            fd
                    );
                }
                /** Gestion de Averias Automaticamente.*/
                String estadoAnterior = null;
                if (!esNuevo) {
                    List<List<String>> datosCond = ExcelManager.leerHoja("Cassette");
                    for (int i = 1; i < datosCond.size(); i++) {
                        List<String> fila = datosCond.get(i);
                        //System.out.println("Comparando: '" + fila.get(0) + "' vs '" + editar.getNumCassette() + "'");
                        if (fila.size() > 0 && fila.get(0).trim().equals(textoNumCassette)) {
                            estadoAnterior = fila.get(2).trim(); // columna ESTADO
                            break;
                        }
                    }
                    if (estadoAnterior == null || estadoAnterior.isEmpty()) {
                        estadoAnterior = (editar != null) ? editar.getEstado() : "";
                    }
                }

                String numAveria = "";
                String estadoActual = comboEstado.getValue() != null ? comboEstado.getValue() : "";
                //System.out.println("estadoAnterior: '" + estadoAnterior + "'");
                //System.out.println("estadoActual: '" + estadoActual + "'");
                /** Si se cambia de ACTIVA a AVERIADO se crea una Averia.*/
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
                    String planta = comboPlanta.getValue() != null ? comboPlanta.getValue() : "";
                    String localizacion = textoLocalizacionCond.getText().trim();
                    String observaciones = textoObservaciones.getText().trim();
                    ExcelManager.registrarAveriaAutomaticamente("CASSETTE", numCassette, planta, localizacion, observaciones,"Cassette",numAveria);
                }
                /** Si se cambia de AVERIADO a ACTIVA se marca la columna estado como REPARADO.*/
                if ("AVERIADO".equals(estadoAnterior) && "ACTIVA".equals(estadoActual)) {
                    ExcelManager.marcarAveriaComoReparada("CASSETTE", numCassette);
                }
                Cassette actualizada = new Cassette(
                    numCassette,
                    numSecuencia,
                    comboEstado.getValue() != null ? comboEstado.getValue() : "",
                    comboPlanta.getValue() != null ? comboPlanta.getValue() : "",
                    comboNombre.getValue() != null ? comboNombre.getValue() : "",
                    potenciaCalor,
                    potenciaFrio,
                    comboMarcaModelo.getValue() != null ? comboMarcaModelo.getValue() : "",
                    textoNumSerieCas.getText().trim(),
                    comboCondensadora.getValue() != null ? comboCondensadora.getValue() : "",
                    textoLocalizacionCond.getText().trim(),
                    textoGas.getText().trim(),
                    fechaInst,
                    fechaBaja,
                    fechaRev,
                    numAveria,
                    textoFoto.getText().trim(),
                    textoObservaciones.getText().trim()
                );
                if(esNuevo){
                }else{
                    int pos = allDatos.indexOf(editar);
                    if (pos != -1) {
                        allDatos.set(pos, actualizada);
                    }
                }
                stage.close();

                final int numSecuenciaFinal = numSecuencia;
                new Thread(() -> {
                    try {
                        int diasRevision = 365;
                        List<List<String>> hojaOrigen = ExcelManager.leerHoja("Cassette");
                        for (List<String> fila : hojaOrigen) {
                            if (fila.size() > 1 &&
                                    numCassette.equals(fila.get(0).trim()) &&
                                    String.valueOf(numSecuenciaFinal).equals(fila.get(1).trim())) {

                                if (fila.size() > 18 && !fila.get(18).trim().isEmpty()) {
                                    try {
                                        diasRevision = Integer.parseInt(fila.get(18).trim());
                                    } catch (NumberFormatException ignored) {}
                                }
                                break;
                            }
                        }
                        ExcelManager.calcularYActualizarRevisionIndividual("CASSETTE", numCassette,fechaInst,numSecuenciaFinal,diasRevision); //  Primero escribe en Excel
                        Platform.runLater(() -> {
                            cargarDatosCas(); //  Luego recarga la tabla
                            tablaCassette.refresh();
                            actualizarFechaEncabezado();

                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();

            } catch (Exception ex) {
                ex.printStackTrace();
                mainAppController.showAlert("Error al guardar.");
            }
        });
        btnCancelar.setOnAction(e -> stage.close());
        HBox botones = new HBox(10, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER);
        grid.add(botones, 1, ++row);

        Scene scene = new Scene(grid,500, 700);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Metodo para abrir el formulario y añadir un nuevo cassette.
     */
    @FXML
    public void onAddCas(){
        formularioCas(null);
    }

    /**
     * Metodo para abrir el formulario y modificar el cassette seleccionado.
     * Valida que haya una selección activa antes de abrir el formulario.
     */
    @FXML
    public void onEditCas(){
        Cassette seleccionado = tablaCassette.getSelectionModel().getSelectedItem();
        if(seleccionado == null){
            mainAppController.showAlert("Selecciona un Cassette para modificar.");
            return;
        }
        formularioCas(seleccionado);
    }

    /**
     * Metodo para eliminar el cassette seleccionado tras confirmación.
     */
    @FXML
    public void onDeleteCas(){
        Cassette seleccionado = tablaCassette.getSelectionModel().getSelectedItem();
        if(seleccionado == null){
            mainAppController.showAlert("Selecciona un Cassette para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("Estas seguro de querer eliminar este Cassette?");
        confirmacion.setContentText("Esta acción no se podrá deshacer");

        confirmacion.showAndWait().ifPresent(response -> {
            if(response == ButtonType.OK){
                tablaCassette.getItems().remove(seleccionado);
                ExcelManager.eliminarFila("Cassette", seleccionado.getNumCassette());
            }
        });
    }

    /**
     * Metodo para sustituir el cassette seleccionado tras confirmación,
     * por otro creado con las mismas propiedades más significativas.
     * El estado del sustituido será BAJA con la fecha actual en FECHA_BAJA.
     * El estado del nuevo será ACTIVA con fecha actual en FECHA_INSTALACION.
     */
    @FXML
    public void onSustituirCas(){
        Cassette selected = tablaCassette.getSelectionModel().getSelectedItem();
        if(selected == null){
            mainAppController.showAlert("Selecciona un Cassette para poder sustituirlo");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar sustitución");
        alert.setHeaderText("Estas seguro de querer sustituir este Cassette?");
        alert.setContentText("se dara de baja este Cassette y se activara uno nuevo.");
        alert.showAndWait().ifPresent(response -> {
            try{
                int secuenciaPorDefecto = 1;
                for(Cassette cas : allDatos){
                    if(cas.getNumCassette().equals(selected.getNumCassette())){
                        if(cas.getNumSecuencia() > secuenciaPorDefecto){
                            secuenciaPorDefecto = cas.getNumSecuencia();
                        }
                    }
                }
                int secuenciaNueva = secuenciaPorDefecto + 1;

                int diasRevisionOriginal = 365;
                List<List<String>> hojaCasOriginal = ExcelManager.leerHoja("Cassette");
                for (List<String> fila : hojaCasOriginal) {
                    if (fila.size() > 1 &&
                            selected.getNumCassette().equals(fila.get(0).trim()) &&
                            String.valueOf(secuenciaPorDefecto).equals(fila.get(1).trim())) {

                        if (fila.size() > 18 && !fila.get(18).trim().isEmpty()) {
                            try {
                                diasRevisionOriginal = Integer.parseInt(fila.get(18).trim());
                            } catch (NumberFormatException ignored) {}
                        }
                        break;
                    }
                }
                int nuevoDiasRevision = diasRevisionOriginal;
                /** Crear nuevo Cassette */
                String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                List<String> filaNueva = Arrays.asList(
                        selected.getNumCassette(),
                        String.valueOf(secuenciaNueva),
                        "ACTIVA",
                        selected.getPlanta(),
                        selected.getNombre(),
                        "",
                        "",
                        selected.getMarcaModelo(),
                        "",
                        selected.getCondensadora(),
                        "",
                        "",
                        fechaActual,
                        "",
                        "",
                        "",
                        "",
                        "",
                        String.valueOf(diasRevisionOriginal)
                );
                ExcelManager.añadirFilaOrdenada("Cassette", filaNueva.toArray(new String[0]));

                /** Actualizar Cassette seleccionado */

                List<List<String>> hojaCasNueva = ExcelManager.leerHoja("Cassette");
                for(int i = 1; i< hojaCasNueva.size(); i++){
                    List<String> filaCas = hojaCasNueva.get(i);
                    if(filaCas.size() > 0 && filaCas.get(0).trim().equals(selected.getNumCassette()) && String.valueOf(secuenciaPorDefecto).equals(filaCas.get(1).trim())){
                        while (filaCas.size() <= 13){
                            filaCas.add("");
                        }
                        filaCas.set(2, "BAJA");
                        filaCas.set(13, fechaActual);
                        ExcelManager.modificarFila("Cassette", i, filaCas.toArray(new String[0]));
                        break;
                    }
                }
                new Thread(() -> {
                    try {

                        ExcelManager.calcularYActualizarRevisionIndividual("CASSETTE", selected.getNumCassette(), fechaActual, secuenciaNueva,nuevoDiasRevision); //  Primero escribe en Excel
                        Platform.runLater(() -> {
                            cargarDatosCas(); //  Luego recarga la tabla
                            tablaCassette.refresh();

                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
                mainAppController.showAlert("Cassette sustituido con exito");
            } catch (Exception e) {
                e.printStackTrace();
                mainAppController.showAlert("Error al sustituir el Cassette");
            }
        });
    }

    /**
     * Modulo para verificar si existe un cassette duplicado con la misma clave compuesta (NUM_CASSETTE + NUM_SECUENCIA).
     *
     * @param numCassette código del cassette
     * @param numSecuencia número de secuencia
     * @return true si existe duplicado, false en caso contrario
     */
    private boolean existeDuplicada(String numCassette, Integer numSecuencia) {
        for (Cassette c : allDatos) {
            if (c.getNumCassette().equals(numCassette) && c.getNumSecuencia().equals(numSecuencia) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Metodo para establecer la fecha en un DatePicker a partir de un String con formato dd/MM/yyyy.
     *
     * @param picker DatePicker a configurar
     * @param fechaStr fecha en formato String (dd/MM/yyyy)
     */
    private void parseDatePicker(DatePicker picker, String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) return;
        try {
            LocalDate date = LocalDate.parse(fechaStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            picker.setValue(date);
        } catch (Exception ignored) {}
    }

    /**
     * Metodo para cargar los parámetros de una hoja específica del Excel (ej. PARAM_ESTADO, PARAM_PLANTAS).
     * Omite valores vacíos y en blanco.
     *
     * @param hoja nombre de la hoja de parámetros
     * @return lista de valores válidos
     */
    private List<String> cargarParametrosExcel(String hoja){
        List<String> lista = new ArrayList<>();
        try{
            List<List<String>> datos = ExcelManager.leerHoja(hoja);
            if(datos.size()>1){
                for(int i = 1; i<datos.size();i++){
                    List<String> fila = datos.get(i);
                    if(!fila.isEmpty() && fila.get(0) != null){
                        String valor = fila.get(0).trim();
                        if(!valor.isEmpty() && !valor.equalsIgnoreCase("en blanco")) {
                            lista.add(valor);
                        }
                    }
                }
            }
        } catch (Exception e) {
            //System.err.println(" Error al cargar: " + hoja);
            e.printStackTrace();
        }
        return lista;
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
     * Metodo para convertir un String a Double de forma segura.
     * Limpia caracteres no numéricos y maneja coma decimal.
     *
     * @param dato String a convertir
     * @return valor Double o null si no es válido
     */
    private Double parseDouble(String dato) {
        if(dato == null || dato.trim().isEmpty()){
            return null;
        }
        try {
            String clean = dato.trim().replaceAll("[^\\d.,]", "").replace(',', '.');
            if (clean.isEmpty()){
                return null;
            }
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Metodo para desactivar la ordenación en todas las columnas de la tabla.
     * Mejora la estabilidad visual al trabajar con datos no ordenados.
     */
    private void noOrdenar(){
        colNumCassette.setSortable(false);
        colNumSecuencia.setSortable(false);
        colEstado.setSortable(false);
        colPlanta.setSortable(false);
        colNombre.setSortable(false);
        colPotenciaCalor.setSortable(false);
        colPotenciaFrio.setSortable(false);
        colMarcaModelo.setSortable(false);
        colNumSerieCas.setSortable(false);
        colCondensadora.setSortable(false);
        colLocalizacionCondensadora.setSortable(false);
        colGas.setSortable(false);
        colFechaInst.setSortable(false);
        colFechaBaja.setSortable(false);
        colFechaRev.setSortable(false);
        colAveria.setSortable(false);
        colFoto.setSortable(false);
        colObservaciones.setSortable(false);
    }

    /**
     * Metodo para actualizar la etiqueta de fecha en el encabezado con la fecha actual.
     */
    private void actualizarFechaEncabezado() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaHoy = LocalDate.now().format(formatter);
        lblActualizado.setText("Actualizado: " + fechaHoy);
    }
}

