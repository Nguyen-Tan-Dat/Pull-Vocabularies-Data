package example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

public class CIC {

    // Hàm để xóa dữ liệu trong cơ sở dữ liệu
    public void deleteEnglish(String word) {
        // Thông tin kết nối cơ sở dữ liệu
        String url = "jdbc:mysql://localhost:3306/cic"; // Địa chỉ database
        String username = "root"; // Tên người dùng database
        String password = "CpqaFVYJ9Mkz6pOj"; // Mật khẩu database

        // Kết nối cơ sở dữ liệu và thực hiện xóa
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            connection.setAutoCommit(false); // Bắt đầu transaction

            // Xóa dữ liệu liên quan trong bảng vocabularies_topics
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM vocabularies_topics " +
                            "WHERE vocabulary IN (SELECT id FROM vocabularies WHERE en = (SELECT id FROM english WHERE word = ?))")) {
                stmt.setString(1, word);
                stmt.executeUpdate();
            }

            // Xóa dữ liệu liên quan trong bảng vocabularies
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM vocabularies WHERE en = (SELECT id FROM english WHERE word = ?)")) {
                stmt.setString(1, word);
                stmt.executeUpdate();
            }

            // Xóa dữ liệu trong bảng english
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM english WHERE word = ?")) {
                stmt.setString(1, word);
                stmt.executeUpdate();
            }

            connection.commit(); // Commit transaction sau khi thành công
            System.out.println("Data related to the word '" + word + "' has been deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error occurred while deleting data.");
        }
    }

    public static void main1(String[] args) {
//        CIC deleter = new CIC();
//        deleter.deleteEnglish("copperhead"); // Thay "abc" bằng từ cần xóa
        var list=Test.readDataVocabularies("All my words part laban.xlsx");
        HashSet<String> words=new HashSet<>();
        for(var i:list){
            words.add(i[0].trim());
        }
        var ens=Test.databaseEnglish();
        HashSet<String> ds=new HashSet<>();
        for(var i:ens){
            if(words.contains(i)){
                ds.add(i);
                System.out.println(i);
            }
        }
        System.out.println(ds.size());
    }
}
