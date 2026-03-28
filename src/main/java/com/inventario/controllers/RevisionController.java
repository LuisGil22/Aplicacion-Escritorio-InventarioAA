package com.inventario.controllers;

import com.inventario.models.Revision;
import com.inventario.utils.ExcelManager;
import com.inventario.utils.FilterUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador para la gestión de revisiones programadas en la hoja REVISIONES del inventario.
 * <p>
 * Las revisiones se generan automáticamente 4 meses después de la fecha de instalación de cada equipo,
 * mostrando un rango de fechas (desde/hasta) y creándose en la hoja cuando se alcanza la fecha "desde".
 * </p>
 *
 * @author Luis Gil
 */
public class RevisionController {
    /** Campos FXML */
    @FXML private TableView<Revision> tablaRevisiones;
    @FXML private TableColumn<Revision,String> colNumRevision;
    @FXML private TableColumn<Revision,String> colEquipo;
    @FXML private TableColumn<Revision,String> colCodigo;
    @FXML private TableColumn<Revision,String> colEstado;
    @FXML private TableColumn<Revision,String> colPlanta;
    @FXML private TableColumn<Revision,String> colLocalizacion;
    @FXML private TableColumn<Revision,String> colFechaRevision;
    @FXML private TableColumn<Revision,String> colRevision;
    @FXML private TableColumn<Revision,String> colObservaciones;
    @FXML private TableColumn<Revision,Boolean> colAccion;

    @FXML private Button btnFiltroNumRevision;
    @FXML private Button btnFiltroEquipo;
    @FXML private Button btnFiltroCodigo;
    @FXML private Button btnFiltroEstado;
    @FXML private Button btnFiltroPlanta;
    @FXML private Button  btnFiltroLocalizacion;
    @FXML private Button btnFiltroFechaRevision;
    @FXML private Button btnFiltroRevision;

    @FXML private Label lblActualizado;

    /** Dependencias */
    private ObservableList<Revision> revisiones;
    private MainAppController mainAppController;

    /**
     * Metodo para establecer la dependencia con el controlador principal de la aplicación.
     *
     * @param mainAppController instancia del controlador principal
     */
    public void setMainAppController(MainAppController mainAppController){
        this.mainAppController = mainAppController;
    }

    /**
     * Inicializa el controlador al cargar la vista FXML.
     * Carga las revisiones existentes y programa la verificación automática.
     */
    public void initialize(){
        configurarTabla();
        actualizarFechaEncabezado();
        cargarDatosRevisiones();
        //crearYVerificarRevisionesPendientes();
        /**Platform.runLater(() -> {
            new Thread(() -> {
                try {
                    Thread.sleep(100); // pequeño delay para que la UI se muestre primero
                    crearYVerificarRevisionesPendientes();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });*/
        noOrdenar();
        /**new Thread(() -> {
            try {
                Thread.sleep(300);
                ExcelManager.calcularYActualizarRevisionIndividual();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();*/
    }

    /**
     * Configura las columnas de la tabla para vincularlas con las propiedades del modelo Revision.
     * La columna ACCION contiene un checkbox para actualizar el campo REVISION.
     */
    private void configurarTabla(){
        colNumRevision.setCellValueFactory(new PropertyValueFactory<>("numRevision"));
        colEquipo.setCellValueFactory(new PropertyValueFactory<>("equipo"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colPlanta.setCellValueFactory(new PropertyValueFactory<>("planta"));
        colLocalizacion.setCellValueFactory(new PropertyValueFactory<>("localizacion"));
        colFechaRevision.setCellValueFactory(new PropertyValueFactory<>("fechaRevision"));
        colRevision.setCellValueFactory(new PropertyValueFactory<>("revision"));
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));

        colAccion.setCellValueFactory(param -> {
            Revision rev = param.getValue();
            boolean checked = "SI".equals(rev.getRevision());
            return new SimpleObjectProperty<>(checked);
        });
        colAccion.setCellFactory(CheckBoxTableCell.forTableColumn(colAccion));
        colAccion.setEditable(true);

        colAccion.setOnEditCommit(event -> {
            Revision rev = event.getRowValue();
            boolean valorNuevo = event.getNewValue();
            rev.setRevision(valorNuevo ? "SI" : "NO");
            actualizarRevisionEnExcel(rev);
        });
    }

    /**
     * Carga las revisiones existentes desde la hoja REVISIONES del archivo Excel.
     */
    private void cargarDatosRevisiones(){
        revisiones= FXCollections.observableArrayList();
        List<List<String>>datosRev = ExcelManager.leerHoja("REVISIONES");

        for(int i = 1; i < datosRev.size(); i++){
            List<String> filaRev = datosRev.get(i);
            if(filaRev.isEmpty() || filaRev.size() < 9) continue;
            while (filaRev.size() < 9){
                filaRev.add("");
            }

            String numRevision = filaRev.get(0).trim();
            String equipo = filaRev.get(1).trim();
            String codigo = filaRev.get(2).trim();
            String estado = filaRev.get(3).trim();
            String planta = filaRev.get(4).trim();
            String localizacion = filaRev.get(5).trim();
            String fechaRevision = filaRev.get(6).trim();
            String revision = filaRev.get(7).trim();
            String observaciones = filaRev.get(8).trim();

            if(numRevision.isEmpty() && equipo.isEmpty()) continue;

            revisiones.add(new Revision(
                    numRevision,equipo,codigo,estado,planta,localizacion,fechaRevision,revision,observaciones
            ));
        }
        tablaRevisiones.setItems(revisiones);
    }

    /**
     * Verifica equipos (Cassette y Condensadora) y crea revisiones pendientes
     * si la fecha desde ya ha sido alcanzada.
     */
    /**public void crearYVerificarRevisionesPendientes(){
        //cargarDatosRevisiones();

        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        int diasRevision = ExcelManager.getDiasRevision();
        //List<List<String>> revisionExcel = ExcelManager.leerHoja("REVISIONES");


        List<List<String>> cassettes = ExcelManager.leerHoja("Cassette");
        for(int i = 1; i < cassettes.size(); i++){
            List<String> filaCas = cassettes.get(i);
            if(filaCas.size() < 13 || filaCas.get(0).trim().isEmpty()) continue;

            String numCassette = filaCas.get(0).trim();
            //String numSecuenciaStr = filaCas.get(1).trim();
            //Integer numSecuencia = numSecuenciaStr.isEmpty() ? 1 : Integer.parseInt(numSecuenciaStr);
            String estado = filaCas.get(2).trim();
            String planta = filaCas.get(3).trim();
            String localizacion = filaCas.get(10).trim();
            String fechaInstStr = filaCas.get(12).trim();

            if(!"ACTIVA".equals(estado) || fechaInstStr.isEmpty()) continue;

            try{
                //LocalDate limite = LocalDate.now().minusMonths(6);
                LocalDate fechaInst = LocalDate.parse(fechaInstStr, formato);
                //LocalDate fechaHasta = fechaInst.plusMonths(4);
                LocalDate fechaRevision = ExcelManager.calcularProximaFechaRevision(fechaInst,diasRevision);
                LocalDate fechaDesde = fechaRevision.minusDays(30);


                 String fechaRevisionStr = fechaRevision.format(formato);
                 ExcelManager.actualizarFechaRevision("Cassette", numCassette, Integer.parseInt(filaCas.get(1).trim()), fechaRevisionStr);

                if(!hoy.isBefore(fechaDesde) && !existeRevision("CASSETTE", numCassette)){
                    String numSecuenciaStr = filaCas.get(1).trim();
                    Integer numSecuencia = numSecuenciaStr.isEmpty() ? 1 : Integer.parseInt(numSecuenciaStr);
                    crearRevisionAutomaticamente("CASSETTE", numCassette, estado, planta, localizacion, fechaInst,numSecuencia);
                }

            } catch (Exception e) {
                System.err.println("Error procesando cassette " + numCassette + ": " + e.getMessage());
            }

        }


        List<List<String>> condensadoras = ExcelManager.leerHoja("Condensadoras");
        for(int i = 1; i < condensadoras.size(); i++){
            List<String> filaCond = condensadoras.get(i);
            if(filaCond.size() < 9 || filaCond.get(0).trim().isEmpty()) continue;

            String condensadora = filaCond.get(0).trim();
            //String numSecuenciaStr = filaCond.get(1).trim();
            //Integer numSecuencia = numSecuenciaStr.isEmpty() ? 1 : Integer.parseInt(numSecuenciaStr);
            String estado = filaCond.get(2).trim();
            String estadoLimpio = estado.isEmpty() ? "" : estado;
            String localizacion = filaCond.get(6).trim();
            String fechaInstStr = filaCond.get(8).trim();

            if(!"ACTIVA".equals(estadoLimpio) || fechaInstStr.isEmpty()) continue;

            try{
                //LocalDate limite = LocalDate.now().minusMonths(6);
                LocalDate fechaInst = LocalDate.parse(fechaInstStr, formato);
                //LocalDate fechaHasta = fechaInst.plusMonths(4);
                LocalDate fechaRevision = ExcelManager.calcularProximaFechaRevision(fechaInst,diasRevision);
                LocalDate fechaDesde = fechaRevision.minusDays(30);


                String fechaRevisionStr = fechaRevision.format(formato);
                ExcelManager.actualizarFechaRevision("Condensadoras", condensadora, Integer.parseInt(filaCond.get(1).trim()), fechaRevisionStr);

                if(!hoy.isBefore(fechaDesde) && !existeRevision("CONDENSADORA", condensadora)){
                    String numSecuenciaStr = filaCond.get(1).trim();
                    Integer numSecuencia = numSecuenciaStr.isEmpty() ? 1 : Integer.parseInt(numSecuenciaStr);
                    crearRevisionAutomaticamente("CONDENSADORA", condensadora, estado, "", localizacion, fechaInst,numSecuencia);
                }

            } catch (Exception e) {
                System.err.println("Error procesando condensadora " + condensadora + ": " + e.getMessage());
            }

        }
        Platform.runLater(this::cargarDatosRevisiones);
    }*/

    /**
     * Verifica si ya existe una revisión para un equipo específico.
     */
    private boolean existeRevision(String equipo, String codigo){
        for(Revision rev : revisiones){
            if(rev.getEquipo().trim().equals(equipo.trim()) && rev.getCodigo().trim().equals(codigo.trim())){
                return true;
            }
        }
        return false;
    }

    /**
     * Crea una nueva revisión en la hoja REVISIONES con el formato de rango de fechas.
     */
    private void crearRevisionAutomaticamente(String equipo, String codigo, String estado, String planta, String localizacion, LocalDate fechaInstalacion,Integer numSecuencia){
        try{
            int numMax = 0;
            for(Revision rev : revisiones){
                try{
                    int num = Integer.parseInt(rev.getNumRevision());
                    if(num > numMax){
                        numMax = num;
                    }
                } catch (NumberFormatException ignored){}
            }
            String numRevision = String.format("%04d", numMax + 1);
            int diasRevision = ExcelManager.getDiasRevision();
            LocalDate fechaRevision = ExcelManager.calcularProximaFechaRevision(fechaInstalacion,diasRevision);
            LocalDate fechaDesde = fechaRevision.minusDays(30);

            DateTimeFormatter form = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String fechas = "desde: " + fechaDesde.format(form) + "\n hasta: " + fechaRevision.format(form);
            String fechaRevisionStr = fechaRevision.format(form);

            ExcelManager.añadirFila(
                    "REVISIONES",
                    numRevision,
                    equipo,
                    codigo,
                    estado,
                    planta,
                    localizacion,
                    fechas,
                    "NO",
                    ""
            );

            // ACTUALIZAR FECHA_REVISION EN LA HOJA DE ORIGEN
            //String fechaHastaStr = fechaHasta.format(form);

            /**if ("CASSETTE".equals(equipo)) {
                ExcelManager.actualizarFechaRevision("Cassette", codigo,numSecuencia, fechaRevisionStr);
            } else if ("CONDENSADORA".equals(equipo)) {
                ExcelManager.actualizarFechaRevision("Condensadoras", codigo,numSecuencia, fechaRevisionStr);
            }*/

            Revision nuevaRevision = new Revision(numRevision,equipo,codigo,estado,planta,localizacion,fechas,"NO","");
            revisiones.add(nuevaRevision);
            //System.out.println("Revision creada: "+ numRevision + "(" + equipo + " " + codigo + ")");
        } catch (Exception e) {
            System.err.println("Error al crear revisión automática: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Actualiza una revisión en la hoja REVISIONES del archivo Excel.
     *
     * @param revision revisión modificada
     */
    private void actualizarRevisionEnExcel(Revision revision){
        try {
            List<List<String>> datos = ExcelManager.leerHoja("REVISIONES");
            for (int i = 1; i < datos.size(); i++) {
                List<String> fila = datos.get(i);
                if (fila.size() > 0 &&
                        fila.get(0).trim().equals(revision.getNumRevision())) {
                    // Actualizar columna REVISION (índice 7)
                    while (fila.size() <= 7) fila.add("");
                    fila.set(7, revision.getRevision());
                    ExcelManager.modificarFila("REVISIONES", i, fila.toArray(new String[0]));
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar revisión en Excel: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    @FXML
    private void configurarFiltroNumRevision(){
        FilterUtils.abrirFiltroGenerico("Filtrar por NumRevision", Revision::getNumRevision, btnFiltroNumRevision, tablaRevisiones,revisiones);
    }
    @FXML
    private void configurarFiltroEquipo(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Equipo", Revision::getEquipo, btnFiltroEquipo, tablaRevisiones,revisiones);
    }
    @FXML
    private void configurarFiltroCodigo(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Codigo", Revision::getCodigo, btnFiltroCodigo, tablaRevisiones,revisiones);
    }
    @FXML
    private void configurarFiltroEstado(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Estado", Revision::getEstado, btnFiltroEstado, tablaRevisiones,revisiones);
    }
    @FXML
    private void configurarFiltroPlanta(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Planta", Revision::getPlanta, btnFiltroPlanta, tablaRevisiones,revisiones);
    }
    @FXML
    private void configurarFiltroLocalizacion(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Localización", Revision::getLocalizacion, btnFiltroLocalizacion, tablaRevisiones,revisiones);
    }
    @FXML
    private void configurarFiltroFechaRevision(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Fecha Revision", Revision::getFechaRevision, btnFiltroFechaRevision, tablaRevisiones,revisiones);
    }
    @FXML
    private void configurarFiltroRevision(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Revision", Revision::getRevision, btnFiltroRevision, tablaRevisiones,revisiones);
    }

    /** Metodos Auxiliares */

    /**
     * Metodo para actualizar la etiqueta de fecha en el encabezado con la fecha actual.
     */
    private void actualizarFechaEncabezado() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblActualizado.setText("Actualizado: " + LocalDate.now().format(fmt));
    }

    /**
     * Metodo para desactivar la ordenación en todas las columnas de la tabla.
     */
    private void noOrdenar(){
        colNumRevision.setSortable(false);
        colEquipo.setSortable(false);
        colCodigo.setSortable(false);
        colEstado.setSortable(false);
        colPlanta.setSortable(false);
        colLocalizacion.setSortable(false);
        colFechaRevision.setSortable(false);
        colRevision.setSortable(false);
        colObservaciones.setSortable(false);
        colAccion.setSortable(false);
    }

   /** @FXML
    private void generarRevisionesPendientes() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Generar revisiones");
        confirm.setHeaderText("¿Crear revisiones para equipos cuya fecha 'desde' ya ha llegado?");
        confirm.setContentText("Se crearán revisiones para equipos ACTIVOS con fecha_instalacion + 3 meses ≤ hoy.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                crearYVerificarRevisionesPendientes();
                Platform.runLater(() -> {
                    cargarDatosRevisiones();
                    mainAppController.showAlert("Revisión generada: " + revisiones.size() + " entradas.");
                });
            }
        });
    }*/
}
