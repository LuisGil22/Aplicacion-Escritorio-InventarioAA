package com.inventario.controllers;

import com.inventario.models.Revision;
import com.inventario.utils.ExcelManager;
import com.inventario.utils.FilterUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
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
@SuppressWarnings("ALL")
public class RevisionController {
    /** Campos FXML */
    @FXML public TableView<Revision> tablaRevisiones;
    @FXML private TableColumn<Revision,String> colNumRevision;
    @FXML private TableColumn<Revision,String> colEquipo;
    @FXML private TableColumn<Revision,String> colCodigo;
    @FXML private TableColumn<Revision,String> colEstado;
    @FXML private TableColumn<Revision,String> colPlanta;
    @FXML private TableColumn<Revision,String> colLocalizacion;
    @FXML private TableColumn<Revision,String> colFechaRevision;
    @FXML private TableColumn<Revision,String> colRevision;
    @FXML private TableColumn<Revision,String> colObservaciones;
    @FXML private TableColumn<Revision,String> colEnviarMail;
    @FXML private TableColumn<Revision,String> colDiasRestantes;
    @FXML private TableColumn<Revision,Boolean> colAccion;

    @FXML private Button btnEliminarRevision;

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
        noOrdenar();

        ExcelManager.revisionActualizada.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                cargarDatosRevisiones(); // recargar desde Excel
                tablaRevisiones.refresh();
            }
        });

        /** Configura el estilo y comportamiento de la columna
         *  de observaciones para permitir texto multilínea
         */
        if(colObservaciones != null) {
            colObservaciones.getStyleClass().add("col-observaciones");
            colObservaciones.setCellFactory(column -> new TableCell<Revision, String>() {
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
        colEnviarMail.setCellValueFactory(new PropertyValueFactory<>("enviarMail"));
        colDiasRestantes.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDiasRestantes() ));

        colAccion.setCellFactory(param -> new TableCell<Revision, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            private final Tooltip tooltip = new Tooltip("Si lo marcas, se actualizara a Revisado (SI) y no se podrá deshacer.");

            {
                checkBox.setTooltip(tooltip);
                checkBox.setOnAction(e -> {
                    Revision rev = getTableRow() != null ? getTableRow().getItem() : null;
                    if (rev == null) return;

                    if ("NO".equals(rev.getRevision())) {
                        rev.setRevision("SI");
                        actualizarRevisionEnExcel(rev);
                        checkBox.setDisable(true);
                    }
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item);

                    Revision rev = getTableRow() != null ? getTableRow().getItem() : null;
                    if (rev != null && "SI".equals(rev.getRevision())) {
                        checkBox.setDisable(true);
                    } else {
                        checkBox.setDisable(false);
                    }
                    setGraphic(checkBox);
                }
            }
        });

        colAccion.setCellValueFactory(param -> {
            String revStr = param.getValue().getRevision();
            boolean isChecked = "SI".equals(revStr);
            return new SimpleObjectProperty<>(isChecked);
        });
        tablaRevisiones.setEditable(false);
    }

    /**
     * Carga las revisiones existentes desde la hoja REVISIONES del archivo Excel.
     */
    public void cargarDatosRevisiones(){
        revisiones= FXCollections.observableArrayList();
        List<List<String>>datosRev = ExcelManager.leerHoja("REVISIONES");

        for(int i = 1; i < datosRev.size(); i++){
            List<String> filaRev = datosRev.get(i);
            if(filaRev.isEmpty() || filaRev.size() < 9) continue;
            while (filaRev.size() < 11){
                filaRev.add("");
            }

            String numRevision = String.format("%04d",
                    Integer.parseInt(filaRev.get(0).trim())
            );
            String equipo = filaRev.get(1).trim();
            String codigo = filaRev.get(2).trim();
            String estado = filaRev.get(3).trim();
            String planta = filaRev.get(4).trim();
            String localizacion = filaRev.get(5).trim();
            String fechaRevision = filaRev.get(6).trim();
            String revision = filaRev.get(7).trim();
            String observaciones = filaRev.get(8).trim();
            String enviarMail = filaRev.size() > 9 ? filaRev.get(9).trim() : "NO ENVIADO";

            if(numRevision.isEmpty() && equipo.isEmpty()) continue;

            Revision rev = new Revision(numRevision,equipo,codigo,estado,planta,localizacion,fechaRevision,revision,observaciones,enviarMail);
            rev.calcularDiasRestantesRevision();
            revisiones.add(rev);

        }
        tablaRevisiones.setItems(revisiones);
    }

    /**
     * Actualiza una revisión en la hoja REVISIONES del archivo Excel.
     *
     * @param revision revisión modificada
     */
    private void actualizarRevisionEnExcel(Revision revision){
        System.out.println(" actualizarRevisionEnExcel llamado. NumRev: " + revision.getNumRevision() + ", Revision: " + revision.getRevision());
        try {
            List<List<String>> datos = ExcelManager.leerHoja("REVISIONES");
            for (int i = 1; i < datos.size(); i++) {
                List<String> fila = datos.get(i);
                if (fila.size() > 0 && revision.getNumRevision().equals(fila.get(0).trim())) {
                    while (fila.size() <= 7) fila.add("");
                    fila.set(7, "SI");
                    ExcelManager.modificarFila("REVISIONES", i, fila.toArray(new String[0]));

                    ExcelManager.enviarCorreoConfirmacionRevision(revision.getEquipo(), revision.getCodigo());
                    break;
                }
            }

            for (Revision r : revisiones) {
                if (r.getNumRevision().equals(revision.getNumRevision())) {
                    r.setRevision("SI");
                    break;
                }
            }
            tablaRevisiones.refresh();

            String equipo = revision.getEquipo();
            String codigo = revision.getCodigo();
            String hojaOrigen = "CONDENSADORA".equals(equipo) ? "Condensadoras" : "Cassette";
            List<List<String>> datosOrigen = ExcelManager.leerHoja(hojaOrigen);

            for (int i = 1; i < datosOrigen.size(); i++){
                List<String> fila = datosOrigen.get(i);
                if (fila.size() > 1 && codigo.equals(fila.get(0).trim()) && "ACTIVA".equals(fila.get(2).trim())){
                    int diasRevision = 365;
                    int indiceDias = "Condensadoras".equals(hojaOrigen) ? 13 : 18;
                    if (fila.size() > indiceDias && !fila.get(indiceDias).trim().isEmpty()) {
                        try {
                            diasRevision = Integer.parseInt(fila.get(indiceDias).trim());
                        } catch (Exception ignored) {}
                    }

                    int colFechaRev = "Condensadoras".equals(hojaOrigen) ? 10 : 14;
                    String fechaRevActualStr = "";
                    if (fila.size() > colFechaRev) {
                        fechaRevActualStr = fila.get(colFechaRev).trim();
                    }

                    if (!fechaRevActualStr.isEmpty()) {
                        LocalDate fechaRevActual = LocalDate.parse(fechaRevActualStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        LocalDate nuevaFechaRev = fechaRevActual.plusDays(diasRevision);
                        String frStr = nuevaFechaRev.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                        while (fila.size() <= colFechaRev) fila.add("");
                        fila.set(colFechaRev, frStr);

                        ExcelManager.modificarFila(hojaOrigen, i, fila.toArray(new String[0]));

                        LocalDate fechaDesde = nuevaFechaRev.minusDays(30);
                        if (!LocalDate.now().isBefore(fechaDesde)) {
                            String planta = "CONDENSADORA".equals(equipo) ? "" : fila.get(3).trim();
                            String localizacion = "CONDENSADORA".equals(equipo) ?  (fila.size() > 6 ? fila.get(6).trim() : "") : (fila.size() > 10 ? fila.get(10).trim() : "");
                            String estado = "ACTIVA";

                            ExcelManager.crearEntradaRevision(equipo, codigo, estado, planta, localizacion, nuevaFechaRev, fechaDesde);


                        }
                    }
                    break;
                }
            }

                Platform.runLater(() -> {
                for (Revision r : revisiones) {
                    if (r.getNumRevision().equals(revision.getNumRevision())) {
                        r.setRevision(revision.getRevision()); // actualiza el objeto en memoria
                        break;
                    }
                }
                tablaRevisiones.refresh();

            });

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * metodo para obtener los dias de revision desde la fila del equipo en su hoja correspondiente,
     * dependiendo si es condensadora o cassette, con un valor por defecto de 365 dias si no se encuentra
     * el valor o hay algun error. Se asume que el indice de la columna de dias de revision es fijo (13 para condensadoras y 18 para cassette),
     * pero se verifica que exista antes de intentar leerlo.
     */
    private int obtenerDiasRevisionDeFila(List<String> fila, String hoja) {
        //int indiceDias = "Condensadoras".equals(hoja) ? 13 : 15; // Cassette tiene más columnas
        if ("Condensadoras".equals(hoja) && fila.size() > 13) {
            String valor = fila.get(13).trim();
            if(!valor.isEmpty()) {
                try {
                    return Integer.parseInt(valor);
                } catch (NumberFormatException e) {
                    // fallback
                }
            }
        }
        return 365;
    }

    /** Metodos para configurar los filtros de cada columna. Se utiliza la clase FilterUtils para mostrar un menú de selección de valores únicos en la columna, con opciones de ordenación ascendente/descendente. Al aplicar un filtro, se ordena la tabla según el valor seleccionado y el orden indicado.
     * Cada metodo corresponde a una columna específica y llama a FilterUtils.abrirFiltroGenerico con los parámetros adecuados.
     */
    @FXML
    private void configurarFiltroNumRevision(){
        FilterUtils.abrirFiltroGenerico("Filtrar por NumRevision", Revision::getNumRevision, btnFiltroNumRevision, tablaRevisiones,revisiones,(ascending) -> {
            ObservableList<Revision> sorted = FXCollections.observableArrayList(revisiones);
            sorted.sort(Comparator.comparing(
                    Revision::getNumRevision,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaRevisiones.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroEquipo(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Equipo", Revision::getEquipo, btnFiltroEquipo, tablaRevisiones,revisiones,(ascending) -> {
            ObservableList<Revision> sorted = FXCollections.observableArrayList(revisiones);
            sorted.sort(Comparator.comparing(
                    Revision::getEquipo,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaRevisiones.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroCodigo(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Codigo", Revision::getCodigo, btnFiltroCodigo, tablaRevisiones,revisiones,(ascending) -> {
            ObservableList<Revision> sorted = FXCollections.observableArrayList(revisiones);
            sorted.sort(Comparator.comparing(
                    Revision::getCodigo,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaRevisiones.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroEstado(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Estado", Revision::getEstado, btnFiltroEstado, tablaRevisiones,revisiones,(ascending) -> {
            ObservableList<Revision> sorted = FXCollections.observableArrayList(revisiones);
            sorted.sort(Comparator.comparing(
                    Revision::getEstado,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaRevisiones.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroPlanta(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Planta", Revision::getPlanta, btnFiltroPlanta, tablaRevisiones,revisiones,(ascending) -> {
            ObservableList<Revision> sorted = FXCollections.observableArrayList(revisiones);
            sorted.sort(Comparator.comparing(
                    Revision::getPlanta,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaRevisiones.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroLocalizacion(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Localización", Revision::getLocalizacion, btnFiltroLocalizacion, tablaRevisiones,revisiones,(ascending) -> {
            ObservableList<Revision> sorted = FXCollections.observableArrayList(revisiones);
            sorted.sort(Comparator.comparing(
                    Revision::getLocalizacion,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaRevisiones.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroFechaRevision(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Fecha Revision", Revision::getFechaRevision, btnFiltroFechaRevision, tablaRevisiones,revisiones,(ascending) -> {
            ObservableList<Revision> sorted = FXCollections.observableArrayList(revisiones);
            sorted.sort(Comparator.comparing(
                    Revision::getFechaRevision,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaRevisiones.setItems(sorted);
        });
    }
    @FXML
    private void configurarFiltroRevision(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Revision", Revision::getRevision, btnFiltroRevision, tablaRevisiones,revisiones,(ascending) -> {
            ObservableList<Revision> sorted = FXCollections.observableArrayList(revisiones);
            sorted.sort(Comparator.comparing(
                    Revision::getRevision,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));
            if (!ascending) Collections.reverse(sorted);
            tablaRevisiones.setItems(sorted);
        });
    }

    /**
     *  Metodo para eliminar una revisión seleccionada de la tabla y de la hoja REVISIONES del archivo Excel.
     *  Se muestra una confirmación antes de eliminar, indicando el número de revisión.
     *  Si se confirma, se elimina la revisión de la tabla y se llama a ExcelManager.eliminarFila para eliminarla del Excel.
     *  Luego se muestra un mensaje de éxito.
     */
    @FXML
    private void onDeleteRevision() {
        Revision selected = tablaRevisiones.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mainAppController.showAlert("Selecciona una revisión para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar esta revisión?");
        confirm.setContentText("Esta acción no se puede deshacer.\nNúmero de revisión: " + selected.getNumRevision());
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {

                    tablaRevisiones.getItems().remove(selected);
                    ExcelManager.eliminarFila("REVISIONES", selected.getNumRevision());

                    // Eliminar de la lista en memoria
                    //revisiones.remove(selected);

                    // Refrescar tabla
                    //tablaRevisiones.refresh();

                    mainAppController.showAlert("Revisión eliminada correctamente.");

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

    /**
     * Metodo para abrir el diálogo de observaciones al hacer clic en una celda de la columna de observaciones.
     * Permite editar las observaciones y guarda los cambios tanto en memoria como en el archivo Excel.
     */
    @FXML
    private void abrirObservacionesRevision() {
        Revision selected = tablaRevisiones.getSelectionModel().getSelectedItem();
        if (selected == null){
            mainAppController.showAlert("Selecciona una Revision para crear las observaciones");
            return;
        }
        //System.out.println("Buscando en Excel -> Hoja: REVISIONES, Codigo: " + selected.getNumRevision());
        mainAppController.abrirDialogoObservaciones(
                "Observaciones - Revision " + selected.getNumRevision(),
                selected.getObservaciones(),
                nuevaObs -> {
                    selected.setObservaciones(nuevaObs);

                    int indiceFila = ExcelManager.obtenerIndiceFilaPorCodigo(
                            "REVISIONES",
                            selected.getNumRevision()
                    );

                    if (indiceFila != -1) {
                        ExcelManager.actualizarCeldaObservacionConEstilo("REVISIONES", indiceFila, 8, nuevaObs);
                    }else{
                        System.err.println("Error: No se encontró la fila en Excel para " + selected.getNumRevision());
                    }
                    tablaRevisiones.refresh();
                }
        );
    }
}
