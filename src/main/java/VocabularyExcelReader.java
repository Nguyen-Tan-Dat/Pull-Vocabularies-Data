import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

public class VocabularyExcelReader {
    private static void writeToExcel(HashMap<String, HashMap<String, String>> data, String outputPath) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Vocabularies");

        int rowIndex = 0;

        // Header row
        Row headerRow = sheet.createRow(rowIndex++);
        headerRow.createCell(0).setCellValue("english_word");
        headerRow.createCell(1).setCellValue("part_of_speech");
        headerRow.createCell(2).setCellValue("vietnamese_word");

        // Data rows
        for (String english : data.keySet()) {
            HashMap<String, String> posMap = data.get(english);
            for (String pos : posMap.keySet()) {
                String vietnamese = posMap.get(pos);
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(english);
                row.createCell(1).setCellValue(pos);
                row.createCell(2).setCellValue(vietnamese);
            }
        }

        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outputPath)) {
            workbook.write(fos);
            workbook.close();
            System.out.println("Xuất file thành công: " + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String excelFilePath = "Vocabularies data.xlsx";
        HashMap<String, HashMap<String, String>> list=new HashMap<>();

        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Sheet đầu tiên

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) { // bỏ dòng tiêu đề
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                Cell cellEnglish = row.getCell(2); // Cột C (index = 2)
                Cell cellVietnamese = row.getCell(3); // Cột D (index = 3)
                Cell cellPOS = row.getCell(4); // Cột E (index = 4)

                String english = getCellStringValue(cellEnglish);
                String vietnamese = getCellStringValue(cellVietnamese);
                String pos = getCellStringValue(cellPOS);
                if(!list.containsKey(english))list.put(english,new HashMap<>());
                if(!list.get(english).containsKey(pos))list.get(english).put(pos,vietnamese);
                else list.get(english).put(pos,list.get(english).get(pos)+" | "+vietnamese);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        for(var i:list.keySet()){
            for(var j: list.get(i).keySet()){
                System.out.println(i+ "\t"+j+"\t"+list.get(i).get(j));
            }
        }
        writeToExcel(list, "data.xlsx");
    }

    private static String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        else if (cell.getCellType() == CellType.NUMERIC) return String.valueOf(cell.getNumericCellValue());
        else if (cell.getCellType() == CellType.BOOLEAN) return String.valueOf(cell.getBooleanCellValue());
        else return "";
    }
}
