package com.inventario.controllers;

import com.inventario.models.Condensadora;
import com.inventario.utils.ExcelManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Predicate;

import static java.lang.Integer.parseInt;

public class CondensadorasController {
    @FXML
    private TableView<Condensadora> tablaCondensadoras;
    @FXML
    private TableColumn<Condensadora,String>colCondensadoras;
    @FXML
    private TableColumn<Condensadora,String>colNumSecuencia;
    @FXML
    private TableColumn<Condensadora,String>colEstado;
    @FXML
    private TableColumn<Condensadora,String>colMarca;
    @FXML
    private TableColumn<Condensadora,String>colModelo;
    @FXML
    private TableColumn<Condensadora,String>colNumSerie;
    @FXML
    private TableColumn<Condensadora,String>colLocCondensadoras;
    @FXML
    private TableColumn<Condensadora,String>colGas;
    @FXML
    private TableColumn<Condensadora,String>colFechaInstal;
    @FXML
    private TableColumn<Condensadora,String>colFechaRev;
    @FXML
    private TableColumn<Condensadora,String>colAveria;
    @FXML
    private TableColumn<Condensadora,String>colObservaciones;

    @FXML
    private ComboBox<String> comboFiltroEstado;
    @FXML
    private Button btnFiltrar;


    private MainAppController mainAppController;
    private ObservableList<Condensadora> allDatos;
    private Boolean filtroActivo = true;
    private Boolean filtroBaja = true;

    //private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    @FXML
    public void initialize(){
        confColumnas();
        cargarFiltroEstados();
        cargarDatos();
        noOrdenar();
    }

    private void confColumnas(){
        colCondensadoras.setCellValueFactory(new PropertyValueFactory<>("condensadoras"));
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

    private void cargarFiltroEstados(){
        ObservableList<String>estados = FXCollections.observableArrayList("Todos");
        var datos = ExcelManager.leerHoja("PARAM_ESTADO");
        for(int i=1;i<datos.size();i++){
            if(!datos.get(i).isEmpty() && !datos.get(i).get(0).trim().isEmpty()){
                estados.add(datos.get(i).get(0));
            }
        }
        comboFiltroEstado.setItems(estados);
        comboFiltroEstado.setValue("Todos");
    }

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
            int numSecuencia = numSecuenciaStr.trim().isEmpty() ? 0 : parseInt(numSecuenciaStr);
            long numSerieLong = numSerie.trim().isEmpty() ? 0L : parseLong(numSerie);
            fechaInst = (fechaInst != null) ? fechaInst.trim() : "";
            fechaRev = (fechaRev != null) ? fechaRev.trim() : "";
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
    }

    public void configurarFiltroEstado(){

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
    }


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


    private void aplicarFiltro(CheckBox chkActivo, CheckBox chkBaja) {
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
