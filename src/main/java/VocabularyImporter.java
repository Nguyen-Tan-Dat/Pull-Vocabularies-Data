import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class VocabularyImporter {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/cic";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "CpqaFVYJ9Mkz6pOj";

    // Cache
    private static final Map<String, Long> englishCache = new HashMap<>();
    private static final Map<String, Long> vietnameseCache = new HashMap<>();
    private static final Map<String, Boolean> vocabularyCache = new HashMap<>();

    public static void main(String[] args) {
//        String excelPath = "vocabulary_none.xlsx";
        String excelPath = "input.xlsx";
        int userId = 1;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false); // Gộp transaction
            preloadCaches(conn);
            importFromExcel(conn, excelPath, userId);
            conn.commit(); // Chỉ commit 1 lần
            System.out.println("✅ Import thành công.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void preloadCaches(Connection conn) throws SQLException {
        // Cache English
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id, word FROM english")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                englishCache.put(rs.getString("word").trim(), rs.getLong("id"));
            }
        }

        // Cache Vietnamese
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id, signify FROM vietnamese")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                vietnameseCache.put(rs.getString("signify").trim(), rs.getLong("id"));
            }
        }

        // Cache Vocabularies
        try (PreparedStatement stmt = conn.prepareStatement("SELECT en, vi, part_of_speech FROM vocabularies")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String key = rs.getLong("en") + "|" + rs.getLong("vi") + "|" + rs.getString("part_of_speech").trim();
                vocabularyCache.put(key, true);
            }
        }
    }

    private static void importFromExcel(Connection conn, String filePath, int userId) throws IOException, SQLException {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis);
             PreparedStatement insertEn = conn.prepareStatement("INSERT INTO english (word, phonetic, audio) VALUES (?, ?, '') RETURNING id");
             PreparedStatement insertVi = conn.prepareStatement("INSERT INTO vietnamese (signify) VALUES (?) RETURNING id");
             PreparedStatement insertVocab = conn.prepareStatement("INSERT INTO vocabularies (en, vi, part_of_speech, img, user_own) VALUES (?, ?, ?, '', ?)")) {

            Sheet sheet = workbook.getSheetAt(0);
            int count = 0;

            for (Row row : sheet) {
//                if (row.getRowNum() == 0) continue; // Nếu có tiêu đề

                String word = getCellValue(row.getCell(2));
                String signify = getCellValue(row.getCell(3));
                String partOfSpeech = getCellValue(row.getCell(4));
                String phonetic = getCellValue(row.getCell(5));

                if (word.isBlank() || signify.isBlank()) continue;

                long enId = getOrInsertEnglish(conn, insertEn, word, phonetic);
                long viId = getOrInsertVietnamese(conn, insertVi, signify);
                String vocabKey = enId + "|" + viId + "|" + partOfSpeech;

                if (!vocabularyCache.containsKey(vocabKey)) {
                    insertVocab.setLong(1, enId);
                    insertVocab.setLong(2, viId);
                    insertVocab.setString(3, partOfSpeech);
                    insertVocab.setInt(4, userId);
                    insertVocab.executeUpdate();
                    vocabularyCache.put(vocabKey, true);
                    count++;
                } else {
                    System.out.println("⚠️ Đã tồn tại: " + word + " - " + signify + " (" + partOfSpeech + ")");
                }
            }

            System.out.println("✅ Đã thêm " + count + " từ vựng mới.");
        }
    }

    private static long getOrInsertEnglish(Connection conn, PreparedStatement insertStmt, String word, String phonetic) throws SQLException {
        if (englishCache.containsKey(word)) return englishCache.get(word);

        insertStmt.setString(1, word);
        insertStmt.setString(2, phonetic);
        ResultSet rs = insertStmt.executeQuery();
        rs.next();
        long id = rs.getLong(1);
        englishCache.put(word, id);
        return id;
    }

    private static long getOrInsertVietnamese(Connection conn, PreparedStatement insertStmt, String signify) throws SQLException {
        if (vietnameseCache.containsKey(signify)) return vietnameseCache.get(signify);

        insertStmt.setString(1, signify);
        ResultSet rs = insertStmt.executeQuery();
        rs.next();
        long id = rs.getLong(1);
        vietnameseCache.put(signify, id);
        return id;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
