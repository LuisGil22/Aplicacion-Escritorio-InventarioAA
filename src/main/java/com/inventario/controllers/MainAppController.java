package com.inventario.controllers;

import com.inventario.utils.ExcelManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

/**
 * Controlador principal de la aplicación de inventario de aire acondicionado.
 * <p>
 * Gestiona la navegación entre vistas, el diseño responsivo, la barra lateral (sidebar),
 * y proporciona acceso global a funcionalidades comunes (como mostrar alertas).
 * </p>
 * <p>
 * Implementa el patrón Singleton mediante {@link #INSTANCE} para permitir acceso global
 * desde otros controladores.
 * </p>
 *
 * @author Luis Gil
 */
@SuppressWarnings("ALL")
public class MainAppController {

    /**
     * Instancia única del controlador principal.
     * Permite acceso global desde otros controladores para mostrar alertas,
     * cargar vistas o ajustar el diseño.
     */
    public static MainAppController INSTANCE;

    /** Campos FXML */
    @FXML private VBox sidebarInclude;
    @FXML private SidebarController sidebarIncludeController;
    @FXML private Button SidebarButtonMostrar;
    @FXML private StackPane stackPaneContent;
    @FXML private Label VolverInicio;
    @FXML private HeaderController headerController;

    /** Estado interno */
    private boolean isSidebarVisible = true;
    private ImageView fondoImageView;

    /**
     * Metodo para inicializar el controlador al cargar la vista FXML.
     * Configura:
     * <ul>
     *   <li>La instancia singleton ({@link #INSTANCE})</li>
     *   <li>La dependencia con SidebarController</li>
     *   <li>El evento de clic en "volver al inicio"</li>
     *   <li>El listener de cambio de tamaño para diseño responsivo</li>
     * </ul>
     */
    @FXML
    public void initialize(){
        INSTANCE = this;

        if (sidebarIncludeController != null) {
            sidebarIncludeController.setMainController(this);
        }

        if(VolverInicio != null){
            VolverInicio.setCursor(Cursor.HAND);
            VolverInicio.setOnMouseClicked(event -> VolverAlInicio());
        }

        mostrarMensajeBienvenida();

        verificarRevisionesPendientesAlIniciar();

        stackPaneContent.widthProperty().addListener((obs, oldW, newW) ->{
            double w = newW.doubleValue();
            if (fondoImageView != null){
                if (w >= 800) {
                    fondoImageView.setFitWidth(800);
                    fondoImageView.setFitHeight(600);
                    fondoImageView.setVisible(true);
                }else if (w >= 600) {
                    fondoImageView.setFitWidth(600);
                    fondoImageView.setFitHeight(400);
                    fondoImageView.setVisible(true);
                }else if (w >= 400) {
                    fondoImageView.setFitWidth(400);
                    fondoImageView.setFitHeight(200);
                    fondoImageView.setVisible(true);
                }else {
                    fondoImageView.setVisible(false); // Desaparece si < 400px
                }
            }
        });

    }

    /**
     * Metodo para manejar el evento de clic en el botón de menú móvil (☰) del header.
     * Alterna la visibilidad del sidebar con animación suave.
     */
    @FXML
    public void ClickSidebar(){
        toggleSidebarMovil();
    }

    /**
     * Metodo para animar el sidebar cambiando su ancho con una transición suave de 700ms.
     *
     * @param fromWidth ancho inicial en píxeles
     * @param toWidth ancho final en píxeles
     * @param onFinished acción a ejecutar al finalizar la animación
     */
    private void animacionSidebar(double fromWidth, double toWidth, Runnable onFinished){
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(
                Duration.millis(700),
                new KeyValue(sidebarInclude.prefWidthProperty(),toWidth),
                new KeyValue(sidebarInclude.minWidthProperty(),toWidth),
                new KeyValue(sidebarInclude.maxWidthProperty(),toWidth)
        ));
        if(onFinished != null){
            timeline.setOnFinished(e -> {
                stackPaneContent.setMinWidth(600);
                onFinished.run();
                Platform.runLater(() -> stackPaneContent.setMinWidth(0));
            });
        }
        timeline.play();
    }

    /**
     * Metodo para cargar una vista específica en el contenedor principal según el nombre del menú.
     * <p>
     * Soporta todas las vistas del sistema: parámetros, equipos y averías.
     * </p>
     *
     * @param menuName nombre del menú (ej. "CONDENSADORAS", "AVERÍAS", "ESTADO")
     */
    public void loadView(String menuName) {
        String path;
        switch (menuName) {
            case "ESTADO":
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/estado.fxml"));
                    Node view = loader.load();
                    EstadoController estadoController = loader.getController();
                    estadoController.setMainController(this);

                    stackPaneContent.getChildren().setAll(view, SidebarButtonMostrar, VolverInicio);
                    VolverInicio.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "GASES":
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/gases.fxml"));
                    Node view = loader.load();
                    GasesController gasesController = loader.getController();
                    gasesController.setMainAppController(this);

                    stackPaneContent.getChildren().setAll(view, SidebarButtonMostrar, VolverInicio);
                    VolverInicio.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "LOC-CONDENSADORAS":
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/loc_condensadoras.fxml"));
                    Node view = loader.load();
                    Loc_CondensadorasController loc_condensadorasController = loader.getController();
                    loc_condensadorasController.setMainController(this);

                    stackPaneContent.getChildren().setAll(view, SidebarButtonMostrar, VolverInicio);
                    VolverInicio.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "MARCAS":
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/marcas.fxml"));
                    Node view = loader.load();
                    MarcaController marcaController = loader.getController();
                    marcaController.setMainAppController(this);

                    stackPaneContent.getChildren().setAll(view, SidebarButtonMostrar, VolverInicio);
                    VolverInicio.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "MODEL-CASSETTES":
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/model_cassettes.fxml"));
                    Node view = loader.load();
                    Model_CassettesController modelCassettesController = loader.getController();
                    modelCassettesController.setMainAppController(this);

                    stackPaneContent.getChildren().setAll(view, SidebarButtonMostrar, VolverInicio);
                    VolverInicio.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "MODEL-CONDENSADORAS":
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/model_condensadoras.fxml"));
                    Node view = loader.load();
                    Model_CondensadorasController modelCondensadorasController = loader.getController();
                    modelCondensadorasController.setMainAppController(this);

                    stackPaneContent.getChildren().setAll(view, SidebarButtonMostrar, VolverInicio);
                    VolverInicio.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "PLANTAS":
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/planta.fxml"));
                    Node view = loader.load();
                    PlantaController plantaController = loader.getController();
                    plantaController.setMainAppController(this);

                    stackPaneContent.getChildren().setAll(view, SidebarButtonMostrar, VolverInicio);
                    VolverInicio.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "UBICACION-CASSETTE":
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ubicacion_cassette.fxml"));
                    Node view = loader.load();
                    UbicacionCassetteController ubicacionCassetteController = loader.getController();
                    ubicacionCassetteController.setMainAppController(this);

                    stackPaneContent.getChildren().setAll(view, SidebarButtonMostrar, VolverInicio);
                    VolverInicio.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "CONDENSADORAS":
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/condensadoras.fxml"));
                    Node view = loader.load();
                    CondensadorasController condensadorasController = loader.getController();
                    condensadorasController.setMainAppController(this);

                    stackPaneContent.getChildren().setAll(view, SidebarButtonMostrar, VolverInicio);
                    VolverInicio.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "CASSETTES":
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/cassette.fxml"));
                    Node view = loader.load();
                    CassetteController cassetteController = loader.getController();
                    cassetteController.setMainAppController(this);

                    stackPaneContent.getChildren().setAll(view, SidebarButtonMostrar, VolverInicio);
                    VolverInicio.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "AVERÍAS":
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/averia.fxml"));
                    Node view = loader.load();
                    AveriaController averiaController = loader.getController();
                    averiaController.setMainAppController(this);

                    stackPaneContent.getChildren().setAll(view, SidebarButtonMostrar, VolverInicio);
                    VolverInicio.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "CORREOS-ELECTRONICOS":
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/correo.fxml"));
                    Node view = loader.load();
                    CorreoController correoController = loader.getController();
                    correoController.setMainAppController(this);

                    stackPaneContent.getChildren().setAll(view, SidebarButtonMostrar, VolverInicio);
                    VolverInicio.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "REVISIONES":
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/revision.fxml"));
                    Node view = loader.load();
                    RevisionController revisionController = loader.getController();
                    revisionController.setMainAppController(this);

                    stackPaneContent.getChildren().setAll(view, SidebarButtonMostrar, VolverInicio);
                    VolverInicio.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "DIAS-REVISION":
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/diaRevision.fxml"));
                    Node view = loader.load();
                    DiaRevisionController diaRevisionController = loader.getController();
                    diaRevisionController.setMainAppController(this);

                    stackPaneContent.getChildren().setAll(view, SidebarButtonMostrar, VolverInicio);
                    VolverInicio.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                mostrarMensajeBienvenida();
        }
    }

    /**
     * Metodo para mostrar la pantalla de bienvenida con fondo de imagen y mensaje instructivo.
     * <p>
     * Limpia la selección activa del sidebar y ajusta el fondo según el ancho actual.
     * </p>
     */
    public void mostrarMensajeBienvenida() {
        if(sidebarIncludeController != null){
            sidebarIncludeController.clearSeleccion();
        }
        Platform.runLater(() -> {
            if (fondoImageView == null){
                Image img = new Image(getClass().getResourceAsStream("/imagenes/Background.png"));
                fondoImageView = new ImageView(img);
                fondoImageView.setOpacity(0.2);
                fondoImageView.setPreserveRatio(false);
                fondoImageView.setFitWidth(800);
                fondoImageView.setFitHeight(600);
            }

            double w = stackPaneContent.getWidth();
            if (w >= 800) {
                fondoImageView.setFitWidth(800);
                fondoImageView.setFitHeight(600);
                fondoImageView.setVisible(true);
            }else if (w >= 600) {
                fondoImageView.setFitWidth(600);
                fondoImageView.setFitHeight(400);
                fondoImageView.setVisible(true);
            }else if (w >= 400) {
                fondoImageView.setFitWidth(400);
                fondoImageView.setFitHeight(200);
                fondoImageView.setVisible(true);
            }else {
                fondoImageView.setVisible(false);
            }

            VBox content = new VBox(20);
            content.setAlignment(Pos.CENTER);
            Label label = new Label("Haz clic en un menú para cargar su contenido");
            label.getStyleClass().add("label-text");
            content.getChildren().add(label);

            stackPaneContent.getChildren().setAll(fondoImageView, content, SidebarButtonMostrar, VolverInicio);
            VolverInicio.setVisible(false);
        });
    }

    /**
     * Metodo para navegar a la pantalla de inicio (mensaje de bienvenida).
     */
    private void VolverAlInicio(){
        mostrarMensajeBienvenida();
    }

    /**
     * Metodo para mostrar una alerta de advertencia con el mensaje especificado.
     *
     * @param message mensaje a mostrar en la alerta
     */
    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Metodo que alterna la visibilidad del sidebar con animación.
     * <p>
     * Si está visible → lo oculta (ancho 240 → 0).<br>
     * Si está oculto → lo muestra (ancho 0 → 240).
     * </p>
     */
    public void toggleSidebarMovil() {
        if (isSidebarVisible) {
            animacionSidebar(240, 0, () -> SidebarButtonMostrar.setText("»"));
        } else {
            animacionSidebar(0, 240, () -> SidebarButtonMostrar.setText("«"));
        }
        isSidebarVisible = !isSidebarVisible;
    }

    /**
     * Metodo para ajustar el diseño de la aplicación según el ancho de la ventana (diseño responsivo).
     * <p>
     * Cambia:
     * </p>
     * <ul>
     *   <li>El título en el header (versión corta/larga)</li>
     *   <li>La visibilidad del botón de menú móvil (☰)</li>
     *   <li>El tamaño del fondo de bienvenida</li>
     *   <li>La visibilidad del sidebar en modo móvil</li>
     * </ul>
     *
     * @param ancho ancho actual de la ventana en píxeles
     */
    public void ajustarDiseno(double ancho) {
        if (Double.isNaN(ancho) || ancho <= 0){
            return;
        }
        boolean esModoMovil = ancho <= 800;

        if(headerController != null) {
            if (headerController.tituloApp != null) {
                headerController.tituloApp.setText(
                        esModoMovil ? "Inventario A.A." : "Inventario para Mantenimiento de Aire Acondicionado"
                );
            }
            if (headerController.btnMenuMovil != null) {
                headerController.btnMenuMovil.setVisible(esModoMovil);
            }
            if (headerController.exitButton != null) {
                headerController.exitButton.setVisible(true);
            }

        }

        SidebarButtonMostrar.setVisible(!esModoMovil);

        if (esModoMovil && isSidebarVisible) {
            animacionSidebar(240, 0, () -> SidebarButtonMostrar.setText("»"));
            isSidebarVisible = false;
        } else if (!esModoMovil && !isSidebarVisible) {
            animacionSidebar(0, 240, () -> SidebarButtonMostrar.setText("«"));
            isSidebarVisible = true;
        }

        if (fondoImageView != null) {
            if (ancho >= 800) {
                fondoImageView.setFitWidth(800);
                fondoImageView.setFitHeight(600);
                fondoImageView.setVisible(true);
            } else if (ancho >= 600) {
                fondoImageView.setFitWidth(600);
                fondoImageView.setFitHeight(400);
                fondoImageView.setVisible(true);
            } else if (ancho >= 400) {
                fondoImageView.setFitWidth(400);
                fondoImageView.setFitHeight(200);
                fondoImageView.setVisible(true);
            } else {
                fondoImageView.setVisible(false);
            }
        }
    }

    /**
     * Verifica todas las condensadoras y cassettes activos al iniciar la aplicación.
     * Si alguna entra hoy en el rango de revisión (fecha_revision - 30 días ≤ hoy),
     * se crea una entrada en REVISIONES y se envía un correo de aviso.
     */
    private void verificarRevisionesPendientesAlIniciar() {
        new Thread(() -> {
            try {
                // --- Condensadoras ---
                List<List<String>> condensadoras = ExcelManager.leerHoja("Condensadoras");
                for (int i = 1; i < condensadoras.size(); i++) {
                    List<String> fila = condensadoras.get(i);
                    if (fila.size() > 2 && "ACTIVA".equals(fila.get(2).trim())) {
                        String condensadora = fila.get(0).trim();
                        String fechaInst = fila.get(8).trim(); // FECHA_INSTALACION
                        int numSecuencia = 1;
                        try {
                            numSecuencia = Integer.parseInt(fila.get(1).trim());
                        } catch (NumberFormatException ignored) {}

                        int diasRevision = 365;
                        if (fila.size() > 13 && !fila.get(13).trim().isEmpty()) {
                            try {
                                diasRevision = Integer.parseInt(fila.get(13).trim());
                            } catch (NumberFormatException ignored) {}
                        }

                        if (!fechaInst.isEmpty()) {
                            ExcelManager.calcularYActualizarRevisionIndividual("CONDENSADORA", condensadora, fechaInst, numSecuencia, diasRevision);
                        }
                    }
                }

                // --- Cassettes ---
                List<List<String>> cassettes = ExcelManager.leerHoja("Cassette");
                for (int i = 1; i < cassettes.size(); i++) {
                    List<String> fila = cassettes.get(i);
                    if (fila.size() > 2 && "ACTIVA".equals(fila.get(2).trim())) {
                        String cassette = fila.get(0).trim();
                        String fechaInst = fila.get(12).trim(); // FECHA_INSTALACION
                        int numSecuencia = 1;
                        try {
                            numSecuencia = Integer.parseInt(fila.get(1).trim());
                        } catch (NumberFormatException ignored) {}

                        int diasRevision = 365;
                        if (fila.size() > 18 && !fila.get(15).trim().isEmpty()) {
                            try {
                                diasRevision = Integer.parseInt(fila.get(18).trim());
                            } catch (NumberFormatException ignored) {}
                        }

                        if (!fechaInst.isEmpty()) {
                            ExcelManager.calcularYActualizarRevisionIndividual("CASSETTE", cassette, fechaInst, numSecuencia, diasRevision);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Metodo para abrir un diálogo modal que permite editar las observaciones de una condensadora o cassette.
     *
     * @param titulo título del diálogo (ej. "Editar Observaciones - Condensadora X")
     * @param observacionActual texto actual de las observaciones que se mostrará en el área de texto
     * @param onGuardar función callback que se ejecutará al hacer clic en "Guardar", recibiendo el nuevo texto de observaciones
     */
    public void abrirDialogoObservaciones(
            String titulo,
            String observacionActual,
            java.util.function.Consumer<String> onGuardar
    ) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stackPaneContent.getScene().getWindow());
        dialog.setTitle(titulo);

        TextArea textArea = new TextArea(observacionActual);
        textArea.setPrefRowCount(8);
        textArea.setPrefColumnCount(40);

        Button btnGuardar = new Button("Guardar");
        Button btnCancelar = new Button("Cancelar");

        btnGuardar.setOnAction(e -> {
            onGuardar.accept(textArea.getText());
            dialog.close();
        });
        btnCancelar.setOnAction(e -> dialog.close());

        HBox botones = new HBox(10, btnGuardar, btnCancelar);
        botones.setAlignment(Pos.CENTER_RIGHT);

        VBox vbox = new VBox(10, new Label("Observaciones:"), textArea, botones);
        vbox.setPadding(new Insets(15));
        dialog.setScene(new Scene(vbox));
        dialog.showAndWait();
    }

    /**
     * Metodo para cerrar la aplicación de forma segura.
     */
    @FXML
    public void salir() {
        Platform.exit();
    }

}
