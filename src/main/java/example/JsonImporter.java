package example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class JsonImporter {

    // Đường dẫn kết nối database
    static final String dbUrl = "jdbc:mysql://localhost:3306/cic";
    static final String dbUser = "root"; // Thay bằng tên người dùng của bạn
    static final String dbPassword = Test.PASSWORD; // Thay bằng mật khẩu của bạn
    static final String jsonFilePath = "C:\\Users\\Dat\\PhpstormProjects\\Draft_java-code\\output json\\Topics of Oxford.json"; // Đường dẫn đến file JSON

    public static void main(String[] args) {
        importTopicsFromJson(jsonFilePath);
    }

    public static HashMap<String, Integer> getEnglish(Connection connection) {
        HashMap<String, Integer> englishMap = new HashMap<>();
        String query = "SELECT word, id FROM english"; // Giả sử bảng có cột 'word' và 'value'

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String word = resultSet.getString("word");
                int value = resultSet.getInt("id");
                englishMap.put(word, value);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error reading data from 'english' table: " + e.getMessage());
        }

        return englishMap;
    }
    public static HashMap<String, Integer> getVietnamese(Connection connection) {
        HashMap<String, Integer> englishMap = new HashMap<>();
        String query = "SELECT * FROM Vietnamese"; // Giả sử bảng có cột 'word' và 'value'

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String word = resultSet.getString("signify");
                int value = resultSet.getInt("id");
                englishMap.put(word, value);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error reading data from 'Vietnamese' table: " + e.getMessage());
        }

        return englishMap;
    }
    public static HashMap<String, Integer> getTopic(Connection connection) {
        HashMap<String, Integer> englishMap = new HashMap<>();
        String query = "SELECT name, id FROM topics"; // Giả sử bảng có cột 'word' và 'value'

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String word = resultSet.getString("name");
                int value = resultSet.getInt("id");
                englishMap.put(word, value);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error reading data from 'english' table: " + e.getMessage());
        }

        return englishMap;
    }

    public static HashMap<Integer, HashMap<String, Integer>> getVocabularies(Connection connection) {
        // Khởi tạo HashMap để lưu kết quả
        HashMap<Integer, HashMap<String, Integer>> vocabularies = new HashMap<>();

        // Câu truy vấn SQL để lấy dữ liệu từ bảng vocabularies
        String query = "SELECT id, en, part_of_speech FROM vocabularies";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            // Duyệt qua các dòng trong ResultSet
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int en = resultSet.getInt("en");
                String partOfSpeech = resultSet.getString("part_of_speech");

                // Tạo HashMap cho từng bản ghi
                HashMap<String, Integer> details = new HashMap<>();
                details.put(partOfSpeech, id);

                // Thêm vào HashMap chính
                vocabularies.put(en, details);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return vocabularies;
    }
    public static HashMap<Integer, HashSet<Integer>> getVocabulariesTopics(Connection connection) {
        // Khởi tạo HashMap để lưu kết quả
        HashMap<Integer, HashSet<Integer>> vts = new HashMap<>();

        // Câu truy vấn SQL để lấy dữ liệu từ bảng vocabularies
        String query = "SELECT * FROM vocabularies_topics";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            // Duyệt qua các dòng trong ResultSet
            while (resultSet.next()) {
                int vid = resultSet.getInt("vocabulary");
                int tid = resultSet.getInt("topic");
                if (vts.get(vid) == null) {
                    vts.put(vid, new HashSet<>());
                }
                if (!vts.get(vid).contains(tid)) {
                    vts.get(vid).add(tid);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return vts;
    }

    public static void maind(String[] args) {
        // Thay đổi thông tin kết nối của bạn ở đây

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
//            HashMap<String, Integer> englishMap = getEnglish(connection);
            var vs = getVocabularies(connection);
            // In nội dung HashMap ra kiểm tra
            vs.forEach((en, v) -> {
                System.out.println("Word: " + en + ", Value: " + v.get(""));
            });

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
    }

    public static void importTopicsFromJson(String jsonFilePath) {
        Connection connection = null;
        PreparedStatement psCheckTopic = null, psInsertTopic = null;
        HashMap<Integer, Integer> vts = new HashMap<>();
        HashMap<String, Integer> ts = new HashMap<>();
        int ti = 3;
        try {
            // Kết nối với database
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            connection.setAutoCommit(false); // Dùng transaction để đảm bảo tính toàn vẹn dữ liệu

            HashMap<String, Integer> es = getEnglish(connection);
            var vs = getVocabularies(connection);
//
//            // SQL query để kiểm tra xem topic đã tồn tại chưa
            String sqlCheckTopic = "SELECT id FROM topics WHERE name = ?";
            String sqlInsertTopic = "INSERT INTO topics (name) VALUES (?)";
//
//            // Tạo PreparedStatement
            psCheckTopic = connection.prepareStatement(sqlCheckTopic);
            psInsertTopic = connection.prepareStatement(sqlInsertTopic, Statement.RETURN_GENERATED_KEYS);

            // Đọc dữ liệu từ file JSON
            FileReader reader = new FileReader(jsonFilePath);
            Gson gson = new Gson();

            // Đọc và chuyển đổi JSON thành list các đối tượng Topic
            List<Map<String, Object>> topicsList = gson.fromJson(reader, new TypeToken<List<Map<String, Object>>>() {
            }.getType());

            for (Map<String, Object> topicData : topicsList) {
                String name = (String) topicData.get("name");
                if(name.contains("'"))
                ts.put(name,++ti);
                ti = 0;
                psCheckTopic.setString(1, name);
                ResultSet rs = psCheckTopic.executeQuery();
                if (rs.next()) {
//                    // Nếu topic đã tồn tại, in id của topic
                    ti = rs.getInt("id");
//                    System.out.println("Topic already exists with ID: " + topicId);
                }
                else {
                    // Nếu topic chưa tồn tại, chèn mới và in id vừa tạo
                    psInsertTopic.setString(1, name);
                    psInsertTopic.executeUpdate();
                    ResultSet rsInsert = psInsertTopic.getGeneratedKeys();
                    if (rsInsert.next()) {
                        ti = rsInsert.getInt(1);
                        System.out.println("Inserted new topic with ID: " + ti);
                    }
                }

                // Lấy mảng từ "vs" và xử lý các từ
                List<List<String>> words = (List<List<String>>) topicData.get("vs");
                for (List<String> wordData : words) {
                    String word = wordData.get(0);  // Lấy từ
                    String partOfSpeech = wordData.get(1);
                    var ei = es.get(word);
                    var vl = vs.get(ei);
                    if (vl == null||vl.get(partOfSpeech)==null) {
                        System.out.println("Vocabulary does not exist: " + word +"/t"+ partOfSpeech);
                        continue;
                    }
                    var vi = vl.get(partOfSpeech);
                    vts.put(vi, ti);
                }
            }
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (connection != null) {
                    connection.rollback();  // Rollback nếu có lỗi
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        } finally {
            try {
                if (psCheckTopic != null) psCheckTopic.close();
                if (psInsertTopic != null) psInsertTopic.close();
                if (connection != null) connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        String rs = "";
        rs="INSERT INTO `topics` ( `name`,`id`) VALUES\n";
        for(var i:ts.keySet()){
            String escapedName = i.replace("'", "''");
            rs += "('" + escapedName + "', " + ts.get(i) + "),\n";
        }
        rs = rs.substring(0, rs.length() - 2) + ";";
        rs += "\nINSERT INTO `vocabularies_topics` ( `vocabulary`,`topic`) VALUES\n";
        for (var i : vts.keySet()) {
            rs += "(" + i + "," + vts.get(i) + "),\n";
        }
        rs = "SET SQL_MODE = \"NO_AUTO_VALUE_ON_ZERO\";\n" +
                "START TRANSACTION;\n" +
                "SET time_zone = \"+00:00\";\n" + rs.substring(0, rs.length() - 2) + ";\nCOMMIT;";
        writeStringToFile("topic.sql", rs);
    }

    public static void writeStringToFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content); // Ghi nội dung vào file
            System.out.println("Ghi chuỗi vào tệp thành công: " + filePath);
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi chuỗi vào tệp: " + e.getMessage());
        }
    }
}
