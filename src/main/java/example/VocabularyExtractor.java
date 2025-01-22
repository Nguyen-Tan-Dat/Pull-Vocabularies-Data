package example;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class VocabularyExtractor {

    public static void main(String[] args) {
        // Đường dẫn đến file từ vựng
        String filePath = "vocabularies clone/Cambridge Vocabularies/Vocabularies For IELTS.txt";
        HashSet<String> list=new HashSet<>();
        try {
            Map<String, HashSet<String>> vocabularyByLesson = extractVocabularyByLesson(filePath);

            // In danh sách từ vựng theo từng chủ đề
            for (Map.Entry<String, HashSet<String>> entry : vocabularyByLesson.entrySet()) {
                System.out.println(entry.getKey().replaceFirst(":","").toUpperCase());

                Test.writeTopic(entry.getKey().replaceFirst(":","").toUpperCase(),entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, HashSet<String>> extractVocabularyByLesson(String filePath) throws IOException {
        Map<String, HashSet<String>> vocabularyByLesson = new LinkedHashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        String currentLesson = null;

        while ((line = reader.readLine()) != null) {
            line = line.trim();

            // Nếu dòng bắt đầu bằng "lesson", nó là tên chủ đề
            if (line.toLowerCase().startsWith("lesson")) {
                currentLesson = line;
                vocabularyByLesson.put(currentLesson, new HashSet<>());
            } else if (currentLesson != null && line.matches("^\\d+\\.\\s+.*$")) {
                // Nếu dòng có dạng "1. word", thì trích xuất từ
                String word = line.replaceFirst("^\\d+\\.\\s+", "").trim();
                vocabularyByLesson.get(currentLesson).add(word);
            }
        }

        reader.close();
        return vocabularyByLesson;
    }
}
