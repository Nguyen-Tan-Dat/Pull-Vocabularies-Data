import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;

public class VocabularyExtractor {

    public static HashSet<String> extractVocabularyFromTextFile(String filePath) throws IOException {
        String text = Files.readString(Path.of(filePath));

        // Bước 1: Chuẩn hóa text
        text = text.toLowerCase();

        // Bước 2: Loại bỏ dấu câu, ký tự đặc biệt (giữ lại từ + số nếu cần)
        text = text.replaceAll("[^a-zA-Z\\s]", " ");

        // Bước 3: Tách từ bằng khoảng trắng
        String[] words = text.split("\\s+");

        // Bước 4: Lọc ra các từ duy nhất
        HashSet<String> vocabulary = new HashSet<>();
        for (String word : words) {
            if (!word.isBlank()) {
                vocabulary.add(word);
            }
        }

        return vocabulary;
    }

    // Hàm main để kiểm thử
    public static void main(String[] args) {
        String filePath = "text.txt"; // Đường dẫn đến file .txt của bạn
        try {
            HashSet<String> vocabulary = extractVocabularyFromTextFile(filePath);
            System.out.println("Từ vựng trong file:");
            vocabulary.forEach(System.out::println);

            Test.writeTopic("My reading", vocabulary);
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file: " + e.getMessage());
        }
    }
}
