package com.inventario.controllers;

import com.inventario.models.Averia;
import com.inventario.utils.ExcelManager;
import com.inventario.utils.FilterUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Controlador para la gestión de averías en la hoja AVERIAS del inventario.
 * <p>
 * Proporciona funcionalidades para:
 * </p>
 * <ul>
 *   <li>Visualizar averías registradas</li>
 *   <li>Filtrar por cualquier columna</li>
 *   <li>Marcar una avería como REPARADA (sin eliminarla)</li>
 *   <li>Actualizar automáticamente el estado del equipo origen (Cassette/Condensadora)</li>
 * </ul>
 *
 * @author Luis Gil
 */
@SuppressWarnings("ALL")
public class AveriaController {

    /**  Campos FXML  */
    @FXML private TableView<Averia> tablaAverias;
    @FXML private TableColumn<Averia,String> colNumAveria;
    @FXML private TableColumn<Averia,String> colEquipoAveriado;
    @FXML private TableColumn<Averia,String> colCodigo;
    @FXML private TableColumn<Averia,String> colEstado;
    @FXML private TableColumn<Averia,String> colPlanta;
    @FXML private TableColumn<Averia,String> colLocalizacion;
    @FXML private TableColumn<Averia,String> colFechaAveria;
    @FXML private TableColumn<Averia,String> colMail;
    @FXML private TableColumn<Averia,String> colObservaciones;

    @FXML private Button btnFiltroNumAveria;
    @FXML private Button btnFiltroEquipoAveriado;
    @FXML private Button btnFiltroCodigo;
    @FXML private Button btnFiltroEstado;
    @FXML private Button btnFiltroPlanta;
    @FXML private Button btnFiltroLocalizacion;
    @FXML private Button btnFiltroFechaAveria;
    @FXML private Button btnFiltroMail;

    @FXML private Label lblActualizado;

    /** Dependencias  */
    private MainAppController mainAppController;
    private ObservableList<Averia> allDatos;

    /**
     * Metodo para establecer la dependencia con el controlador principal de la aplicación.
     *
     * @param mainAppController instancia del controlador principal
     */
    public void setMainAppController(MainAppController mainAppController){
        this.mainAppController = mainAppController;
    }

    /**
     * Módulo que inicializa el controlador al cargar la vista FXML.
     * Configura la tabla, carga los datos y aplica ajustes de UI.
     */
    public void initialize(){
        configurarTabla();
        cargarDatos();
        noOrdenar();
        actualizarFechaEncabezado();
    }

    /**
     * Módulo que configura las columnas de la tabla para vincularlas con las propiedades del modelo Averia.
     */
    private void configurarTabla(){
        colNumAveria.setCellValueFactory(new PropertyValueFactory<>("numAveria"));
        colEquipoAveriado.setCellValueFactory(new PropertyValueFactory<>("equipoAveriado"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colPlanta.setCellValueFactory(new PropertyValueFactory<>("planta"));
        colLocalizacion.setCellValueFactory(new PropertyValueFactory<>("localizacion"));
        colFechaAveria.setCellValueFactory(new PropertyValueFactory<>("fechaAveria"));
        colMail.setCellValueFactory(new PropertyValueFactory<>("mail"));
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
    }

    /**
     * Módulo para cargar los datos de la hoja AVERIAS del archivo Excel y mostrarlos en la tabla.
     */
    public void cargarDatos(){
        allDatos = FXCollections.observableArrayList();
        List<List<String>> datos = ExcelManager.leerHoja("AVERIAS");

        for( int i = 1; i< datos.size(); i++){
            List<String> fila = datos.get(i);
            //System.out.println("Procesando fila " + i  + ": " + fila);

            if (fila.isEmpty()) continue;

            /** Condicion que asegura que la fila tenga al menos 9 columnas */
            if (fila.size() < 9) {
                while (fila.size() < 9) {
                    fila.add("");
                }
            }
            String numAveria = fila.get(0).trim();
            String equipoAveriado = fila.get(1).trim();
            String codigo = fila.get(2).trim();
            String estado = fila.get(3).trim();
            String planta = fila.get(4).trim();
            String localizacion = fila.get(5).trim();
            String fechaAveria = fila.get(6).trim();
            String mail = fila.get(7).trim();
            String observaciones = fila.get(8).trim();

            /** Condicion para saltar filas vacías (solo encabezado) */
            if(numAveria.isEmpty() && equipoAveriado.isEmpty()) continue;

            allDatos.add(new Averia(
                    numAveria,
                    equipoAveriado,
                    codigo,
                    estado,
                    planta,
                    localizacion,
                    fechaAveria,
                    mail,
                    observaciones
            ));
        }
        tablaAverias.setItems(allDatos);

        /** Ajusta el scroll horizontal */
        Platform.runLater(() -> {
            Node scrollNode = tablaAverias.lookup(".scroll-pane");
            if (scrollNode instanceof ScrollPane sp) {
                sp.setHvalue(0.0);
            }
        });
    }

    /**   Metodos para los filtros de las columnas de la hoja Averias */

    @FXML
    private void configurarFiltroNumAveria(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Numero de Avería", Averia::getNumAveria,btnFiltroNumAveria,tablaAverias,allDatos,(ascending) -> {
            ObservableList<Averia> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Averia::getNumAveria,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaAverias.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroEquipoAveriado(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Equipo Averiado", Averia::getEquipoAveriado,btnFiltroEquipoAveriado,tablaAverias,allDatos,(ascending) -> {
            ObservableList<Averia> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Averia::getEquipoAveriado,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaAverias.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroCodigo(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Código", Averia::getCodigo,btnFiltroCodigo,tablaAverias,allDatos,(ascending) -> {
            ObservableList<Averia> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Averia::getCodigo,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaAverias.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroEstado(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Estado", Averia::getEstado,btnFiltroEstado,tablaAverias,allDatos,(ascending) -> {
            ObservableList<Averia> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Averia::getEstado,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaAverias.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroPlanta(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Planta", Averia::getPlanta,btnFiltroPlanta,tablaAverias,allDatos,(ascending) -> {
            ObservableList<Averia> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Averia::getPlanta,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaAverias.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroLocalizacion(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Localización", Averia::getLocalizacion,btnFiltroLocalizacion,tablaAverias,allDatos,(ascending) -> {
            ObservableList<Averia> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Averia::getLocalizacion,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaAverias.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroFechaAveria(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Fecha", Averia::getFechaAveria,btnFiltroFechaAveria,tablaAverias,allDatos,(ascending) -> {
            ObservableList<Averia> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Averia::getFechaAveria,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaAverias.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroMail(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Mail Enviado", Averia::getMail,btnFiltroMail,tablaAverias,allDatos,(ascending) -> {
            ObservableList<Averia> sorted = FXCollections.observableArrayList(allDatos);
            sorted.sort(Comparator.comparing(
                    Averia::getMail,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaAverias.setItems(sorted);
        });
    }

    /** Modulos para accionar los botones */

    /**
     * Módulo para manejar el evento de modificar una avería (marcar como REPARADA).
     * <p>
     * Actualiza el estado en AVERIAS y automaticamente en el equipo origen (Cassette/Condensadora).
     * </p>
     */
    @FXML public void onEditAveria() {
        Averia sel = tablaAverias.getSelectionModel().getSelectedItem();
        if (sel == null || !"AVERIADO".equals(sel.getEstado())) {
            mainAppController.showAlert("Selecciona una avería.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reparar avería");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Marcar esta avería como REPARADA?");
        confirm.showAndWait().ifPresent(rep ->{
            if(rep == ButtonType.OK){
                /** Actualiza el estado en memória */
                sel.setEstado("REPARADO");
                int index = allDatos.indexOf(sel);
                if(index != -1){
                    allDatos.set(index,sel);
                }

                /** Guardar en la hoja Averias del Excel */
                List<String> actualizado = Arrays.asList(
                        sel.getNumAveria(),
                        sel.getEquipoAveriado(),
                        sel.getCodigo(),
                        sel.getEstado(),
                        sel.getPlanta(),
                        sel.getFechaAveria(),
                        sel.getLocalizacion(),
                        sel.getMail(),
                        sel.getObservaciones()
                );
                ExcelManager.modificarFila("AVERIAS",index + 1, actualizado.toArray(new String[0]));

                /** Actualiza el equipo Origen automaticamente */
                String equipo = sel.getEquipoAveriado();
                String codigo = sel.getCodigo();
                String hojaOrigen = "CASSETTE".equals(equipo) ? "Cassette" : "Condensadoras";
                int colEstado = 2;
                int colAveria = "CASSETTE".equals(equipo) ? 15 : 11;

                List<List<String>> datosOrigen = ExcelManager.leerHoja(hojaOrigen);
                for (int i = 1; i < datosOrigen.size(); i++){
                    List<String> fila = datosOrigen.get(i);
                    if (fila.size() > 0 && fila.get(0).trim().equals(codigo)){
                        /** Actualiza el estado a ACTIVA */
                        while (fila.size() <= colEstado) fila.add("");
                        fila.set(colEstado, "ACTIVA");

                        /** Limpia campo Averia */
                        while (fila.size() <= colAveria) fila.add("");
                        fila.set(colAveria, "");

                        ExcelManager.modificarFila(hojaOrigen, i, fila.toArray(new String[0]));
                        break;
                    }
                }

                /** Carga los datos y refresca la tabla */
                cargarDatos();
                Platform.runLater(() -> {
                    tablaAverias.refresh();
                });
            }
        });


    }

    /**
     * Módulo para manejar el evento de eliminar una avería (NO recomendado, solo para casos extremos).
     */
    @FXML public void onDeleteAveria() {
        Averia sel = tablaAverias.getSelectionModel().getSelectedItem();
        if (sel == null) { mainAppController.showAlert("Selecciona una avería."); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Confirmar eliminación");
        a.setHeaderText(null);
        a.setContentText("¿Eliminar esta avería?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                tablaAverias.getItems().remove(sel);
                ExcelManager.eliminarFila("AVERIAS", sel.getNumAveria());
            }
        });
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
    private void noOrdenar() {
        colNumAveria.setSortable(false);
        colEquipoAveriado.setSortable(false);
        colCodigo.setSortable(false);
        colEstado.setSortable(false);
        colPlanta.setSortable(false);
        colLocalizacion.setSortable(false);
        colFechaAveria.setSortable(false);
        colMail.setSortable(false);
        colObservaciones.setSortable(false);
    }
}
