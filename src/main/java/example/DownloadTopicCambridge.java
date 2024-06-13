package example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class DownloadTopicCambridge {

    public static String readFile(String fileName) {
        StringBuilder content = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("https://dictionary.cambridge.org/plus/wordlist/"))
                    content.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return content.toString();
    }

    public static void main(String[] args) {
        String html = readFile("list.txt");
        Document doc = Jsoup.parse(html);
        var ls=doc.getElementsByTag("a");
        HashSet<String> links=new HashSet<>();
        for (Element link : ls) {
            String href = link.attr("href");
            links.add(href);
        }
        int count=0;
        for (var i:links){
//            var ht=DownloadTopic.getHTML(i);
//            var d=Jsoup.parse(ht);
            System.out.println(i.split("_")[0]+"/export");
//            break;
            count++;
        }
        System.out.println(count);
    }
}
