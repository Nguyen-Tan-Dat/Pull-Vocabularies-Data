package example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;


public class IVerb {
    public static void main(String[] args) {

        ArrayList<String[]> eData = new ArrayList<>();
            String excelFilePath = "IVerb.xlsx";
            try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
                 Workbook workbook = new XSSFWorkbook(fis)) {

                // Giả sử dữ liệu nằm trên sheet đầu tiên
                Sheet sheet = workbook.getSheetAt(0);

                // Bắt đầu đọc từ dòng thứ 3 (chỉ số 2, vì chỉ số dòng bắt đầu từ 0)
                for (int i = 3; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        // Đọc cột 1 (chỉ số 0, vì chỉ số cột bắt đầu từ 0)
                        Cell cell = row.getCell(0);
                        Cell cell1 = row.getCell(1);
                        Cell cell2 = row.getCell(2);
                        if (cell != null&&cell1 != null&&cell2 != null) {
                            String en =cell.getStringCellValue().trim();
                            String vi = cell1.getStringCellValue().trim();
                            String part = cell2.getStringCellValue();
                            var add=true;
                            for(int i1=0;i1< eData.size();i1++){
                                if(en==eData.get(i1)[0]&&vi==eData.get(i1)[1]&&part==eData.get(i1)[2]){
                                    add=false;
                                }
                            }
                            if(add)
                                eData.add(new String[]{en, vi,part});

                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        var database=Test.databaseEnglish();
            int count=0;
        for(var i:eData){
            if(!database.contains(i[0])){
                System.out.println(i[0]);
                count++;
            }
        }
        System.out.println(count);
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
}
