package com.inventario.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;
import org.apache.poi.ss.usermodel.*;
import java.io.IOException;


public class MainAppController {

    public static MainAppController INSTANCE;

    @FXML
    private VBox sidebarInclude;

    @FXML
    private SidebarController sidebarIncludeController;

    @FXML
    private Button SidebarButtonMostrar;

    @FXML
    private StackPane stackPaneContent;

    @FXML
    private Label VolverInicio;

    @FXML
    private HeaderController headerController;


    private boolean isSidebarVisible = true;
    private ImageView fondoImageView;


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
        /**System.out.println("=== MainAppController.initialize() ===");
        System.out.println("headerController = " + headerController);
        if (headerController != null) {
            System.out.println("headerController.tituloApp = " + headerController.tituloApp);
            System.out.println("headerController.exitButton = " + headerController.exitButton);
            System.out.println("headerController.btnMenuMovil = " + headerController.btnMenuMovil);
        }**/
    }

    @FXML
    public void ClickSidebar(){

        toggleSidebarMovil();
        /**if(isSidebarVisible) {
            animacionSidebar(240, 0, () -> {
                SidebarButtonMostrar.setText("»");
            });
        } else {
            animacionSidebar(0,240, () -> {
                SidebarButtonMostrar.setText("«");
            });
        }
        isSidebarVisible = !isSidebarVisible;**/
    }

    private void animacionSidebar(double fromWidth, double toWidth, Runnable onFinished){
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(
                Duration.millis(700),
                new KeyValue(sidebarInclude.prefWidthProperty(),toWidth),
                new KeyValue(sidebarInclude.minWidthProperty(),toWidth),
                new KeyValue(sidebarInclude.maxWidthProperty(),toWidth)
        ));
        if(onFinished != null){
            //timeline.setOnFinished(e -> onFinished.run());
            timeline.setOnFinished(e -> {
                // Asegurar que el center no se mueva
                stackPaneContent.setMinWidth(600); // Fija ancho mínimo temporalmente
                onFinished.run();
                Platform.runLater(() -> stackPaneContent.setMinWidth(0)); // Restaura
            });
        }
        timeline.play();
    }

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
            default:
                mostrarMensajeBienvenida();
        }
    }



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

            // Ajustar tamaño inicial
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
            /**ImageView fondo = new ImageView(new Image(getClass().getResourceAsStream("/imagenes/Background.png")));
            fondo.setPreserveRatio(false);
            fondo.opacityProperty().bind(stackPaneContent.opacityProperty().multiply(0.2));
            fondo.fitWidthProperty().bind(stackPaneContent.widthProperty());
            fondo.fitHeightProperty().bind(stackPaneContent.heightProperty());

            Label label = new Label("Haz clic en un menú para cargar su contenido");
            StackPane.setAlignment(label, Pos.TOP_CENTER);
            label.getStyleClass().add("label-text");

            if (headerController != null) {
                if (headerController.tituloApp != null) {
                    headerController.tituloApp.setText("Inventario para Mantenimiento de Aire Acondicionado");
                }
                if (headerController.btnMenuMovil != null) {
                    headerController.btnMenuMovil.setVisible(false); // Ocultar ☰ en pantalla de inicio
                }
                if (headerController.exitButton != null) {
                    headerController.exitButton.setVisible(true); // Asegurar que "Salir" esté visible
                }
            }

            // Mantener el botón
            stackPaneContent.getChildren().setAll(fondo, label, SidebarButtonMostrar, VolverInicio);
            VolverInicio.setVisible(false);**/
        });
    }

    private void VolverAlInicio(){

        mostrarMensajeBienvenida();
    }

    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void toggleSidebarMovil() {
        if (isSidebarVisible) {
            animacionSidebar(240, 0, () -> SidebarButtonMostrar.setText("»"));
        } else {
            animacionSidebar(0, 240, () -> SidebarButtonMostrar.setText("«"));
        }
        isSidebarVisible = !isSidebarVisible;
    }

    public void ajustarDiseno(double ancho) {
        //System.out.println("=== ajustarDiseno() llamado ===");
        //System.out.println("Ancho recibido: " + ancho);
        if (Double.isNaN(ancho) || ancho <= 0){
            //System.out.println("⚠️ Ancho inválido, saliendo.");
            return;
        }
        boolean esModoMovil = ancho <= 800;
        //System.out.println("Modo móvil: " + esModoMovil);

        if(headerController != null) {

                // 👇 Usar el controlador del header
                if (headerController.tituloApp != null) {
                    //System.out.println("✅ Actualizando título...");
                    headerController.tituloApp.setText(
                            esModoMovil ? "Inventario A.A." : "Inventario para Mantenimiento de Aire Acondicionado"
                    );
                //}else {
                    //System.out.println("❌ tituloApp es NULL");
                }
                // Mostrar/ocultar icono ☰
                if (headerController.btnMenuMovil != null) {
                    //System.out.println("✅ Mostrando/ocultando ☰...");
                    headerController.btnMenuMovil.setVisible(esModoMovil);
                //}else {
                    //System.out.println("❌ btnMenuMovil es NULL");
                }

                if (headerController.exitButton != null) {
                    //System.out.println("✅ Asegurando que 'Salir' esté visible...");
                    headerController.exitButton.setVisible(true);
                //}else {
                    //System.out.println("❌ headerController es NULL");
                }

        }

        // Actualizar sidebar
        SidebarButtonMostrar.setVisible(!esModoMovil);

        if (esModoMovil && isSidebarVisible) {
            animacionSidebar(240, 0, () -> SidebarButtonMostrar.setText("»"));
            isSidebarVisible = false;
        } else if (!esModoMovil && !isSidebarVisible) {
            animacionSidebar(0, 240, () -> SidebarButtonMostrar.setText("«"));
            isSidebarVisible = true;
        }

        // Ajustar el fondo también
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
    @FXML
    public void salir() {
        Platform.exit();
    }

}
