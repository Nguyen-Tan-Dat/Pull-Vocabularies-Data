package example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PullOxfordTopics {
    public static void main(String[] args) {
        writeTopics();
    }
    public static void writeTopics() {
        String directoryPath = "Oxford topics";
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Thư mục không tồn tại hoặc không phải là thư mục: " + directoryPath);
            return ;
        }
        File[] subdirectories = directory.listFiles(File::isDirectory);
        if (subdirectories == null || subdirectories.length == 0) {
            System.out.println("Không có thư mục con nào trong thư mục: " + directoryPath);
            return ;
        }
        ArrayList<Object> data=new ArrayList<>();
        HashSet<String> ps=new HashSet<>();
        for (File subdirectory : subdirectories) {
            List<Workbook> workbooks = readExcelFiles(subdirectory.getAbsolutePath());
            var name=subdirectory.getName();
            HashSet<String[]> list = new HashSet<>();
            for (Workbook workbook : workbooks) {
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    for (Row row : sheet) {
                        String english = row.getCell(0).getStringCellValue();
                        String parts_of_speech = row.getCell(1).getStringCellValue().toLowerCase();
                        String lv = row.getCell(2).getStringCellValue().toLowerCase();
                        if(lv.equals("a1")||lv.equals("a2")||lv.equals("b1")||lv.equals("b2")||lv.equals("c1")||lv.equals("c2"))
                        {
                            var add=true;
                            for (var j:list){
                                if(j[0].equals(english)){
                                    add=false;
                                    break;
                                }
                            }
                            if(add)list.add(new String[]{english,"unknown"});
                            add=true;
                            for (var j:list){
                                if(j[0].equals(english)&&j[1].equals(parts_of_speech)){
                                    add=false;
                                    break;
                                }
                            }
                            if(add)list.add(new String[]{english,parts_of_speech});



                        }

                        ps.add(parts_of_speech);
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
//            HashMapToJson.writeTopics(name,list,"Oxford topics json/"+name);
            HashMap<String,Object> row=new HashMap<>();
            row.put("name",name );
            row.put("vs",list);
            data.add(row);
            HashMapToJson.writeTopics(data,"Oxford topics json/All topics");

        }
        System.out.println("List");
        for(var i: ps) System.out.println(i);
        System.out.println("end.");
    }
    public static void cleanPartsOfSpeech() {
        var oxfordVs = PullOxfordTopics.readOnlineWords();
        System.out.println("Oxford words: " + oxfordVs.size());
        var cambridgeVs = Test.dataBook();
        var cambridgeOnVs = ReadCambridgeTopics.readOnlineWords();
        for (var i : cambridgeOnVs.keySet()) {
            cambridgeVs.add(i);
        }
        System.out.println("Cambridge words: " + cambridgeVs.size());
        System.out.println(oxfordVs.size());
        ArrayList<String[]> vsData = new ArrayList<>();
        vsData.addAll(Test.readDataVocabularies("All my words.xlsx"));
        vsData.addAll(Test.readDataVocabularies("All my words part laban clean.xlsx"));
        HashSet<String> vocabs = new HashSet<>();
        HashSet<String> cmost = new HashSet<>();

        ArrayList<String[]> most = new ArrayList<>();
        for (var i : vsData) {
            if (cambridgeVs.contains(i[0]) || oxfordVs.containsKey(i[0])) {
                if(oxfordVs.get(i[0])==null||i[2].equals("unknown")||oxfordVs.get(i[0]).contains(i[2])) {
                    most.add(i);
                    cmost.add(i[0]);
                }
                vocabs.add(i[0]);
            }
        }
        for (var i : vsData) {
            if (cambridgeVs.contains(i[0]) || oxfordVs.containsKey(i[0])) {
                if (!cmost.contains(i)) {
                    System.out.println(i[0]);
//                    most.add(i);
                }
            }
        }
//        for(var i: most){
//            cmost.add(i[0]);
//        }
        System.out.println(vocabs.size());
        System.out.println(cmost.size());
        Test.writeDataVocabularies(most, "All my words most.xlsx");
    }

    public static void cleanVocabulariesLaban() {
        ArrayList<String[]> vsData = new ArrayList<>();
        vsData.addAll(Test.readDataVocabularies("All my words part laban.xlsx"));
        HashSet<String> ps = new HashSet<>();
        System.out.println(vsData.size());
        ArrayList<String[]> clean = new ArrayList<>();
        for (var i : vsData) {
            var p = i[2].toLowerCase();
            var row = i;
            var add = true;
            if (p.contains("danh từ") || i[2].equals("Danh từ") || i[2].equals("danh từ")) {
                row[2] = "noun";
            } else if (p.contains("động từ") || p.contains("đông từ") || i[2].equals("Động từ")) {
                row[2] = "verb";
            } else if (p.contains("tính từ") || i[2].equals("Tính từ")) {
                row[2] = "adjective";
            } else if (p.contains("phó từ") || i[2].equals("Phó từ")) {
                row[2] = "adverb";
            } else if (p.contains("giới từ")) {
                row[2] = "preposition";
            } else if (p.contains("liên từ")) {
                row[2] = "conjunction";
            } else if (p.contains("đại từ")) {
                row[2] = "pronoun";
            } else if (p.contains("thán từ")) {
                row[2] = "interjection";
            } else if (p.contains("viết tắt")) {
                row[2] = "abbreviation";
            } else if (i[2].equals("Định từ")) {
                row[2] = "determiner";
            } else if (i[2].equals("unknown")) {
                row[2] = "unknown";
            } else {
                ps.add(row[2]);
                add = false;
            }
            if (add) clean.add(row);
        }
//        for(var i:vsData){
//            ps.add(i[2]);
//        }
        Test.writeDataVocabularies(clean, "All my words part laban clean.xlsx");
        System.out.println(clean.size());
        for (var i : ps) {
            System.out.println(i);
        }
        System.out.println(ps.size());
    }

    public static void downloadVocabularies() {
        var oxfordVs = PullOxfordTopics.readOxfordVocabularies();
        System.out.println("Oxford words: " + oxfordVs.size());

        var cambridgeVs = Test.dataBook();
        var cambridgeOnVs = ReadCambridgeTopics.readOnlineWords();
        for (var i : cambridgeOnVs.keySet()) {
            cambridgeVs.add(i);
        }
        System.out.println("Cambridge words: " + cambridgeVs.size());
        for (var i : cambridgeVs) {
            oxfordVs.add(i);
        }
        System.out.println(oxfordVs.size());
        ArrayList<String[]> vsData = new ArrayList<>();
        vsData.addAll(Test.readDataVocabularies("All my words.xlsx"));
        vsData.addAll(Test.readDataVocabularies("All my words part laban.xlsx"));
        HashSet<String> vocabs = new HashSet<>();
        for (var i : vsData) {
            vocabs.add(i[0]);
        }
        System.out.println(vocabs.size());
        int count = 0;
        for (var i : oxfordVs) {
            if (!vocabs.contains(i)) {
                count++;
                Test.printVocabulariesLaban(i, "All my words part laban.xlsx");
            }
        }
        System.out.println(count);
    }

    public static HashSet<String> readOxfordVocabularies() {
        String directoryPath = "Oxford topics";
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Thư mục không tồn tại hoặc không phải là thư mục: " + directoryPath);
            return null;
        }
        File[] subdirectories = directory.listFiles(File::isDirectory);
        if (subdirectories == null || subdirectories.length == 0) {
            System.out.println("Không có thư mục con nào trong thư mục: " + directoryPath);
            return null;
        }
        HashSet<String> list = new HashSet<>();
        for (File subdirectory : subdirectories) {
            List<Workbook> workbooks = readExcelFiles(subdirectory.getAbsolutePath());
            for (Workbook workbook : workbooks) {
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    for (Row row : sheet) {
                        String english = row.getCell(0).getStringCellValue();
                        list.add(english);
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
        return list;
    }

    public static HashMap<String, HashSet<String>> readOnlineWords() {
        String directoryPath = "Oxford topics";
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Thư mục không tồn tại hoặc không phải là thư mục: " + directoryPath);
            return null;
        }
        File[] subdirectories = directory.listFiles(File::isDirectory);
        if (subdirectories == null || subdirectories.length == 0) {
            System.out.println("Không có thư mục con nào trong thư mục: " + directoryPath);
            return null;
        }

        HashMap<String, HashSet<String>> plist = new HashMap<>();
        for (File subdirectory : subdirectories) {
            List<Workbook> workbooks = readExcelFiles(subdirectory.getAbsolutePath());
            for (Workbook workbook : workbooks) {
                Sheet sheet = workbook.getSheetAt(0);
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        Cell cell = row.getCell(0);
                        Cell cell1 = row.getCell(1);
                        Cell cell2 = row.getCell(2);
                        String cellValue = Test.getCellValueAsString(cell);
                        String cellValue1 = Test.getCellValueAsString(cell1);
                        String lv = Test.getCellValueAsString(cell2);
                        cellValue = cellValue.trim();
                        cellValue1 = cellValue1.trim();
                        lv = lv.trim();
                        if(lv.equals("a1")||lv.equals("a2")||lv.equals("b1")||lv.equals("b2")){
                            if (!plist.containsKey(cellValue)) {
                                plist.put(cellValue, new HashSet<>());
                            }
                            plist.get(cellValue).add(cellValue1.toLowerCase());
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
        return plist;
    }

    public static void downloadOxfordTopic(String link, String path) {
        try {
            Document document = Jsoup.connect(link).get();
            Elements topicElements = document.getElementsByClass("topic-box-secondary-heading");
            for (Element element : topicElements) {
                Elements links1 = element.select("a[href]");
                for (Element link1 : links1) {
                    String linkHref1 = link1.attr("href");
                    String linkText1 = link1.text();
                    System.out.println(linkText1);
                    try {
                        Document document1 = Jsoup.connect(linkHref1).get();
                        Element ulElement = document1.selectFirst("ul.top-g");
                        if (ulElement != null) {
                            Elements lis = ulElement.getElementsByTag("li");
                            for (Element li : lis) {
                                var a = li.getElementsByTag("a").get(0);
                                var english = a.text();
                                var noun = li.getElementsByClass("pos").get(0).text();
                                var lv = li.getElementsByClass("belong-to").get(0).text();
                                System.out.println(english + "\t" + noun + "\t" + lv);
                                addLineToExcell(path + "/" + formatString(linkText1) + ".xlsx", english, noun, lv);
                            }
                        } else {
                            System.out.println("Không tìm thấy phần tử ul với class 'top-g'.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void downloadOxfordTopics() {

        String directoryPath = "Oxford topics";
        try {
            Document doc = Jsoup.connect("https://www.oxfordlearnersdictionaries.com/topic/").get();
            Elements topicList = doc.getElementsByClass("topic_list");
            for (Element topic : topicList) {
                Elements links = topic.select("a[href]");
                for (Element link : links) {
                    String linkHref = link.attr("href");
                    String linkText = link.text();
                    String path = directoryPath + "/" + linkText;
                    File directory = new File(path);
                    if (!directory.exists()) {
                        boolean isCreated = directory.mkdir();
                    }
                    downloadOxfordTopic(linkHref, path);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addLineToExcell(String excelFilePath, String en, String noun, String lv) {
        File excelFile = new File(excelFilePath);
        Workbook workbook = null;
        Sheet sheet = null;
        try {
            if (excelFile.exists()) {
                try (FileInputStream fileInputStream = new FileInputStream(excelFile)) {
                    workbook = new XSSFWorkbook(fileInputStream);
                    sheet = workbook.getSheetAt(0);
                }
            } else {
                // Tạo tệp Excel mới
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Topics");
            }
            // Kiểm tra xem dòng đã tồn tại chưa
            boolean exists = false;
            for (Row row : sheet) {
                Cell cell = row.getCell(0);
                Cell cell1 = row.getCell(1);
                if (cell != null && cell.getStringCellValue().equals(en) && cell1.getStringCellValue().equals(noun)) {
                    exists = true;
                    break;
                }
            }
            // Nếu dòng không tồn tại, thêm dòng mới
            if (!exists) {
                int lastRowNum = sheet.getLastRowNum();
                Row newRow = sheet.createRow(lastRowNum + 1);
                newRow.createCell(0).setCellValue(en);
                newRow.createCell(1).setCellValue(noun);
                newRow.createCell(2).setCellValue(lv);
//                newRow.createCell(1).setCellValue(topicDescription);
            }
            try (FileOutputStream fileOutputStream = new FileOutputStream(excelFilePath)) {
                workbook.write(fileOutputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<Workbook> readExcelFiles(String directoryPath) {
        List<Workbook> workbooks = new ArrayList<>();
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Thư mục không tồn tại hoặc không phải là thư mục: " + directoryPath);
            return workbooks;
        }
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".xlsx"));
        if (files == null || files.length == 0) {
            System.out.println("Không có tệp Excel nào trong thư mục: " + directoryPath);
            return workbooks;
        }
        for (File file : files) {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                Workbook workbook = new XSSFWorkbook(fileInputStream);
                workbooks.add(workbook);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return workbooks;
    }

    public static String formatString(String input) {
        // Loại bỏ phần trong ngoặc đơn
        String result = input.replaceAll("\\(.*?\\)", "");
        // Thay thế dấu hai chấm bằng dấu cách
        result = result.replace(":", "");
        // Loại bỏ các khoảng trắng thừa ở đầu và cuối chuỗi
        result = result.trim();
        return result;
    }
}
