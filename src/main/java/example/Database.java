package example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Database {
    static final String DB_URL = "jdbc:postgresql://localhost:5432/cic2";
    static final String DB_USER = "postgres"; // Thay bằng tên người dùng của bạn
    static final String DB_PASSWORD = Test.PASSWORD; // Thay bằng mật khẩu của bạn
    static final String jsonFilePath = "output json/Topics of Oxford.json"; // Đường dẫn đến file JSON

    public static void main(String[] args) {
//        importExcelToDatabase();
        updatePhonetics();
    }
    public static void updatePhonetics(){
        var ens=Test.databaseEnglish();
        for(var i: ens){
            updatePhonetic(i);
        }
    }
    public static void updatePhonetic(String word){
        String phonetic = Oxford.getPhonetic(word);

        if (!phonetic.startsWith("Error")) {
            boolean success = updatePhonetic(word, phonetic);
            System.out.println("Update successful: " + success);
        } else {
            System.out.println(phonetic);
        }
    }
    public static boolean updatePhonetic(String word, String phonetic) {
        String query = "UPDATE english SET phonetic = ? WHERE word = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, phonetic);
            pstmt.setString(2, word);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            System.out.println("Error updating phonetic: " + e.getMessage());
            return false;
        }
    }

    public static void importExcelToDatabase() {
        String excelFilePath = "All none.xlsx";
        Connection connection = null;
        PreparedStatement psEnglish = null, psVietnamese = null, psVocabularies = null, psInsertTopic = null, psInsertVocabularyTopic = null;

        try {
            // Kết nối với database
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/cic", "postgres", Test.PASSWORD);
            connection.setAutoCommit(false); // Dùng transaction
            HashMap<String, Integer> es = JsonImporter.getEnglish(connection);
            HashMap<Integer, HashMap<String, Integer>> vs = JsonImporter.getVocabularies(connection);
            HashMap<Integer, HashSet<Integer>> vts = JsonImporter.getVocabulariesTopics(connection);
            HashMap<String, Integer> ts = JsonImporter.getTopic(connection);
            // SQL để chèn dữ liệu
            String sqlInsertEnglish = "INSERT INTO english (word, phonetic, audio) VALUES (?, ?, ?)";
            String sqlInsertVietnamese = "INSERT INTO vietnamese (signify) VALUES (?)";
            String sqlInsertVocabularies = "INSERT INTO vocabularies (en, vi, part_of_speech, img, user_own) VALUES (?, ?, ?, ?, ?)";
            String sqlInsertTopic = "INSERT INTO topics ( name,of_user) VALUES (?,?)";
            String sqlInsertVocabularyTopic = "INSERT INTO vocabularies_topics (vocabulary, topic) VALUES (?, ?)";

            // Tạo các PreparedStatement
            psEnglish = connection.prepareStatement(sqlInsertEnglish, Statement.RETURN_GENERATED_KEYS);
            psVietnamese = connection.prepareStatement(sqlInsertVietnamese, Statement.RETURN_GENERATED_KEYS);
            psVocabularies = connection.prepareStatement(sqlInsertVocabularies, Statement.RETURN_GENERATED_KEYS);
            psInsertTopic = connection.prepareStatement(sqlInsertTopic, Statement.RETURN_GENERATED_KEYS);
            psInsertVocabularyTopic = connection.prepareStatement(sqlInsertVocabularyTopic);

            // Đọc dữ liệu từ file Excel
            FileInputStream fis = new FileInputStream(new File(excelFilePath));
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);  // Lấy sheet đầu tiên

            int vietnameseId = 0;
            String meaning = "chưa cập nhật";

// 1. Kiểm tra xem từ đã tồn tại chưa
            PreparedStatement checkStmt = connection.prepareStatement("SELECT id FROM vietnamese WHERE signify = ?");
            checkStmt.setString(1, meaning);
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next()) {
                // Nếu tồn tại, lấy id
                vietnameseId = checkRs.getInt("id");
            } else {
                // Nếu chưa có, thì tạo mới
                PreparedStatement insertStmt = connection.prepareStatement(
                        "INSERT INTO vietnamese(signify) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
                insertStmt.setString(1, meaning);
                insertStmt.executeUpdate();

                ResultSet rsVietnamese = insertStmt.getGeneratedKeys();
                if (rsVietnamese.next()) {
                    vietnameseId = rsVietnamese.getInt(1);
                }

                rsVietnamese.close();
                insertStmt.close();
            }

            // Duyệt qua các dòng trong file Excel
            for (Row row : sheet) {
                String word = row.getCell(2).getStringCellValue(); // Cột word
                String partOfSpeech = row.getCell(4).getStringCellValue(); // Cột từ loại

                if (!es.containsKey(word)) {
                    psEnglish.setString(1, word);
                    psEnglish.setString(2, ""); // phonetic
                    psEnglish.setString(3, ""); // audio
                    psEnglish.executeUpdate();

                    ResultSet rsEnglish = psEnglish.getGeneratedKeys();
                    int englishId = 0;
                    if (rsEnglish.next()) {
                        englishId = rsEnglish.getInt(1); // Lấy ID từ bảng `english`
                    }
                    es.put(word, englishId);
                }

                if (!vs.containsKey(es.get(word))) {
                    vs.put(es.get(word), new HashMap<>());
                }

                if (!vs.get(es.get(word)).containsKey(partOfSpeech)) {
                    try{
                    psVocabularies.setInt(1, es.get(word)); // ID từ bảng `english`
                    psVocabularies.setInt(2, vietnameseId); // ID từ bảng `vietnamese`
                    psVocabularies.setString(3, partOfSpeech); // Từ loại
                    psVocabularies.setString(4, ""); // img
                    psVocabularies.setInt(5, 1); // user
                    psVocabularies.executeUpdate();
                    ResultSet rs = psVocabularies.getGeneratedKeys();
                    int id = 0;
                    if (rs.next()) {
                        id = rs.getInt(1); // Lấy ID từ bảng `english`
                    }
                    vs.get(es.get(word)).put(partOfSpeech, id);}catch (Exception e){}
                }
            }

            // Đọc dữ liệu từ file JSON
            FileReader reader = new FileReader(jsonFilePath);
            Gson gson = new Gson();

            List<Map<String, Object>> topicsList = gson.fromJson(reader, new TypeToken<List<Map<String, Object>>>() {
            }.getType());

            for (Map<String, Object> topicData : topicsList) {
                String topic = (String) topicData.get("name");
                if (!ts.containsKey(topic)) {
                    psInsertTopic.setString(1, topic);
                    psInsertTopic.setInt(2, 1);
                    psInsertTopic.executeUpdate();
                    ResultSet rs = psInsertTopic.getGeneratedKeys();
                    int id = 0;
                    if (rs.next()) {
                        id = rs.getInt(1); // Lấy ID từ bảng `english`
                    }
                    ts.put(topic, id);
                }

                List<List<String>> words = (List<List<String>>) topicData.get("vs");
                for (List<String> wordData : words) {
                    String word = wordData.get(0); // Lấy từ
                    String partOfSpeech = wordData.get(1);
                    try{
                    int vid = vs.get(es.get(word)).get(partOfSpeech);
                    int tid = ts.get(topic).intValue();
//                    System.out.println(topic + "|" + word + "|" + partOfSpeech);
                    if (vts.get(vid) == null) {
                        vts.put(vid, new HashSet<>());
                    }
                    if (!vts.get(vid).contains(tid)) {
                        psInsertVocabularyTopic.setInt(1, vid);
                        psInsertVocabularyTopic.setInt(2, tid);
                        psInsertVocabularyTopic.executeUpdate();
                        vts.get(vid).add(tid);
                    }}catch(Exception e){
                        System.out.println(word+"\t"+partOfSpeech);
                    }

                }
            }

            connection.commit();
            System.out.println("Data imported successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (connection != null) {
                    connection.rollback(); // Rollback nếu có lỗi
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        } finally {
            try {
                if (psEnglish != null) psEnglish.close();
                if (psVietnamese != null) psVietnamese.close();
                if (psVocabularies != null) psVocabularies.close();
                if (psInsertTopic != null) psInsertTopic.close();
                if (psInsertVocabularyTopic != null) psInsertVocabularyTopic.close();
                if (connection != null) connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public static HashMap<String, Integer> countWordInTopics() {
        HashMap<String, Integer> topicCountMap = new HashMap<>();

        // Cấu hình kết nối cơ sở dữ liệu

        String query = "SELECT t.name AS topic_name, COUNT(vt.vocabulary) AS word_count " +
                "FROM topics t " +
                "LEFT JOIN vocabularies_topics vt ON t.id = vt.topic " +
                "GROUP BY t.name";

        try (Connection conn = DriverManager.getConnection(Test.DATABASE_URL, Test.USERNAME, Test.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            // Duyệt kết quả truy vấn và thêm vào HashMap
            while (rs.next()) {
                String topicName = rs.getString("topic_name");
                int wordCount = rs.getInt("word_count");
                topicCountMap.put(topicName, wordCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return topicCountMap;
    }

    public static void showTopics(String[] args) {
        HashMap<String, Integer> result = countWordInTopics();
        System.out.println(result.size());
        result.forEach((topic, count) ->
                System.out.println("Topic: " + topic + ", Word Count: " + count)
        );
    }
}
