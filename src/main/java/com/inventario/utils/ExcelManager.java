// com/inventario/utils/ExcelManager.java
package com.inventario.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ExcelManager {

    private static File excelFile;

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

    // Inicializa y devuelve la copia del Excel
    public static File getExcelFile() {
        if (excelFile == null) {
            String userHome = System.getProperty("user.home");
            File dataDir = new File(userHome, "InventarioAA");
            dataDir.mkdirs();

            excelFile = new File(dataDir, "Inventario AA V2.xlsx");

            if (!excelFile.exists()) {
                // Copiar desde recursos
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

    /**public static List<List<String>> leerHoja(String nombreHoja){
        List<List<String>> datos = new ArrayList<>();
        File file = getExcelFile();
        try(FileInputStream fileInput = new FileInputStream(file)) {
            Workbook workbook = new XSSFWorkbook(fileInput);
            Sheet sheet = workbook.getSheet(nombreHoja);

            if(sheet != null){
                for(Row row : sheet){
                    List<String> filaDatos = new ArrayList<>();
                    int lastCell = row.getLastCellNum();
                    for(int i=0;i<lastCell;i++){
                        Cell cell = row.getCell(i);
                        String valorCelda = "";
                        if(cell != null){
                            switch (cell.getCellType()){
                                case STRING:
                                    valorCelda = cell.getStringCellValue();
                                    break;
                                case NUMERIC:
                                    if (DateUtil.isCellDateFormatted(cell)) {
                                        valorCelda = cell.getDateCellValue().toString();
                                    } else {
                                        valorCelda = String.valueOf((long) cell.getNumericCellValue());
                                    }
                                    break;
                                case BOOLEAN:
                                    valorCelda = String.valueOf(cell.getBooleanCellValue());
                                    break;
                                default:
                                    valorCelda = "";
                            }
                        }
                        filaDatos.add(valorCelda);
                    }
                    datos.add(filaDatos);
                }
            }
            workbook.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return datos;
    }**/

    public static List<List<String>> leerHoja(String nombreHoja) {
        List<List<String>> datos = new ArrayList<>();
        File file = getExcelFile();
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook = new XSSFWorkbook(fis);
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
                    String valor = getCellValueAsString(cell);
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

    // Añadir una nueva fila a cualquier parametro.

    public static void añadirFila(String hoja, String... valores) {

        File file = getExcelFile();
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet(hoja);
            if (sheet == null) return;

            // Encontrar la última fila REAL con contenido (ignorar filas vacías con formato)
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

            CellStyle estiloCelda = workbook.createCellStyle();
            estiloCelda.setAlignment(HorizontalAlignment.CENTER);
            estiloCelda.setVerticalAlignment(VerticalAlignment.CENTER);
            estiloCelda.setBorderBottom(BorderStyle.THIN);
            estiloCelda.setBorderTop(BorderStyle.THIN);
            estiloCelda.setBorderRight(BorderStyle.THIN);
            estiloCelda.setBorderLeft(BorderStyle.THIN);


            for (int i = 0; i < valores.length; i++) {
                Cell cell = newRow.createCell(i);
                String val = valores[i];
                if (val == null || val.trim().isEmpty()) {
                    cell.setCellValue("");
                } else if (isNumeric(val)) {
                    cell.setCellValue(Double.parseDouble(val.replace(',', '.')));
                } else {
                    cell.setCellValue(val);
                }
                cell.setCellStyle(estiloCelda);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            workbook.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

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

    private static void setCellValue(Cell cell, String valor){
        if(valor == null || valor.trim().isEmpty()){
            cell.setCellValue("");
        }else if(isNumeric(valor)){
            cell.setCellValue(Double.parseDouble(valor.replace(",",".")));
        }else{
            cell.setCellValue(valor);
        }
    }



    private static boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Double.parseDouble(s.replace(',', '.'));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    try {
                        LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
                        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception e) {
                        // Fallback: como número
                        return String.valueOf((long) cell.getNumericCellValue());
                    }
                } else {
                    double d = cell.getNumericCellValue();
                    if (Double.isNaN(d) || Double.isInfinite(d)) {
                        return "";
                    }
                    if (d == (long) d) {
                        return String.valueOf((long) d);
                    } else {
                        DecimalFormat df = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                        return df.format(d).replace('.', ',');
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    public static boolean existParametroEnCondensadoras(String valor, String columna){
        if (valor == null || valor.trim().isEmpty()) return false;
        List<List<String>> cond = leerHoja("Condensadoras");
        if (cond.size() < 2){
            return false;
        }
        // Obtener encabezados (primera fila)
        List<String> encabezados = cond.get(0);
        // Encontrar el índice de la columna solicitada
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

    public static boolean existParametroEnCassettes(String valor, String columna){
        if (valor == null || valor.trim().isEmpty()) return false;
        List<List<String>> cassettes = leerHoja("Cassette");
        if (cassettes.size() < 2){
            return false;
        }
        // Obtener encabezados (primera fila)
        List<String> encabezados = cassettes.get(0);
        // Encontrar el índice de la columna solicitada
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

}
