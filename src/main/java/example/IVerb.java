package example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class IVerb {
    public static void main(String[] args) {
        var ens= Test.databaseEnglish();
        ArrayList<String[]> eData = new ArrayList<>();
        String excelFilePath = "IVerb.xlsx";
        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(0);
                    Cell cell1 = row.getCell(1);
                    Cell cell2 = row.getCell(2);
                    if (cell != null && cell1 != null && cell2 != null) {
                        String v1 = cell.getStringCellValue().trim();
                        String v2 = cell1.getStringCellValue().trim();
                        String v3 = cell2.getStringCellValue();
                        var add = false;
                        for (var en:ens) {
                            if (v1.contains(en)) {
                                add = true;
                                break;
                            }
                        }
                        if (add)
                            eData.add(new String[]{v1, v2, v3});

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        var database = Test.databaseEnglish();
        int count = 0;
        ArrayList<String[]> rs = new ArrayList<>();
        for (var i : eData) {
            if (!i[0].contains("*")) {
                rs.add(new String[]{i[0], i[1] + " | " + i[2].trim(), "irregular verb"});
//                    rs.add(new String[]{i[0],i[1]+" | "+i[2].trim(),"verb"});
            }

        }
        writeExcel(rs, "Irregular verbs.xlsx");
        HashSet<String[]> vs = new HashSet<>();
        for (var i : eData) {
            vs.add(new String[]{i[0],"irregular verb"});
            vs.add(new String[]{i[0],"verb"});
        }
        ArrayList<Object> data = new ArrayList<>();
        HashMap<String, Object> row = new HashMap<>();
        row.put("vs", vs);
        row.put("name", "Irregular verbs");
        data.add(row);
        Test.writeTopics(data, "topics/Irregular verbs");

    }

    public static String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    public static void writeExcel(List<String[]> eData, String excelFilePath) {
        Workbook workbook = new XSSFWorkbook();
        try (FileOutputStream fileOut = new FileOutputStream(new File(excelFilePath))) {
            // Tạo sheet mới
            Sheet sheet = workbook.createSheet("eDataSheet");

            // Ghi dữ liệu vào tệp Excel, bắt đầu từ cột 3 (chỉ số cột là 2)
            for (int i = 0; i < eData.size(); i++) {
                Row row = sheet.createRow(i);
                String[] data = eData.get(i);

                // Bắt đầu từ cột 3 (chỉ số 2)
                for (int j = 0; j < data.length; j++) {
                    Cell cell = row.createCell(j + 2);
                    cell.setCellValue(data[j]);
                }
            }

            // Ghi workbook vào file
            workbook.write(fileOut);
            System.out.println("Excel file has been generated successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
