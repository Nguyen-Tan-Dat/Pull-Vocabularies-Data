import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

public class ExcelMerger {

    public static void main(String[] args) {
        String inputFolder = "C:\\Users\\Dat\\OneDrive\\Documents\\English\\IELTS\\Cambridge Vocabulary for IELTS\\Data"; // Thư mục chứa các file Excel
        String outputFile = "MergedData.xlsx"; // File Excel đầu ra

        try (XSSFWorkbook outputWorkbook = new XSSFWorkbook()) {
            XSSFSheet outputSheet = outputWorkbook.createSheet("Merged");

            // Tạo hàng tiêu đề
            Row headerRow = outputSheet.createRow(0);
            String[] headers = {"Từ", "Nghĩa", "Từ loại"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIndex = 1;

            List<Path> excelFiles = Files.list(Paths.get(inputFolder))
                    .filter(p -> p.toString().endsWith(".xlsx"))
                    .collect(Collectors.toList());

            for (Path filePath : excelFiles) {
                try (FileInputStream fis = new FileInputStream(filePath.toFile());
                     XSSFWorkbook inputWorkbook = new XSSFWorkbook(fis)) {

                    XSSFSheet sheet = inputWorkbook.getSheetAt(0);

                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;

                        Row newRow = outputSheet.createRow(rowIndex++);
                        for (int j = 2; j <= 4; j++) { // Bỏ cột đầu (trống), lấy cột 1-3
                            Cell oldCell = row.getCell(j);
                            Cell newCell = newRow.createCell(j - 1);
                            if (oldCell != null) {
                                oldCell.setCellType(CellType.STRING);
                                newCell.setCellValue(oldCell.getStringCellValue().trim());
                            }
                        }
                    }
                }
            }

            // Ghi dữ liệu ra file
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                outputWorkbook.write(fos);
            }

            System.out.println("Đã gộp xong " + excelFiles.size() + " file vào " + outputFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
