// com/inventario/utils/ExcelManager.java
package com.inventario.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExcelManager {

    private static File excelFile;

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
            if (sheet != null) {
                System.out.println("Hoja '" + nombreHoja + "' tiene " + sheet.getLastRowNum() + " filas.");
                for (Row row : sheet) {
                    System.out.print("Fila " + row.getRowNum() + ": ");
                    List<String> fila = new ArrayList<>();
                    for (Cell cell : row) {
                        String valor = "";
                        switch (cell.getCellType()) {
                            case STRING:
                                valor = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                double numericValue = cell.getNumericCellValue();
                                if (DateUtil.isValidExcelDate(numericValue)) {
                                LocalDate date = Instant.ofEpochMilli(DateUtil.getJavaDate(numericValue).getTime())
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate();
                                    valor = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                } else {
                                    // Es un número normal (no fecha)
                                    if (numericValue == Math.floor(numericValue)) {
                                        valor = String.valueOf((long) numericValue); // entero
                                    } else {
                                        valor = String.valueOf(numericValue); // decimal
                                    }
                                }
                                break;
                            case BOOLEAN:
                                valor = String.valueOf(cell.getBooleanCellValue());
                                break;
                            default:
                                valor = "";
                        }
                        fila.add(valor);
                        System.out.print("[" + valor + "] ");
                    }
                    System.out.println();
                    datos.add(fila);
                }
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
        try (FileInputStream fileInput = new FileInputStream(file)) {
            Workbook workbook = new XSSFWorkbook(fileInput);
            Sheet sheet = workbook.getSheet(hoja);
            if (sheet != null) {
                int lastRow = sheet.getLastRowNum();
                Row newRow = sheet.createRow(lastRow + 1);
                for (int i = 0; i < valores.length; i++) {
                    newRow.createCell(i).setCellValue(valores[i]);
                }
            }
            try (FileOutputStream fileOutput = new FileOutputStream(file)) {
                workbook.write(fileOutput);
            }
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void modificarFila(String hoja, String[] valoresActuales, String[] valoresNuevos){
        File excelManager = getExcelFile();
        try(FileInputStream fileInput = new FileInputStream(excelManager)){
            Workbook workbook = new XSSFWorkbook(fileInput);
            Sheet sheet = workbook.getSheet(hoja);
            if(sheet != null){
                for(Row row : sheet){
                    if(filaCoincide(row, valoresActuales)) {
                        for(int i=0; i<valoresNuevos.length && i< row.getLastCellNum(); i++){
                            row.getCell(i).setCellValue(valoresNuevos[i]);
                        }
                        break;
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

    public static void eliminarFila(String hoja, String... eliminarValor){
        File excelManager = getExcelFile();
        try(FileInputStream fileInput = new FileInputStream(excelManager)){
            Workbook workbook = new XSSFWorkbook(fileInput);
            Sheet sheet = workbook.getSheet(hoja);
            if(sheet != null){
                Row deleteRow = null;
                for(Row row : sheet){
                    if(filaCoincide(row, eliminarValor)){
                        deleteRow = row;
                        break;
                    }
                }
                if(deleteRow != null){
                    sheet.removeRow(deleteRow);
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

    private static boolean filaCoincide(Row row, String[] valores){
        if(row.getLastCellNum()< valores.length) {
            return false;
        }
        for(int i=0; i<valores.length; i++){
            Cell cell = row.getCell(i);
            String valor = (cell != null && cell.getCellType() == CellType.STRING)? cell.getStringCellValue():"";
            if(!valor.equals(valores[i])){
                return false;
            }
        }
        return true;
    }

    public static boolean existEstadoEnCondensadoras(String valorEstado){
        List<List<String>> estadoCond = leerHoja("Condensadoras");
        for(int i = 1; i<estadoCond.size();i++){
            List<String>fila = estadoCond.get(i);
            if(fila.size()>2 && valorEstado.equals(fila.get(2))){
                return true;
            }
        }
        return false;
    }

}
