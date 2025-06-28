import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;

public class VocabularyImporter {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/cic1";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "CpqaFVYJ9Mkz6pOj";

    public static void main(String[] args) {
        String excelPath = "input.xlsx";
        int userId = 1; // ID người dùng sở hữu từ vựng
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            importFromExcel(conn, excelPath, userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void importFromExcel(Connection conn, String filePath, int userId) throws IOException, SQLException {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int count = 0;

            for (Row row : sheet) {
//                if (row.getRowNum() == 0) continue; // bỏ qua header

                String word = getCellValue(row.getCell(2));
                String signify = getCellValue(row.getCell(3));
                String partOfSpeech = getCellValue(row.getCell(4));
                String phonetic = getCellValue(row.getCell(5));

                if (word.isEmpty() || signify.isEmpty()) continue;

                long enId = insertEnglish(conn, word, phonetic);
                long viId = insertVietnamese(conn, signify);

                if (!vocabularyExists(conn, enId, viId, partOfSpeech)) {
                    insertVocabulary(conn, enId, viId, partOfSpeech, "", userId);
                    count++;
                } else {
                    System.out.println("Đã tồn tại: " + word + " - " + signify + " (" + partOfSpeech + ")");
                }
            }

            System.out.println("Đã thêm " + count + " từ vựng mới.");
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            default -> "";
        };
    }

    private static long insertEnglish(Connection conn, String word, String phonetic) throws SQLException {
        String select = "SELECT id,word FROM english WHERE word = ?";
        try (PreparedStatement stmt = conn.prepareStatement(select)) {
            stmt.setString(1, word);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                return rs.getLong("id");
            }
        }

        String insert = "INSERT INTO english (word, phonetic, audio) VALUES (?, ?, '') RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(insert)) {
            stmt.setString(1, word);
            stmt.setString(2, phonetic);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getLong("id");
        }
    }

    private static long insertVietnamese(Connection conn, String signify) throws SQLException {
        String select = "SELECT id FROM vietnamese WHERE signify = ?";
        try (PreparedStatement stmt = conn.prepareStatement(select)) {
            stmt.setString(1, signify);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong("id");
        }

        String insert = "INSERT INTO vietnamese (signify) VALUES (?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(insert)) {
            stmt.setString(1, signify);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getLong("id");
        }
    }

    private static boolean vocabularyExists(Connection conn, long enId, long viId, String partOfSpeech) throws SQLException {
        String sql = "SELECT 1 FROM vocabularies WHERE en = ? AND vi = ? AND part_of_speech = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, enId);
            stmt.setLong(2, viId);
            stmt.setString(3, partOfSpeech);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    private static void insertVocabulary(Connection conn, long enId, long viId, String partOfSpeech, String img, int userId) throws SQLException {
        String sql = "INSERT INTO vocabularies (en, vi, part_of_speech, img, user_own) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, enId);
            stmt.setLong(2, viId);
            stmt.setString(3, partOfSpeech);
            stmt.setString(4, img);
            stmt.setInt(5, userId);
            stmt.executeUpdate();
        }catch (Exception e){
            System.out.println(enId+"\t"+viId+"\t"+partOfSpeech+"\t");
        }
    }
}
