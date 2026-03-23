import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class TopicImporter {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/cic";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "160500";
    public static HashSet<String> databaseEnglish() {
        HashSet<String> data = new HashSet<>();
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            if (connection != null) {
                String sqlQuery = "SELECT word FROM english";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);
                while (resultSet.next()) {
                    String name = resultSet.getString("word");
                    data.add(name);
                }
                resultSet.close();
                statement.close();
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Không thể kết nối đến cơ sở dữ liệu!");
            e.printStackTrace();
        }
        return data;
    }
    public static HashSet<String> databaseEnglishWhere(String where) {
        HashSet<String> data = new HashSet<>();
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            if (connection != null) {
                String sqlQuery = "SELECT word FROM english Where "+where;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);
                while (resultSet.next()) {
                    String name = resultSet.getString("word");
                    data.add(name);
                }
                resultSet.close();
                statement.close();
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Không thể kết nối đến cơ sở dữ liệu!");
            e.printStackTrace();
        }
        return data;
    }

    // Cache dữ liệu để tránh truy vấn lặp lại
    private static final Map<String, Integer> topicCache = new HashMap<>();
    private static final Map<String, Set<Long>> vocabularyCache = new HashMap<>();
    private static final Map<Integer, Set<Long>> vocabTopicCache = new HashMap<>();

    private static final int BATCH_LIMIT = 1000;

    public static void main(String[] args) {
        HashSet<String> filePaths = new HashSet<>();
//        filePaths.add("output json/all.json");
//        filePaths.add("output json/Oxford topics.json");
        filePaths.add("output json/Oxford word list.json");
        filePaths.add("output json/Oxford topics.json");
//        filePaths.add("output json/Vocabulary in use Elementary.json");
//        filePaths.add("output json/Vocabulary in use Upper-intermediate.json");
//        filePaths.add("output json/Vocabulary in use Pre-Intermediate.json");
//        filePaths.add("output json/Vocabulary in use Advanced.json");
//        filePaths.add("output json/IELTS Vocabulary in Use.json");
//        filePaths.add("output json/Academic Vocabulary in Use.json");
        int userId = 1;
        for (var filePath : filePaths) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                conn.setAutoCommit(false); // Giao dịch tăng hiệu năng
                // GỌI Ở ĐÂY: Xóa dữ liệu cũ của user trước khi nạp đống file mới
//                clearTopics(conn, userId);
                preloadTopics(conn, userId);
                preloadVocabularies(conn);
                preloadVocabulariesTopics(conn);
                importTopics(conn, filePath, userId);
                conn.commit(); // Xác nhận thay đổi
                System.out.println("✅ Nhập dữ liệu hoàn tất." + filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static void clearTopics(Connection conn, int userId) throws SQLException {
        System.out.println("Wait... Đang dọn dẹp dữ liệu cũ cho user: " + userId);

        // 1. Xóa các liên kết trong bảng vocabularies_topics của các topic thuộc về user này
        String deleteLinksSql = """
        DELETE FROM vocabularies_topics 
        WHERE topic IN (SELECT id FROM topics WHERE of_user = ?)
    """;

        // 2. Xóa các topic của user này
        String deleteTopicsSql = "DELETE FROM topics WHERE of_user = ?";

        try (PreparedStatement st1 = conn.prepareStatement(deleteLinksSql);
             PreparedStatement st2 = conn.prepareStatement(deleteTopicsSql)) {

            st1.setInt(1, userId);
            int linksDeleted = st1.executeUpdate();

            st2.setInt(1, userId);
            int topicsDeleted = st2.executeUpdate();

            System.out.println("✅ Đã xóa " + linksDeleted + " liên kết từ vựng và " + topicsDeleted + " topics cũ.");

            // Quan trọng: Phải clear cache sau khi xóa DB để tránh cache cũ làm sai lệch logic insert
            topicCache.clear();
            vocabTopicCache.clear();
        }
    }

    private static void preloadTopics(Connection conn, int userId) throws SQLException {
        String sql = "SELECT id, name FROM topics WHERE of_user = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                topicCache.put(rs.getString("name"), rs.getInt("id"));
            }
        }
    }

    private static void preloadVocabularies(Connection conn) throws SQLException {
        String sql = """
                    SELECT v.id, e.word, v.part_of_speech
                    FROM vocabularies v
                    JOIN english e ON v.en = e.id
                    JOIN vietnamese vi ON v.vi = vi.id
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String key = rs.getString("word") + "|" + rs.getString("part_of_speech");
                vocabularyCache.computeIfAbsent(key, k -> new HashSet<>()).add(rs.getLong("id"));
            }
        }
    }

    private static void preloadVocabulariesTopics(Connection conn) throws SQLException {
        String sql = "SELECT topic, vocabulary FROM vocabularies_topics";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int topicId = rs.getInt("topic");
                long vocabId = rs.getLong("vocabulary");
                vocabTopicCache.computeIfAbsent(topicId, k -> new HashSet<>()).add(vocabId);
            }
        }
    }

    private static void importTopics(Connection conn, String filePath, int userId) throws IOException, SQLException {
        String json = Files.readString(Paths.get(filePath));
        JSONArray data = new JSONArray(json);

        int count = 0;
        int batchSize = 0;

        try (PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO vocabularies_topics (topic, vocabulary) VALUES (?, ?)")) {

            for (int i = 0; i < data.length(); i++) {
                JSONObject topicJson = data.getJSONObject(i);
                String topicName = topicJson.getString("name");
                int topicId = getOrCreateTopic(conn, topicName, userId);
                JSONArray vocabArray = topicJson.getJSONArray("vs");
                for (int j = 0; j < vocabArray.length(); j++) {
                    if (vocabArray.get(j) instanceof JSONArray) {
                        JSONArray voc = vocabArray.getJSONArray(j);
                        String word = voc.getString(0);
                        String pos = voc.getString(1);
                        if (pos != null && !pos.equals("")) {
                            String key = word + "|" + pos;
                            Set<Long> vocabIds = vocabularyCache.getOrDefault(key, Set.of());
                            for (long vocabId : vocabIds) {
                                if (!vocabTopicCache.getOrDefault(topicId, Set.of()).contains(vocabId)) {
                                    insertStmt.setInt(1, topicId);
                                    insertStmt.setLong(2, vocabId);
                                    insertStmt.addBatch();
                                    vocabTopicCache.computeIfAbsent(topicId, k -> new HashSet<>()).add(vocabId);

                                    batchSize++;
                                    if (batchSize >= BATCH_LIMIT) {
                                        insertStmt.executeBatch();
                                        batchSize = 0;
                                    }
                                }
                            }
                        } else {
                            for (String key : vocabularyCache.keySet()) {
                                if (key.startsWith(word + "|")) {
                                    Set<Long> vocabIds = vocabularyCache.getOrDefault(key, Set.of());
                                    for (long vocabId : vocabIds) {
                                        if (!vocabTopicCache.getOrDefault(topicId, Set.of()).contains(vocabId)) {
                                            insertStmt.setInt(1, topicId);
                                            insertStmt.setLong(2, vocabId);
                                            insertStmt.addBatch();
                                            vocabTopicCache.computeIfAbsent(topicId, k -> new HashSet<>()).add(vocabId);

                                            batchSize++;
                                            if (batchSize >= BATCH_LIMIT) {
                                                insertStmt.executeBatch();
                                                batchSize = 0;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (vocabArray.get(j) instanceof String word) {
                        word = word.trim();
                        for (String key : vocabularyCache.keySet()) {
                            if (key.startsWith((word + "|"))) {
                                Set<Long> vocabIds = vocabularyCache.getOrDefault(key, Set.of());
                                for (long vocabId : vocabIds) {
                                    if (!vocabTopicCache.getOrDefault(topicId, Set.of()).contains(vocabId)) {
                                        insertStmt.setInt(1, topicId);
                                        insertStmt.setLong(2, vocabId);
                                        insertStmt.addBatch();
                                        vocabTopicCache.computeIfAbsent(topicId, k -> new HashSet<>()).add(vocabId);

                                        batchSize++;
                                        if (batchSize >= BATCH_LIMIT) {
                                            insertStmt.executeBatch();
                                            batchSize = 0;
                                        }
//                                        return;
                                    }
                                }
                            }
                        }
                    }
                }

                count++;
            }

            insertStmt.executeBatch(); // Execute phần batch còn lại
        }

        System.out.println("✅ Đã xử lý " + count + " topic.");
    }

    private static int getOrCreateTopic(Connection conn, String name, int userId) throws SQLException {
        if (topicCache.containsKey(name)) return topicCache.get(name);

        String insert = "INSERT INTO topics (name, of_user) VALUES (?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(insert)) {
            stmt.setString(1, name);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                topicCache.put(name, id);
                return id;
            }
        }

        throw new SQLException("Không thể thêm topic: " + name);
    }
}
