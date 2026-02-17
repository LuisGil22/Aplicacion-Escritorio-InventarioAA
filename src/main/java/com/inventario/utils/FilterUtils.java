package com.inventario.utils;

import com.inventario.models.Condensadora;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.*;
import java.util.function.Function;

public class FilterUtils {

    // Estado global por columna (clave = título del filtro)
    private static final Map<String, Set<String>> selectedValues = new HashMap<>();
    private static final Map<String, Boolean> todosSelected = new HashMap<>();



    /**
     * Abre un menú de filtro genérico para una columna.
     *
     * @param titulo        Título del filtro (ej. "Filtrar por Condensadora")
     * @param extractor     Función que extrae el valor de cada fila (ej. item -> item.getCondensadora())
     * @param boton         Botón ▼ que activa el filtro (para posicionamiento)
     * @param tableView     TableView donde se aplica el filtro
     */
    public static <T> void abrirFiltroGenerico(
            String titulo,
            Function<T, String> extractor,
            Button boton,
            TableView<T> tableView,
            ObservableList<T> originalItems
    ) {
        if (originalItems == null || originalItems.isEmpty()) {
            showAlert("No hay datos para filtrar.");
            return;
        }

        // Inicializar estado si es primera vez
        if (!selectedValues.containsKey(titulo)) {
            selectedValues.put(titulo, new LinkedHashSet<>());
            todosSelected.put(titulo, true);
        }

        Set<String> seleccionados = selectedValues.get(titulo);
        boolean todos = todosSelected.get(titulo);

        // Obtener valores únicos (solo no vacíos)
        Set<String> valoresUnicos = new LinkedHashSet<>();
        for (T item : originalItems) {
            String valor = extractor.apply(item);
            if (valor != null && !valor.trim().isEmpty()) {
                valoresUnicos.add(valor.trim());
            }
        }

        Stage stage = new Stage();
        stage.setTitle(titulo);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(boton.getScene().getWindow());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setMaxWidth(250);

        // Checkbox "Todos"
        Map<String, CheckBox> checkBoxes = new HashMap<>();
        String clave = titulo;
        CheckBox cbTodos = new CheckBox("Todos");
        cbTodos.setSelected(todos);
        cbTodos.setOnAction(e -> {
            boolean sel = cbTodos.isSelected();
            todosSelected.put(clave, sel);
            if (sel) {
                seleccionados.clear();
                checkBoxes.values().forEach(cb -> cb.setSelected(false));
            }
        });
        vbox.getChildren().add(cbTodos);

        // Checkboxes por valor

        for (String valor : valoresUnicos) {
            CheckBox cb = new CheckBox(valor);
            cb.setSelected(seleccionados.contains(valor));
            checkBoxes.put(valor, cb);

            cb.setOnAction(e -> {
                if (cb.isSelected()) {
                    seleccionados.add(valor);
                    cbTodos.setSelected(false);
                    todosSelected.put(titulo, false);
                } else {
                    seleccionados.remove(valor);
                }
            });
            vbox.getChildren().add(cb);
        }

        // Botones
        HBox hbButtons = new HBox(10);
        Button btnAceptar = new Button("Aceptar");
        Button btnCancelar = new Button("Cancelar");


        btnAceptar.setOnAction(e -> {
            Boolean esTodos = todosSelected.getOrDefault(clave, false);
            if (Boolean.TRUE.equals(esTodos)) {
                tableView.setItems(originalItems); // ✅ Usa la lista original
            } else {
                ObservableList<T> filtrados = originalItems.filtered(item -> {
                    String valor = extractor.apply(item);
                    return valor != null && seleccionados.contains(valor.trim());
                });
                tableView.setItems(filtrados);
            }
            stage.close();
        });

        btnCancelar.setOnAction(e -> stage.close());
        hbButtons.getChildren().addAll(btnAceptar, btnCancelar);
        vbox.getChildren().add(hbButtons);

        // Scroll y posición
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefWidth(220);
        scrollPane.setPrefHeight(350);
        scrollPane.setMaxHeight(500);
        scrollPane.setMinHeight(350);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Bounds btnBounds = boton.localToScreen(boton.getBoundsInLocal());
        stage.setX(btnBounds.getMinX() - 40);
        stage.setY(btnBounds.getMaxY() + 5);
        stage.setScene(new javafx.scene.Scene(scrollPane));
        stage.show();
    }

    private static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
