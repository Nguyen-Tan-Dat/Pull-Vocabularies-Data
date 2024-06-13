package edu.cmu.sphinx.demo.transcriber;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {
    public static void main(String[] args) {
        String excelFilePath = "Saved translations.xlsx"; // Update path if necessary
        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<List<String>> data = new ArrayList<>();

            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
                for (int i = 2; i <= 3; i++) { // Only process columns 3 and 4
                    Cell cell = row.getCell(i);
                    if (cell != null) {
                        rowData.add(cell.toString());
                    } else {
                        rowData.add(""); // Handle empty cells
                    }
                }
                data.add(rowData);
            }

            System.out.println(phpArrayOutput(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String phpArrayOutput(List<List<String>> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?php\n");
        sb.append("$data = [\n");

        for (List<String> row : data) {
            sb.append("    [");
            for (int i = 0; i < row.size(); i++) {
                sb.append("\"").append(row.get(i)).append("\"");
                if (i < row.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("],\n");
        }

        sb.append("];\n");
        sb.append("?>");
        return sb.toString();
    }
}
