package example;

import java.io.*;
import java.nio.file.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ReadCambridgeTopics {
    public static void main(String[] args) {
//        HashSet<String> list = new HashSet<>();
        HashMap<String, HashSet<String>> plist = new HashMap<>();
        for (int number = 1; number <= 4; number++) {

            // Đường dẫn tới thư mục chứa các tệp PDF
            String directoryPath = "Cambridge Vocabularies/" + number;

            // Tạo đối tượng Path cho thư mục
            Path path = Paths.get(directoryPath);

            // Sử dụng DirectoryStream để duyệt qua các tệp trong thư mục
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.xlsx")) {
                int count = 0;
                for (Path filePath : stream) {
                    try (FileInputStream fis = new FileInputStream(filePath.toString());
                         Workbook workbook = new XSSFWorkbook(fis)) {

                        // Giả sử dữ liệu nằm trên sheet đầu tiên
                        Sheet sheet = workbook.getSheetAt(0);

                        // Bắt đầu đọc từ dòng thứ 3 (chỉ số 2, vì chỉ số dòng bắt đầu từ 0)
                        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                            Row row = sheet.getRow(i);
                            if (row != null) {
                                // Đọc cột 1 (chỉ số 0, vì chỉ số cột bắt đầu từ 0)
                                Cell cell = row.getCell(0);
                                Cell cell1 = row.getCell(1);
//                                if (cell != null) {
                                    // Lấy giá trị của ô
                                    String cellValue = getCellValueAsString(cell);
                                    String cellValue1 = getCellValueAsString(cell1);
                                    cellValue = cellValue.trim();
                                    cellValue1 = cellValue1.trim();
//                                    list.add(cellValue);
                                    if (!plist.containsKey(cellValue)) {
                                        plist.put(cellValue, new HashSet<>());
//                                        plist.get(cellValue).add("");
                                    }
                                    plist.get(cellValue).add(cellValue1.toLowerCase());

//                                }
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    count++;
                }
            } catch (IOException | DirectoryIteratorException e) {
                System.err.println("Lỗi khi đọc thư mục: " + e.getMessage());
            }
        }
        ArrayList<String[]> eData = new ArrayList<>();
        for (int number = 1; number <= 4; number++) {
            String excelFilePath = "cambridge-vocabularies-" + number + ".xlsx";
            try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
                 Workbook workbook = new XSSFWorkbook(fis)) {

                // Giả sử dữ liệu nằm trên sheet đầu tiên
                Sheet sheet = workbook.getSheetAt(0);

                // Bắt đầu đọc từ dòng thứ 3 (chỉ số 2, vì chỉ số dòng bắt đầu từ 0)
                for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        // Đọc cột 1 (chỉ số 0, vì chỉ số cột bắt đầu từ 0)
                        Cell cell = row.getCell(2);
                        Cell cell1 = row.getCell(3);
                        Cell cell2 = row.getCell(4);
                        Cell cell3 = row.getCell(5);
                        if (cell != null) {
                            String en = getCellValueAsString(cell);
                            String vi = getCellValueAsString(cell1);
                            String part = getCellValueAsString(cell2);
                            String phonetic = getCellValueAsString(cell3);
                            var add=true;
                            for(int i1=0;i1< eData.size();i1++){
                                if(en==eData.get(i1)[0]&&vi==eData.get(i1)[1]&&part==eData.get(i1)[2]){
                                    add=false;
                                }
                            }
                            if(add)
                            eData.add(new String[]{en, vi,part,phonetic});

                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int count = 0;
        for (var i : plist.keySet()) {
                count+=plist.get(i).size();
        }
        System.out.println(plist.size());
//            System.out.println(count);
        count = 0;
        System.out.println(eData.size());
        HashSet<String> parts=new HashSet<>();
        var database=Test.databaseEnglish();
        for (int i=0;i< eData.size();i++ ) {
            var en=eData.get(i)[0];
            var part=eData.get(i)[2];
            if (plist.containsKey(en)){
                var ad=true;
                for (var j:plist.get(en)){
                    if(j.contains(part)||part.contains(j)){
                        ad=false;
                        break;
                    }
                }
                if(ad)
                eData.remove(i);
            }
//                System.out.println(i[0]+"\t"+i[1]);
        }
        System.out.println(eData.size());
        for(var i:eData){
            parts.add(i[0]);
        }
        System.out.println(parts.size());
        count=0;
        for(var i:database){
            if(!parts.contains(i)){
                count++;
//                System.out.println(i);
            }
        }
        System.out.println(count);
        String excelFilePath = "Vocabularies in use.xlsx";

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {

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
        }
    }

private static String getCellValueAsString(Cell cell) {
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
