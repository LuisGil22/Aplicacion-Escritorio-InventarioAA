package com.inventario.controllers;

import com.inventario.models.Correo_Electronico;
import com.inventario.utils.ExcelManager;
import com.inventario.utils.FilterUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * Controlador para la gestión de correos electrónicos en la hoja PARAM_CORREOS_ELECTRONICOS del inventario.
 * <p>
 * Proporciona funcionalidades para:
 * </p>
 * <ul>
 *   <li>Visualizar correos registrados</li>
 *   <li>Añadir nuevos correos</li>
 *   <li>Modificar correos existentes</li>
 *   <li>Eliminar correos</li>
 *   <li>Filtrar por correo electrónico</li>
 * </ul>
 *
 * @author Luis Gil
 */
public class CorreoController {

    /** Campos FXML */
    @FXML private TableView<Correo_Electronico> tablaCorreos;
    @FXML private TableColumn<Correo_Electronico,String> colCorreoElectronico;
    @FXML private Button btnFiltroCorreo;

    /** Dependencias */
    private MainAppController mainAppController;
    private ObservableList<Correo_Electronico> correos;

    /**
     * Metodo para establecer la dependencia con el controlador principal de la aplicación.
     *
     * @param mainController instancia del controlador principal
     */
    public void setMainAppController(MainAppController mainController) {
        this.mainAppController = mainController;
    }

    /**
     * Metodo para inicializar el controlador al cargar la vista FXML.
     * Configura columnas, carga datos y desactiva ordenación.
     */
    @FXML
    public void initialize(){
        colCorreoElectronico.setCellValueFactory(new PropertyValueFactory<>("correoElectronico"));
        cargarDatosCorreo();
        noOrdenar();
    }

    /**
     * Metodo para cargar los datos de la hoja PARAM_CORREO_ELECTRONICO del archivo Excel y mostrarlos en la tabla.
     * Omite filas vacías o con valores nulos.
     */
    private void cargarDatosCorreo(){
        correos = FXCollections.observableArrayList();
        List<List<String>> datos = ExcelManager.leerHoja("PARAM_CORREOS_ELECTRONICOS");
        for (int i = 1;i < datos.size(); i++){
            List<String> fila = datos.get(i);
            if(!fila.isEmpty() && !fila.get(0).trim().isEmpty()){
                correos.add(new Correo_Electronico(fila.get(0)));
            }
        }
        tablaCorreos.setItems(correos);
    }

    /**
     * Metodo para abrir un diálogo y añadir un nuevo correo electrónico.
     * Valida que el valor no esté vacío antes de guardar.
     */
    @FXML
    public void onAddCorreo(){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo correo electrónico");
        dialog.setHeaderText("Añadir nuevo Correo Electrónico");
        dialog.setContentText("Correo Electrónico: ");

        dialog.showAndWait().ifPresent(correo -> {
            if(!correo.trim().isEmpty() && correo.contains("@")){
                tablaCorreos.getItems().add(new Correo_Electronico(correo));
                ExcelManager.añadirFila("PARAM_CORREOS_ELECTRONICOS", correo);
            }else{
                mainAppController.showAlert("Introduce un correo electrónico valido");
            }
        });
    }

    /**
     * Metodo para abrir un diálogo para modificar el correo electrónico seleccionado.
     * Valida que haya una selección activa y que el nuevo valor no esté vacío.
     */
    @FXML
    public void onEditCorreo(){
        Correo_Electronico selected = tablaCorreos.getSelectionModel().getSelectedItem();
        if(selected == null){
            mainAppController.showAlert("Selecciona un correo electrónico");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selected.getCorreoElectronico());
        dialog.setTitle("Modificar correo electrónico");
        dialog.setHeaderText("edita el correo para modificarlo");
        dialog.setContentText("Nuevo Correo Electrónico: ");

        dialog.showAndWait().ifPresent(nuevoCorreo -> {
            if(!nuevoCorreo.trim().isEmpty() && nuevoCorreo.contains("@")){
                List<List<String>> datos = ExcelManager.leerHoja("PARAM_CORREOS_ELECTRONICOS");
                int index = -1;
                for (int i = 1;i < datos.size(); i++){
                    if(datos.get(i).size() > 0 && datos.get(i).get(0).trim().equals(selected.getCorreoElectronico())){
                        index = i;
                        break;
                    }
                }
                if(index != -1){
                    ExcelManager.modificarFila("PARAM_CORREOS_ELECTRONICOS", index, new String[]{nuevoCorreo});
                    selected.setCorreoElectronico(nuevoCorreo);
                    tablaCorreos.refresh();
                }else{
                    mainAppController.showAlert("No se encontró el correo electrónico en el archivo");
                }
            }else{
                mainAppController.showAlert("Introduce un correo electrónico valido");
            }
        });
    }

    /**
     * Metodo para eliminar el correo electrónico seleccionado tras confirmación.
     * Verifica que no esté en uso.
     */
    @FXML
    public void onDeleteCorreo(){
        Correo_Electronico selected = tablaCorreos.getSelectionModel().getSelectedItem();
        if(selected == null){
            mainAppController.showAlert("Selecciona un correo electrónico");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar correo electrónico");
        alert.setHeaderText("Estas seguro de querer eliminarlo?");
        alert.setContentText("Esta acción no se puede deshacer.");

        alert.showAndWait().ifPresent(correo -> {
            if(correo == ButtonType.OK){
                tablaCorreos.getItems().remove(selected);
                ExcelManager.eliminarFila("PARAM_CORREOS_ELECTRONICOS", selected.getCorreoElectronico());
            }
        });
    }

    /**
     * Metodo para configurar el filtro de la columna CORREO_ELECTRONICO.
     */
    @FXML
    private void configurarFiltroCorreo(){
        FilterUtils.abrirFiltroGenerico("Filtrar por correo", Correo_Electronico::getCorreoElectronico, btnFiltroCorreo, tablaCorreos, correos);
    }

    /**
     * Metodo para desactivar la ordenación en la columna de la tabla.
     * Mejora la estabilidad visual al trabajar con datos no ordenados.
     */
    @FXML
    private void noOrdenar(){
        colCorreoElectronico.setSortable(false);
    }
}
