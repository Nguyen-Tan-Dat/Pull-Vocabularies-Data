package example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class Oxford {
    public static void main(String[] args) {
//        writeAllTopics();
//        createTopics();
//        writeTopics();
//        createSQL();
//        writeAllNone();
        pullIPA();
    }
    public static void pullIPA(){
        Scanner scanner=new Scanner(System.in);
        for(int i=0;i<100;i++) {
            var input = scanner.nextLine();
            System.out.println(getPhonetic(input));
        }
    }
    public static String getPhonetic(String word) {
        try {
            String url = "https://www.oxfordlearnersdictionaries.com/definition/english/" + word + "?q=" + word;
            Document doc = Jsoup.connect(url).get();
            Element phoneticSpan = doc.selectFirst("span.phon");

            if (phoneticSpan != null) {
                return phoneticSpan.text();
            } else {
                return "Phonetic transcription not found.";
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static ArrayList<String> listExcelFiles(String pathRoot) {
        ArrayList<String> excelFiles = new ArrayList<>();
        Path rootPath = Paths.get(pathRoot);
        try {
            Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // Kiểm tra file có đuôi là .xlsx hoặc .xls
                    if (file.toString().endsWith(".xlsx") || file.toString().endsWith(".xls")) {
                        excelFiles.add(file.toAbsolutePath().toString());
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    // Nếu không thể truy cập file nào đó, bỏ qua nó
                    System.err.println("Failed to access file: " + file + " - " + exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.err.println("Error while reading files: " + e.getMessage());
        }
        return excelFiles;
    }

    public static String pathToTopic(String path) {
        // Tách chuỗi bằng dấu '\\' để phân cấp thư mục
        String[] parts = path.split("\\\\");

        // Xác định vị trí bắt đầu của phần "Topics of Oxford"
        int startIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equalsIgnoreCase("Topics of Oxford")) {
                startIndex = i + 1;
                break;
            }
        }

        // Nếu không tìm thấy "Topics of Oxford", trả về chuỗi rỗng
        if (startIndex == -1 || startIndex >= parts.length) {
            return "";
        }

        // Lấy phần sau "Topics of Oxford" và bỏ phần mở rộng của file
        StringBuilder topic = new StringBuilder();
        for (int i = startIndex; i < parts.length; i++) {
            if (i == parts.length - 1) { // Xử lý file cuối cùng
                String fileName = parts[i];
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex != -1) {
                    fileName = fileName.substring(0, dotIndex); // Bỏ phần mở rộng
                }
                topic.append(fileName);
            } else {
                topic.append(parts[i]).append(" > ");
            }
        }

        return topic.toString();
    }

    public static void writeExcel(String filePath, ArrayList<String[]> data) {
        // Tạo workbook
        Workbook workbook = new XSSFWorkbook();
        // Tạo sheet
        Sheet sheet = workbook.createSheet("Sheet1");

        // Duyệt qua danh sách dữ liệu và ghi vào sheet
        for (int i = 0; i < data.size(); i++) {
            // Tạo hàng (row)
            Row row = sheet.createRow(i);

            // Ghi dữ liệu vào, bắt đầu từ cột thứ 3 (index = 2)
            String[] rowData = data.get(i);
            for (int j = 0; j < rowData.length; j++) {
                Cell cell = row.createCell(j + 2); // Bắt đầu từ cột thứ 3
                cell.setCellValue(rowData[j]);
            }
        }

        // Ghi workbook ra file
        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            workbook.write(outputStream);
            System.out.println("Ghi file Excel thành công: " + filePath);
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file Excel: " + e.getMessage());
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                System.err.println("Lỗi khi đóng workbook: " + e.getMessage());
            }
        }
    }

    public static void createTopics() {
        String pathRoot = "Topics of Oxford";
        ArrayList<String> excelFiles = listExcelFiles(pathRoot);
        var ens = Test.databaseEnglish();
        HashSet<String> lvs = new HashSet<>();
        HashSet<String[]> a1 = new HashSet<>();
        HashSet<String[]> a2 = new HashSet<>();
        HashSet<String[]> b1 = new HashSet<>();
        HashSet<String[]> b2 = new HashSet<>();
        HashSet<String[]> c1 = new HashSet<>();
        HashSet<String[]> c2 = new HashSet<>();
        ArrayList<Object> data = new ArrayList<>();
        ArrayList<String[]> listData = new ArrayList<>();
        var ts = Database.countWordInTopics();
        for (String file : excelFiles) {
            var name = pathToTopic(file);
            HashSet<String[]> list = new HashSet<>();
            int countAdd = 0;
            try {
                Workbook workbook = new XSSFWorkbook(file);
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    for (Row row : sheet) {
                        String english = row.getCell(0).getStringCellValue();
                        String parts_of_speech = row.getCell(1).getStringCellValue().toLowerCase();
                        String lv = row.getCell(2).getStringCellValue().toLowerCase();

                        if (!ens.contains(english)) {
                            System.out.println(file + "\t" + english + "\t" + parts_of_speech);
//                            continue;
                        }
                        ;
//                        if (
//                                lv.equals("c1") || lv.equals("c2")
//                                lv.equals("b2")||lv.equals("b1")||lv.equals("a2"||lv.equals("a1")
//                        ) continue;
                        list.add(new String[]{english, parts_of_speech});
                        if (lv.equals("a1")
                                || lv.equals("a2")
                                || lv.equals("b1")
//                                ||lv.equals("b2")
//                                ||lv.equals("c1")
//                                ||lv.equals("c2")
                        ) countAdd++;
                        if (lv.equals("a1")) a1.add(new String[]{english, parts_of_speech});
                        if (lv.equals("a2")) a2.add(new String[]{english, parts_of_speech});
                        if (lv.equals("b1")) b1.add(new String[]{english, parts_of_speech});
                        if (lv.equals("b2")) b2.add(new String[]{english, parts_of_speech});
                        if (lv.equals("c1")) c1.add(new String[]{english, parts_of_speech});
                        if (lv.equals("c2")) c2.add(new String[]{english, parts_of_speech});
                        var ltn=name.split(">");
                        if(!ltn[2].trim().equals("All")){
                            listData.add(new String[]{ltn[0].trim(),ltn[1].trim(),ltn[2].trim(),english, parts_of_speech});
                        }
//                        listData.add(new String[]{name, english, parts_of_speech});
                    }

                }
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (list.size() == countAdd) {
                System.out.println(name);
            }
            HashMap<String, Object> row = new HashMap<>();
            row.put("name", name);
            row.put("vs", list);
            data.add(row);
            if (ts.containsKey(name)) {
                if (ts.get(name) == list.size()) System.out.println(name);
            } else {
                System.out.println("       " + name);
            }
        }
        writeExcel("All.xlsx", listData);
        HashMap<String, Object> row = new HashMap<>();
        row.put("name", "A1");
        row.put("vs", a1);
        data.add(row);
        row = new HashMap<>();
        row.put("name", "A2");
        row.put("vs", a2);
        data.add(row);
        row = new HashMap<>();
        row.put("name", "B1");
        row.put("vs", b1);
        data.add(row);
        row = new HashMap<>();
        row.put("name", "B2");
        row.put("vs", b2);
        data.add(row);
        row = new HashMap<>();
        row.put("name", "C1");
        row.put("vs", c1);
        data.add(row);
        row = new HashMap<>();
        row.put("name", "C2");
        row.put("vs", c2);
        data.add(row);
        Test.writeTopics(data, "Topics of Oxford");
        System.out.println(a1.size());
        System.out.println(a2.size());
        System.out.println(b1.size());
        System.out.println(b2.size());
        System.out.println(c1.size());
        System.out.println(c2.size());
    }

    public static void createSQL() {
        String pathRoot = "Topics of Oxford";
        ArrayList<String> excelFiles = listExcelFiles(pathRoot);
        HashSet<String[]> a1 = new HashSet<>();
        HashSet<String[]> a2 = new HashSet<>();
        HashSet<String[]> b1 = new HashSet<>();
        HashSet<String[]> b2 = new HashSet<>();
        HashSet<String[]> c1 = new HashSet<>();
        HashSet<String[]> c2 = new HashSet<>();
        ArrayList<Object> data = new ArrayList<>();
        HashMap<String, Integer> es = new HashMap<>();
        int ei = 1;
        HashMap<String, HashMap<String, Integer>> vs = new HashMap<>();
        int vi = 1;
        int ti = 4;
        String rs = "INSERT INTO vietnamese (id,signify) VALUES (1,'none');\n";
        for (String file : excelFiles) {
            var name = pathToTopic(file);
            rs += "INSERT INTO topics (id,name) VALUES (" + ti + ",'" + name + "');\n";
            try {
                Workbook workbook = new XSSFWorkbook(file);
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    for (Row row : sheet) {
                        String english = row.getCell(0).getStringCellValue();
                        String parts_of_speech = row.getCell(1).getStringCellValue().toLowerCase();
                        String lv = row.getCell(2).getStringCellValue().toLowerCase();
                        if (!es.containsKey(english)) {
                            rs += "INSERT INTO english (id,word, phonetic) VALUES (" + ei + ",'" + english + "','');\n";
                            es.put(english, ei++);
                        }
                        if (!vs.containsKey(english)) {
                            vs.put(english, new HashMap<>());
                        }
                        if (!vs.get(english).containsKey(parts_of_speech)) {
                            rs += "INSERT INTO vocabularies (id,en, vi, part_of_speech) VALUES (" + vi + "," + es.get(english) + ", 1, '" + parts_of_speech + "');\n";
                            vs.get(english).put(parts_of_speech, vi++);
                        }
                        rs += "INSERT INTO vocabularies_topics (topic, vocabulary) VALUES (" + ti + "," + vs.get(english).get(parts_of_speech) + ");\n";
                    }
                }
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ti++;
//            break;
        }
        System.out.println(rs);
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

    public static void listTopics() {
        var list = readOnlineWords();
        HashSet<String> topics = new HashSet<>();
        String pathRoot = "Topics of Oxford";
        ArrayList<String> excelFiles = listExcelFiles(pathRoot);
        for (String file : excelFiles) {
            var name = pathToTopic(file);
            try {
                Workbook workbook = new XSSFWorkbook(file);
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    for (Row row : sheet) {
                        String english = row.getCell(0).getStringCellValue();
                        String parts_of_speech = row.getCell(1).getStringCellValue().toLowerCase();
                        String lv = row.getCell(2).getStringCellValue().toLowerCase();
                        if (list.get(english) != null && list.get(english).contains(parts_of_speech)) {
                            topics.add(name);
//                            System.out.println(name);
                        }
                    }

                }
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (var i : topics) {
            System.out.println(i);
        }
    }

    public static void writeAllNone() {
        String pathRoot = "Topics of Oxford";
        ArrayList<String> excelFiles = listExcelFiles(pathRoot);
        var ens = Test.databaseEnglish();
        HashSet<String> lvs = new HashSet<>();
        HashSet<String[]> a1 = new HashSet<>();
        HashSet<String[]> a2 = new HashSet<>();
        HashSet<String[]> b1 = new HashSet<>();
        HashSet<String[]> b2 = new HashSet<>();
        HashSet<String[]> c1 = new HashSet<>();
        HashSet<String[]> c2 = new HashSet<>();
        ArrayList<Object> data = new ArrayList<>();

        ArrayList<String[]> list = new ArrayList<>();
        for (String file : excelFiles) {
            var name = pathToTopic(file);
            try {
                Workbook workbook = new XSSFWorkbook(file);
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    for (Row row : sheet) {
                        String english = row.getCell(0).getStringCellValue();
                        String parts_of_speech = row.getCell(1).getStringCellValue().toLowerCase();
                        list.add(new String[]{english, "none", parts_of_speech});
                    }
                }
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writeExcel("All none.xlsx", list);
    }

    public static void writeVocabulariesToExcel(String path, List<HashMap<String, String>> vocabularies) {
        // Tạo workbook và sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Vocabularies");

        // Tạo dòng tiêu đề
//        Row headerRow = sheet.createRow(0);
//        String[] headers = {"Word", "Part of Speech", "Level", "Word Link", "MP3 UK Link", "MP3 US Link"};
//        for (int i = 0; i < headers.length; i++) {
//            Cell cell = headerRow.createCell(i);
//            cell.setCellValue(headers[i]);
//            CellStyle style = workbook.createCellStyle();
//            Font font = workbook.createFont();
//            font.setBold(true);
//            style.setFont(font);
//            cell.setCellStyle(style);
//        }

        // Ghi dữ liệu từ vựng
        int rowIndex = 0; // Dòng bắt đầu ghi dữ liệu
        for (HashMap<String, String> vocabulary : vocabularies) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(vocabulary.getOrDefault("word", ""));
            row.createCell(1).setCellValue(vocabulary.getOrDefault("partOfSpeech", ""));
            row.createCell(2).setCellValue(vocabulary.getOrDefault("level", ""));
            row.createCell(3).setCellValue(vocabulary.getOrDefault("wordLink", ""));
            row.createCell(4).setCellValue(vocabulary.getOrDefault("mp3UKLink", ""));
            row.createCell(5).setCellValue(vocabulary.getOrDefault("mp3USLink", ""));
        }

        // Ghi workbook vào file
        try (FileOutputStream fos = new FileOutputStream(path)) {
            workbook.write(fos);
            System.out.println("Excel file written successfully to: " + path);
        } catch (IOException e) {
            System.err.println("Error writing Excel file: " + e.getMessage());
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                System.err.println("Error closing workbook: " + e.getMessage());
            }
        }
    }

    public static List<HashMap<String, String>> getVocabularies(String url) {
        List<HashMap<String, String>> vocabularies = new ArrayList<>();

        // Kết nối tới URL và lấy nội dung HTML
        Document doc = Jsoup.parse(SeleniumHelper.getHTML(url));

        // Lấy tất cả các <li> trong <ul class="top-g">
        Elements vocabElements = doc.select("ul.top-g > li");

        for (Element vocabElement : vocabElements) {
            // Bỏ qua nếu có class "hidden"
            if (vocabElement.attr("class").contains("hidden")) {
                continue;
            }
            if (vocabElement.select(".hidden").size() > 0) {
                continue; // Bỏ qua các phần tử có class 'hidden'
            }

            HashMap<String, String> vocab = new HashMap<>();

            // Lấy từ vựng (word)
            String word = vocabElement.getElementsByTag("a").get(0).text();

            // Lấy từ loại (part of speech)
            String partOfSpeech = vocabElement.selectFirst("span.pos") != null
                    ? vocabElement.selectFirst("span.pos").text()
                    : "";

            // Lấy cấp độ từ vựng (level)
            String level = vocabElement.selectFirst("span.belong-to") != null
                    ? vocabElement.selectFirst("span.belong-to").text()
                    : "";

            // Lấy link từ (word link)
            String wordLink = vocabElement.selectFirst("a") != null
                    ? "https://www.oxfordlearnersdictionaries.com" + vocabElement.selectFirst("a").attr("href")
                    : "";

            // Lấy link mp3 UK
            String mp3UK = vocabElement.selectFirst("div.pron-uk") != null
                    ? vocabElement.selectFirst("div.pron-uk").attr("data-src-mp3")
                    : "";

            // Lấy link mp3 US
            String mp3US = vocabElement.selectFirst("div.pron-us") != null
                    ? vocabElement.selectFirst("div.pron-us").attr("data-src-mp3")
                    : "";

            // Chỉ thêm từ vựng nếu có dữ liệu hợp lệ
            if (!word.isEmpty()) {
                vocab.put("word", word);
                vocab.put("partOfSpeech", partOfSpeech);
                vocab.put("level", level);
                vocab.put("wordLink", wordLink);
                vocab.put("mp3UK", mp3UK);
                vocab.put("mp3US", mp3US);
                vocabularies.add(vocab);
            }
        }

        return vocabularies;
    }


    public static HashMap<String, HashMap<String, String>> getSecondTopics(String url) {
        HashMap<String, HashMap<String, String>> secondTopics = new HashMap<>();

        try {
            // Kết nối và lấy tài liệu HTML từ URL
            Document doc = Jsoup.connect(url).get();

            // Chọn các phần tử có class 'topic-box topic-box-secondary'
            Elements secondaryBoxes = doc.select(".topic-box.topic-box-secondary");

            for (Element box : secondaryBoxes) {
                // Lấy tiêu đề chính từ thẻ <a> có class 'topic-box-secondary-heading'
                Element heading = box.selectFirst(".topic-box-secondary-heading");
                if (heading != null) {
                    String mainTopic = heading.text().replace("(see all)", "").trim();
                    String mainUrl = heading.absUrl("href");

                    // HashMap chứa các sub-topics
                    HashMap<String, String> subTopics = new HashMap<>();
                    subTopics.put("All", mainUrl);
                    Elements subTopicLinks = box.select(".l3 a");
                    for (Element subLink : subTopicLinks) {
                        String subTopicText = subLink.text().replaceAll(":", " in ");
                        String subTopicUrl = subLink.absUrl("href");
                        subTopics.put(subTopicText, subTopicUrl);
                    }
                    secondTopics.put(mainTopic.replaceAll(":", " in "), subTopics);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return secondTopics;
    }

    public static boolean createDirectory(String parent, String name) {
        // Tạo đối tượng File đại diện cho đường dẫn thư mục
        File directory = new File(parent, name);

        // Kiểm tra xem thư mục đã tồn tại chưa
        if (directory.exists()) {
            System.out.println("Directory already exists: " + directory.getAbsolutePath());
            return false; // Trả về false vì không tạo mới
        }

        // Tạo thư mục nếu chưa tồn tại
        boolean created = directory.mkdirs();
        if (created) {
            System.out.println("Directory created: " + directory.getAbsolutePath());
        } else {
            System.out.println("Failed to create directory: " + directory.getAbsolutePath());
        }

        return created; // Trả về true nếu tạo mới thành công
    }

    public static void writeAllTopics() {
        SeleniumHelper.setup();
        var root = "Topics of Oxford";
        createDirectory(System.getProperty("user.dir"), root);
        root = System.getProperty("user.dir") + "/" + root;
        var topics = getRootTopics();
        for (String name : topics.keySet()) {
//            createDirectory(root, name);
            var secondTopics = getSecondTopics(topics.get(name));
            for (var sn : secondTopics.keySet()) {
//                createDirectory(root + "/" + name, sn);
                for (var tn : secondTopics.get(sn).keySet()) {
//                    System.out.println(name+" > "+sn+" > "+tn);
                    var path = root + "/" + name + "/" + sn + "/" + tn.replaceAll("/", " or ") + ".xlsx";
                    var vocabularies = getVocabularies(secondTopics.get(sn).get(tn));
                    writeVocabulariesToExcel(path, vocabularies);
                }
            }
        }
    }

    public static void main1(String[] args) {
//        test();
//        downloadOxfordTopics();
//        downloadOxfordOxford3000And5000();
//        writeTopics();

//        var subdirectories1 = getDirectories("Oxford wordlists");
//        ArrayList<Object> data = new ArrayList<>();
//        for (var subdirectory1 : subdirectories1) {
//            var subdirectories = getDirectories(subdirectory1.getAbsolutePath());
//            if(subdirectories!=null)
//            for (File subdirectory : subdirectories) {
//
//                System.out.println(subdirectory.getAbsolutePath());
//                List<Workbook> workbooks = readExcelFiles(subdirectory.getAbsolutePath());
//                var name = subdirectory.getName();
//                HashSet<String[]> list = new HashSet<>();
//                for (Workbook workbook : workbooks) {
//                    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
//                        Sheet sheet = workbook.getSheetAt(i);
//                        for (Row row : sheet) {
//                            String english = row.getCell(0).getStringCellValue();
//                            String parts_of_speech = row.getCell(1).getStringCellValue().toLowerCase();
//                            String lv = row.getCell(2).getStringCellValue().toLowerCase();
//                            if (lv.equals("A1") || lv.equals("A2") || lv.equals("B1") || lv.equals("B2")) {
//                                var add = true;
//                                for (var j : list) {
//                                    if (j[0].equals(english)) {
//                                        add = false;
//                                        break;
//                                    }
//                                }
//                                if (add) list.add(new String[]{english, "unknown"});
//                                add = true;
//                                for (var j : list) {
//                                    if (j[0].equals(english) && j[1].equals(parts_of_speech)) {
//                                        add = false;
//                                        break;
//                                    }
//                                }
//                                if (add) list.add(new String[]{english, parts_of_speech});
//                            }
//
//                        }
//                    }
//                    try {
//                        workbook.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
////            Test.writeTopics(name,list,"Oxford topics json/"+name);
//                HashMap<String, Object> row = new HashMap<>();
//                row.put("name", name);
//                row.put("vs", list);
//                data.add(row);
//
//            }
//        }
//        Test.writeTopics(data, "Oxford topics json/All level");
    }

    public static HashMap<String, String> getRootTopics() {
        HashMap<String, String> topics = new HashMap<>();
        String url = "https://www.oxfordlearnersdictionaries.com/topic/";

        try {
            // Kết nối và lấy tài liệu HTML từ URL
            Document doc = Jsoup.connect(url).get();

            // Chọn các phần tử có class là 'topic-box'
            Elements topicBoxes = doc.select(".topic-box a");

            for (Element topic : topicBoxes) {
                // Lấy text (tên chủ đề) và URL từ thẻ <a>
                String topicName = topic.text();
                String topicUrl = topic.absUrl("href"); // absUrl để lấy URL đầy đủ

                // Thêm vào HashMap
                topics.put(topicName, topicUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return topics;
    }

    private static void test() {
        File file = new File("Data");
        List<Workbook> workbooks = readExcelFiles(file.getAbsolutePath());
        var name = file.getName();
        HashSet<String> words = new HashSet<>();
        for (Workbook workbook : workbooks) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (Row row : sheet) {
                    String english = row.getCell(2).getStringCellValue();
                    words.add(english);
                }
            }
        }
        System.out.println(words.size());
    }

    public static String pathToTopicName(String path) {
        // Tách chuỗi theo ký tự phân cách "\\"
        String[] parts = path.split("\\\\");

        // Kiểm tra tính hợp lệ của đường dẫn
        if (parts.length < 2) {
            return "Invalid path";
        }

        // Lấy các phần cần thiết
        String mainTopic = parts[parts.length - 2]; // Chủ đề chính (thư mục trước cùng)
        String subTopic = parts[parts.length - 1].replace(".xlsx", ""); // Chủ đề phụ (tên file)

        // Trả về kết quả
        return mainTopic + " | " + subTopic;
    }

    private static void writeWordlist() {
        var ens = Test.databaseEnglish();
        ArrayList<Object> data = new ArrayList<>();
        File[] oxfSubdirectories = getDirectories("Oxford topics");
        File[] camSubdirectories = new File[]{};
//        File[] camSubdirectories = getDirectories("Cambridge word lists");
        File[] subdirectories = new File[oxfSubdirectories.length + camSubdirectories.length];

        System.arraycopy(oxfSubdirectories, 0, subdirectories, 0, oxfSubdirectories.length);
        System.arraycopy(camSubdirectories, 0, subdirectories, oxfSubdirectories.length, camSubdirectories.length);

        for (File subdirectory : subdirectories) {
            var files = getExcelFiles(subdirectory.getAbsolutePath());
            for (File file : files) {
                HashSet<String> list = new HashSet<>();
                var topic = pathToTopicName(file.getAbsolutePath());
                System.out.println(topic);
//                try (FileInputStream fileInputStream = new FileInputStream(file)) {
//                    Workbook workbook = new XSSFWorkbook(fileInputStream);
//                    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
//                        Sheet sheet = workbook.getSheetAt(i);
//                        for (Row row : sheet) {
//                            String english = row.getCell(0).getStringCellValue();
//                            String lv = row.getCell(2).getStringCellValue().toLowerCase();
//                            if (lv.equals("a1")
//                                    || lv.equals("")
//                                    || lv.equals("a2")
//                                    || lv.equals("b1")
//                                    || lv.equals("b2")
////                                || lv.equals("c1")
////                                || lv.equals("c2")
//                            ) {
//                                list.add(english);
//                            }
//                        }
//                    }
//                    workbook.close();
//                } catch (FileNotFoundException e) {
//                    throw new RuntimeException(e);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                HashMap<String, Object> row = new HashMap<>();
//                row.put("name", topic);
//                row.put("vs", list);
//                data.add(row);
            }

        }


//        String name = "Learning vocabularies";
//        HashMap<String, Object> row = new HashMap<>();
//        row.put("name", name);
//        row.put("vs", list);
//        data.add(row);
//        Test.writeTopics(data, "Oxford topics");
    }

    private static void writeTopics3000() {
        var ens = Test.databaseEnglish();
        ArrayList<Object> data = new ArrayList<>();
        File[] oxfSubdirectories = getDirectories("Oxford topics");
        File[] camSubdirectories = new File[]{};
//        File[] camSubdirectories = getDirectories("Cambridge word lists");
        File[] subdirectories = new File[oxfSubdirectories.length + camSubdirectories.length];

        System.arraycopy(oxfSubdirectories, 0, subdirectories, 0, oxfSubdirectories.length);
        System.arraycopy(camSubdirectories, 0, subdirectories, oxfSubdirectories.length, camSubdirectories.length);

        for (File subdirectory : subdirectories) {
            var files = getExcelFiles(subdirectory.getAbsolutePath());
            for (File file : files) {
                HashSet<String> list = new HashSet<>();
                var topic = pathToTopicName(file.getAbsolutePath());
                System.out.println(topic);
//                try (FileInputStream fileInputStream = new FileInputStream(file)) {
//                    Workbook workbook = new XSSFWorkbook(fileInputStream);
//                    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
//                        Sheet sheet = workbook.getSheetAt(i);
//                        for (Row row : sheet) {
//                            String english = row.getCell(0).getStringCellValue();
//                            String lv = row.getCell(2).getStringCellValue().toLowerCase();
//                            if (lv.equals("a1")
//                                    || lv.equals("")
//                                    || lv.equals("a2")
//                                    || lv.equals("b1")
//                                    || lv.equals("b2")
////                                || lv.equals("c1")
////                                || lv.equals("c2")
//                            ) {
//                                list.add(english);
//                            }
//                        }
//                    }
//                    workbook.close();
//                } catch (FileNotFoundException e) {
//                    throw new RuntimeException(e);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                HashMap<String, Object> row = new HashMap<>();
//                row.put("name", topic);
//                row.put("vs", list);
//                data.add(row);
            }

        }


//        String name = "Learning vocabularies";
//        HashMap<String, Object> row = new HashMap<>();
//        row.put("name", name);
//        row.put("vs", list);
//        data.add(row);
//        Test.writeTopics(data, "Oxford topics");
    }

    public static File[] getDirectories(String directoryPath) {
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
        return subdirectories;
    }

    public static void writeTopics() {
        String[] levels = new String[]{"a1", "a2", "b1", "b2", "c1", "c2"};
        List<Integer> counts = new ArrayList<>();
        var subdirectories = getDirectories("Oxford wordlists");
        for (var level : levels) {
            int count = 0;
            ArrayList<Object> data = new ArrayList<>();
            HashSet<String> ps = new HashSet<>();
//            for (File subdirectory1 : subdirectories)
            for (File subdirectory : subdirectories) {
//                System.out.println(subdirectory.getAbsolutePath());
                List<Workbook> workbooks = readExcelFiles(subdirectory.getAbsolutePath());
                var name = subdirectory.getName();
                HashSet<String[]> list = new HashSet<>();
                for (Workbook workbook : workbooks) {
                    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                        Sheet sheet = workbook.getSheetAt(i);
                        for (Row row : sheet) {
                            String english = row.getCell(0).getStringCellValue();
                            String parts_of_speech = row.getCell(1).getStringCellValue().toLowerCase();
                            String lv = row.getCell(2).getStringCellValue().toLowerCase();
                            if (lv.equals(level)) {
                                var add = true;
                                for (var j : list) {
                                    if (j[0].equals(english)) {
                                        add = false;
                                        break;
                                    }
                                }
                                if (add) list.add(new String[]{english, "unknown"});
                                add = true;
                                for (var j : list) {
                                    if (j[0].equals(english) && j[1].equals(parts_of_speech)) {
                                        add = false;
                                        break;
                                    }
                                }
                                if (add) list.add(new String[]{english, parts_of_speech});
                                count++;

                            }
                            ps.add(parts_of_speech);
                        }
                    }
                    try {
                        workbook.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
//            Test.writeTopics(name,list,"Oxford topics json/"+name);
                HashMap<String, Object> row = new HashMap<>();
                row.put("name", level.toUpperCase() + " " + name);
                row.put("vs", list);
                data.add(row);
                Test.writeTopics(data, "Oxford topics json/" + level.toUpperCase() + " topics");

            }
            System.out.println("List");
            for (var i : ps) System.out.println(i);
            System.out.println("end.");
        }
        for (var i : counts) {
            System.out.println(i);
        }
    }

    public static void cleanPartsOfSpeech() {
        var oxfordVs = Oxford.readOnlineWords();
        var ens = Test.databaseEnglish();
        System.out.println("Oxford words: " + oxfordVs.size());

        var cambridgeOnVs = Cambridge.readOnlineWords();

        System.out.println(oxfordVs.size());
        ArrayList<String[]> vsData = new ArrayList<>();
        vsData.addAll(Test.readDataVocabularies("All my words.xlsx"));
        vsData.addAll(Test.readDataVocabularies("All my words part laban clean.xlsx"));
        HashSet<String> vocabs = new HashSet<>();
        HashSet<String> cmost = new HashSet<>();

        ArrayList<String[]> most = new ArrayList<>();

//        for(var i: most){
//            cmost.add(i[0]);
//        }
        System.out.println(vocabs.size());
        System.out.println(cmost.size());
        System.out.println(most.size());
        Test.writeDataVocabularies(most, "All words more.xlsx");
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
        var oxfordVs = Oxford.readOxfordVocabularies();
        System.out.println("Oxford words: " + oxfordVs.size());

        var cambridgeOnVs = Cambridge.readOnlineWords();
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
        String directoryPath = "Oxford wordlists";
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
//                        if (lv.equals("a1") || lv.equals("a2") || lv.equals("b1") || lv.equals("b2")) {
                        if (!plist.containsKey(cellValue)) {
                            plist.put(cellValue, new HashSet<>());
                        }
                        plist.get(cellValue).add(cellValue1.toLowerCase());
//                        }
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

    public static void downloadOxford3000And5000(String html, String path) {
        Document document1 = Jsoup.parse(html);
        Element ulElement = document1.selectFirst("ul.top-g");
        if (ulElement != null) {
            Elements lis = ulElement.getElementsByTag("li");
            for (Element li : lis) {
                if (li.hasClass("hidden")) {
//                    System.out.println(li.text() + "\t" + "phrase" + "\t" + "");
                    continue; // Nếu có, bỏ qua phần tử này và tiếp tục với phần tử tiếp theo
                }
                var a = li.getElementsByTag("a").get(0);
                var english = a.text();
                String meaningURL = "https://www.oxfordlearnersdictionaries.com/" + a.attr("href");
                Elements soundDivs = li.select("div.sound");
                String ukmp3 = null;
                String usmp3 = null;
                for (Element soundDiv : soundDivs) {
                    String mp3Src = soundDiv.attr("data-src-mp3");
                    // Kiểm tra thẻ có lớp "pron-uk"
                    if (soundDiv.hasClass("pron-uk")) {
                        ukmp3 = "https://www.oxfordlearnersdictionaries.com" + mp3Src;  // Gán đường dẫn MP3 của UK
                    }
                    // Kiểm tra thẻ có lớp "pron-us"
                    else if (soundDiv.hasClass("pron-us")) {
                        usmp3 = "https://www.oxfordlearnersdictionaries.com" + mp3Src;  // Gán đường dẫn MP3 của US
                    }
                }
//                System.out.println(ukmp3);
//                System.out.println(usmp3);
                var noun = "phrase";
                try {
                    noun = li.getElementsByClass("pos").get(0).text();
                    var lv = li.getElementsByClass("belong-to").get(0).text();
                    System.out.println(english + "\t" + noun + "\t" + lv + "\t" + meaningURL + "\t" + ukmp3 + "\t" + usmp3);
//                    addLineToExcell(path + "/list.xlsx", english, noun, lv);
                } catch (Exception e) {
                    System.out.println(english + "\t" + noun + "\t" + "" + "\t" + meaningURL + "\t" + ukmp3 + "\t" + usmp3);
//
                }

            }
        } else {
            System.out.println("Không tìm thấy phần tử ul với class 'top-g'.");
        }

    }

    public static void downloadOxfordAcademic(String html, String path) {
        Document document1 = Jsoup.parse(html);
        Element ulElement = document1.selectFirst("ul.top-g");
        if (ulElement != null) {
            Elements lis = ulElement.getElementsByTag("li");
            for (Element li : lis) {
                try {
                    if (li.hasClass("hidden")) {
                        System.out.println(li.text() + "\t" + "phrase" + "\t" + "");
                        continue; // Nếu có, bỏ qua phần tử này và tiếp tục với phần tử tiếp theo
                    }
                    var a = li.getElementsByTag("a").get(0);
                    var english = a.text();
                    var noun = "phrase";
                    try {
                        noun = li.getElementsByClass("pos").get(0).text();
                    } catch (Exception e) {

                    }

                    var lv = "";
//                    var lv = li.getElementsByClass("belong-to").get(0).text();
                    System.out.println(english + "\t" + noun + "\t" + lv);
//                    addLineToExcell(path + "/list.xlsx", english, noun, lv);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(li);
                    System.out.println(li.text());
                    return;
                }
            }
        } else {
            System.out.println("Không tìm thấy phần tử ul với class 'top-g'.");
        }

    }


    public static void downloadOxfordOxford3000And5000() {
        String directoryPath = "Oxford topics";
        String topicName = "Oxford Phrasal Academic Lexicon Writing 1-10";
        downloadOxford3000And5000(Test.readFile(topicName + ".txt"), directoryPath + "/" + topicName);
//        topicName = "Oxford Phrasal Academic Lexicon";
//        downloadOxfordAcademic(Test.readFile(topicName + ".txt"), directoryPath + "/" + topicName);
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

        // Kiểm tra và tạo thư mục nếu chưa tồn tại
        File parentDir = excelFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (parentDir.mkdirs()) {
                System.out.println("Thư mục đã được tạo: " + parentDir.getAbsolutePath());
            } else {
                System.out.println("Không thể tạo thư mục!");
                return;
            }
        }

        try {
            if (excelFile.exists()) {
                // Đọc tệp Excel nếu nó tồn tại
                try (FileInputStream fileInputStream = new FileInputStream(excelFile)) {
                    workbook = new XSSFWorkbook(fileInputStream);
                    sheet = workbook.getSheetAt(0);
                }
            } else {
                // Tạo tệp Excel mới nếu nó không tồn tại
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
            }

            // Ghi lại nội dung vào file
            try (FileOutputStream fileOutputStream = new FileOutputStream(excelFilePath)) {
                workbook.write(fileOutputStream);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Đảm bảo đóng workbook để giải phóng tài nguyên
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static File[] getExcelFiles(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Thư mục không tồn tại hoặc không phải là thư mục: " + directoryPath);
            return null;
        }

        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".xlsx"));
        if (files == null || files.length == 0) {
            System.out.println("Không có tệp Excel nào trong thư mục: " + directoryPath);
        }
        return files;
    }

    public static List<Workbook> readExcelFiles(String directoryPath) {
        List<Workbook> workbooks = new ArrayList<>();
        var files = getExcelFiles(directoryPath);
        for (File file : files) {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                Workbook workbook = new XSSFWorkbook(fileInputStream);
                workbooks.add(workbook);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(file.getAbsolutePath());
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
