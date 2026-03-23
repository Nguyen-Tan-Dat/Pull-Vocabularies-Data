import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

public class OxfordFolderImporter{

    // ===== DB CONFIG =====
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/cic";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "160500";

    // ===== IMPORT CONFIG =====
    private static final long USER_ID = 1L;
    private static final Path ROOT_DIR =
            Paths.get("C:\\Users\\Nguyen Tan Dat\\Documents\\Topics of Oxford to Database");

    private static final boolean SKIP_ALL_XLSX = false;

    // commit theo lô
    private static final int COMMIT_EVERY_N_ROWS = 1000;

    // ===== CACHE =====
    private final Map<String, Long> topicCache = new HashMap<>();
    private final Map<String, Long> englishCache = new HashMap<>();
    private final Map<String, Long> vietnameseCache = new HashMap<>();
    private final Map<String, Long> vocabularyCache = new HashMap<>();

    private long processedSinceCommit = 0;

    public static void main(String[] args) {
        new OxfordFolderImporter().run();
    }

    public void run() {
        long start = System.currentTimeMillis();

        int fileCount = 0;
        int rowCount = 0;
        int importedCount = 0;
        int skippedCount = 0;
        int errorCount = 0;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);

            DbOps db = new DbOps(conn);

            try (Stream<Path> pathStream = Files.walk(ROOT_DIR)) {
                List<Path> xlsxFiles = pathStream
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".xlsx"))
                        .sorted()
                        .toList();

                System.out.println("Tổng file xlsx: " + xlsxFiles.size());

                for (Path file : xlsxFiles) {
                    String fileNameNoExt = removeExtension(file.getFileName().toString());

                    if (SKIP_ALL_XLSX && "all".equalsIgnoreCase(fileNameNoExt)) {
                        System.out.println("Bỏ qua file All: " + file);
                        continue;
                    }

                    fileCount++;

                    String topicPath = buildTopicPath(ROOT_DIR, file);
                    long topicId = getOrCreateTopicCached(db, topicPath, USER_ID);

                    ImportResult result = importOneExcelFile(db, file, topicId, USER_ID);

                    rowCount += result.totalRows;
                    importedCount += result.importedRows;
                    skippedCount += result.skippedRows;
                    errorCount += result.errorRows;

                    conn.commit();
                    processedSinceCommit = 0;

                    System.out.printf(
                            Locale.ROOT,
                            "[%d] %s | rows=%d imported=%d skipped=%d errors=%d%n",
                            fileCount,
                            topicPath,
                            result.totalRows,
                            result.importedRows,
                            result.skippedRows,
                            result.errorRows
                    );
                }
            }

            conn.commit();

            long ms = System.currentTimeMillis() - start;
            System.out.println("\n===== HOÀN TẤT =====");
            System.out.println("File đã xử lý: " + fileCount);
            System.out.println("Tổng row đọc: " + rowCount);
            System.out.println("Imported: " + importedCount);
            System.out.println("Skipped: " + skippedCount);
            System.out.println("Errors: " + errorCount);
            System.out.println("Thời gian: " + ms + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ImportResult importOneExcelFile(DbOps db, Path excelPath, long topicId, long userId) {
        ImportResult result = new ImportResult();

        try (InputStream is = Files.newInputStream(excelPath);
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                result.totalRows++;

                try {
                    ParsedRow data = parseRow(row);

                    if (data == null) {
                        result.skippedRows++;
                        continue;
                    }

                    long englishId = getOrCreateEnglishCached(db, data.word, "", data.link);
                    long vietnameseId = getOrCreateVietnameseCached(db, data.meaning);

                    long vocabularyId = getOrCreateVocabularyCached(
                            db, englishId, vietnameseId, data.partOfSpeech, userId, data.level
                    );

                    db.linkVocabularyToTopic(vocabularyId, topicId);

                    result.importedRows++;
                    processedSinceCommit++;

                    if (processedSinceCommit >= COMMIT_EVERY_N_ROWS) {
                        db.conn.commit();
                        processedSinceCommit = 0;
                    }

                } catch (Exception rowEx) {
                    result.errorRows++;
                    System.err.println("Lỗi row ở file " + excelPath + " | rowIndex=" + rowIndex + " | " + rowEx.getMessage());
                }
            }

        } catch (Exception e) {
            result.errorRows++;
            System.err.println("Lỗi đọc file: " + excelPath);
            e.printStackTrace();
        }

        return result;
    }

    // =========================================================
    // PARSE ROW
    // nếu thiếu nghĩa => "none"
    // chỉ skip nếu thiếu word hoặc part_of_speech
    // =========================================================
    private ParsedRow parseRow(Row row) {
        String word = getCellString(row.getCell(0));
        String partOfSpeech = getCellString(row.getCell(1));
        String levelText = getCellString(row.getCell(2));
        String link = getCellString(row.getCell(3));
        String meaning = getCellString(row.getCell(4));

        if (isBlank(meaning)) {
            meaning = getCellString(row.getCell(5));
        }

        // row rỗng hoàn toàn
        if (isBlank(word) && isBlank(partOfSpeech) && isBlank(levelText) && isBlank(link) && isBlank(meaning)) {
            return null;
        }

        // chỉ cần word + pos
        if (isBlank(word) || isBlank(partOfSpeech)) {
            return null;
        }

        if (isBlank(meaning)) {
            meaning = "none";
        }

        ParsedRow data = new ParsedRow();
        data.word = normalizeText(word);
        data.partOfSpeech = normalizePartOfSpeech(partOfSpeech);
        data.level = normalizeLevel(levelText);
        data.link = normalizeText(link);
        data.meaning = normalizeMeaning(meaning);

        if (isBlank(data.meaning)) {
            data.meaning = "none";
        }

        return data;
    }

    // =========================================================
    // CACHE OPS
    // =========================================================
    private long getOrCreateTopicCached(DbOps db, String topicName, long userId) throws SQLException {
        String key = userId + "|" + topicName.toLowerCase(Locale.ROOT);
        Long cached = topicCache.get(key);
        if (cached != null) return cached;

        Long found = db.findTopic(topicName, userId);
        if (found != null) {
            topicCache.put(key, found);
            return found;
        }

        long id = db.insertTopic(topicName, userId);
        topicCache.put(key, id);
        return id;
    }

    private long getOrCreateEnglishCached(DbOps db, String word, String phonetic, String audio) throws SQLException {
        String key = normalizeKey(word) + "|" + normalizeKey(phonetic) + "|" + normalizeKey(audio);
        Long cached = englishCache.get(key);
        if (cached != null) return cached;

        Long found = db.findEnglish(word, phonetic, audio);
        if (found != null) {
            englishCache.put(key, found);
            return found;
        }

        long id = db.insertEnglish(word, phonetic, audio);
        englishCache.put(key, id);
        return id;
    }

    private long getOrCreateVietnameseCached(DbOps db, String meaning) throws SQLException {
        String safeMeaning = isBlank(meaning) ? "none" : meaning;
        String key = normalizeKey(safeMeaning);

        Long cached = vietnameseCache.get(key);
        if (cached != null) return cached;

        Long found = db.findVietnamese(safeMeaning);
        if (found != null) {
            vietnameseCache.put(key, found);
            return found;
        }

        long id = db.insertVietnamese(safeMeaning);
        vietnameseCache.put(key, id);
        return id;
    }

    private long getOrCreateVocabularyCached(
            DbOps db,
            long enId,
            long viId,
            String partOfSpeech,
            long userId,
            short level
    ) throws SQLException {
        String key = userId + "|" + enId + "|" + viId + "|" + normalizeKey(partOfSpeech);

        Long cached = vocabularyCache.get(key);
        if (cached != null) return cached;

        Long found = db.findVocabulary(enId, viId, partOfSpeech, userId);
        if (found != null) {
            vocabularyCache.put(key, found);
            return found;
        }

        long id = db.insertVocabulary(enId, viId, partOfSpeech, userId, level);
        vocabularyCache.put(key, id);
        return id;
    }

    private static String normalizeKey(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    // =========================================================
    // TOPIC PATH
    // =========================================================
    private static String buildTopicPath(Path rootDir, Path file) {
        Path rel = rootDir.relativize(file);
        List<String> parts = new ArrayList<>();

        for (int i = 0; i < rel.getNameCount(); i++) {
            String part = rel.getName(i).toString();
            if (i == rel.getNameCount() - 1) {
                part = removeExtension(part);
            }
            parts.add(part.trim());
        }

        return String.join(" > ", parts);
    }

    private static String removeExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(0, idx) : filename;
    }

    // =========================================================
    // NORMALIZE
    // =========================================================
    private static String normalizeText(String s) {
        if (s == null) return "";
        return s.trim().replace("\u00A0", " ");
    }

    private static String normalizeMeaning(String s) {
        if (s == null) return "none";
        String x = normalizeText(s);
        x = x.replaceAll("\\s+", " ").trim();
        x = x.replaceAll("[;,/\\s]+$", "").trim();
        return x.isEmpty() ? "none" : x;
    }

    private static String normalizePartOfSpeech(String s) {
        if (s == null) return null;
        String x = s.trim().toLowerCase(Locale.ROOT);

        return switch (x) {
            case "n", "noun" -> "noun";
            case "v", "verb" -> "verb";
            case "adj", "adjective" -> "adjective";
            case "adv", "adverb" -> "adverb";
            case "pron", "pronoun" -> "pronoun";
            case "prep", "preposition" -> "preposition";
            case "conj", "conjunction" -> "conjunction";
            case "det", "determiner" -> "determiner";
            case "exclamation", "interjection", "int" -> "interjection";
            default -> x;
        };
    }

    private static short normalizeLevel(String levelText) {
        if (levelText == null) return 0;
        String s = levelText.trim().toLowerCase(Locale.ROOT);

        return switch (s) {
            case "a1" -> 1;
            case "a2" -> 2;
            case "b1" -> 3;
            case "b2" -> 4;
            case "c1" -> 5;
            case "c2" -> 6;
            default -> 0;
        };
    }

    // =========================================================
    // EXCEL HELPERS
    // =========================================================
    private static String getCellString(Cell cell) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                if (v == (long) v) yield String.valueOf((long) v);
                yield String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (Exception e) {
                    try {
                        double v = cell.getNumericCellValue();
                        if (v == (long) v) yield String.valueOf((long) v);
                        yield String.valueOf(v);
                    } catch (Exception ex) {
                        yield "";
                    }
                }
            }
            case BLANK -> "";
            default -> "";
        };
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String emptyToNull(String s) {
        return isBlank(s) ? null : s.trim();
    }

    // =========================================================
    // DTO
    // =========================================================
    private static class ParsedRow {
        String word;
        String partOfSpeech;
        short level;
        String link;
        String meaning;
    }

    private static class ImportResult {
        int totalRows = 0;
        int importedRows = 0;
        int skippedRows = 0;
        int errorRows = 0;
    }

    // =========================================================
    // DB OPS
    // =========================================================
    private static class DbOps {
        private final Connection conn;

        private final PreparedStatement psFindTopic;
        private final PreparedStatement psInsertTopic;

        private final PreparedStatement psFindEnglish;
        private final PreparedStatement psInsertEnglish;

        private final PreparedStatement psFindVietnamese;
        private final PreparedStatement psInsertVietnamese;

        private final PreparedStatement psFindVocabulary;
        private final PreparedStatement psInsertVocabulary;

        private final PreparedStatement psLinkVocabularyTopic;

        DbOps(Connection conn) throws SQLException {
            this.conn = conn;

            psFindTopic = conn.prepareStatement("""
                    SELECT id
                    FROM topics
                    WHERE name = ? AND of_user = ?
                    LIMIT 1
                    """);

            psInsertTopic = conn.prepareStatement("""
                    INSERT INTO topics (name, of_user)
                    VALUES (?, ?)
                    RETURNING id
                    """);

            psFindEnglish = conn.prepareStatement("""
                    SELECT id
                    FROM english
                    WHERE lower(word) = lower(?)
                      AND coalesce(phonetic, '') = coalesce(?, '')
                      AND coalesce(audio, '') = coalesce(?, '')
                    LIMIT 1
                    """);

            psInsertEnglish = conn.prepareStatement("""
                    INSERT INTO english (word, phonetic, audio)
                    VALUES (?, ?, ?)
                    RETURNING id
                    """);

            psFindVietnamese = conn.prepareStatement("""
                    SELECT id
                    FROM vietnamese
                    WHERE lower(signify) = lower(?)
                    LIMIT 1
                    """);

            psInsertVietnamese = conn.prepareStatement("""
                    INSERT INTO vietnamese (signify)
                    VALUES (?)
                    RETURNING id
                    """);

            psFindVocabulary = conn.prepareStatement("""
                    SELECT id
                    FROM vocabularies
                    WHERE en = ?
                      AND vi = ?
                      AND user_own = ?
                      AND (
                            (? IS NULL AND part_of_speech IS NULL)
                            OR lower(part_of_speech) = lower(?)
                      )
                    LIMIT 1
                    """);

            psInsertVocabulary = conn.prepareStatement("""
                    INSERT INTO vocabularies (
                        en,
                        part_of_speech,
                        img,
                        vi,
                        user_own,
                        level,
                        create_date
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    RETURNING id
                    """);

            psLinkVocabularyTopic = conn.prepareStatement("""
                    INSERT INTO vocabularies_topics (topic, vocabulary)
                    VALUES (?, ?)
                    ON CONFLICT (vocabulary, topic) DO NOTHING
                    """);
        }

        Long findTopic(String topicName, long userId) throws SQLException {
            psFindTopic.setString(1, topicName);
            psFindTopic.setLong(2, userId);
            try (ResultSet rs = psFindTopic.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }

        long insertTopic(String topicName, long userId) throws SQLException {
            psInsertTopic.setString(1, topicName);
            psInsertTopic.setLong(2, userId);
            try (ResultSet rs = psInsertTopic.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }

        Long findEnglish(String word, String phonetic, String audio) throws SQLException {
            psFindEnglish.setString(1, word);
            psFindEnglish.setString(2, phonetic == null ? "" : phonetic);
            psFindEnglish.setString(3, audio == null ? "" : audio);
            try (ResultSet rs = psFindEnglish.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }

        long insertEnglish(String word, String phonetic, String audio) throws SQLException {
            psInsertEnglish.setString(1, word);
            psInsertEnglish.setString(2, phonetic == null ? "" : phonetic);
            psInsertEnglish.setString(3, audio == null ? "" : audio);
            try (ResultSet rs = psInsertEnglish.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }

        Long findVietnamese(String meaning) throws SQLException {
            psFindVietnamese.setString(1, meaning);
            try (ResultSet rs = psFindVietnamese.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }

        long insertVietnamese(String meaning) throws SQLException {
            psInsertVietnamese.setString(1, meaning);
            try (ResultSet rs = psInsertVietnamese.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }

        Long findVocabulary(long enId, long viId, String partOfSpeech, long userId) throws SQLException {
            psFindVocabulary.setLong(1, enId);
            psFindVocabulary.setLong(2, viId);
            psFindVocabulary.setLong(3, userId);
            psFindVocabulary.setString(4, partOfSpeech);
            psFindVocabulary.setString(5, partOfSpeech);

            try (ResultSet rs = psFindVocabulary.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }

        long insertVocabulary(long enId, long viId, String partOfSpeech, long userId, short level) throws SQLException {
            psInsertVocabulary.setLong(1, enId);
            psInsertVocabulary.setString(2, emptyToNull(partOfSpeech));
            psInsertVocabulary.setString(3, "");
            psInsertVocabulary.setLong(4, viId);
            psInsertVocabulary.setLong(5, userId);
            psInsertVocabulary.setShort(6, level);
            psInsertVocabulary.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            try (ResultSet rs = psInsertVocabulary.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }

        void linkVocabularyToTopic(long vocabularyId, long topicId) throws SQLException {
            psLinkVocabularyTopic.setLong(1, topicId);
            psLinkVocabularyTopic.setLong(2, vocabularyId);
            psLinkVocabularyTopic.executeUpdate();
        }
    }
}