package com.inventario.controllers;

import com.inventario.models.Cassette;
import com.inventario.models.Condensadora;
import com.inventario.utils.ExcelManager;
import com.inventario.utils.FilterUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

public class CassetteController {
    @FXML private TableView<Cassette>tablaCassette;
    @FXML private TableColumn<Cassette,Integer>colNumCassette;
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
    @FXML private TableColumn<Cassette,String>colFechaRev;
    @FXML private TableColumn<Cassette,String>colAveria;
    @FXML private TableColumn<Cassette,String>colFoto;

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
    @FXML private Button btnFiltroFechaRev;
    @FXML private Button btnFiltroAveria;
    @FXML private Button btnFiltroFoto;

    private MainAppController mainAppController;
    private ObservableList<Cassette> allDatos;

    private final Map<String, Set<String>> selectedValores = new HashMap<>();
    private final Map<String,Boolean>seleccionarTodos = new HashMap<>();
    private Button botonActual;

    @FXML
    public void initialize(){
        configurarColumnasCas();
        cargarDatosCas();
        noOrdenar();
    }

    private void configurarColumnasCas(){
        colNumCassette.setCellValueFactory(new PropertyValueFactory<>("numCassette"));
        colNumSecuencia.setCellValueFactory(new PropertyValueFactory<>("numSecuencia"));
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
        colFechaRev.setCellValueFactory(new PropertyValueFactory<>("fechaRevision"));
        colAveria.setCellValueFactory(new PropertyValueFactory<>("averia"));
        colFoto.setCellValueFactory(new PropertyValueFactory<>("foto"));
    }

    private void cargarDatosCas(){
        allDatos = FXCollections.observableArrayList();
        List<List<String>>datos = ExcelManager.leerHoja("Cassette");
        for(int i = 1;i<datos.size();i++){
            List<String>fila = datos.get(i);

            String numCassette = fila.get(0);
            String estado = fila.get(2);

            if (numCassette.isEmpty() &&
                    !estado.equals("ACTIVA") &&
                    !estado.equals("BAJA") &&
                    !estado.isEmpty()) {
                continue;
            }

            long valoresNoVacios = fila.stream().filter(s -> !s.trim().isEmpty()).count();
            if (valoresNoVacios < 3) {
                continue;
            }

            try{

                String numSecuencia = fila.get(1);

                String planta = fila.get(3);
                String nombre = fila.get(4);
                Double potenciaCalor =parseDouble(fila.get(5));
                Double potenciaFrio = parseDouble(fila.get(6));
                String marcaModelo = fila.get(7);
                String numSerieCas = fila.get(8);
                String condensadora = fila.get(9);
                String localizacionCondensadora = limpiarZero(fila.get(10));
                String gas = limpiarZero(fila.get(11));
                String fechaInstalacion = fila.get(12).trim();
                String fechaRevision = fila.get(13).trim();
                String averia = fila.get(14).trim();
                String foto = fila.get(15).trim();

                if(numCassette.isEmpty()&&numSecuencia.isEmpty()&&estado.isEmpty()) continue;

                allDatos.add(new Cassette(
                        numCassette,numSecuencia,estado,planta,nombre,
                        potenciaCalor,potenciaFrio,marcaModelo,numSerieCas,condensadora,
                        localizacionCondensadora,gas,fechaInstalacion,fechaRevision,averia,foto
                ));

            } catch (Exception e) {
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

    private String limpiarZero(String s) {
        if (s == null || s.trim().isEmpty()) return "";
        String t = s.trim();
        if ("0".equals(t) || "0.0".equals(t) || "0,0".equals(t)) return "";
        return t;
    }


    @FXML
    private void configurarFiltroNumCassette(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Num_Cassette",Cassette::getNumCassette,btnFiltroNumCassette,tablaCassette,allDatos);
    }
    @FXML
    private void configurarFiltroNumSecuencia(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Num_Secuencia",Cassette::getNumSecuencia,btnFiltroNumSecuencia,tablaCassette,allDatos);
    }
    @FXML
    private void configurarFiltroEstado(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Estado",Cassette::getEstado,btnFiltroEstado,tablaCassette,allDatos);
    }
    @FXML
    private void configurarFiltroPlanta(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Planta",Cassette::getPlanta,btnFiltroPlanta,tablaCassette,allDatos);
    }
    @FXML
    private void configurarFiltroNombre(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Nombre",Cassette::getNombre,btnFiltroNombre,tablaCassette,allDatos);
    }
    @FXML
    private void configurarFiltroPotenciaCalor(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Potencia_Calor",item -> String.valueOf(item.getPotenciaCalor()),btnFiltroPotenciaCalor,tablaCassette,allDatos);
    }
    @FXML
    private void configurarFiltroPotenciaFrio(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Potencia_Frio",item -> String.valueOf(item.getPotenciaFrio()),btnFiltroPotenciaFrio,tablaCassette,allDatos);
    }
    @FXML
    private void configurarFiltroMarcaModelo(){
        FilterUtils.abrirFiltroGenerico("Filtrar por MarcaModelo",Cassette::getMarcaModelo,btnFiltroMarcaModelo,tablaCassette,allDatos);
    }
    @FXML
    private void configurarFiltroNumSerieCas(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Num_Serie_Cas",Cassette::getNumSerieCas,btnFiltroNumSerieCas,tablaCassette,allDatos);
    }
    @FXML
    private void configurarFiltroCondensadora(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Condensadora",Cassette::getCondensadora,btnFiltroCondensadora,tablaCassette,allDatos);
    }
    @FXML
    private void configurarFiltroLocalizacionCondensadora(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Localizacion_Condensadora",Cassette::getLocalizacionCondensadora,btnFiltroLocalizacionCondensadora,tablaCassette,allDatos);
    }
    @FXML
    private void configurarFiltroGas(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Gas",Cassette::getGas,btnFiltroGas,tablaCassette,allDatos);
    }
    @FXML
    private void configurarFiltroFechaInst(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Fecha_Instalacion",Cassette::getFechaInstalacion,btnFiltroFechaInst,tablaCassette,allDatos);
    }
    @FXML
    private void configurarFiltroFechaRev(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Fecha_Revision",Cassette::getFechaRevision,btnFiltroFechaRev,tablaCassette,allDatos);
    }
    @FXML
    private void configurarFiltroAveria(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Averia",Cassette::getAveria,btnFiltroAveria,tablaCassette,allDatos);
    }
    @FXML
    private void configurarFiltroFoto(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Foto",Cassette::getFoto,btnFiltroFoto,tablaCassette,allDatos);
    }


    @FXML
    public void onAddCas(){

    }
    @FXML
    public void onEditCas(){

    }
    @FXML
    public void onDeleteCas(){

    }

    private boolean esClaveCompuestaDuplicada(String numCassette, String numSecuencia, Cassette excluir) {
        for (Cassette c : allDatos) {
            if (c.equals(excluir)) continue;
            if (c.getNumCassette().equals(numCassette) && c.getNumSecuencia().equals(numSecuencia)) {
                return true;
            }
        }
        return false;
    }

    private void parseAndSetDate(DatePicker picker, String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) return;
        try {
            LocalDate date = LocalDate.parse(fechaStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            picker.setValue(date);
        } catch (Exception ignored) {}
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

    /**private int parseInt(String dato) {
        try {
            return dato == null || dato.trim().isEmpty() ? 0 : Integer.parseInt(dato.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }**/

    private Double parseDouble(String dato) {
        if(dato == null || dato.trim().isEmpty()){
            return null;
        }
        try {
            return Double.parseDouble(dato.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

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
        colFechaRev.setSortable(false);
        colAveria.setSortable(false);
        colFoto.setSortable(false);
    }

}

