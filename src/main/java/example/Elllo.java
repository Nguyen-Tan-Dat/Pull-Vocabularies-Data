package example;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Elllo {
    public static void main(String[] args) {
        var topics=getTopics();
        for(var topic:topics){
            writeListeningTopic(topic);
        }
    }
    private static List<String> getTopics(){
        List<String> topics = new ArrayList<>();
        String[] lvs={"A1","A2"};
        for (var lv:lvs)
        try {
            Document document = Jsoup.connect("https://elllo.org/book/"+lv+"/index.html").get();
            Elements mobileListElements = document.select(".mobilelist a"); // Chọn các thẻ <a> trong các phần tử có class="mobilelist"
            for (Element link : mobileListElements) {
                String href = link.attr("href");
                topics.add(href);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return topics;
    }
    private static String getText(String url) {
        String text="";
        try {
            Document document = Jsoup.connect(url).get();
            Element view1Element = document.getElementById("view1");
            if (view1Element != null) {
                 text = view1Element.text();
            } else {
                System.out.println("Không tìm thấy phần tử có id='view1'.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }
    private static void writeListeningTopic(String topic) {
        var ens = Test.databaseEnglish();
        ArrayList<Object> data = new ArrayList<>();
        File[] oxfSubdirectories = Oxford.getDirectories("Oxford topics");
        File[] camSubdirectories = Oxford.getDirectories("Cambridge word lists");
        File[] subdirectories = new File[oxfSubdirectories.length + camSubdirectories.length];
        System.arraycopy(oxfSubdirectories, 0, subdirectories, 0, oxfSubdirectories.length);
        System.arraycopy(camSubdirectories, 0, subdirectories, oxfSubdirectories.length, camSubdirectories.length);
        String text=getText("https://elllo.org/book/A1/"+topic);
        HashSet<String> list = new HashSet<>();
        for (File subdirectory : subdirectories) {
            List<Workbook> workbooks = Oxford.readExcelFiles(subdirectory.getAbsolutePath());
            for (Workbook workbook : workbooks) {
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    for (Row row : sheet) {
                        String english = row.getCell(0).getStringCellValue();
                        String lv = row.getCell(2).getStringCellValue().toLowerCase();
                        if (lv.equals("a1") || lv.equals("a2") || lv.equals("b1") || lv.equals("b2")
//                                || lv.equals("c1")
//                                || lv.equals("c2")
                        ) {
                            for (var en : ens) {
                                if (en.equals(english)) {
                                    if (text.toLowerCase().contains(en)) list.add(english);
                                }
                            }
                        }
                    }
                }
            }
            for (Workbook workbook : workbooks) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        HashMap<String, Object> row = new HashMap<>();
        row.put("name", topic);
        row.put("vs", list);
        data.add(row);
        Test.writeTopics(data, "Oxford topics json/"+topic);
    }
}
