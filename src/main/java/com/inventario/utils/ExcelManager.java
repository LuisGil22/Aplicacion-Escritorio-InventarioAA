// com/inventario/utils/ExcelManager.java
package com.inventario.utils;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Alert;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

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

            if (!excelFile.exists()) {

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
                        cell.setCellStyle(estiloNumAveria);
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
                            cell.setCellStyle(estiloNumAveria);
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
                    } else {
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
        File file = getExcelFile();
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet(hoja);
            if (sheet == null || indiceFila < 1 || indiceFila > sheet.getLastRowNum()) {
                return;
            }

            Row rowToDelete = sheet.getRow(indiceFila);
            if (rowToDelete != null) {
                sheet.removeRow(rowToDelete);
            }

            // Desplazar filas hacia arriba
            if (indiceFila < sheet.getLastRowNum()) {
                sheet.shiftRows(indiceFila + 1, sheet.getLastRowNum(), -1);
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
        new Thread(() -> {
            try{
                String asunto, cuerpo;
                if ("CONDENSADORA".equals(equipo)) {
                    asunto = "AVERIA CONDENSADORA " + codigo;
                    cuerpo = "La condensadora " + codigo + " se ha averiado por el motivo que sea.";
                } else {
                    asunto = "AVERIA CASSETTE " + codigo;
                    cuerpo = "El cassette " + codigo + " se ha averiado por el motivo que sea.";
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
                        return new PasswordAuthentication(mailOrigenUsuario, mailOrigenPassword.replace(" ", ""));
                    }
                });
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(mailOrigenUsuario));
                //String mailUsuarioDestino = "qgil@euromadi.es";
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatarios.toString()));
                message.setSubject(asunto);
                message.setText(cuerpo);

                System.out.println("    [PRUEBA] Correo simulado:");
                System.out.println("   De: " + mailOrigenUsuario);
                System.out.println("   Para: qgil@euromadi.es");
                System.out.println("   Asunto: " + asunto);
                System.out.println("   Cuerpo: " + cuerpo);
                System.out.println("--------------------------------------------------");

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
                        equipo.equals(fila.get(1).trim()) &&
                        codigo.equals(fila.get(2).trim()) &&
                        "AVERIADO".equals(fila.get(3).trim())) {
                    fila.set(3, "REPARADO");
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
     * Este método es más eficiente que {@link #modificarFila(String, int, String[])}
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
     */
    public static void calcularYActualizarRevisionIndividual(String equipo, String codigo, String fechaInstalacion, int numSecuencia, int diasRevision) {
        try {
            if (codigo == null || codigo.trim().isEmpty() || fechaInstalacion == null || fechaInstalacion.trim().isEmpty()) {
                return;
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate fi = parsearFechaRobusto(fechaInstalacion);
            if (fi == null) return;

            LocalDate fr = calcularProximaFechaRevision(fi, diasRevision);
            LocalDate fd = fr.minusDays(30);

            //diasRevision = 365;
            String frStr = fr.format(fmt);
            String hojaOrigen = "CONDENSADORA".equals(equipo) ? "Condensadoras" : "Cassette";
            actualizarFechaRevision(
                    hojaOrigen,
                    codigo,
                    numSecuencia,
                    frStr
            );

            // Crear en REVISIONES si está en rango
            if (!LocalDate.now().isBefore(fd)) {
                String planta = "";
                String localizacion = "";
                String estado = "ACTIVA";

                List<List<String>> datos = leerHoja(hojaOrigen);
                for (List<String> f : datos) {
                    if (f.size() > 1 && codigo.equals(f.get(0).trim()) && String.valueOf(numSecuencia).equals(f.get(1).trim())) {
                        estado= f.get(2).trim();
                        if ("CASSETTE".equals(equipo)) {
                            if (f.size() > 10){
                                planta = f.get(3).trim();
                                localizacion = f.get(10).trim();
                            }
                        }else {
                            if (f.size() > 6){
                                localizacion = f.get(6).trim();
                            }
                        }
                        break;
                    }
                }


                crearEntradaRevision(equipo, codigo, estado, planta, localizacion, fr, fd);
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
                if (rev.size() > 2 && equipo.equals(rev.get(1)) && codigo.equals(rev.get(2))) {
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
            añadirFila("REVISIONES", numRev, equipo, codigo, estado, planta, localizacion, rango, "NO", "","NO ENVIADO","");

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
        new Thread(() -> {
            boolean exito = false;
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
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");


                String mailOrigenUsuario = "javilopez2269@gmail.com";
                String mailOrigenPassword = "akeq sedl dydi kfwu";

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

                System.out.println("    [PRUEBA] Correo simulado:");
                System.out.println("   De: " + mailOrigenUsuario);
                System.out.println("   Para: " + destinos);
                System.out.println("   Asunto: " + asunto);
                System.out.println("   Cuerpo: " + cuerpo);
                System.out.println("--------------------------------------------------");

                Transport.send(message);
                exito = true;
                //System.out.println("✅ Correo enviado para: " + equipo);

            } catch (Exception e) {
                e.printStackTrace();
                exito = false;
                //System.err.println("❌ Error al enviar correo: " + e.getMessage());
            }

            if (exito) {
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
        }).start();
    }

    /**
     * Envía un correo de confirmación cuando se marca una revisión como completada.
     *
     * @param equipo tipo de equipo ("CASSETTE" o "CONDENSADORA")
     * @param codigo identificador del equipo
     */
    public static void enviarCorreoConfirmacionRevision(String equipo, String codigo) {
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
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");


                String mailOrigenUsuario = "javilopez2269@gmail.com";
                String mailOrigenPassword = "akeq sedl dydi kfwu";

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

                System.out.println("    [PRUEBA] Correo simulado:");
                System.out.println("   De: " + mailOrigenUsuario);
                System.out.println("   Para: " + destinos);
                System.out.println("   Asunto: " + asunto);
                System.out.println("   Cuerpo: " + cuerpo);
                System.out.println("--------------------------------------------------");

                Transport.send(message);
                //System.out.println("✅ Correo enviado para: " + equipo);
            } catch (Exception e) {
                e.printStackTrace();
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
                        // Intentar parsear como número (ignora "NO ENVIADO", etc.)
                        int num = Integer.parseInt(fila.get(0).trim());
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException e) {
                        // Ignorar filas con texto en NUM_REVISION
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format("%04d", maxNum );
    }

    /**
     * Método robusto para parsear fechas desde cadenas con formatos comunes.
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


}
