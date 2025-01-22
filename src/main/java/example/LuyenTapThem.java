package example;

import java.util.HashSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;

public class LuyenTapThem {


    public static void main(String[] args) {
        String url = "https://test-english.com/grammar-points/a1/imperative-sit-dont-talk/2/";
        String text = Test.readTextFromUrl(url);
        text=text.toLowerCase();
        System.out.println(text);
        var es=Test.databaseEnglish();
        HashSet<String> list=new HashSet<>();
        for(var i: es){
            if(text.contains(i)){
                list.add(i);
            }
        }
        Test.writeTopic("Today vocabularies",list  );
    }
}
