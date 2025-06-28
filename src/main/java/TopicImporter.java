import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;

public class TopicImporter {

    // Thông tin kết nối cơ sở dữ liệu
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/cic1";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "CpqaFVYJ9Mkz6pOj";

    public static void main(String[] args) {
        // Đọc và nhập dữ liệu từ file JSON
        String filePath = "output json/Topics of Oxford.json"; // Đường dẫn đến file JSON của bạn
        int userId = 1; // userId của người dùng đang nhập dữ liệu (có thể thay bằng ID thực tế)

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            importTopic(conn, filePath, userId);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void importTopic(Connection conn, String filePath, int userId) throws IOException, SQLException {
        FileReader fileReader = new FileReader(new File(filePath));
        StringBuilder jsonContent = new StringBuilder();

        int c;
        while ((c = fileReader.read()) != -1) {
            jsonContent.append((char) c);
        }

        JSONArray data = new JSONArray(jsonContent.toString());

        int count = 0;

        for (int i = 0; i < data.length(); i++) {
            JSONObject topicJson = data.getJSONObject(i);
            String topicName = topicJson.getString("name");
            System.out.println(topicName);
            // Kiểm tra và thêm topic vào database nếu chưa tồn tại
            int topicId = addTopicIfNotExists(conn, topicName, userId);

            JSONArray vocabularies = topicJson.getJSONArray("vs");

            for (int j = 0; j < vocabularies.length(); j++) {
                Object vocabulary = vocabularies.get(j);

                if (vocabulary instanceof JSONArray) {
                    JSONArray vocabularyData = (JSONArray) vocabulary;
                    String word = vocabularyData.getString(0);
                    String partOfSpeech = vocabularyData.getString(1);

                    // Kiểm tra và thêm từ vào bảng English nếu chưa có
                    var vs = vocabularyExists(conn,word,partOfSpeech);
                    // Kiểm tra và thêm từ vựng vào topic nếu chưa có
                    for(var vid:vs)
                    addVocabularyToTopicIfNotExists(conn, topicId, vid);
                }

            }

            count++;
        }

        System.out.println("Đã thêm " + count + " topics.");
    }

    public static HashSet<Long> vocabularyExists(Connection conn, String word, String partOfSpeech) throws SQLException {
        HashSet<Long> vocabularyIds = new HashSet<>();

        String sql = """
            SELECT v.id
            FROM vocabularies v
            JOIN english e ON v.en = e.id
            JOIN vietnamese vi ON v.vi = vi.id
            WHERE e.word = ? AND v.part_of_speech = ? AND vi.signify!='none'
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, word);
            stmt.setString(2, partOfSpeech);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                vocabularyIds.add(rs.getLong("id"));
            }
        }

        return vocabularyIds;
    }    // Kiểm tra và thêm topic vào database nếu chưa tồn tại
    private static int addTopicIfNotExists(Connection conn, String name, int userId) throws SQLException {
        String select = "SELECT id FROM topics WHERE name = ? AND of_user = ?";
        try (PreparedStatement stmt = conn.prepareStatement(select)) {
            stmt.setString(1, name);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }

        // Nếu topic chưa tồn tại, thêm mới
        String insert = "INSERT INTO topics (name, of_user) VALUES (?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(insert)) {
            stmt.setString(1, name);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }

        return -1;
    }

    // Thêm từ vào bảng English
    private static long insertEnglish(Connection conn, String word) throws SQLException {
        String select = "SELECT id FROM english WHERE word = ?";
        try (PreparedStatement stmt = conn.prepareStatement(select)) {
            stmt.setString(1, word);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("id");
            }
        }

        // Nếu từ chưa tồn tại, thêm mới
        String insert = "INSERT INTO english (word, phonetic) VALUES (?, '') RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(insert)) {
            stmt.setString(1, word);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("id");
            }
        }

        return -1;
    }

    // Kiểm tra từ vựng đã tồn tại hay chưa
    private static boolean vocabularyExists(Connection conn, long enId, String partOfSpeech) throws SQLException {
        String sql = "SELECT 1 FROM vocabularies WHERE en = ? AND part_of_speech = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, enId);
            stmt.setString(2, partOfSpeech);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    // Thêm từ vựng vào bảng vocabularies
    private static void insertVocabulary(Connection conn, long enId, String partOfSpeech, int userId) throws SQLException {
        String sql = "INSERT INTO vocabularies (en, part_of_speech, user_own) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, enId);
            stmt.setString(2, partOfSpeech);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
        }
    }

    // Kiểm tra và thêm từ vựng vào topic nếu chưa có
    private static void addVocabularyToTopicIfNotExists(Connection conn, int topicId, long wordId) throws SQLException {
        String sql = "SELECT 1 FROM vocabularies_topics WHERE topic = ? AND vocabulary = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, topicId);
            stmt.setLong(2, wordId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                // Nếu chưa có, thêm vào
                String insert = "INSERT INTO vocabularies_topics (topic, vocabulary) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insert)) {
                    insertStmt.setInt(1, topicId);
                    insertStmt.setLong(2, wordId);
                    insertStmt.executeUpdate();
                }
            }
        }
    }
}
