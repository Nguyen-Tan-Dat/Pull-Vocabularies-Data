package example;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

public class VocabularyCounter {

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

    public static void main(String[] args) {
        HashMap<String, Integer> result = countWordInTopics();
        System.out.println(result.size());
//
//        // Hiển thị kết quả
//        result.forEach((topic, count) ->
//                System.out.println("Topic: " + topic + ", Word Count: " + count)
//        );
    }
}
