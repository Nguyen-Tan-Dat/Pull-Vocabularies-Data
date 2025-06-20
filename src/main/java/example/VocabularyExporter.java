package example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;

public class VocabularyExporter {
    public static void main(String[] args) {
        String jdbcURL = "jdbc:postgresql://localhost:5432/cic2";
        String username = "postgres";
        String password = "CpqaFVYJ9Mkz6pOj";
        String excelFilePath = "CO_vocabularies1.xlsx";

        String sql = """
            SELECT e.word, e.phonetic, v.part_of_speech, vi.signify
            FROM vocabularies v
            JOIN english e ON v.en = e.id
            JOIN vietnamese vi ON v.vi = vi.id
        """;

        try (
                Connection conn = DriverManager.getConnection(jdbcURL, username, password);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                Workbook workbook = new XSSFWorkbook();
        ) {
            Sheet sheet = workbook.createSheet("Vocabulary");

            // Header
            Row header = sheet.createRow(0);
            String[] columns = {"Từ", "Phát âm", "Từ loại", "Nghĩa"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Data
            int rowNum = 1;
            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rs.getString("word"));
                row.createCell(3).setCellValue(rs.getString("phonetic"));
                row.createCell(2).setCellValue(rs.getString("part_of_speech"));
                row.createCell(1).setCellValue(rs.getString("signify"));
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
                workbook.write(fileOut);
            }

            System.out.println("✅ Exported to " + excelFilePath);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
