package example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.sql.*;
import java.util.*;

public class VocabulariesImporter {
    static final String dbUrl = "jdbc:mysql://localhost:3306/cic";
    static final String dbUser = "root"; // Thay bằng tên người dùng của bạn
    static final String dbPassword = Test.PASSWORD; // Thay bằng mật khẩu của bạn
    static final String jsonFilePath = "output json/Topics of Oxford.json"; // Đường dẫn đến file JSON

    public static void main(String[] args) {
        String excelFilePath = "All none.xlsx";  // Đường dẫn đến file Excel
        importExcelToDatabase(excelFilePath);
    }

    public static void importExcelToDatabase(String excelFilePath) {
        Connection connection = null;
        PreparedStatement psEnglish = null, psVietnamese = null, psVocabularies = null, psInsertTopic = null, psInsertVocabularyTopic = null;

        try {
            // Kết nối với database
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/cic", "postgres", Test.PASSWORD);
            connection.setAutoCommit(false); // Dùng transaction
            HashMap<String, Integer> es = JsonImporter.getEnglish(connection);
            HashMap<Integer, HashMap<String, Integer>> vs = JsonImporter.getVocabularies(connection);
//            HashMap<String, Integer> es = new HashMap<>();
//            HashMap<Integer, HashMap<String, Integer>> vs =new HashMap<>();
            HashMap<Integer, HashSet<Integer>> vts = new HashMap<>();
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

            // Chèn vào bảng `vietnamese`
            psVietnamese.setString(1, "chưa cập nhật");
            psVietnamese.executeUpdate();
            ResultSet rsVietnamese = psVietnamese.getGeneratedKeys();
            int vietnameseId = 0;
            if (rsVietnamese.next()) {
                vietnameseId = rsVietnamese.getInt(1);  // Lấy id vừa tạo
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
                    vs.get(es.get(word)).put(partOfSpeech, id);
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
                    int vid = vs.get(es.get(word)).get(partOfSpeech);
                    int tid = ts.get(topic).intValue();
                    System.out.println(topic + "|" + word+ "|" + partOfSpeech);
                    if (vts.get(vid) == null) {
                        vts.put(vid, new HashSet<>());
                    }
                    if (!vts.get(vid).contains(tid)) {
                        psInsertVocabularyTopic.setInt(1, vid);
                        psInsertVocabularyTopic.setInt(2, tid);
                            psInsertVocabularyTopic.executeUpdate();
                        vts.get(vid).add(tid);
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
}

