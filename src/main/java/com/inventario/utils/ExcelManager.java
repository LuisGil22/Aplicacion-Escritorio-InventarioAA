// com/inventario/utils/ExcelManager.java
package com.inventario.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private static final DataFormatter formatter = new DataFormatter(Locale.ENGLISH);

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
            File dataDir = new File(userHome, "InventarioAA");
            dataDir.mkdirs();

            excelFile = new File(dataDir, "Inventario AA V2.xlsx");

            if (!excelFile.exists()) {

                try (InputStream is = ExcelManager.class.getResourceAsStream("/datos/Inventario AA V2.xlsx");
                     FileOutputStream fos = new FileOutputStream(excelFile)) {
                    if (is != null) {
                        is.transferTo(fos);
                        System.out.println("Excel copiado a: " + excelFile.getAbsolutePath());
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
        List<List<String>> datos = new ArrayList<>();
        File file = getExcelFile();
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook = new XSSFWorkbook(fis);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Sheet sheet = workbook.getSheet(nombreHoja);
            if (sheet == null) {
                return datos;
            }
            System.out.println("Hoja '" + nombreHoja + "' tiene " + sheet.getLastRowNum() + " filas.");
            for (Row row : sheet) {
                System.out.print("Fila " + row.getRowNum() + ": ");
                List<String> fila = new ArrayList<>();
                int lastCell = row.getLastCellNum();
                for (int i = 0; i < Math.max(lastCell,1); i++) {
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

    /**
     * Añade una nueva fila a la hoja especificada, aplicando formato y bordes.
     *
     * @param hoja    nombre de la hoja
     * @param valores valores de la nueva fila
     */
    public static void añadirFila(String hoja, String... valores) {

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
                if (i == 0) {
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
        File excelManager = getExcelFile();
        try(FileInputStream fileInput = new FileInputStream(excelManager)){
            Workbook workbook = new XSSFWorkbook(fileInput);
            Sheet sheet = workbook.getSheet(hoja);
            if(sheet == null || index > sheet.getLastRowNum()) {
                return;
            }
            Row row = sheet.getRow(index);
            if(row == null) {
                row = sheet.createRow(index);
            }

            for(int i=0; i<valoresNuevos.length; i++){
                Cell cell = row.getCell(i);
                if (cell == null) {
                    cell = row.createCell(i);
                }
                setCellValue(cell, valoresNuevos[i]);
            }

            try(FileOutputStream fileOutput = new FileOutputStream(excelManager)){
                workbook.write(fileOutput);
            }
            workbook.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Elimina una fila por el valor de su primera celda.
     *
     * @param hoja nombre de la hoja
     * @param eliminarValor valor de la primera celda de la fila a eliminar
     */
    public static void eliminarFila(String hoja, String eliminarValor){
        File excelManager = getExcelFile();
        try(FileInputStream fileInput = new FileInputStream(excelManager)){
            Workbook workbook = new XSSFWorkbook(fileInput);
            Sheet sheet = workbook.getSheet(hoja);
            if(sheet == null){
                return;
            }
             int index = -1;

            for(int i=1; i<=sheet.getLastRowNum(); i++){
                Row row = sheet.getRow(i);
                if(row != null && row.getLastCellNum()>0){
                    Cell cell = row.getCell(0);
                    if(cell != null && cell.getCellType() == CellType.STRING){
                        if (cell.getStringCellValue().trim().equals(eliminarValor)){
                            index = i;
                            break;
                        }
                    }else if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                        // Si es número, convertir a string (poco probable en PARAM_ESTADO)
                        String numeroStr = String.valueOf((long) cell.getNumericCellValue());
                        if (numeroStr.equals(eliminarValor)) {
                            index = i;
                            break;
                        }
                    }
                }
            }
            if(index != -1){
                sheet.removeRow(sheet.getRow(index));
                for(int i = index; i <= sheet.getLastRowNum(); i++){
                    Row row = sheet.getRow(i + 1);
                    if(row != null){
                        sheet.shiftRows(i + 1, i, 1);
                    }
                }
            }
            try(FileOutputStream fileOutput = new FileOutputStream(excelManager)){
                workbook.write(fileOutput);
            }
            workbook.close();
        }catch (IOException e){
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
            System.err.println("La columna " + columna + " no se encuentra en condensadoras");
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
            System.err.println("La columna " + columna + " no se encuentra en cassette");
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
        System.out.println("registrando avería: num=" + numAveria + ", equipo=" + equipo + ", codigo=" + codigo);
        try{
            String fechaAveria = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String mails = "qgil@euromadi.es";

            añadirFila("AVERIAS",
            numAveria,
                    equipo,
                    codigo,
                    "AVERIADO",
                    planta != null ? planta : "",
                    localizacion != null ? localizacion : "",
                    fechaAveria,
                    mails,
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

            System.out.println("Avería automática creada");



        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al registrar avería automática");
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
                String mailUsuarioDestino = "qgil@euromadi.es";
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailUsuarioDestino));
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
                                    mailUsuarioDestino
                    );
                    exito.showAndWait();
                });
            } catch (Exception e) {
                //System.err.println(" Error al enviar correo: " + e.getMessage());
                //e.printStackTrace();
                String mensajeError = e.getMessage() != null ? e.getMessage() : "Error desconocido al enviar el correo.";
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
                    System.out.println("Avería marcada como REPARADA: " + codigo);
                    return;
                }
            }
            System.err.println("No se encontró avería");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al modificar avería automática");
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
     * Elimina una fila en una hoja Excel por su índice (1-based, como en getLastRowNum).
     */
    /**public static void eliminarFilaPorIndice(String hoja, int indiceFila) {
        File file = getExcelFile();
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet(hoja);
            if (sheet == null || indiceFila < 0 || indiceFila > sheet.getLastRowNum()) {
                return;
            }


            Row rowToDelete = sheet.getRow(indiceFila);
            if (rowToDelete != null) {
                sheet.removeRow(rowToDelete);
            }


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
    }**/

}
