// com/inventario/utils/ExcelManager.java
package com.inventario.utils;

import com.inventario.controllers.MainAppController;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.GrayColor;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;



/**
 * Gestor de operaciones con el archivo Excel "Inventario AA V2.xlsx".
 * <p>
 * Proporciona métodos para leer, añadir, modificar y eliminar filas en las hojas del libro,
 * así como funcionalidades de envío de correo y gestión automática de averías.
 * </p>
 *
 * @author Luis Gil
 */
@SuppressWarnings("ALL")
public class ExcelManager {

    private static File excelFile;
    private static final DataFormatter formatter = new DataFormatter(new Locale("es", "ES"));
    private static final Object EXCEL_LOCK = new Object();
    public static final SimpleBooleanProperty revisionActualizada = new SimpleBooleanProperty(false);

    private static final String PREFS_NODE = "com/inventario/app/columnas";


    /**
     * Constantes para nombres de columnas en las hojas del Excel.
     * Útiles para evitar errores tipográficos en búsquedas.
     */
    public class Columnas {
        public static final String CONDENSADORA = "CONDENSADORA";
        public static final String NUM_SECUENCIA = "NUM_SECUENCIA";
        public static final String ESTADO = "ESTADO";
        public static final String MARCA = "Marca";
        public static final String MODELO = "Model";
        public static final String NUM_SERIE = "NUM_SERIE_COND";
        public static final String LOCALIZACION_CONDENSADORA = "LOCALIZACIÓN_CONDENSADORA";
        public static final String GAS = "GAS";
        public static final String FECHA_INSTALACION = "FECHA_INSTALACION";
        public static final String FECHA_REVISION = "FECHA_REVISION";
        public static final String AVERIA = "AVERIA";
        public static final String OBSERVACIONES = "OBSERVACIONES";
    }

    /**
     * Obtiene la ruta del archivo Excel, copiándolo desde recursos si no existe.
     *
     * @return archivo Excel listo para uso
     */
    public static File getExcelFile() {
        if (excelFile == null) {
            String userHome = System.getProperty("user.home");
            File dataDir = new File(userHome, "Desktop");
            dataDir.mkdirs();

            excelFile = new File(dataDir, "Inventario AA V2.xlsx");

            if (!excelFile.exists() || excelFile.length() == 0) {

                try (InputStream is = ExcelManager.class.getResourceAsStream("/datos/Inventario AA V2.xlsx");
                     FileOutputStream fos = new FileOutputStream(excelFile)) {
                    if (is != null) {
                        is.transferTo(fos);
                        //System.out.println("Excel copiado a: " + excelFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return excelFile;
    }

    /**
     * Lee todas las filas de una hoja del Excel, evaluando fórmulas si existen.
     *
     * @param nombreHoja nombre de la hoja a leer
     * @return lista de filas, cada fila es una lista de valores en string.
     */
    public static List<List<String>> leerHoja(String nombreHoja) {
        synchronized (EXCEL_LOCK) {
            List<List<String>> datos = new ArrayList<>();
            File file = getExcelFile();
            try (FileInputStream fis = new FileInputStream(file)) {
                Workbook workbook = new XSSFWorkbook(fis);
                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                Sheet sheet = workbook.getSheet(nombreHoja);
                if (sheet == null) {
                    return datos;
                }
                //System.out.println("Hoja '" + nombreHoja + "' tiene " + sheet.getLastRowNum() + " filas.");
                for (Row row : sheet) {
                    //System.out.print("Fila " + row.getRowNum() + ": ");
                    List<String> fila = new ArrayList<>();
                    int lastCell = row.getLastCellNum();
                    for (int i = 0; i < Math.max(lastCell, 1); i++) {
                        Cell cell = row.getCell(i);
                        String valor = getCellValueAsString(cell, evaluator);
                        fila.add(valor);
                    }
                    datos.add(fila);
                }
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return datos;
        }
    }

    /**
     * Añade una nueva fila a la hoja especificada, aplicando formato y bordes.
     *
     * @param hoja    nombre de la hoja
     * @param valores valores de la nueva fila
     */
    public static void añadirFila(String hoja, String... valores) {
        synchronized (EXCEL_LOCK) {
            File file = getExcelFile();
            try (FileInputStream fis = new FileInputStream(file)) {
                Workbook workbook = new XSSFWorkbook(fis);
                Sheet sheet = workbook.getSheet(hoja);
                if (sheet == null) return;

                /** Encontrar la última fila REAL con contenido (ignorar filas vacías con formato)*/
                int ultimaFilaConDatos = -1;
                for (int i = sheet.getLastRowNum(); i >= 0; i--) {
                    Row row = sheet.getRow(i);
                    if (row != null && tieneContenido(row)) {
                        ultimaFilaConDatos = i;
                        break;
                    }
                }

                int nuevaFilaIndex = ultimaFilaConDatos + 1;
                if (nuevaFilaIndex > 1048575) {
                    throw new IllegalStateException("Límite de filas de Excel alcanzado.");
                }

                Row newRow = sheet.createRow(nuevaFilaIndex);

                /** Estilo General */
                CellStyle estiloCelda = workbook.createCellStyle();
                estiloCelda.setAlignment(HorizontalAlignment.CENTER);
                estiloCelda.setVerticalAlignment(VerticalAlignment.CENTER);
                estiloCelda.setBorderBottom(BorderStyle.THIN);
                estiloCelda.setBorderTop(BorderStyle.THIN);
                estiloCelda.setBorderRight(BorderStyle.THIN);
                estiloCelda.setBorderLeft(BorderStyle.THIN);
                estiloCelda.setWrapText(true);

                /** Estilo especial para NUM_AVERIA (columna 0): formato "0000"*/
                CellStyle estiloNumAveria = workbook.createCellStyle();
                estiloNumAveria.cloneStyleFrom(estiloCelda); // copiar bordes, centrado, etc.
                DataFormat format = workbook.createDataFormat();
                estiloNumAveria.setDataFormat(format.getFormat("0000"));

                for (int i = 0; i < valores.length; i++) {
                    Cell cell = newRow.createCell(i);
                    String val = valores[i];
                    if (val == null || val.trim().isEmpty()) {
                        cell.setCellValue("");
                    } else if (i == 0) {
                        cell.setCellValue(val);
                    } else if (isNumeric(val)) {
                        cell.setCellValue(Double.parseDouble(val.replace(',', '.')));
                    } else {
                        cell.setCellValue(val);
                    }
                    if (("AVERIA".equals(hoja) && i == 0) || ("Cassette".equals(hoja) && i == 15) || ("Condensadoras".equals(hoja) && i == 11)){
                        try {
                            int num = Integer.parseInt(val);
                            cell.setCellValue(num);
                            cell.setCellStyle(estiloNumAveria);
                        }catch (NumberFormatException e){
                            cell.setCellValue(val);
                            cell.setCellStyle(estiloCelda);
                        }
                    } else {
                        cell.setCellStyle(estiloCelda);
                    }
                }

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
                workbook.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Añade una nueva fila en la hoja especificada, manteniendo el orden alfabético por la primera columna.
     * Si ya existen filas con el mismo valor en la primera columna, la nueva se inserta después de la última.
     *
     * @param hoja nombre de la hoja (ej. "Condensadoras")
     * @param valores valores de la nueva fila
     */
    public static void añadirFilaOrdenada(String hoja, String... valores){
        synchronized (EXCEL_LOCK) {
            File file = getExcelFile();
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                Workbook workbook = new XSSFWorkbook(fileInputStream);
                Sheet sheet = workbook.getSheet(hoja);
                if (sheet == null) return;

                String valorNuevo = valores[0].trim();
                int posicionAInsertar = 1;
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null && row.getCell(0) != null) {
                        String valorExistente = formatter.formatCellValue(row.getCell(0)).trim();
                        if (valorExistente.compareTo(valorNuevo) > 0) {
                            break;
                        }
                        posicionAInsertar = i + 1;
                    }
                }
                if (posicionAInsertar <= sheet.getLastRowNum()) {
                    sheet.shiftRows(posicionAInsertar, sheet.getLastRowNum(), 1);
                }
                Row nuevaRow = sheet.createRow(posicionAInsertar);

                /** Estilo General */
                CellStyle estiloCelda = workbook.createCellStyle();
                estiloCelda.setAlignment(HorizontalAlignment.CENTER);
                estiloCelda.setVerticalAlignment(VerticalAlignment.CENTER);
                estiloCelda.setBorderBottom(BorderStyle.THIN);
                estiloCelda.setBorderTop(BorderStyle.THIN);
                estiloCelda.setBorderRight(BorderStyle.THIN);
                estiloCelda.setBorderLeft(BorderStyle.THIN);
                estiloCelda.setWrapText(true);

                /** Estilo especial para NUM_AVERIA (columna 0): formato "0000"*/
                CellStyle estiloNumAveria = workbook.createCellStyle();
                estiloNumAveria.cloneStyleFrom(estiloCelda); // copiar bordes, centrado, etc.
                DataFormat format = workbook.createDataFormat();
                estiloNumAveria.setDataFormat(format.getFormat("0000"));

                for (int i = 0; i < valores.length; i++) {
                    Cell cell = nuevaRow.createCell(i);
                    if ("REVISIONES".equals(hoja) && i == 0) {
                        // Guardar como número + aplicar formato "0000"
                        try {
                            int num = Integer.parseInt(valores[i]);
                            cell.setCellValue(num);

                            CellStyle estiloNum = workbook.createCellStyle();
                            estiloNum.cloneStyleFrom(estiloCelda);
                            estiloNum.setDataFormat(workbook.createDataFormat().getFormat("0000"));
                            cell.setCellStyle(estiloNum);
                        } catch (NumberFormatException e) {
                            cell.setCellValue(valores[i]); // fallback a texto
                        }
                    } else {
                        String val = valores[i];
                        if (val == null || val.trim().isEmpty()) {
                            cell.setCellValue("");
                        } else if (i == 0) {
                            cell.setCellValue(val);
                        } else if (isNumeric(val)) {
                            cell.setCellValue(Double.parseDouble(val.replace(',', '.')));
                        } else {
                            cell.setCellValue(val);
                        }
                        if (("AVERIA".equals(hoja) && i == 0) || ("Cassette".equals(hoja) && i == 15) || ("Condensadoras".equals(hoja) && i == 11)) {
                            try {
                                int num = Integer.parseInt(val);
                                cell.setCellValue(num);
                                cell.setCellStyle(estiloNumAveria);
                            }catch (NumberFormatException e){
                                cell.setCellValue(val);
                                cell.setCellStyle(estiloCelda);
                            }
                        } else {
                            cell.setCellStyle(estiloCelda);
                        }
                    }
                }

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
                workbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Verifica si una fila contiene algún dato útil (ignora fórmulas vacías).
     */
    private static boolean tieneContenido(Row row) {
        if (row == null) return false;
        for (Cell cell : row) {
            if (cell != null) {
                String valor = getCellValueAsString(cell).trim();
                if (!valor.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Modifica una fila existente buscando por el valor de la primera celda.
     * <p>
     * Este metodo busca por valor, no por índice. Para operaciones por índice,
     * use {@link #modificarFilaPorIndice(String, int, String[])}.
     * </p>
     *
     * @param hoja nombre de la hoja
     * @param index índice sugerido (no se usa directamente)
     * @param valoresNuevos nuevos valores para la fila
     */
    public static void modificarFila(String hoja, int index, String[] valoresNuevos){
        synchronized (EXCEL_LOCK) {
            File excelManager = getExcelFile();
            try (FileInputStream fileInput = new FileInputStream(excelManager)) {
                Workbook workbook = new XSSFWorkbook(fileInput);
                Sheet sheet = workbook.getSheet(hoja);
                if (sheet == null || index > sheet.getLastRowNum()) {
                    return;
                }

                Row row = sheet.getRow(index);
                if (row == null) {
                    row = sheet.createRow(index);
                }
                for (int i = 0; i < valoresNuevos.length; i++) {
                    Cell cell = row.getCell(i);
                    if (cell == null) {
                        cell = row.createCell(i);
                    }

                    if ("REVISIONES".equals(hoja) && i == 0) {
                        try {
                            int num = Integer.parseInt(valoresNuevos[i]);
                            cell.setCellValue(num);

                            CellStyle estilo = workbook.createCellStyle();
                            estilo.cloneStyleFrom(cell.getCellStyle());
                            estilo.setDataFormat(workbook.createDataFormat().getFormat("0000"));
                            cell.setCellStyle(estilo);
                        } catch (NumberFormatException e) {
                            setCellValue(cell, valoresNuevos[i]);
                        }
                    } else if (("AVERIA".equals(hoja) && i == 0) || ("Cassette".equals(hoja) && i == 15) || ("Condensadoras".equals(hoja) && i == 11)) {
                        try {
                            int num = Integer.parseInt(valoresNuevos[i]);
                            cell.setCellValue(num);

                            CellStyle estiloNumAveria = workbook.createCellStyle();
                            estiloNumAveria.cloneStyleFrom(cell.getCellStyle()); // copiar bordes, centrado, etc.
                            DataFormat format = workbook.createDataFormat();
                            estiloNumAveria.setDataFormat(format.getFormat("0000"));
                            cell.setCellStyle(estiloNumAveria);
                        }catch (NumberFormatException e){
                            setCellValue(cell,valoresNuevos[i]);

                        }
                    }
                    else {
                        setCellValue(cell, valoresNuevos[i]);
                    }
                }

                try (FileOutputStream fileOutput = new FileOutputStream(excelManager)) {
                    workbook.write(fileOutput);
                }
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Elimina una fila por el valor de su primera celda.
     *
     * @param hoja nombre de la hoja
     * @param eliminarValor valor de la primera celda de la fila a eliminar
     */
    public static void eliminarFila(String hoja, String eliminarValor){
        File excelFile = getExcelFile();
        try (FileInputStream fis = new FileInputStream(excelFile)) {
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet(hoja);
            if (sheet == null) return;

            int rowIndex = -1;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null && row.getCell(0) != null) {
                    String cellValue = formatter.formatCellValue(row.getCell(0)).trim();
                    if (eliminarValor.equals(cellValue)) {
                        rowIndex = i;
                        break;
                    }
                }
            }

            if (rowIndex == -1) return; // No encontrada

            // Eliminar la fila
            sheet.removeRow(sheet.getRow(rowIndex));

            // Desplazar filas hacia arriba solo si hay filas después
            if (rowIndex < sheet.getLastRowNum()) {
                sheet.shiftRows(rowIndex + 1, sheet.getLastRowNum(), -1);
            }

            try (FileOutputStream fos = new FileOutputStream(excelFile)) {
                workbook.write(fos);
            }
            workbook.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Elimina una fila en una hoja Excel por su índice (1-based).
     */
    public static void eliminarFilaPorIndice(String hoja, int indiceFila) {
        //System.out.println("DEBUG eliminarFilaPorIndice: Hoja='" + hoja + "', Indice=" + indiceFila);
        try {
            File file = getExcelFile();
            if (!file.exists() || file.length() == 0) {
                //System.err.println("ERROR: Archivo Excel no existe o está vacío al intentar eliminar fila.");
                return;
            }
            FileInputStream fis = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet(hoja);
            if (sheet == null) {
                //System.err.println("ERROR: Hoja '" + hoja + "' no encontrada.");
                workbook.close();
                fis.close();
                return;
            }
            //System.out.println("🔍 DEBUG: LastRowNum en hoja " + hoja + ": " + sheet.getLastRowNum());

            if(indiceFila < 1 || indiceFila > sheet.getLastRowNum()){
                //System.err.println("ERROR: Índice " + indiceFila + " fuera de rango (1-" + sheet.getLastRowNum() + ").");
                workbook.close(); fis.close();
                return;
            }
            Row rowToDelete = sheet.getRow(indiceFila);
            if (rowToDelete != null) {
                sheet.removeRow(rowToDelete);
                //System.out.println("Fila física eliminada del Sheet.");
            }else {
                //System.out.println("La fila era nula, no se pudo remover físicamente.");
            }

            // Desplazar filas hacia arriba
            if (indiceFila < sheet.getLastRowNum()) {
                sheet.shiftRows(indiceFila + 1, sheet.getLastRowNum(), -1);
                //System.out.println("Filas desplazadas hacia arriba.");
            }

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();
            fis.close();
            //System.out.println("Fila " + indiceFila + " eliminada correctamente de la hoja " + hoja);
        } catch (IOException e) {
            e.printStackTrace();
            //System.err.println("Excepción al eliminar fila por índice: " + e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            //System.err.println("Excepción General al eliminar fila por índice: " + e.getMessage());
        }
    }



    /**
     * Establece el valor de una celda según su tipo.
     */
    private static void setCellValue(Cell cell, String valor){
        if(valor == null || valor.trim().isEmpty()){
            cell.setCellValue("");
        }else if(isNumeric(valor)){
            cell.setCellValue(Double.parseDouble(valor.replace(",",".")));
        }else{
            cell.setCellValue(valor);
        }
    }

    /**
     * Verifica si una cadena representa un valor numérico.
     */
    private static boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Double.parseDouble(s.replace(',', '.'));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Obtiene el valor de una celda como string, evaluando fórmulas si se proporciona un evaluador.
     */
    private static String getCellValueAsString(Cell cell) {
        return getCellValueAsString(cell,null);
    }

    /**
     * Versión sobrecargada que acepta un evaluador de fórmulas.
     */
    public static String getCellValueAsString(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return "";

        try {
            if (cell.getCellType() == CellType.FORMULA) {
                CellValue cv = evaluator.evaluate(cell);
                switch (cv.getCellType()) {
                    case STRING: return cv.getStringValue() != null ? cv.getStringValue().trim() : "";
                    case NUMERIC: return String.valueOf(cv.getNumberValue());
                    case BOOLEAN: return String.valueOf(cv.getBooleanValue());
                    default: return "";
                }
            }

            /** Si no es fórmula, usa el formateador (maneja "0000", fechas, etc.)*/
            return formatter.formatCellValue(cell).trim();

        } catch (Exception e) {
            /** Fallback: intentar leer como string directamente (solo si no es fórmula)*/
            if (cell.getCellType() == CellType.STRING) {
                return cell.getStringCellValue().trim();
            }
            return "";
        }
    }


    /** Metodos de busqueda */

    /**
     * Verifica si un valor existe en una columna específica de la hoja Condensadoras.
     *
     * @param valor valor a buscar
     * @param columna nombre de la columna (case-insensitive)
     * @return true si se encuentra el valor, false en caso contrario
     */
    public static boolean existParametroEnCondensadoras(String valor, String columna){
        if (valor == null || valor.trim().isEmpty()) return false;
        List<List<String>> cond = leerHoja("Condensadoras");
        if (cond.size() < 2){
            return false;
        }
        /** Obtener encabezados (primera fila)*/
        List<String> encabezados = cond.get(0);
        /** Encontrar el índice de la columna solicitada */
        int indexColumn = -1;
        for(int i = 0; i<encabezados.size();i++){
            if(encabezados.get(i).trim().equalsIgnoreCase(columna)){
                indexColumn = i;
                break;
            }
        }
        if(indexColumn == -1) {
            //System.err.println("La columna " + columna + " no se encuentra en condensadoras");
            return false;
        }
        for(int i = 1; i<cond.size();i++){
            List<String>fila = cond.get(i);
            if(fila.size()>indexColumn){
                String valorFila = fila.get(indexColumn).trim();
                if(valor.equals(valorFila)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Verifica si un valor existe en una columna específica de la hoja Cassette.
     *
     * @param valor valor a buscar
     * @param columna nombre de la columna (case-insensitive)
     * @return true si se encuentra el valor, false en caso contrario
     */
    public static boolean existParametroEnCassettes(String valor, String columna){
        if (valor == null || valor.trim().isEmpty()) return false;
        List<List<String>> cassettes = leerHoja("Cassette");
        if (cassettes.size() < 2){
            return false;
        }
        /** Obtener encabezados (primera fila) */
        List<String> encabezados = cassettes.get(0);
        /** Encontrar el índice de la columna solicitada */
        int indexColumn = -1;
        for(int i = 0; i<encabezados.size();i++){
            if(encabezados.get(i).trim().equalsIgnoreCase(columna)){
                indexColumn = i;
                break;
            }
        }
        if(indexColumn == -1) {
            //System.err.println("La columna " + columna + " no se encuentra en cassette");
            return false;
        }
        for(int i = 1; i<cassettes.size();i++){
            List<String>fila = cassettes.get(i);
            if(fila.size()>indexColumn){
                String valorFila = fila.get(indexColumn).trim();
                if(valor.equals(valorFila)){
                    return true;
                }
            }
        }
        return false;
    }

    /** Gestion de Averias*/

     /**
     * Registra automáticamente una avería en la hoja AVERIAS y actualiza el equipo origen.
     * <p>
     *    Este metodo asume que el número de avería ya ha sido generado externamente.
     * </p>
     * @param equipo clase de equipo (Cassette o Condensadora)
     * @param codigo codigo del equipo seleccionado
     * @param planta planta donde se encuentra el equipo (Solo Cassette)
     * @param localizacion localizacion del equipo
     * @param observaciones anotaciones
     * @param hoja hoja del Excel a la que pertenece el equipo
      * @param numAveria numero que corresponde a la averia
     */
    public static void registrarAveriaAutomaticamente(String equipo, String codigo, String planta, String localizacion, String observaciones, String hoja, String numAveria ){
        //System.out.println("registrando avería: num=" + numAveria + ", equipo=" + equipo + ", codigo=" + codigo);
        try{
            String fechaAveria = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            //String mails = "qgil@euromadi.es";
            List<List<String>> correos = leerHoja("PARAM_CORREOS_ELECTRONICOS");
            StringBuilder destinos = new StringBuilder();
            for(int i = 1; i < correos.size(); i++){
                if(!correos.get(i).isEmpty() && !correos.get(i).get(0).trim().isEmpty()){
                    if(destinos.length() > 0) {
                        destinos.append("\n");
                    }
                    destinos.append(correos.get(i).get(0).trim());
                }
            }

            añadirFila("AVERIAS",
            numAveria,
                    "NO",
                    equipo,
                    codigo,
                    "AVERIADO",
                    planta != null ? planta : "",
                    localizacion != null ? localizacion : "",
                    fechaAveria,
                    destinos.toString(),
                    observaciones != null ? observaciones : "");

            List<List<String>> hojaOrigen = leerHoja(hoja);
            int columnAveria = ("Cassette".equals(hoja)) ? 15 : 11;

            for(int i = 1; i<hojaOrigen.size(); i++){
                List<String> filaOrigen = hojaOrigen.get(i);
                if(filaOrigen.size() > 0 && filaOrigen.get(0).trim().equals(codigo)){
                    while (filaOrigen.size() <= columnAveria) filaOrigen.add("");

                    filaOrigen.set(columnAveria, numAveria);
                    modificarFilaConFormato(hoja, i, filaOrigen.toArray(new String[0]),columnAveria);
                    break;
                }
            }

            /** Enviar correo automático. */
            enviarCorreoAveria(equipo, codigo, observaciones);

            //System.out.println("Avería automática creada");



        } catch (Exception e) {
            e.printStackTrace();
            //System.err.println("Error al registrar avería automática");
        }
    }

    /**
     * Envía un correo de notificación de avería usando Gmail SMTP.
     *
     * @param equipo equipo en el que se ha producido la averia
     * @param codigo codigo del equipo
     * @param motivo explicacion del motivo causante de la averia
     */
    public static  void enviarCorreoAveria(String equipo, String codigo, String motivo){
        Platform.runLater(() -> {
            MainAppController.enviandoCorreo = true;
            showAlert("Iniciando envío de correo de AVERÍA...");
        });
        new Thread(() -> {
            try{

                showAlert("Iniciando envío de correo de AVERÍA...");

                String asunto, cuerpo;
                if ("CONDENSADORA".equals(equipo)) {
                    asunto = "AVERIA CONDENSADORA " + codigo;
                    cuerpo = "La condensadora " + codigo + " esta averiada.";
                } else {
                    asunto = "AVERIA CASSETTE " + codigo;
                    cuerpo = "El cassette " + codigo + " esta averiado.";
                }

                List<List<String>> correos = leerHoja("PARAM_CORREOS_ELECTRONICOS");
                StringBuilder destinatarios = new StringBuilder();
                for(int i = 1; i < correos.size(); i++){
                    if(!correos.get(i).isEmpty() && !correos.get(i).get(0).trim().isEmpty()){
                        if(destinatarios.length() > 0) destinatarios.append(", ");
                        destinatarios.append(correos.get(i).get(0).trim());
                    }
                }
                if(destinatarios.length() == 0){
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Advertencia");
                    alert.setHeaderText(null);
                    alert.setContentText("No existen destinatarios");
                    alert.showAndWait();
                }

                Properties props = new Properties();
                props.put("mail.smtp.host", "192.168.26.117");
                props.put("mail.smtp.port", "25");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "false");

                String mailOrigenUsuario = "no-reply@euromadi.es";
                String mailOrigenPassword = "n0n0n0";

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(mailOrigenUsuario, mailOrigenPassword);
                    }
                });
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(mailOrigenUsuario));
                //String mailUsuarioDestino = "qgil@euromadi.es";
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatarios.toString()));
                message.setSubject(asunto);
                message.setText(cuerpo);

                Transport.send(message);
                //System.out.println(" [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] Correo enviado desde " + mailOrigenUsuario + " a lugilla2269@gmail.com" );

                Platform.runLater(() -> {
                    Alert exito = new Alert(Alert.AlertType.INFORMATION);
                    exito.setTitle("Correo enviado");
                    exito.setHeaderText(null);
                    exito.setContentText(
                            "Correo enviado correctamente desde:\n" +
                                    mailOrigenUsuario + "\n" +
                                    "A:\n" +
                                    destinatarios
                    );
                    exito.showAndWait();
                });
            } catch (Exception e) {
                //System.err.println(" Error al enviar correo: " + e.getMessage());
                //e.printStackTrace();
                String mensajeError = e.getMessage() != null ? e.getMessage() : "Error desconocido al enviar el correo." ;
                Platform.runLater(() -> {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error al enviar correo");
                    error.setHeaderText("No se pudo enviar la notificación de avería");
                    error.setContentText("Detalles:\n" + mensajeError);
                    error.showAndWait();
                });
            }finally {
                Platform.runLater(() -> {
                    MainAppController.enviandoCorreo = false;
                    showAlert("Proceso de correo de AVERÍA finalizado.");
                });
            }
        }).start();
    }

    /**
     * Envía un correo de confirmación cuando una avería se marca como REPARADA.
     *
     * @param equipo tipo de equipo ("CASSETTE" o "CONDENSADORA")
     * @param codigo identificador del equipo
     * @param numAveria número de avería reparada
     */
    public static void enviarCorreoConfirmacionReparacionAveria(String equipo, String codigo, String numAveria) {
        Platform.runLater(() -> {
            MainAppController.enviandoCorreo = true;
            showAlert("Iniciando envío de correo de AVERÍA...");
        });
        new Thread(() -> {
            // Activar bandera de envío para bloquear UI
            try {
                String asunto = "Avería REPARADA - " + equipo + " " + codigo;
                String cuerpo = "La avería nº " + numAveria + " del equipo " + equipo + " " + codigo +
                        " ha sido marcada como REPARADA y el equipo ha sido restaurado a estado ACTIVO.\n\n" +
                        "Fecha de reparación: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                // Leer destinatarios desde PARAM_CORREOS_ELECTRONICOS
                List<List<String>> correos = leerHoja("PARAM_CORREOS_ELECTRONICOS");
                StringBuilder destinos = new StringBuilder();
                for (int i = 1; i < correos.size(); i++) {
                    if (!correos.get(i).isEmpty() && !correos.get(i).get(0).trim().isEmpty()) {
                        if (destinos.length() > 0) destinos.append(", ");
                        destinos.append(correos.get(i).get(0).trim());
                    }
                }

                if (destinos.length() == 0) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Sin destinatarios");
                        alert.setHeaderText(null);
                        alert.setContentText("No hay correos configurados en PARAM_CORREOS_ELECTRONICOS");
                        alert.showAndWait();
                    });
                    return;
                }

                // Configuración SMTP
                Properties props = new Properties();
                props.put("mail.smtp.host", "192.168.26.117");
                props.put("mail.smtp.port", "25");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "false");

                String mailOrigenUsuario = "no-reply@euromadi.es";
                String mailOrigenPassword = "n0n0n0";

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(mailOrigenUsuario, mailOrigenPassword);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(mailOrigenUsuario));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinos.toString()));
                message.setSubject(asunto);
                message.setText(cuerpo);

                Transport.send(message);

                // Mostrar alerta de éxito en hilo principal
                Platform.runLater(() -> {
                    Alert exito = new Alert(Alert.AlertType.INFORMATION);
                    exito.setTitle("Correo enviado");
                    exito.setHeaderText(null);
                    exito.setContentText(
                            "Correo de confirmación de reparación enviado correctamente desde:\n" +
                                    mailOrigenUsuario + "\n" +
                                    "A:\n" +
                                    destinos
                    );
                    exito.showAndWait();
                });

            } catch (Exception e) {
                //e.printStackTrace();
                String mensajeError = e.getMessage() != null ? e.getMessage() : "Error desconocido al enviar el correo.";

                Platform.runLater(() -> {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error al enviar correo");
                    error.setHeaderText("No se pudo enviar la confirmación de reparación");
                    error.setContentText("Detalles:\n" + mensajeError);
                    error.showAndWait();
                });
            } finally {
                // Siempre desactivar la bandera al terminar
                Platform.runLater(() -> {
                    MainAppController.enviandoCorreo = false;
                    showAlert("Proceso de correo de AVERÍA finalizado.");
                });
            }
        }).start();
    }

    /**
     * Modifica una fila en una hoja Excel y aplica un estilo específico a una columna (opcional).
     *
     * @param hoja Nombre de la hoja
     * @param indiceFila Índice de la fila (1-based)
     * @param valores Nuevos valores de la fila
     * @param columnaConFormato Índice de la columna que requiere formato (ej. 15 para AVERIA en Cassette)
     */
    public static void modificarFilaConFormato(String hoja, int indiceFila, String[] valores, int columnaConFormato) {
        File file = getExcelFile();
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet(hoja);
            if (sheet == null || indiceFila < 1 || indiceFila > sheet.getLastRowNum()) return;

            Row row = sheet.getRow(indiceFila);
            if (row == null) return;

            /** Crear estilo general */
            CellStyle estiloGeneral = workbook.createCellStyle();
            estiloGeneral.setAlignment(HorizontalAlignment.CENTER);
            estiloGeneral.setVerticalAlignment(VerticalAlignment.CENTER);
            estiloGeneral.setBorderTop(BorderStyle.THIN);
            estiloGeneral.setBorderBottom(BorderStyle.THIN);
            estiloGeneral.setBorderLeft(BorderStyle.THIN);
            estiloGeneral.setBorderRight(BorderStyle.THIN);
            estiloGeneral.setWrapText(true);

            /** Crear estilo con formato "0000" */
            CellStyle estiloNumAveria = workbook.createCellStyle();
            estiloNumAveria.cloneStyleFrom(estiloGeneral);
            DataFormat format = workbook.createDataFormat();
            estiloNumAveria.setDataFormat(format.getFormat("0000"));

            /** Actualizar celdas */
            for (int i = 0; i < valores.length; i++) {
                Cell cell = row.getCell(i);
                if (cell == null) {
                    cell = row.createCell(i);
                }

                String val = valores[i];
                if (val == null || val.trim().isEmpty()) {
                    cell.setCellValue("");
                    cell.setCellStyle(estiloGeneral);
                } else if (i == columnaConFormato) {
                    // 👉 Columna especial: guardar como número + formato "0000"
                    try {
                        int num = Integer.parseInt(val);
                        cell.setCellValue(num);
                        cell.setCellStyle(estiloNumAveria);
                    } catch (NumberFormatException e) {
                        cell.setCellValue(val);
                        cell.setCellStyle(estiloGeneral);
                    }
                } else if (isNumeric(val)) {
                    cell.setCellValue(Double.parseDouble(val.replace(',', '.')));
                    cell.setCellStyle(estiloGeneral);
                } else {
                    cell.setCellValue(val);
                    cell.setCellStyle(estiloGeneral);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            workbook.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Marca una avería como REPARADA en la hoja AVERIAS.
     *
     * @param equipo tipo de equipo ("CASSETTE" o "CONDENSADORA")
     * @param codigo identificador del equipo afectado
     */
    public static void marcarAveriaComoReparada(String equipo, String codigo) {
        try {
            List<List<String>> averias = leerHoja("AVERIAS");
            for (int i = 1; i < averias.size(); i++) {
                List<String> fila = averias.get(i);
                if (fila.size() >= 4 &&
                        equipo.equals(fila.get(2).trim()) &&
                        codigo.equals(fila.get(3).trim()) &&
                        "AVERIADO".equals(fila.get(4).trim())) {
                    fila.set(4, "REPARADO");
                    modificarFilaPorIndice("AVERIAS", i, fila.toArray(new String[0]));

                    String hojaOrigen = "CASSETTE".equals(equipo) ? "Cassette" : "Condensadoras";
                    int colAveria = "CASSETTE".equals(equipo) ? 15 : 11;

                    List<List<String>> datosOrigen = leerHoja(hojaOrigen);
                    for (int j = 1; j < datosOrigen.size(); j++){
                        List<String> filaOrigen = datosOrigen.get(j);
                        if (filaOrigen.size() > 0 && filaOrigen.get(0).trim().equals(codigo)){
                            while (filaOrigen.size() <= colAveria) filaOrigen.add("");
                            filaOrigen.set(colAveria, ""); // Limpia el campo AVERIA
                            modificarFilaConFormato(hojaOrigen, j, filaOrigen.toArray(new String[0]), colAveria);
                            break;
                        }
                    }
                    //System.out.println("Avería marcada como REPARADA: " + codigo);
                    return;
                }
            }
            //System.err.println("No se encontró avería");
        } catch (Exception e) {
            e.printStackTrace();
            //System.err.println("Error al modificar avería automática");
        }
    }

    /**
     * Modifica una fila en una hoja Excel por su índice.
     * <p>
     * Este metodo es más eficiente que {@link #modificarFila(String, int, String[])}
     * cuando ya se conoce el índice exacto de la fila.
     * </p>
     *
     * @param hoja nombre de la hoja
     * @param indiceFila índice de la fila (0-based)
     * @param valores nuevos valores de la fila
     */
    public static void modificarFilaPorIndice(String hoja, int indiceFila, String[] valores) {
        File file = getExcelFile();
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet(hoja);
            if (sheet == null || indiceFila < 0 || indiceFila > sheet.getLastRowNum()) {
                return;
            }

            Row row = sheet.getRow(indiceFila);
            if (row == null) {
                row = sheet.createRow(indiceFila);
            }

            for (int i = 0; i < valores.length; i++) {
                Cell cell = row.getCell(i);
                if (cell == null) {
                    cell = row.createCell(i);
                }else if (("AVERIA".equals(hoja) && i == 0) || ("Cassette".equals(hoja) && i == 15) || ("Condensadoras".equals(hoja) && i == 11)) {
                    try {
                        int num = Integer.parseInt(valores[i]);
                        cell.setCellValue(num);

                        CellStyle estiloNumAveria = workbook.createCellStyle();
                        estiloNumAveria.cloneStyleFrom(cell.getCellStyle()); // copiar bordes, centrado, etc.
                        DataFormat format = workbook.createDataFormat();
                        estiloNumAveria.setDataFormat(format.getFormat("0000"));
                        cell.setCellStyle(estiloNumAveria);
                    } catch (NumberFormatException e) {
                        setCellValue(cell, valores[i]);

                    }
                }
                setCellValue(cell, valores[i]);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            workbook.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * Metodo para mostrar una alerta de advertencia con el mensaje especificado.
     *
     * @param message mensaje a mostrar en la alerta
     */
    public static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Actualiza la columna FECHA_REVISION de un equipo en su hoja correspondiente.
     *
     * @param hoja nombre de la hoja ("Cassette" o "Condensadoras")
     * @param codigo identificador del equipo (NUM_CASSETTE o CONDENSADORA)
     * @param fechaRevision fecha de revisión en formato dd/MM/yyyy
     */
    public static void actualizarFechaRevision(String hoja, String codigo,int numSecuencia, String fechaRevision) {
        try {
            List<List<String>> datos = leerHoja(hoja);

            int colFechaRev = "Condensadoras".equals(hoja) ? 10 : 14; // Índice de FECHA_REVISION
            //int colNumSecuencia = 1;

            for (int i = 1; i < datos.size(); i++) {
                List<String> fila = datos.get(i);
                if (fila.size() > 1 && codigo.equals(fila.get(0).trim()) && String.valueOf(numSecuencia).equals(fila.get(1).trim())) {
                    while (fila.size() <= colFechaRev) {
                        fila.add("");
                    }

                    fila.set(colFechaRev, fechaRevision);
                    modificarFila(hoja, i, fila.toArray(new String[0]));
                    break;
                }
            }
        } catch (Exception e) {
            //System.err.println("Error al actualizar");
            e.printStackTrace();
        }
    }

    /**
     * Obtiene el número de días entre revisiones desde la hoja PARAM_DIAS_REVISION.
     */
    public static int getDiasRevision(){
        try {
            List<List<String>> datos = leerHoja("PARAM_DIAS_REVISION");
            if(datos.size() > 1 && !datos.get(1).isEmpty()){
                String valor = datos.get(1).get(0).trim();
                if(!valor.isEmpty()){
                    return Integer.parseInt(valor);
                }
            }
        } catch (Exception e) {
            //System.err.println("Error al leer PARAM_DIAS_REVISION. Usando 365 días.");
            e.printStackTrace();
        }
        return 365;
    }

    /**
     * Calcula la primera fecha de revisión >= hoy.
     */
    public static LocalDate calcularProximaFechaRevision(LocalDate fechaInstalacion, int diasRevision){
        LocalDate hoy = LocalDate.now();
        if(!hoy.isAfter(fechaInstalacion)){
            return fechaInstalacion.plusDays(diasRevision);
        }
        long diasDesdeInst = ChronoUnit.DAYS.between(fechaInstalacion, hoy);
        long periodosCompletos = diasDesdeInst / diasRevision;
        return fechaInstalacion.plusDays((periodosCompletos + 1) * diasRevision);
    }

    /**
     * Calcula y actualiza la fecha de revisión para un equipo específico.
     * Si la celda FECHA_REVISION en el equipo está vacía, calcula la próxima fecha basada en la instalación.
     * Si ya tiene fecha, NO la modifica (para respetar cambios manuales).
     * Si la fecha (calculada o existente) entra en el rango de aviso (hoy >= fecha - 30 días),
     * crea o actualiza la entrada en la hoja REVISIONES.
     *
     * @param equipo         "CONDENSADORA" o "CASSETTE"
     * @param codigo         Identificador del equipo (ej. "A", "14")
     * @param fechaInstStr   Fecha de instalación en formato "dd/MM/yyyy"
     * @param numSecuencia   Número de secuencia (usualmente 1)
     * @param diasRevision   Días entre revisiones (ej. 365)
     */
    public static void calcularYActualizarRevisionIndividual(String equipo, String codigo, String fechaInstStr, int numSecuencia, int diasRevision) {
        try {
            if (codigo == null || codigo.trim().isEmpty() || fechaInstStr == null || fechaInstStr.trim().isEmpty()) {
                return;
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate fechaInst = parsearFechaRobusto(fechaInstStr);
            if (fechaInst == null) return;

            String hojaOrigen = "CONDENSADORA".equals(equipo) ? "Condensadoras" : "Cassette";

            int colFechaRevIndex = "Condensadoras".equals(hojaOrigen) ? 10 : 14;

            List<List<String>> datosEquipo = leerHoja(hojaOrigen);
            boolean encontrado = false;

            for (int i = 1; i < datosEquipo.size(); i++) {
                List<String> fila = datosEquipo.get(i);

                // Verificar si coincide el código y la secuencia
                if (fila.size() > 1 &&
                        codigo.equals(fila.get(0).trim()) &&
                        String.valueOf(numSecuencia).equals(fila.get(1).trim())) {

                    encontrado = true;
                    String fechaRevExistenteStr = "";
                    if (fila.size() > colFechaRevIndex) {
                        fechaRevExistenteStr = fila.get(colFechaRevIndex).trim();
                    }

                    LocalDate fechaRevisionFinal = null;

                    if (!fechaRevExistenteStr.isEmpty()) {
                        fechaRevisionFinal = parsearFechaRobusto(fechaRevExistenteStr);
                        if (fechaRevisionFinal == null) {
                            fechaRevisionFinal = calcularProximaFechaRevision(fechaInst, diasRevision);
                        }
                    } else {
                        fechaRevisionFinal = calcularProximaFechaRevision(fechaInst, diasRevision);

                        String frStr = fechaRevisionFinal.format(fmt);
                        while (fila.size() <= colFechaRevIndex) {
                            fila.add("");
                        }
                        fila.set(colFechaRevIndex, frStr);
                        modificarFila(hojaOrigen, i, fila.toArray(new String[0]));
                    }

                    if (fechaRevisionFinal != null) {
                        LocalDate hoy = LocalDate.now();
                        LocalDate fechaDesde = fechaRevisionFinal.minusDays(30);

                        if (!hoy.isBefore(fechaDesde)) {
                            String estado = fila.size() > 2 ? fila.get(2).trim() : "ACTIVA";
                            String planta = "";
                            String localizacion = "";

                            if ("Cassette".equals(hojaOrigen)) {
                                planta = fila.size() > 3 ? fila.get(3).trim() : ""; // Col 3 es Planta en Cassette
                                localizacion = fila.size() > 10 ? fila.get(10).trim() : ""; // Col 10 es Loc_Cond en Cassette
                            } else {
                                planta = "";
                                localizacion = fila.size() > 6 ? fila.get(6).trim() : "";
                            }

                            List<List<String>> datosRev = leerHoja("REVISIONES");
                            boolean yaExisteRevision = false;
                            int indiceRevisionExistente = -1;

                            for (int j = 1; j < datosRev.size(); j++) {
                                List<String> filaRev = datosRev.get(j);
                                if (filaRev.size() > 4 &&
                                        equipo.equals(filaRev.get(3).trim()) &&
                                        codigo.equals(filaRev.get(4).trim())) {

                                    yaExisteRevision = true;
                                    indiceRevisionExistente = j;
                                    break;
                                }
                            }

                            if (!yaExisteRevision) {
                                crearEntradaRevision(equipo, codigo, estado, planta, localizacion, fechaRevisionFinal, fechaDesde);
                            } else {}
                        } else {}
                    }
                    break;
                }
            }

            if (!encontrado) {
                System.err.println("⚠️ Equipo no encontrado para calcular revisión: " + equipo + " - " + codigo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Crea una nueva entrada en la hoja REVISIONES para un equipo que necesita revisión.
     *
     * @param equipo tipo de equipo ("CASSETTE" o "CONDENSADORA")
     * @param codigo identificador del equipo
     * @param estado estado actual del equipo (ej. ACTIVA, INACTIVA)
     * @param planta planta donde se encuentra el equipo (solo para cassette)
     * @param localizacion localización del equipo
     * @param fechaRevision fecha programada para la revisión
     * @param fechaDesde fecha desde la cual se considera que el equipo necesita revisión
     */
    public static void crearEntradaRevision(String equipo, String codigo, String estado, String planta, String localizacion, LocalDate fechaRevision, LocalDate fechaDesde){
        //System.out.println("📧 Creando revisión para: " + equipo + " " + codigo);
        try{
            List<List<String>> revisiones = leerHoja("REVISIONES");
            for (List<String> rev : revisiones) {
                if (rev.size() > 2 && equipo.equals(rev.get(2)) && codigo.equals(rev.get(3))) {
                    return;
                }
            }
            int maxNum = 0;

            for (int i = 1; i < revisiones.size(); i++) {
                if (!revisiones.get(i).isEmpty() && !revisiones.get(i).get(0).trim().isEmpty()) {
                    try {
                        int n = Integer.parseInt(revisiones.get(i).get(0).trim());
                        if (n > maxNum) maxNum = n;
                    } catch (NumberFormatException ignored) {}
                }
            }

            String numRev = String.format("%04d", maxNum + 1);
            DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String rango = "desde: " + fechaDesde.format(f) + "\n hasta: " + fechaRevision.format(f);
            añadirFila("REVISIONES", numRev,"NO", equipo, codigo, estado, planta, localizacion, rango,"","NO ENVIADO","","");

            enviarCorreoAvisoRevision(equipo, codigo, numRev);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Envía un correo de aviso cuando se crea una revisión.
     *
     * @param equipo tipo de equipo ("CASSETTE" o "CONDENSADORA")
     * @param codigo identificador del equipo
     * @param numRevision número de revisión asignado en la hoja REVISIONES
     *
     */
    public static void enviarCorreoAvisoRevision(String equipo, String codigo, String numRevision) {
        //System.out.println("📤 Intentando enviar correo para revisión: " + equipo);
        Platform.runLater(() -> {
            MainAppController.enviandoCorreo=true;
            showAlert("Iniciando envío de correo de una REVISION...");
        });
        new Thread(() -> {
            boolean exitos = false;
            try {
                String asunto = "Revisión de la " + equipo + " " + codigo;
                String cuerpo = "Tienes 30 días para pasar la revisión de la " + equipo + " " + codigo + ".";

                List<List<String>> correos = leerHoja("PARAM_CORREOS_ELECTRONICOS");
                StringBuilder destinos = new StringBuilder();
                for (int i = 1; i < correos.size(); i++) {
                    if (!correos.get(i).isEmpty() && !correos.get(i).get(0).trim().isEmpty()) {
                        if (destinos.length() > 0) destinos.append(", ");
                        destinos.append(correos.get(i).get(0).trim());
                    }
                }

                if (destinos.length() == 0) return;

                Properties props = new Properties();
                props.put("mail.smtp.host", "192.168.26.117");
                props.put("mail.smtp.port", "25");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "false");

                String mailOrigenUsuario = "no-reply@euromadi.es";
                String mailOrigenPassword = "n0n0n0";

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(mailOrigenUsuario, mailOrigenPassword);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(mailOrigenUsuario));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinos.toString()));
                message.setSubject(asunto);
                message.setText(cuerpo);

                Transport.send(message);
                exitos = true;
                //System.out.println("✅ Correo enviado para: " + equipo);
                Platform.runLater(() -> {
                    Alert exito = new Alert(Alert.AlertType.INFORMATION);
                    exito.setTitle("Correo enviado");
                    exito.setHeaderText(null);
                    exito.setContentText(
                            "Correo enviado correctamente desde:\n" +
                                    mailOrigenUsuario + "\n" +
                                    "A:\n" +
                                    destinos
                    );
                    exito.showAndWait();
                });
            } catch (Exception e) {
                //e.printStackTrace();
                exitos = false;
                //System.err.println("❌ Error al enviar correo: " + e.getMessage());
                String mensajeError = e.getMessage() != null ? e.getMessage() : "Error desconocido al enviar el correo." ;
                Platform.runLater(() -> {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error al enviar correo");
                    error.setHeaderText("No se pudo enviar la notificación de revision");
                    error.setContentText("Detalles:\n" + mensajeError);
                    error.showAndWait();
                });
            }finally {
                boolean finalExitos = exitos;
                Platform.runLater(()->{
                    MainAppController.enviandoCorreo = false;
                    showAlert("Proceso de envío de correo de REVISION finalizado.");

                    if (finalExitos) {
                        Platform.runLater(() -> {
                            try {
                                List<List<String>> datos = leerHoja("REVISIONES");
                                for (int i = 1; i < datos.size(); i++) {
                                    List<String> fila = datos.get(i);
                                    if (fila.size() > 0 && numRevision.equals(fila.get(0).trim())) {
                                        while (fila.size() <= 9) fila.add("");
                                        fila.set(9, "ENVIADO"); // índice 9 = ENVIAR_MAIL
                                        modificarFila("REVISIONES", i, fila.toArray(new String[0]));
                                        break;
                                    }
                                }

                                revisionActualizada.set(true);
                                revisionActualizada.set(false);
                                //System.out.println("✅ Correo enviado para: " + numRevision + " con la tabla actualizada");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                    }
                });
            }

        }).start();
    }

    /**
     * Envía un correo de confirmación cuando se marca una revisión como completada.
     *
     * @param equipo tipo de equipo ("CASSETTE" o "CONDENSADORA")
     * @param codigo identificador del equipo
     */
    public static void enviarCorreoConfirmacionRevision(String equipo, String codigo) {
        Platform.runLater(() -> {
            MainAppController.enviandoCorreo=true;
            showAlert("Iniciando confirmacion de envio de correo de una REVISION...");
        });

        new Thread(() -> {
            try {
                String asunto = "Revisión realizada de la " + equipo + " " + codigo;
                String cuerpo = "La revisión de la " + equipo + " " + codigo + " se ha realizado con éxito.";

                List<List<String>> correos = leerHoja("PARAM_CORREOS_ELECTRONICOS");
                StringBuilder destinos = new StringBuilder();
                for (int i = 1; i < correos.size(); i++) {
                    if (!correos.get(i).isEmpty() && !correos.get(i).get(0).trim().isEmpty()) {
                        if (destinos.length() > 0) destinos.append(", ");
                        destinos.append(correos.get(i).get(0).trim());
                    }
                }

                if (destinos.length() == 0) return;

                Properties props = new Properties();
                props.put("mail.smtp.host", "192.168.26.117");
                props.put("mail.smtp.port", "25");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "false");

                String mailOrigenUsuario = "no-reply@euromadi.es";
                String mailOrigenPassword = "n0n0n0";

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(mailOrigenUsuario, mailOrigenPassword);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(mailOrigenUsuario));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinos.toString()));
                message.setSubject(asunto);
                message.setText(cuerpo);

                Transport.send(message);
                //System.out.println("✅ Correo enviado para: " + equipo);
                Platform.runLater(() -> {
                    Alert exito = new Alert(Alert.AlertType.INFORMATION);
                    exito.setTitle("Correo enviado");
                    exito.setHeaderText(null);
                    exito.setContentText(
                            "Correo enviado correctamente desde:\n" +
                                    mailOrigenUsuario + "\n" +
                                    "A:\n" +
                                    destinos
                    );
                    exito.showAndWait();
                });
            } catch (Exception e) {
                //e.printStackTrace();
                String mensajeError = e.getMessage() != null ? e.getMessage() : "Error desconocido al enviar el correo." ;
                Platform.runLater(() -> {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error al enviar correo");
                    error.setHeaderText("No se pudo enviar la confirmacion de la revision");
                    error.setContentText("Detalles:\n" + mensajeError);
                    error.showAndWait();
                });
            }finally {
                Platform.runLater(() -> {
                    MainAppController.enviandoCorreo=false;
                    showAlert("Confirmacion de envío de correo de REVISION realizada finalizado...");
                });
            }
        }).start();
    }

    /**
     * Obtiene el siguiente número de revisión disponible (como "0001", "0002", etc.)
     * basado en el máximo existente en la hoja REVISIONES.
     *
     * @return siguiente número de revisión en formato de 4 dígitos
     */
    public static String obtenerUltimoNumRevision() {
        int maxNum = 0;
        try {
            List<List<String>> revisiones = ExcelManager.leerHoja("REVISIONES");
            for (int i = 1; i < revisiones.size(); i++) {
                List<String> fila = revisiones.get(i);
                if (!fila.isEmpty() && !fila.get(0).trim().isEmpty()) {
                    try {
                        int num = Integer.parseInt(fila.get(0).trim());
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException e) {

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format("%04d", maxNum );
    }

    /**
     * Metodo robusto para parsear fechas desde cadenas con formatos comunes.
     * Intenta varios formatos antes de devolver null si no se puede parsear.
     *
     * @param fechaStr cadena de fecha a parsear
     * @return LocalDate si se pudo parsear, o null si no se reconoce el formato
     */
    private static LocalDate parsearFechaRobusto(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) return null;
        String s = fechaStr.trim();
        try {
            return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e1) {
            try {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern("M/d/yy"));
            } catch (Exception e2) {
                try {
                    return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yy"));
                } catch (Exception e3) {
                    return null;
                }
            }
        }
    }

    /**
     * Carga las opciones de días de revisión desde la hoja PARAM_DIAS_REVISION.
     * Si no se encuentran opciones válidas, devuelve una lista con el valor por defecto "365".
     *
     * @return lista de opciones de días de revisión
     */
    public static List<String> cargarDiasRevision() {
        List<String> dias = new ArrayList<>();
        try {
            List<List<String>> params = ExcelManager.leerHoja("PARAM_DIAS_REVISION");
            for (int i = 1; i < params.size(); i++) {
                String val = params.get(i).get(0).trim();
                if (!val.isEmpty()) {
                    dias.add(val);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (dias.isEmpty()) {
            dias.add("365");
        }
        return dias;
    }

    /**
     * Obtiene las opciones de días de revisión desde PARAM_DIAS_REVISION.
     */
    public static List<String> getOpcionesDiasRevision() {
        List<String> opciones = new ArrayList<>();
        try {
            List<List<String>> datos = leerHoja("PARAM_DIAS_REVISION");
            for (int i = 1; i < datos.size(); i++) {
                List<String> fila = datos.get(i);
                if (!fila.isEmpty() && !fila.get(0).trim().isEmpty()) {
                    opciones.add(fila.get(0).trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (opciones.isEmpty()) opciones.add("365");
        return opciones;
    }

    /**
     * Añade un nuevo número de días de revisión a la hoja PARAM_DIAS_REVISION
     * manteniendo el orden numérico descendente (de mayor a menor: 365, 180, 100, 90...).
     *
     * @param nuevoDias el valor numérico a añadir (ej. "100")
     */
    public static void añadirDiaRevisionOrdenado(String nuevoDias) {
        synchronized (EXCEL_LOCK) {
            File file = getExcelFile();
            try (FileInputStream fis = new FileInputStream(file)) {
                Workbook workbook = new XSSFWorkbook(fis);
                Sheet sheet = workbook.getSheet("PARAM_DIAS_REVISION");

                if (sheet == null) {
                    workbook.close(); fis.close();
                    return;
                }

                List<Integer> diasList = new ArrayList<>();

                // Leer valores existentes
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null && row.getCell(0) != null) {
                        String val = getCellValueAsString(row.getCell(0)).trim();
                        if (!val.isEmpty()) {
                            try {
                                diasList.add(Integer.parseInt(val));
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }

                // Añadir el nuevo valor si no existe ya
                try {
                    int nuevoVal = Integer.parseInt(nuevoDias);
                    if (!diasList.contains(nuevoVal)) {
                        diasList.add(nuevoVal);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("El valor '" + nuevoDias + "' no es un número válido.");
                    workbook.close(); fis.close();
                    return;
                }

                // Ordenar de MAYOR a MENOR (Descendente)
                Collections.sort(diasList, Collections.reverseOrder());

                // Borrar filas antiguas (manteniendo la cabecera en fila 0)
                for (int i = sheet.getLastRowNum(); i > 0; i--) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        sheet.removeRow(row);
                    }
                }

                // Escribir los valores ordenados
                CellStyle estiloCelda = workbook.createCellStyle();
                estiloCelda.setAlignment(HorizontalAlignment.CENTER);
                estiloCelda.setVerticalAlignment(VerticalAlignment.CENTER);
                estiloCelda.setBorderBottom(BorderStyle.THIN);
                estiloCelda.setBorderTop(BorderStyle.THIN);
                estiloCelda.setBorderRight(BorderStyle.THIN);
                estiloCelda.setBorderLeft(BorderStyle.THIN);

                for (int i = 0; i < diasList.size(); i++) {
                    Row newRow = sheet.createRow(i + 1); // Empezar en fila 1 (debajo de cabecera)
                    Cell cell = newRow.createCell(0);
                    cell.setCellValue(diasList.get(i));
                    cell.setCellStyle(estiloCelda);
                }

                // Guardar cambios
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
                workbook.close();
                fis.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Guarda las observaciones de una avería en la hoja correspondiente (Cassette o Condensadoras).
     *
     * @param hojaNombre nombre de la hoja ("Cassette" o "Condensadoras")
     * @param codigo identificador del equipo
     * @param numSecuencia número de secuencia del equipo (solo para cassette)
     * @param observaciones texto de las observaciones a guardar
     * @param indiceColumnaObs índice de la columna donde se guardarán las observaciones
     */
    public static void guardarObservacionesEnExcel(
            String hojaNombre,
            String codigo,
            int numSecuencia,
            String observaciones,
            int indiceColumnaObs
    ) {
        try {
            List<List<String>> datos = leerHoja(hojaNombre);
            for (int i = 1; i < datos.size(); i++) {
                List<String> fila = datos.get(i);
                if (fila.size() > 1 &&
                        codigo.equals(fila.get(0).trim()) &&
                        String.valueOf(numSecuencia).equals(fila.get(1).trim())) {

                    while (fila.size() <= indiceColumnaObs) {
                        fila.add("");
                    }
                    fila.set(indiceColumnaObs, observaciones);
                    modificarFila(hojaNombre, i, fila.toArray(new String[0]));
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Actualiza el texto de una celda específica en una hoja de Excel.
     * Si la celda no existe, se crea. No se aplica ningún estilo especial.
     *
     * @param nombreHoja nombre de la hoja (ej. "Cassette")
     * @param filaIndex índice de la fila (0-based)
     * @param colIndex índice de la columna (0-based)
     * @param texto nuevo texto a colocar en la celda
     */
    public static void actualizarCeldaObservacion(String nombreHoja, int filaIndex, int colIndex, String texto) {
        try {
            File file = getExcelFile();
            FileInputStream fis = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(fis);
            fis.close();

            Sheet sheet = workbook.getSheet(nombreHoja);
            Row row = sheet.getRow(filaIndex);
            if (row == null) row = sheet.createRow(filaIndex);

            Cell cell = row.getCell(colIndex);
            if (cell == null) cell = row.createCell(colIndex);

            cell.setCellValue(texto);


            CellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.TOP);
            cell.setCellStyle(style);


            row.setHeight((short) -1);

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtiene el índice de la fila en una hoja de Excel que coincide con un código y número de secuencia específicos.
     *
     * @param nombreHoja nombre de la hoja (ej. "Cassette")
     * @param codigo valor del código a buscar (columna A)
     * @param numSecuencia valor del número de secuencia a buscar (columna B)
     * @return índice de la fila encontrada (0-based), o -1 si no se encuentra
     */
    public static int obtenerIndiceFilaExcel(String nombreHoja, String codigo, int numSecuencia) {
        try {
            File file = getExcelFile();
            FileInputStream fis = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet(nombreHoja);

            if (sheet == null) {
                workbook.close();
                fis.close();
                return -1;
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell cellCodigo = row.getCell(0);
                Cell cellSecuencia = row.getCell(1);

                String valCodigo = "";
                String valSecuencia = "";

                if (cellCodigo != null) {
                    valCodigo = formatter.formatCellValue(cellCodigo).trim();
                }

                if (cellSecuencia != null) {
                    valSecuencia = formatter.formatCellValue(cellSecuencia).trim();
                }


                if (valCodigo.equals(codigo) && valSecuencia.equals(String.valueOf(numSecuencia))) {
                workbook.close();
                fis.close();
                return i;
                }
            }

            workbook.close();
            fis.close();

            //System.err.println("❌ No se encontró la fila para: [" + codigo + "] Sec: [" + numSecuencia + "] en hoja: " + nombreHoja);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Guarda una observación en una celda específica aplicando estilo de "Ajuste de Texto" (Wrap Text).
     * Esto permite que el texto baje de línea automáticamente en Excel.
     *
     * @param nombreHoja nombre de la hoja (ej. "Cassette")
     * @param filaIndex índice de la fila (0-based)
     * @param colIndex índice de la columna (0-based)
     * @param texto texto de la observación a guardar
     */
    public static void actualizarCeldaObservacionConEstilo(String nombreHoja, int filaIndex, int colIndex, String texto) {
        try{
            File file = getExcelFile();
            FileInputStream fis = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(fis);
            fis.close();

            Sheet sheet = workbook.getSheet(nombreHoja);
            if (sheet == null) {
                workbook.close();
                return;
            }

            Row row = sheet.getRow(filaIndex);
            if (row == null) {
                row = sheet.createRow(filaIndex);
            }

            Cell cell = row.getCell(colIndex);
            if (cell == null) {
                cell = row.createCell(colIndex);
            }

            cell.setCellValue(texto);

            CellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            style.setVerticalAlignment(VerticalAlignment.TOP);

            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);

            cell.setCellStyle(style);

            row.setHeight((short) -1);

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtiene el índice de la fila en una hoja de Excel que coincide con un código específico en la columna A.
     *
     * @param nombreHoja nombre de la hoja (ej. "Condensadoras")
     * @param codigo valor del código a buscar (columna A)
     * @return índice de la fila encontrada (0-based), o -1 si no se encuentra
     */
    public static int obtenerIndiceFilaPorCodigo(String nombreHoja, String codigo) {
        try {
            File file = getExcelFile();
            FileInputStream fis = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet(nombreHoja);
            if (sheet == null) { workbook.close(); fis.close(); return -1; }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Cell cellCodigo = row.getCell(0);
                String valCodigo = (cellCodigo != null) ? formatter.formatCellValue(cellCodigo).trim() : "";
                if (valCodigo.equals(codigo)) {
                    workbook.close(); fis.close(); return i;
                }
            }
            workbook.close(); fis.close();
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    /**
     * Oculta una columna específica en una hoja de Excel.
     * @param nombreHoja Nombre de la hoja (ej. "Condensadoras")
     * @param indiceColumna Índice de la columna a ocultar (0-based)
     */
    public static void ocultarColumna(String nombreHoja, int indiceColumna) {
        try {
            File file = getExcelFile();
            FileInputStream fis = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet(nombreHoja);

            if (sheet != null) {

                sheet.setColumnHidden(indiceColumna, true);

                FileOutputStream fos = new FileOutputStream(file);
                workbook.write(fos);
                fos.close();
            }
            workbook.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Abre un diálogo para configurar qué columnas de una TableView son visibles.
     * Maneja encabezados personalizados (con botones de filtro) extrayendo el texto del Label interno.
     * @param tabla TableView a configurar
     * @param titulo título del diálogo (ej. "Cassette" o "Condensadoras")
     * @param idTabla identificador único para guardar preferencias (ej. "TABLE_CASSETTE")
     * @param hojaExcel nombre de la hoja de Excel para sincronizar visibilidad (ej. "Cassette")
     */
    public static void abrirConfiguracionColumnas(TableView<?> tabla, String titulo, String idTabla, String hojaExcel) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(tabla.getScene().getWindow());
        dialog.setTitle("Ocultar Columnas - " + titulo);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        VBox containerChecks = new VBox(5);

        for (TableColumn<?, ?> col : tabla.getColumns()) {
            String nombreColumna = "Columna Sin Nombre";

            if (col.getText() != null && !col.getText().isEmpty()) {
                nombreColumna = col.getText();
            }
            else if (col.getGraphic() instanceof HBox) {
                HBox headerBox = (HBox) col.getGraphic();
                for (Node node : headerBox.getChildren()) {
                    if (node instanceof Label) {
                        nombreColumna = ((Label) node).getText();
                        break;
                    }
                }
            }

            CheckBox cb = new CheckBox(nombreColumna);
            cb.setSelected(col.isVisible());

            cb.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                col.setVisible(isSelected);
                guardarPreferenciasColumnas(tabla, idTabla, hojaExcel);
            });

            containerChecks.getChildren().add(cb);
        }

        ScrollPane scrollPane = new ScrollPane(containerChecks);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setOnAction(e -> dialog.close());

        HBox botones = new HBox(btnCerrar);
        botones.setAlignment(Pos.CENTER_RIGHT);
        botones.setPadding(new Insets(10, 0, 0, 0));

        vbox.getChildren().addAll(scrollPane, botones);

        Scene scene = new Scene(vbox, 450, 450);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * Guarda la visibilidad de las columnas de una TableView en las preferencias del usuario.
     * @param tabla TableView a guardar
     * @param idTabla identificador único para guardar preferencias (ej. "TABLE_CASSETTE")
      * @param hojaExcel nombre de la hoja de Excel para sincronizar visibilidad (ej. "Cassette")
     */
    public static void guardarPreferenciasColumnas(TableView<?> tabla, String idTabla, String hojaExcel) {
        Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
        StringBuilder sb = new StringBuilder();

        for (TableColumn<?, ?> col : tabla.getColumns()) {
            String colId = col.getId() != null ? col.getId() : "idx_" + tabla.getColumns().indexOf(col);
            if (col.isVisible()) {

                if (sb.length() > 0) sb.append(",");
                sb.append(colId);
            }
        }

        prefs.put(idTabla + "_VISIBLE_COLS", sb.toString());
        actualizarVisibilidadColumnasEnExcel(hojaExcel, tabla);
    }

    /**
     * Carga y aplica la visibilidad de las columnas desde las preferencias del usuario.
     * Si no hay preferencias guardadas, se mantiene la visibilidad por defecto.
      * @param tabla TableView a configurar
     * @param idTabla identificador único para cargar preferencias (ej. "TABLE_CASSETTE")
     * @param hojaExcel nombre de la hoja de Excel para sincronizar visibilidad (ej. "Cassette")
     */
    public static void cargarPreferenciasColumnas(TableView<?> tabla, String idTabla, String hojaExcel) {
        Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
        String savedCols = prefs.get(idTabla + "_VISIBLE_COLS", "");

        if (savedCols.isEmpty()) return; // Si no hay preferencias guardadas, dejar por defecto

        List<String> visibleColIds = new ArrayList<>();

        if (!savedCols.isEmpty()){
            visibleColIds = Arrays.asList(savedCols.split(","));
        }else{
            for (TableColumn<?, ?> col : tabla.getColumns()) {
                String colId = col.getId() != null ? col.getId() : "idx_" + tabla.getColumns().indexOf(col);
            }
        }

        for (TableColumn<?, ?> col : tabla.getColumns()) {
            String colId = col.getId() != null ? col.getId() : "idx_" + tabla.getColumns().indexOf(col);

            // Si el nombre de la columna está en la lista guardada, se muestra. Si no, se oculta.
            boolean shouldBeVisible = visibleColIds.contains(colId);
            col.setVisible(shouldBeVisible);
        }
        actualizarVisibilidadColumnasEnExcel(hojaExcel, tabla);
    }

    /**
     * Metodo auxiliar que abre el Excel y oculta/muestra columnas según el estado de la TableView.
     * Esto asegura que la visibilidad en Excel esté sincronizada con la configuración del usuario en la aplicación.
      * @param nombreHoja nombre de la hoja de Excel a modificar (ej. "Cassette")
     * @param tabla TableView con la configuración actual de visibilidad de columnas.
     */
    private static void actualizarVisibilidadColumnasEnExcel(String nombreHoja, TableView<?> tabla) {
        try {
            File file = getExcelFile();
            FileInputStream fis = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet(nombreHoja);

            if (sheet != null) {
                int colIndex = 0;
                for (TableColumn<?, ?> col : tabla.getColumns()) {
                    // setColumnHidden usa el índice de columna (0-based)
                    // true = OCULTA, false = VISIBLE
                    sheet.setColumnHidden(colIndex, !col.isVisible());
                    colIndex++;
                }

                FileOutputStream fos = new FileOutputStream(file);
                workbook.write(fos);
                fos.close();
            }
            workbook.close();
            fis.close();



        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al sincronizar columnas con Excel: " + e.getMessage());
        }
    }

    /**
     * Abre el diálogo de impresión nativo del sistema con vista previa.
     * Permite elegir impresora física o PDF, y ajusta la escala automáticamente.
     * Genera un PDF temporal con la tabla filtrada y lo abre para impresión, asegurando que solo los datos visibles se impriman.
      * @param stage ventana principal para mostrar el diálogo
     * @param encabezados lista de encabezados de la tabla (visibles)
     * @param datosFiltrados lista de filas de datos que están actualmente visibles (filtrados)
     * @param tituloPorDefecto título para el informe PDF (ej. "Informe Cassette")
     */
    public static void imprimirConDialogoNativo(Stage stage, List<String> encabezados, List<List<String>> datosFiltrados, String tituloPorDefecto) {

        if (datosFiltrados == null || datosFiltrados.isEmpty()) {
            System.err.println("No hay datos visibles para imprimir.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Informe PDF");


        // 1. Crear archivo temporal en el escritorio
        String fechaSegura = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String nombreArchivo = tituloPorDefecto.replace(" ", "_") + "_" + fechaSegura + ".pdf";
        fileChooser.setInitialFileName(nombreArchivo);

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);

        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));

        File fileToSave = fileChooser.showSaveDialog(stage);

        if (fileToSave != null) {
            try {
                // 2. Configurar documento PDF (Horizontal para tablas anchas)
                Document document = new Document(PageSize.A4.rotate());
                PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                document.open();

                // 3. Título
                Font fontTitulo = new Font(Font.HELVETICA, 16, Font.BOLD);
                Paragraph parrafoTitulo = new Paragraph(tituloPorDefecto.toUpperCase(), fontTitulo);
                parrafoTitulo.setAlignment(Element.ALIGN_CENTER);
                parrafoTitulo.setSpacingAfter(15);
                document.add(parrafoTitulo);

                // 4. Fecha y Nota
                Font fontInfo = new Font(Font.HELVETICA, 9, Font.NORMAL);
                Paragraph info = new Paragraph("Generado el: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " | Vista Filtrada", fontInfo);
                info.setAlignment(Element.ALIGN_RIGHT);
                info.setSpacingAfter(10);
                document.add(info);

                // 5. Crear Tabla
                PdfPTable table = new PdfPTable(encabezados.size());
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);

                // Estilos
                Font fontHeader = new Font(Font.HELVETICA, 9, Font.BOLD);
                Font fontData = new Font(Font.HELVETICA, 8, Font.NORMAL);
                GrayColor grayColor = new GrayColor(220);

                PdfPCell cell;

                // Encabezados
                for (String header : encabezados) {
                    cell = new PdfPCell(new Phrase(header, fontHeader));
                    cell.setBackgroundColor(grayColor);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(4);
                    table.addCell(cell);
                }

                // Datos Filtrados
                for (List<String> fila : datosFiltrados) {
                    for (String dato : fila) {
                        cell = new PdfPCell(new Phrase(dato != null ? dato : "", fontData));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(3);
                        table.addCell(cell);
                    }
                }

                document.add(table);
                document.close();

                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(fileToSave);
                } else {
                    //System.out.println("PDF generado. Ábralo manualmente desde: " + fileToSave.getAbsolutePath());
                }

            } catch(Exception e){
                e.printStackTrace();
                //System.err.println("Error al generar/imprimir vista actual: " + e.getMessage());
            }
        }
    }

    /**
     * Aplica formato "0000" a la columna NUM_AVERIA de una fila específica en AVERIAS.
     * Si la celda no existe, se crea. Si el valor no es un número válido, se deja sin cambios.
      * @param hoja nombre de la hoja (ej. "Cassette" o "Condensadoras")
     * @param indexFila índice de la fila a modificar (0-based)
     * @param numAveriaStr nuevo valor para NUM_AVERIA, que se intentará parsear como número para aplicar el formato.
     *                     Si no es un número válido, no se aplicará el formato.
     */
    public static void modificarFilaAveria(String hoja, int indexFila, String numAveriaStr) {
        try {
            File file = getExcelFile();
            FileInputStream fis = new FileInputStream(file);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet(hoja);

            if (sheet == null || indexFila < 1 || indexFila > sheet.getLastRowNum()) {
                workbook.close(); fis.close(); return;
            }

            Row row = sheet.getRow(indexFila);
            if (row == null) { workbook.close(); fis.close(); return; }

            Cell cell = row.getCell(0); // Columna 0 es NUM_AVERIA
            if (cell == null) cell = row.createCell(0);

            // Escribir el número
            int num = Integer.parseInt(numAveriaStr);
            cell.setCellValue(num);

            // Aplicar formato "0000"
            CellStyle style = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            style.setDataFormat(format.getFormat("0000"));

            // Copiar otros estilos existentes si los hay (bordes, etc.)
            if (cell.getCellStyle() != null) {
                style.cloneStyleFrom(cell.getCellStyle());
            }
            style.setDataFormat(format.getFormat("0000"));

            cell.setCellStyle(style);

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();
            fis.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Actualiza o elimina la entrada en la hoja REVISIONES al cambiar la fecha de revisión de un equipo.
     * @param tipoEquipo "CONDENSADORA" o "CASSETTE"
     * @param codigo El identificador del equipo
     * @param nuevaFechaRevision La nueva fecha calculada (LocalDate)
     */
    public static void actualizarFechaEnHojaRevisiones(String tipoEquipo, String codigo, LocalDate nuevaFechaRevision) {
        try {
            List<List<String>> datosRev = leerHoja("REVISIONES");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            LocalDate hoy = LocalDate.now();
            LocalDate fechaDesdeNueva = nuevaFechaRevision.minusDays(30);

            boolean nuevaEnRango = !hoy.isBefore(fechaDesdeNueva);

            for (int i = 1; i < datosRev.size(); i++) {
                List<String> fila = datosRev.get(i);

                if (fila.size() > 7 &&
                        tipoEquipo.equals(fila.get(2).trim()) &&
                        codigo.equals(fila.get(3).trim())) {

                    if (nuevaEnRango) {
                        String nuevaFechaStr = nuevaFechaRevision.format(fmt);
                        String fechaDesdeStr = fechaDesdeNueva.format(fmt);
                        String nuevoRango = "desde: " + fechaDesdeStr + "\n hasta: " + nuevaFechaStr;

                        fila.set(7, nuevoRango);
                        modificarFila("REVISIONES", i, fila.toArray(new String[0]));
                        //System.out.println("✅ Fecha de revisión actualizada en REVISIONES para: " + tipoEquipo + " " + codigo);
                    } else {
                        eliminarFilaPorIndice("REVISIONES", i);
                        //System.out.println("🗑️ Entrada eliminada de REVISIONES (fuera de rango) para: " + tipoEquipo + " " + codigo);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Elimina una entrada de la hoja REVISIONES basada en el tipo de equipo y su código.
     * @param tipoEquipo "CONDENSADORA" o "CASSETTE"
     * @param codigo Identificador del equipo
     */
    public static void eliminarRevisionPendiente(String tipoEquipo, String codigo) {
        System.out.println("DEBUG: Intentando eliminar revisión para: " + tipoEquipo + " - " + codigo);
        try {
            List<List<String>> datosRev = leerHoja("REVISIONES");
            System.out.println("DEBUG: Filas leídas en REVISIONES: " + datosRev.size());

            boolean encontrado=false;

            for (int i = 1; i < datosRev.size(); i++) {
                List<String> fila = datosRev.get(i);

                if (fila.size() > 4){
                    String equipoEnExcel = fila.get(2).trim();
                    String codigoEnExcel = fila.get(3).trim();
                    //System.out.println("   -> Fila " + i + ": EQUIPO=['" + equipoEnExcel + "'], CODIGO=['" + codigoEnExcel + "]");
                    if(tipoEquipo.equals(equipoEnExcel) && codigo.equals(codigoEnExcel)) {
                        //System.out.println("MATCH ENCONTRADO! Fila índice " + i + ": [" + equipoEnExcel + "] - [" + codigoEnExcel + "]");
                        eliminarFilaPorIndice("REVISIONES", i);
                        encontrado = true;

                        break;
                    }
                }
            }
            if(!encontrado){
                //System.out.println("NO SE ENCONTRÓ COINCIDENCIA. Verifica que el EQUIPO sea 'CONDENSADORA' y el CODIGO sea exactamente '" + codigo + "' sin espacios extra.");
            }
        } catch (Exception e) {
            //System.err.println("ERROR al eliminar revisión pendiente:");
            e.printStackTrace();
        }
    }
}

