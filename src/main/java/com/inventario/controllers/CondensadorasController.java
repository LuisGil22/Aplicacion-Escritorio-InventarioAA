package com.inventario.controllers;

import com.inventario.models.Condensadora;
import com.inventario.utils.ExcelManager;
import com.inventario.utils.FilterUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

public class CondensadorasController {
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
    @FXML private TableColumn<Condensadora,String>colFechaRev;
    @FXML private TableColumn<Condensadora,String>colAveria;
    @FXML private TableColumn<Condensadora,String>colObservaciones;

    //@FXML private ComboBox<String> comboFiltroEstado;
    //@FXML private Button btnFiltrar;

    @FXML private Button btnFiltroCondensadora;
    @FXML private Button btnFiltroNumSecuencia;
    @FXML private Button btnFiltroEstado;
    @FXML private Button btnFiltroMarca;
    @FXML private Button btnFiltroModelo;
    @FXML private Button btnFiltroNumSerie;
    @FXML private Button btnFiltroLocCondensadoras;
    @FXML private Button btnFiltroGas;
    @FXML private Button btnFiltroFechaInst;
    @FXML private Button btnFiltroFechaRev;

    private MainAppController mainAppController;
    private ObservableList<Condensadora> allDatos;
    private Boolean filtroActivo = true;
    private Boolean filtroBaja = true;

    //private final Set<String>selectedValoresCondensadora = new LinkedHashSet<>();
    //private Boolean seleccionarTodos = true;
    //private final Map<String, CheckBox> checkItems = new HashMap<>();
    private final Map<String,Set<String>> selectedValores = new HashMap<>();
    private final Map<String,Boolean>seleccionarTodos = new HashMap<>();
    private Button botonActual;

    // ComboBoxes para parámetros
    /**private ComboBox<String>comboEstado;
    private ComboBox<String>comboGas;
    private ComboBox<String>comboMarca;
    private ComboBox<String>comboModelo;
    private ComboBox<String>comLocCondensadora;

    // Campos de texto
    private TextField textCondensadora;
    private TextField textNumSerie;
    private TextField textAveria;
    private TextField textObservacion;

    //variable para modificar condensadora.

    private Condensadora seleccionarCondensadora;

    //private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");**/


    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    @FXML
    public void initialize(){
        confColumnas();
        //cargarFiltroEstados();
        cargarDatos();
        noOrdenar();
    }

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
        colFechaRev.setCellValueFactory(new PropertyValueFactory<>("fechaRevision"));
        colAveria.setCellValueFactory(new PropertyValueFactory<>("averia"));
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
    }

    /**private void cargarFiltroEstados(){
        ObservableList<String>estados = FXCollections.observableArrayList("Todos");
        var datos = ExcelManager.leerHoja("PARAM_ESTADO");
        for(int i=1;i<datos.size();i++){
            if(!datos.get(i).isEmpty() && !datos.get(i).get(0).trim().isEmpty()){
                estados.add(datos.get(i).get(0));
            }
        }
        comboFiltroEstado.setItems(estados);
        comboFiltroEstado.setValue("Todos");
    }**/

    /**@FXML
    private void configurarFiltroCondensadora() {
        if (allDatos == null || allDatos.isEmpty()) {
            mainAppController.showAlert("No hay datos para filtrar.");
            return;
        }

        // Obtener valores únicos de CONDENSADORA (sin duplicados, ordenados)
        Set<String> set = new LinkedHashSet<>();
        for (Condensadora item : allDatos) {
            String v = item.getCondensadora();
            if (v != null && !v.trim().isEmpty()) {
                set.add(v.trim());
            }
        }
        List<String> valoresColumnasCondensadora = new ArrayList<>(set);

        // Crear ventana de filtro (como en ParamEstado)
        Stage stage = new Stage();
        stage.setTitle("Filtrar por Condensadora");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(btnFiltroCondensadora.getScene().getWindow());


        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setMaxWidth(250);

        // Checkbox "Todos"
        CheckBox cbTodos = new CheckBox("Todos");
        cbTodos.setSelected(seleccionarTodos);
        cbTodos.setOnAction(e -> {
            boolean sel = cbTodos.isSelected();
            seleccionarTodos = sel;
            if(sel) {
                selectedValoresCondensadora.clear();
                checkItems.values().forEach(cb -> cb.setSelected(false));
            }
        });

        vbox.getChildren().add(cbTodos);

        // Checkboxes por valor
        checkItems.clear();
        for (String valor : valoresColumnasCondensadora) {
            CheckBox cb = new CheckBox(valor);
            boolean wasSelected = selectedValoresCondensadora.contains(valor);
            cb.setSelected(wasSelected);
            checkItems.put(valor, cb);
            cb.setOnAction(e ->{
                if(cb.isSelected()){
                    selectedValoresCondensadora.add(valor);
                    cbTodos.setSelected(false);
                    seleccionarTodos = false;
                }else{
                    selectedValoresCondensadora.remove(valor);
                }
            });
            vbox.getChildren().add(cb);
        }

        // Botones Aceptar/Cancelar
        HBox hbButtons = new HBox(10);
        Button btnAceptar = new Button("Aceptar");
        Button btnCancelar = new Button("Cancelar");

        btnAceptar.setOnAction(e -> {

            if(seleccionarTodos){
                tablaCondensadoras.setItems(allDatos);
            }else{
                ObservableList<Condensadora> filtrados = allDatos.filtered(item ->{
                    String cond = item.getCondensadora();
                    return cond != null && selectedValoresCondensadora.contains(cond.trim());
                });
                tablaCondensadoras.setItems(filtrados);
            }
            stage.close();
        });

        btnCancelar.setOnAction(e -> stage.close());

        hbButtons.getChildren().addAll(btnAceptar, btnCancelar);
        vbox.getChildren().add(hbButtons);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefWidth(250);
        scrollPane.setPrefHeight(450);
        scrollPane.setMaxHeight(600);
        scrollPane.setMinHeight(350);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // 🎯 Posicionar debajo del botón ▼
        Bounds btnBounds = btnFiltroCondensadora.localToScreen(btnFiltroCondensadora.getBoundsInLocal());
        stage.setX(btnBounds.getMinX() - 40);
        stage.setY(btnBounds.getMaxY() + 5);
        stage.setScene(new Scene(scrollPane));
        stage.show();
    }**/
    /**private void aplicarFiltroCondensadora() {
        // Si no hay checkboxes, mostrar todo
        if (checkItems.isEmpty()) {
            tablaCondensadoras.setItems(allDatos);
            return;
        }

        // Obtener valores seleccionados
        Set<String> seleccionados = checkItems.entrySet().stream()
                .filter(entry -> entry.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // Si ninguno está seleccionado, mostrar vacío
        if (seleccionados.isEmpty()) {
            tablaCondensadoras.setItems(FXCollections.observableArrayList());
            return;
        }

        // Filtrar
        ObservableList<Condensadora> filtrados = allDatos.filtered(
                item -> {
                    String cond = item.getCondensadora();
                    return cond != null && seleccionados.contains(cond.trim());
                }
        );

        tablaCondensadoras.setItems(filtrados);
    }**/
    /**private void cargarDatos(){
        allDatos = FXCollections.observableArrayList();
        var datos = ExcelManager.leerHoja("Condensadoras");
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("d/M/yyyy");

        for(int i=2;i<datos.size();i++){
            var fila = datos.get(i);
            if (fila.isEmpty() || (fila.size() > 0 && "CONDENSADORA".equals(fila.get(0)))) {
                continue;
            }
            if(fila.size() <= 12) {continue;}
            String condensadoras = fila.get(0);
            String numSecuenciaStr = fila.get(1);
            String estado = fila.get(2);
            String marca = fila.get(3);
            String modelo = fila.get(4);
            String numSerieStr = fila.get(5);
            String loc_condensadoras = fila.get(6);
            String gas = fila.get(7);
            String fechaInstStr = fila.get(8);
            String fechaRevStr = fila.get(9);
            String averia = fila.get(10);
            String observaciones = fila.get(11);

            int numSecuencia = parseInt(numSecuenciaStr);
            long numSerie = parseLong(numSerieStr);
            LocalDate fechaInst = parseDate(fechaInstStr,formato);
            LocalDate fechaRev = parseDate(fechaRevStr,formato);

            allDatos.add(new Condensadora(
                    condensadoras,numSecuencia,estado,marca,modelo,numSerie,loc_condensadoras,gas,fechaInst != null? Date.valueOf(fechaInst):null,fechaRev != null? Date.valueOf(fechaRev):null,averia,observaciones
            ));
        }
        tablaCondensadoras.setItems(allDatos);
        System.out.println("Cargados " + allDatos.size() + " registros de Condensadoras");
    }**/

    private void cargarDatos() {
        allDatos = FXCollections.observableArrayList();
        var datos = ExcelManager.leerHoja("Condensadoras");
        //DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/M/yyyy");

        // Empezar desde i=1 (primera fila de datos)
        for (int i = 1; i < datos.size(); i++) {
            var fila = datos.get(i);

            // Saltar filas vacías
            if (fila.isEmpty()) continue;

            // Verificar que tenga al menos 1 columna
            if (fila.size() < 12) {
                // Completar con valores vacíos
                while (fila.size() < 12) {
                    fila.add("");
                }
            }

            String condensadora = fila.get(0);
            String numSecuenciaStr = fila.get(1);
            String estado = fila.get(2);
            String marca = fila.get(3);
            String modelo = fila.get(4);
            String numSerie = fila.get(5);
            String loc = fila.get(6);
            String gas = fila.get(7);
            String fechaInst = fila.get(8);
            String fechaRev = fila.get(9);
            String averia = fila.get(10);
            String observaciones = fila.get(11);

            // Solo procesar si hay al menos un valor significativo
            if (condensadora.trim().isEmpty() && estado.trim().isEmpty()) {
                continue;
            }

            // Parseo seguro (¡no asumas que los strings no están vacíos!)
            int numSecuencia = numSecuenciaStr.trim().isEmpty() ? 1 : parseInt(numSecuenciaStr);
            long numSerieLong = numSerie.trim().isEmpty() ? 0L : parseLong(numSerie);
            //fechaInst = (fechaInst != null) ? fechaInst.trim() : "";
            //fechaRev = (fechaRev != null) ? fechaRev.trim() : "";
            //LocalDate fechaInst = parseDate(formato, fechaInstStr);


            allDatos.add(new Condensadora(
                    condensadora,
                    numSecuencia,
                    estado,
                    marca,
                    modelo,
                    numSerieLong,
                    loc,
                    gas,
                    fechaInst,
                    fechaRev,
                    averia,
                    observaciones
            ));
        }

        tablaCondensadoras.setItems(allDatos);
        System.out.println("Cargados " + allDatos.size() + " registros de Condensadoras");
    }

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
    private void configurarFiltroFechaRev(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Fecha de Revisión", Condensadora::getFechaRevision,btnFiltroFechaRev,tablaCondensadoras,allDatos);
    }

    private void configurarFiltro(String titulo, Function<Condensadora,String>extractor,Button button){
        if (allDatos == null || allDatos.isEmpty()) {
            mainAppController.showAlert("No hay datos para filtrar.");
            return;
        }
        this.botonActual = button;
        String clave = titulo;

        if(!selectedValores.containsKey(clave)){
            selectedValores.put(clave,new LinkedHashSet<>());
            seleccionarTodos.put(clave,true);
        }
        Set<String>seleccionados = selectedValores.get(clave);
        boolean todos = seleccionarTodos.get(clave);

        Set<String> valoresUnicos = new LinkedHashSet<>();
        for (Condensadora item : allDatos) {
            String v = extractor.apply(item);
            if (v != null && !v.trim().isEmpty()) {
                valoresUnicos.add(v.trim());
            }
        }
        Stage stage = new Stage();
        stage.setTitle(titulo);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(button.getScene().getWindow());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setMaxWidth(250);

        Map<String,CheckBox>checkBoxMap = new HashMap<>();
        // Checkbox "Todos"
        CheckBox cbTodos = new CheckBox("Todos");
        cbTodos.setSelected(todos);
        cbTodos.setOnAction(e -> {
            boolean sel = cbTodos.isSelected();
            seleccionarTodos.put(clave,sel);
            if(sel) {
                seleccionados.clear();
                checkBoxMap.values().forEach(cb -> cb.setSelected(false));
            }
        });

        vbox.getChildren().add(cbTodos);

        // Checkboxes por valor
        for (String valor : valoresUnicos) {
            CheckBox cb = new CheckBox(valor);
            cb.setSelected(seleccionados.contains(valor));
            checkBoxMap.put(valor, cb);
            cb.setOnAction(e -> {
                if (cb.isSelected()) {
                    seleccionados.add(valor);
                    cbTodos.setSelected(false);
                    seleccionarTodos.put(clave, false);
                } else {
                    seleccionados.remove(valor);
                }
            });
            vbox.getChildren().add(cb);
        }

        // Botones Aceptar/Cancelar
        HBox hbButtons = new HBox(10);
        Button btnAceptar = new Button("Aceptar");
        Button btnCancelar = new Button("Cancelar");

        btnAceptar.setOnAction(e -> {

            if(seleccionarTodos.get(clave)){
                tablaCondensadoras.setItems(allDatos);
            }else{
                ObservableList<Condensadora> filtrados = allDatos.filtered(item ->{
                    String cond = extractor.apply(item);
                    return cond != null && seleccionados.contains(cond.trim());
                });
                tablaCondensadoras.setItems(filtrados);
            }
            stage.close();
        });
        btnCancelar.setOnAction(e -> stage.close());

        hbButtons.getChildren().addAll(btnAceptar, btnCancelar);
        vbox.getChildren().add(hbButtons);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefWidth(250);
        scrollPane.setPrefHeight(450);
        scrollPane.setMaxHeight(600);
        scrollPane.setMinHeight(350);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // 🎯 Posicionar debajo del botón ▼
        Bounds btnBounds = button.localToScreen(button.getBoundsInLocal());
        stage.setX(btnBounds.getMinX() - 40);
        stage.setY(btnBounds.getMaxY() + 5);
        stage.setScene(new Scene(scrollPane));
        stage.show();
    }
    /**@FXML
    public void onFiltrar(){
        String seleccionado = comboFiltroEstado.getValue();
        if("Todos".equals(seleccionado)){
            tablaCondensadoras.setItems(allDatos);
        }else {
            ObservableList<Condensadora> filtrados = FXCollections.observableArrayList();

            String seleccionLimpia = seleccionado.trim().toUpperCase();

            for (Condensadora condensadora : allDatos) {
                String estado = condensadora.getEstado();
                if (estado != null && estado.trim().toUpperCase().equals(seleccionLimpia)) {
                    filtrados.add(condensadora);
                }
            }
            tablaCondensadoras.setItems(filtrados);
        }
    }**/

    /**public void configurarFiltroEstado(){

        Popup popup = new Popup();

        VBox menu = new VBox(5);
        menu.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-padding: 10px;");

        // Checkbox para "Todos"
        CheckBox chkTodos = new CheckBox("Todos");
        CheckBox chkActivo = new CheckBox("ACTIVO");
        CheckBox chkBaja = new CheckBox("BAJA");

        chkTodos.setSelected(filtroActivo && filtroBaja);
        chkActivo.setSelected(filtroActivo);
        chkBaja.setSelected(filtroBaja);

        chkTodos.setOnAction(e -> {
            if (chkTodos.isSelected()) {
                chkActivo.setSelected(true);
                chkBaja.setSelected(true);
            }else{
                chkActivo.setSelected(false);
                chkBaja.setSelected(false);
            }
            filtroActivo = chkActivo.isSelected();
            filtroBaja = chkBaja.isSelected();
            aplicarFiltro(chkActivo, chkBaja);
        });

        // Checkbox para ACTIVO
        //CheckMenuItem chkActivo = new CheckMenuItem("ACTIVO");

        chkActivo.setOnAction(e -> {
            if (!chkActivo.isSelected()) {
                chkTodos.setSelected(false);
            }
            filtroActivo = chkActivo.isSelected();
            aplicarFiltro(chkActivo, chkBaja);
        });

        // Checkbox para BAJA
        //CheckMenuItem chkBaja = new CheckMenuItem("BAJA");

        chkBaja.setOnAction(e -> {
            if (!chkBaja.isSelected()) {
                chkTodos.setSelected(false);
            }
            filtroBaja = chkBaja.isSelected();
            aplicarFiltro(chkActivo, chkBaja);
        });



        // Botones Aceptar/Cancelar
        HBox botones = new HBox(10);
        Button btnAceptar = new Button("Aceptar");
        Button btnCancelar = new Button("Cancelar");

        btnAceptar.setOnAction(e -> popup.hide());
        btnCancelar.setOnAction(e -> {
            // Restaurar estado
            chkActivo.setSelected(true);
            chkBaja.setSelected(true);
            chkTodos.setSelected(true);
            aplicarFiltro(chkActivo, chkBaja);
            popup.hide();
        });
        botones.getChildren().addAll(btnAceptar, btnCancelar);

        menu.getChildren().addAll(
                chkTodos,
                chkActivo,
                chkBaja,
                new Separator(),
                botones
        );

        popup.getContent().add(menu);
        HBox graphic = (HBox) colEstado.getGraphic();
        Button boton = (Button) graphic.getChildren().get(1);
        var bounds = boton.localToScreen(boton.getBoundsInLocal());
        popup.show(boton.getScene().getWindow(), bounds.getMinX(), bounds.getMaxY());
    }**/


    private int parseInt(String dato) {
        try {
            return dato == null || dato.trim().isEmpty() ? 0 : Integer.parseInt(dato.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long parseLong(String dato){
        try {
            return dato.isEmpty()? 0L : Long.parseLong(dato);
        }catch (NumberFormatException e){
            return 0L;
        }
    }


    /**private void aplicarFiltro(CheckBox chkActivo, CheckBox chkBaja) {
        ObservableList<Condensadora> filtrados = FXCollections.observableArrayList();


        for (Condensadora c : allDatos) {
            String estado = c.getEstado();
            if (estado == null) continue;

            if (filtroActivo && "ACTIVO".equalsIgnoreCase(estado.trim())) {
                filtrados.add(c);
            } else if (filtroBaja && "BAJA".equalsIgnoreCase(estado.trim())) {
                filtrados.add(c);
            }
        }
        tablaCondensadoras.setItems(filtrados);
    }**/

    @FXML
    private void onAddCond(){
        onAddForm(null);
    }

    private void onAddForm(Condensadora editar){
        Stage stage = new Stage();
        stage.setTitle(editar == null ? "Añadir Nueva Condensadora" : "Modificar Condensadora");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(tablaCondensadoras.getScene().getWindow());

        //Campos de texto
        TextField textCondensadora = new TextField();
        TextField textNumSerie = new TextField();
        TextField textAveria = new TextField();
        TextField textObservacion = new TextField();
        textAveria.setPrefHeight(40);
        textObservacion.setPrefHeight(60);

        Spinner<Integer> spiNumSecuencia = new Spinner<>(1,100,1);
        spiNumSecuencia.setEditable(false);

        DatePicker dateFechaInst = new DatePicker();
        CheckBox desinstaladaCond = new CheckBox("DESINSTALADA");

        DatePicker dateFechaRev = new DatePicker();
        CheckBox fechaSinVerificar = new CheckBox("Posible fecha sin verificar");

        desinstaladaCond.setOnAction(e -> dateFechaInst.setDisable(desinstaladaCond.isSelected()));
        fechaSinVerificar.setOnAction(e -> dateFechaRev.setDisable(fechaSinVerificar.isSelected()));

        //Combos que se cargan desde las hojas de parámetros del excel
        List<String> estados = cargarParametrosExcel("PARAM_ESTADO");
        List<String> gases = cargarParametrosExcel("PARAM_GASES");
        List<String> marcas = cargarParametrosExcel("PARAM_MARCAS");
        List<String> modelos = cargarParametrosExcel("PARAM_MODELOS_COND");
        List<String> locCondensadoras = cargarParametrosExcel("PARAM_LOC_COND");

        ComboBox<String> comboEstado = new ComboBox<>(FXCollections.observableArrayList(estados));
        ComboBox<String> comboGas = new ComboBox<>(FXCollections.observableArrayList(gases));
        ComboBox<String> comboMarca = new ComboBox<>(FXCollections.observableArrayList(marcas));
        ComboBox<String> comboModelo = new ComboBox<>(FXCollections.observableArrayList(modelos));
        ComboBox<String> comLocCondensadora = new ComboBox<>(FXCollections.observableArrayList(locCondensadoras));

        //precargar los datos para modificar.

        if(editar != null){
            textCondensadora.setText(editar.getCondensadora());
            spiNumSecuencia.getValueFactory().setValue(editar.getNumSecuencia());
            if(estados.contains(editar.getEstado())){
                comboEstado.setValue(editar.getEstado());
            }
            if(marcas.contains(editar.getMarca())){
                comboMarca.setValue(editar.getMarca());
            }
            if(modelos.contains(editar.getModelo())){
                comboModelo.setValue(editar.getModelo());
            }
            textNumSerie.setText(String.valueOf(editar.getNumSerieCond()));
            if(locCondensadoras.contains(editar.getLoc_condensadora())){
                comLocCondensadora.setValue(editar.getLoc_condensadora());
            }
            if(gases.contains(editar.getGas())){
                comboGas.setValue(editar.getGas());
            }
            if(editar.getFechaInstalacion() != null && editar.getFechaInstalacion().startsWith("DESINSTALADA")){
                desinstaladaCond.setSelected(true);
                dateFechaInst.setDisable(true);
            }else{
                try{
                    LocalDate fecha = LocalDate.parse(editar.getFechaInstalacion(),DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    dateFechaInst.setValue(fecha);
                } catch (Exception e) {
                    mainAppController.showAlert("fecha invalida o vacia");
                }
            }
            if(editar.getFechaRevision() != null && editar.getFechaRevision().startsWith("Posible fecha sin verificar")){
                fechaSinVerificar.setSelected(true);
                dateFechaRev.setDisable(true);
            }else{
                try{
                    LocalDate fecha = LocalDate.parse(editar.getFechaRevision(),DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    dateFechaRev.setValue(fecha);
                } catch (Exception e) {
                    mainAppController.showAlert("fecha invalida o vacia");
                }
            }
            textAveria.setText(editar.getAveria());
            textObservacion.setText(editar.getObservaciones());
        }

        //Creacion del formulario.
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(8);
        gridPane.setPadding(new Insets(15));

        int row = 0;

        gridPane.add(new Label("Condensadora: "), 0 , row);
        gridPane.add(textCondensadora, 1, row++);
        gridPane.add(new Label("num.Secuencia: "),0 , row);
        gridPane.add(spiNumSecuencia,1 ,row++);
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
        HBox hbFechaInst = new HBox(10,dateFechaInst,desinstaladaCond);
        gridPane.add(new Label("Fecha Instalación: "), 0 , row);
        gridPane.add(hbFechaInst, 1, row++);
        HBox hbFechaRev = new HBox(10,dateFechaRev,fechaSinVerificar);
        gridPane.add(new Label("Fecha Revisión: "), 0 , row);
        gridPane.add(hbFechaRev, 1, row++);
        gridPane.add(new Label("Averia: "), 0 , row);
        gridPane.add(textAveria, 1, row++);
        gridPane.add(new Label("Observación: "), 0 , row);
        gridPane.add(textObservacion, 1, row++);

        //Creacion de los botones guardar y cancelar
        Button btnGuardar = new Button("Guardar");
        Button btnCancelar = new Button("Cancelar");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        btnGuardar.setOnAction(e ->{
            String condensadora = textCondensadora.getText().trim();
            if(condensadora.isEmpty()){
                mainAppController.showAlert("El campo Condensadora es obligatorio.");
                return;
            }

            //Formatear fecha o texto
            String fechaInst = desinstaladaCond.isSelected()? "DESINSTALADA" + LocalDate.now().format(formatter): (dateFechaInst.getValue() != null? dateFechaInst.getValue().format(formatter) : "");
            String fechaRev = fechaSinVerificar.isSelected()? "Posible fecha sin verificar" + LocalDate.now().format(formatter): (dateFechaRev.getValue() != null? dateFechaRev.getValue().format(formatter) : "");
            try {
                List<String>filaNueva = Arrays.asList(
                        condensadora,
                        spiNumSecuencia.getValue().toString(),
                        comboEstado.getValue() != null? comboEstado.getValue() : "",
                        comboMarca.getValue() != null? comboMarca.getValue() : "",
                        comboModelo.getValue() != null? comboModelo.getValue() : "",
                        textNumSerie.getText().trim(),
                        comLocCondensadora.getValue() != null? comLocCondensadora.getValue() : "",
                        comboGas.getValue() != null? comboGas.getValue() : "",
                        fechaInst,
                        fechaRev,
                        textAveria.getText().trim(),
                        textObservacion.getText().trim()
                );
                if(editar == null){
                    ExcelManager.añadirFila("Condensadoras", filaNueva.toArray(new String[0]));
                    cargarDatos();
                }else{
                    List<String>antiguaFila = Arrays.asList(
                            editar.getCondensadora(),
                            String.valueOf(editar.getNumSecuencia()),
                            editar.getEstado(),
                            editar.getMarca(),
                            editar.getModelo(),
                            String.valueOf(editar.getNumSerieCond()),
                            editar.getLoc_condensadora(),
                            editar.getGas(),
                            editar.getFechaInstalacion(),
                            editar.getFechaRevision(),
                            editar.getAveria(),
                            editar.getObservaciones()
                    );
                    ExcelManager.modificarFila("Condensadoras", antiguaFila.toArray(new String[0]),filaNueva.toArray(new String[0]));
                    cargarDatos();
                }
                stage.close();

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

    @FXML
    public void onEditCond(){
        Condensadora seleccionada = tablaCondensadoras.getSelectionModel().getSelectedItem();
        if(seleccionada == null){
            mainAppController.showAlert("Selecciona una condensadora para modificar.");
            return;
        }
        //this.seleccionarCondensadora = seleccionada;
        onAddForm(seleccionada);
    }

    @FXML
    public void onDeleteCond(){
        Condensadora selected = tablaCondensadoras.getSelectionModel().getSelectedItem();
        if(selected == null){
            mainAppController.showAlert("Selecciona una condensadora para poder eliminarla");
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

    private List<String> cargarParametrosExcel(String hoja){
        List<String> lista = new ArrayList<>();
        try{
             List<List<String>> datos = ExcelManager.leerHoja(hoja);
            if(datos.size()>1){
                for(int i = 1; i<datos.size();i++){
                    List<String> fila = datos.get(i);
                    if(fila != null && !fila.isEmpty()){
                        String valor = fila.get(0).trim();
                        if(!valor.isEmpty() && !valor.equalsIgnoreCase("en blanco")) {
                            lista.add(valor);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error al cargar: " + hoja);
            e.printStackTrace();
        }
        return lista;
    }


    private void noOrdenar(){
        colCondensadoras.setSortable(false);
        colNumSecuencia.setSortable(false);
        colEstado.setSortable(false);
        colMarca.setSortable(false);
        colModelo.setSortable(false);
        colNumSerie.setSortable(false);
        colLocCondensadoras.setSortable(false);
        colGas.setSortable(false);
        colFechaInstal.setSortable(false);
        colFechaRev.setSortable(false);
        colAveria.setSortable(false);
        colObservaciones.setSortable(false);
    }
    /**private String formatearFecha(String valor) {
        if (valor == null || valor.isEmpty()) return "";
        if (valor.contains("DESINSTALADO") || valor.contains("Posible")) {
            return valor; // Mantener texto especial
        }

        // Intentar parsear como fecha
        try {
            // Soportar formatos: "1/1/07", "6/17/25", "7/1/24"
            DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("M/d/yy");
            LocalDate date = LocalDate.parse(valor, inputFormat);

            // Convertir año 2 dígitos a 4 dígitos con lógica razonable
            int year = date.getYear();
            if (year < 1950) year += 100; // Ej: 25 → 2025, 07 → 2007

            LocalDate corrected = LocalDate.of(year, date.getMonth(), date.getDayOfMonth());
            return corrected.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return valor; // Si falla, devolver el original
        }
    }**/

    /**private LocalDate parseDate( DateTimeFormatter formato, String dato){
        if(dato == null || dato.trim().isEmpty() || dato.contains("DESINSTALADO")){
            return null;
        }
        try{
            if(dato.contains("/")) {
                return LocalDate.parse(dato.trim(), formato);
            }else{
                return LocalDate.parse(dato.trim().split(" ")[2] + "/" +
                                getNumeroMes(dato.trim().split(" ")[1]) + "/" +
                                dato.trim().split(" ")[5],
                        DateTimeFormatter.ofPattern("dd/M/yyyy"));
            }
        }catch(DateTimeParseException e){
            return null;
        }
    }

    private String getNumeroMes(String numeroMes){
        return switch (numeroMes.toLowerCase()){
            case "jan" -> "1";
            case "feb" -> "2";
            case "mar" -> "3";
            case "apr" -> "4";
            case "may" -> "5";
            case "jun" -> "6";
            case "jul" -> "7";
            case "aug" -> "8";
            case "sep" -> "9";
            case "oct" -> "10";
            case "nov" -> "11";
            case "dec" -> "12";
            default -> "1";
        };
    }**/
}
