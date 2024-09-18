package example;

import java.util.HashSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;

public class LuyenTapThem {
    public static String readTextFromUrl(String url) {
        try {
            // Kết nối đến URL và lấy nội dung HTML
            Document document = Jsoup.connect(url).get();
            String text = document.text();

            return text;
        } catch (IOException e) {
            return "An error occurred: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        String url = "https://test-english.com/grammar-points/a1/imperative-sit-dont-talk/2/";
        String text = readTextFromUrl(url);
        text=text.toLowerCase();
        System.out.println(text);
        var es=Test.databaseEnglish();
        HashSet<String> list=new HashSet<>();
        for(var i: es){
            if(text.contains(i)){
                list.add(i);
            }
        }
        HashMapToJson.writeTopic("Today vocabularies",list,"Today vocabularies");
    }
}
