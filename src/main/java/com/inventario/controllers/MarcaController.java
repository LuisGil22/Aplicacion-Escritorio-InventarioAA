package com.inventario.controllers;

import com.inventario.models.Marca;
import com.inventario.utils.ExcelManager;
import com.inventario.utils.FilterUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.util.List;

/**
 * Controlador para la gestión de marcas en la hoja PARAM_MARCAS del inventario.
 * <p>
 * Proporciona funcionalidades para:
 * </p>
 * <ul>
 *   <li>Visualizar marcas registradas</li>
 *   <li>Añadir nuevas marcas</li>
 *   <li>Modificar marcas existentes</li>
 *   <li>Eliminar marcas (con validación de uso en Condensadoras)</li>
 *   <li>Filtrar por marca o descripción</li>
 * </ul>
 *
 * @author Luis Gil
 */
public class MarcaController {

    /** Campos FXML */
    @FXML private TableView<Marca> tablaMarca;
    @FXML private TableColumn<Marca,String> colMarca;
    @FXML private TableColumn<Marca,String> colDescripcion;
    @FXML private Button btnFiltroMarca;

    /** Dependencias */
    private MainAppController mainAppController;
    private ObservableList<Marca> marcas;

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
     * Configura columnas, carga datos y desactiva ordenación.
     */
    public void initialize(){
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        cargarDatos();
        noOrdenar();
    }

    /**
     * Metodo para cargar los datos de la hoja PARAM_MARCAS del archivo Excel y mostrarlos en la tabla.
     * Omite filas vacías o con valores nulos.
     */
    private void cargarDatos(){
        marcas = FXCollections.observableArrayList();
        var excelFile = ExcelManager.leerHoja("PARAM_MARCAS");
        for(int i=1; i<excelFile.size() ; i++){
            var fila = excelFile.get(i);
            if(!fila.isEmpty()){
                String marca = fila.get(0).trim();
                if(!marca.isEmpty()){
                    String desc = (fila.size() > 1) ? fila.get(1).trim() : "";
                    marcas.add(new Marca(marca,desc));
                }
            }
        }
        tablaMarca.setItems(marcas);
    }

    /**
     * Metodo para abrir un diálogo y añadir una nueva Marca.
     * Valida que el valor no esté vacío antes de guardar.
     */
    @FXML
    public void onAddMarca(){
        Dialog<Marca> dialog = crearDialogo(null);
        dialog.showAndWait().ifPresent(marca -> {
            tablaMarca.getItems().add(marca);
            ExcelManager.añadirFila("PARAM_MARCAS", marca.getMarca(), marca.getDescripcion());
        });
    }

    /**
     * Metodo para abrir un diálogo para modificar la Marca seleccionada.
     * Valida que haya una selección activa y que el nuevo valor no esté vacío.
     */
    @FXML
    public void onEditMarca(){
        Marca seleccionada = tablaMarca.getSelectionModel().getSelectedItem();
        if(seleccionada == null){
            mainAppController.showAlert("Selecciona una marca para modificar.");
            return;
        }
        Dialog<Marca>dialog = crearDialogo(seleccionada);
        dialog.showAndWait().ifPresent(nueva -> {
            List<List<String>> datos = ExcelManager.leerHoja("PARAM_MARCAS");
            int index = -1;
            for (int i = 1; i < datos.size(); i++) {
                List<String> fila = datos.get(i);

                if (fila.size() > 0 && fila.get(0).trim().equals(seleccionada.getMarca())) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {

                ExcelManager.modificarFila("PARAM_MARCAS", index, new String[]{nueva.getMarca(), nueva.getDescripcion()}
                );
                seleccionada.setMarca(nueva.getMarca());
                seleccionada.setDescripcion(nueva.getDescripcion());
                tablaMarca.refresh();
            } else {
                mainAppController.showAlert("No se encontró la Marca en el archivo.");
            }
        });
    }

    /**
     * Metodo para eliminar la Marca seleccionada tras confirmación.
     * Verifica que no esté en uso en las hojas Cassette o Condensadoras.
     */
    @FXML
    public void onDeleteMarca(){
        Marca seleccionada = tablaMarca.getSelectionModel().getSelectedItem();
        if(seleccionada == null){
            mainAppController.showAlert("Selecciona una marca para eliminar.");
            return;
        }
        String marcaAEliminar = seleccionada.getMarca();
        if(ExcelManager.existParametroEnCondensadoras(marcaAEliminar,ExcelManager.Columnas.MARCA)){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Esta acción no está permitida");
            alert.setHeaderText("No se puede eliminar esta marca");
            alert.setContentText("La marca '" +marcaAEliminar+ "' no se puede eliminar porque está siendo usada en la hoja 'Condensadoras'.");
            alert.showAndWait();
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar que la quieres eliminar");
        confirmacion.setHeaderText("¿Estas seguro que deseas eliminarla?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if(respuesta == ButtonType.OK){
                tablaMarca.getItems().remove(seleccionada);
                ExcelManager.eliminarFila("PARAM_MARCAS", marcaAEliminar);
            }
        });
    }


    private Dialog<Marca>crearDialogo(Marca marca){
        Dialog<Marca>dialog = new Dialog<>();
        dialog.setTitle(marca == null ? "Añadir Marca" : "Modificar Marca");
        dialog.setHeaderText(null);

        TextField marcaField = new TextField();
        TextField descField = new TextField();

        marcaField.setPromptText("Marca");
        descField.setPromptText("Descripcion");

        if(marca != null){
            marcaField.setText(marca.getMarca());
            descField.setText(marca.getDescripcion());
        }

        VBox vBox = new VBox(10, new Label("Marca: "),marcaField, new Label("Descripcion: "), descField);
        vBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(vBox);
        ButtonType añadirBoton = new ButtonType(marca == null? "Añadir":"Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(añadirBoton,ButtonType.CANCEL);

        dialog.setResultConverter(botonDialog -> {
            if(botonDialog == añadirBoton) {
                return new Marca(marcaField.getText(), descField.getText());
            }
            return null;
        });
        return dialog;
    }

    /**
     * Metodo para configurar el filtro de la columna MARCA.
     */
    @FXML
    private void configurarFiltroMarca(){
        FilterUtils.abrirFiltroGenerico("Filtrar por Marca", Marca::getMarca,btnFiltroMarca,tablaMarca,marcas);
    }

    /**
     * Metodo para desactivar la ordenación en la columna de la tabla.
     * Mejora la estabilidad visual al trabajar con datos no ordenados.
     */
    private void noOrdenar(){
        colMarca.setSortable(false);
        colDescripcion.setSortable(false);
    }
}
