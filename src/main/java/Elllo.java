import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Elllo {
    public static void main(String[] args) {
//        pullTopics();
        var ens = Test.databaseEnglish();

        var list=extractWords(Test.readFile("list.txt"));
//        var list = reafile("list.txt");
        HashSet<String> rs = new HashSet<>();
        for (var i : list) {
            if (ens.contains(i)) {
                rs.add(i);
            }
//            else if (ens.contains(i.toLowerCase())) {
//                rs.add(i.toLowerCase());
//            } else {
//                System.out.println(i);
//            }
        }

            Test.writeTopic("A1 Vocabulary Lessons", list);
//        HashSet<String[]> data=new HashSet<>();
//        for(var i: list){
//            data.add(new String[]{i});
//        }
//        Test.writeTopicHaveTypes("Untitle",data);
    }

    public static HashSet<String> reafile(String path) {
        HashSet<String> linesSet = new HashSet<>();
        try {
            // Đọc các dòng từ file và lưu vào Set
            Files.lines(Paths.get(path)).forEach(linesSet::add);
        } catch (IOException e) {
            e.printStackTrace(); // In lỗi nếu có vấn đề khi đọc file
        }
        return linesSet;
    }

    public static HashSet<String> extractWords(String input) {
        // Tạo một HashSet để lưu các từ duy nhất
        HashSet<String> wordSet = new HashSet<>();

        // Biểu thức chính quy để tìm các từ, bao gồm cả từ có dấu nháy và dấu gạch nối
        Pattern pattern = Pattern.compile("\\b[A-Za-z]+(?:[-'][A-Za-z]+)*\\b");
        Matcher matcher = pattern.matcher(input);

        // Tìm và thêm các từ vào HashSet
        while (matcher.find()) {
            wordSet.add(matcher.group());  // Chuyển về chữ thường để tránh trùng lặp
        }

        return wordSet;
    }

    private static void pullTopics() {
        String[] lvs = {
//                "A1"
                "A2"
        };

        ArrayList<Object> data = new ArrayList<>();
        HashSet<String> list = new HashSet<>();
        var topics = getTopics(lvs);
        for (var topic : topics) {
            var row = writeListeningTopic(topic);
            data.add(row);
            list.addAll((Collection<? extends String>) row.get("vs"));
        }
        HashMap<String, Object> row = new HashMap<>();
        row.put("name", "A2 Listening");
        row.put("vs", list);
        Test.writeTopics(data, "Oxford topics json/Listening");
    }

    private static List<String> getTopics(String[] lvs) {
        List<String> topics = new ArrayList<>();

        for (var lv : lvs)
            try {
                Document document = Jsoup.connect("https://elllo.org/book/" + lv + "/index.html").get();
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
        String text = "";
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

    private static HashMap<String, Object> writeListeningTopic(String topic) {
        var ens = Test.databaseEnglish();
        File[] oxfSubdirectories = Oxford.getDirectories("Oxford topics");
        File[] camSubdirectories = Oxford.getDirectories("Cambridge word lists");
        File[] subdirectories = new File[oxfSubdirectories.length + camSubdirectories.length];
        System.arraycopy(oxfSubdirectories, 0, subdirectories, 0, oxfSubdirectories.length);
        System.arraycopy(camSubdirectories, 0, subdirectories, oxfSubdirectories.length, camSubdirectories.length);
        String text = getText("https://elllo.org/book/A2/" + topic).toLowerCase();
        var words = extractWords(text);
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
                                || lv.equals("c1")
//                                || lv.equals("c2")
                        ) {
                            for (var en : ens) {
                                if (en.equals(english)) {
                                    if (words.contains(en)) list.add(english);
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
        return row;
    }
}
