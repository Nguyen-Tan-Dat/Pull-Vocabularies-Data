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

import static org.apache.commons.lang3.StringUtils.capitalize;

public class Oxford {
    static void main() {
//        createTopicsOfOxfordExcel();
//        createWordListsExcel();
        createTopics();
//        createEmptyData();
    }


    public static void createEmptyData() {
        var words = allwords(); // Hàm bạn đã có sẵn
        String outputFile = "Oxford_Level_A1.xlsx";
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Oxford Levels");
            int rowIndex = 0;
            for (var en : words.keySet()) {
                var posMap = words.get(en);
                for (var pos : posMap.keySet()) {
                    var levels = posMap.get(pos);
                    for (var lv : levels) {
                        if (lv.equals("a1")||lv.equals("a2")) continue;
                        Row row = sheet.createRow(rowIndex++);
                        row.createCell(2).setCellValue(en);
                        row.createCell(3).setCellValue("none");
                        row.createCell(4).setCellValue(pos);
                    }
                }
            }
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, HashMap<String, HashSet<String>>> allwords() {
        String pathRoot = "Topics of Oxford";
        ArrayList<String> excelFiles = listExcelFiles(pathRoot);

        // Map chính: English → { parts_of_speech → { levels } }
        HashMap<String, HashMap<String, HashSet<String>>> wordsMap = new HashMap<>();

        for (String file : excelFiles) {
            try (Workbook workbook = new XSSFWorkbook(file)) {
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    Sheet sheet = workbook.getSheetAt(i);

                    for (Row row : sheet) {
                        try {
                            String english = row.getCell(0).getStringCellValue().trim();
                            String pos = row.getCell(1).getStringCellValue().toLowerCase().trim();
                            String lv = "";
                            try {
                                lv = row.getCell(2).getStringCellValue().toLowerCase().trim();
                            } catch (Exception ignored) {
                            }

                            if (english.isEmpty() || pos.isEmpty()) continue;

                            // Nếu chưa có từ trong map, tạo HashMap mới
                            wordsMap.putIfAbsent(english, new HashMap<>());

                            // Nếu chưa có pos trong từ đó → tạo HashSet mới
                            wordsMap.get(english).putIfAbsent(pos, new HashSet<>());

                            // Thêm cấp độ nếu không rỗng
                            if (!lv.isEmpty()) {
                                wordsMap.get(english).get(pos).add(lv);
                            }

                        } catch (Exception e) {
                            System.out.println("Lỗi đọc dữ liệu từ file: " + file);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return wordsMap;
    }


    public static ArrayList<String> listExcelFiles(String pathRoot) {
        ArrayList<String> excelFiles = new ArrayList<>();
        Path rootPath = Paths.get(pathRoot);
        try {
            Files.walkFileTree(rootPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().endsWith(".xlsx") || file.toString().endsWith(".xls")) {
                        excelFiles.add(file.toAbsolutePath().toString());
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
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

    public static String pathToTopic(String root, String path) {
        String[] parts = path.split("\\\\");
        int startIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equalsIgnoreCase(root)) {
                startIndex = i + 1;
                break;
            }
        }
        if (startIndex == -1 || startIndex >= parts.length) {
            return "";
        }
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
        HashMap<String, HashSet<String[]>> partOfSpeechMap = new HashMap<>();
        String[] partsOfSpeech = {
                "indefinite article", "preposition", "adverb", "noun", "verb", "adjective",
                "pronoun", "determiner", "conjunction", "auxiliary verb", "exclamation",
                "modal verb", "number", "ordinal number", "definite article",
                "infinitive marker", "phrase", "linking verb"
        };
        for (String pos : partsOfSpeech) {
            partOfSpeechMap.put(pos, new HashSet<>());
        }
        HashSet<String[]> a1 = new HashSet<>();
        HashSet<String[]> a2 = new HashSet<>();
        HashSet<String[]> b1 = new HashSet<>();
        HashSet<String[]> b2 = new HashSet<>();
        HashSet<String[]> c1 = new HashSet<>();
        HashSet<String[]> c2 = new HashSet<>();
        HashSet<String[]> all = new HashSet<>();
        ArrayList<Object> data = new ArrayList<>();

        for (String file : excelFiles) {
            var name = pathToTopic(pathRoot, file);
            String rootname = "";
            HashSet<String[]> list = new HashSet<>();
            try (Workbook workbook = new XSSFWorkbook(file)) {
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    for (Row row : sheet) {
                        try {
                            String english = row.getCell(0).getStringCellValue().trim();
                            String parts_of_speech = row.getCell(1).getStringCellValue().toLowerCase();
                            String lv = "";
                            try {
                                lv = row.getCell(2).getStringCellValue().toLowerCase();
                            } catch (Exception _) {
                            }
//                            if (!lv.equals("b1")
//                            ) continue;
                            if (!ens.contains(english)) {
                                System.out.println(english + "\t" + parts_of_speech + "\t" + lv);
                            }
                            if (lv.equals("")) System.out.println(english + "\t" + parts_of_speech + "\t" + lv);
                            switch (lv) {
                                case "a1" -> a1.add(new String[]{english, parts_of_speech});
                                case "a2" -> a2.add(new String[]{english, parts_of_speech});
                                case "b1" -> b1.add(new String[]{english, parts_of_speech});
                                case "b2" -> b2.add(new String[]{english, parts_of_speech});
                                case "c1" -> c1.add(new String[]{english, parts_of_speech});
                                case "c2" -> c2.add(new String[]{english, parts_of_speech});
                            }
                            if (partOfSpeechMap.containsKey(parts_of_speech)) {
                                partOfSpeechMap.get(parts_of_speech).add(new String[]{english, parts_of_speech});
                            }
                            all.add(new String[]{english, parts_of_speech});
                            rootname = name.split(">")[0].trim();
//                            if (lv.contains("a"))
                                list.add(new String[]{english, parts_of_speech});
                        } catch (Exception e) {
                            System.out.println("Lỗi ở file: " + file);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!list.isEmpty()) {
                HashMap<String, Object> row = new HashMap<>();
                row.put("name", name );
                row.put("vs", list);
                data.add(row);
            }
            HashMap<String, Object> row1 = new HashMap<>();
            row1.put("name", rootname );
            row1.put("vs", list);
            data.add(row1);
            if (!rootname.startsWith("Word Lists")) {
                HashMap<String, Object> row2 = new HashMap<>();
                row2.put("name", "Oxford all topics");
                row2.put("vs", list);
                data.add(row2);
            } else {
                HashMap<String, Object> row2 = new HashMap<>();
                row2.put("name", "Oxford Word Lists");
                row2.put("vs", list);
                data.add(row2);
            }
        }
        data.add(Map.of("name", "A1", "vs", a1));
        data.add(Map.of("name", "A2", "vs", a2));
        data.add(Map.of("name", "B1", "vs", b1));
        data.add(Map.of("name", "B2", "vs", b2));
        data.add(Map.of("name", "C1", "vs", c1));
        data.add(Map.of("name", "C2", "vs", c2));
        for (var entry : partOfSpeechMap.entrySet()) {
            data.add(Map.of("name", capitalize(entry.getKey()), "vs", entry.getValue()));
        }
        data.add(Map.of("name", "Oxford all", "vs", all));
        Test.writeTopics(data, "Oxford all");
    }

    public static void writeVocabulariesToExcel(String path, List<HashMap<String, String>> vocabularies) {
        // Tạo workbook và sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Vocabularies");

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
        Document doc = Jsoup.parse(SeleniumHelper.getHTML(url));
        Elements vocabElements = doc.select("ul.top-g > li");

        for (Element vocabElement : vocabElements) {
            // Bỏ qua nếu có class hoặc element 'hidden'
            if (vocabElement.hasClass("hidden") || !vocabElement.select(".hidden").isEmpty()) {
                continue;
            }

            Element aTag = vocabElement.selectFirst("a");
            if (aTag == null) continue;

            String word = aTag.text().trim();
            if (word.isEmpty()) continue;

            HashMap<String, String> vocab = new HashMap<>();
            vocab.put("word", word);
            vocab.put("partOfSpeech", getTextOrEmpty(vocabElement, "span.pos"));
            vocab.put("level", getTextOrEmpty(vocabElement, "span.belong-to"));
            vocab.put("wordLink", "https://www.oxfordlearnersdictionaries.com" + aTag.attr("href"));
            vocab.put("mp3UK", getAttrOrEmpty(vocabElement, "div.pron-uk"));
            vocab.put("mp3US", getAttrOrEmpty(vocabElement, "div.pron-us"));

            vocabularies.add(vocab);
        }

        return vocabularies;
    }

    // Helper methods
    private static String getTextOrEmpty(Element element, String cssQuery) {
        Element found = element.selectFirst(cssQuery);
        return found != null ? found.text().trim() : "";
    }

    private static String getAttrOrEmpty(Element element, String cssQuery) {
        Element found = element.selectFirst(cssQuery);
        return found != null ? found.attr("data-src-mp3").trim() : "";
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

    public static void createDirectory(String parent, String name) {
        File directory = new File(parent, name);
        if (directory.exists()) {
            return;
        }
        boolean created = directory.mkdirs();
        if (created) {
            System.out.println("Directory created: " + directory.getAbsolutePath());
        } else {
            System.out.println("Failed to create directory: " + directory.getAbsolutePath());
        }
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
                                var a = li.getElementsByTag("a").getFirst();
                                var english = a.text();
                                var noun = li.getElementsByClass("pos").getFirst().text();
                                var lv = li.getElementsByClass("belong-to").getFirst().text();
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

    public static void createOxford3000and5000Excel(String html, String path) {
        Document document = Jsoup.parse(html);
        Element ulElement = document.selectFirst("ul.top-g");

        if (ulElement == null) {
            System.out.println("Không tìm thấy phần tử ul với class 'top-g'.");
            return;
        }

        // Create directory if not exists
        File dir = new File(path);
        if (!dir.exists()) dir.mkdirs();

        List<String[]> all = new ArrayList<>();
        Map<String, List<String[]>> levelMap = new HashMap<>();
        levelMap.put("a1", new ArrayList<>());
        levelMap.put("a2", new ArrayList<>());
        levelMap.put("b1", new ArrayList<>());
        levelMap.put("b2", new ArrayList<>());
        levelMap.put("c1", new ArrayList<>());
        levelMap.put("c2", new ArrayList<>());

        Elements lis = ulElement.getElementsByTag("li");

        for (Element li : lis) {
            if (li.hasClass("hidden")) continue;
            String english = Objects.requireNonNull(li.selectFirst("a")).text();
            String noun = "phrase";
            String lv = "";
            String meaningURL = "https://www.oxfordlearnersdictionaries.com" + li.selectFirst("a").attr("href");

            String ukmp3 = null;
            String usmp3 = null;

            for (Element soundDiv : li.select("div.sound")) {
                String mp3Src = soundDiv.attr("data-src-mp3");
                if (soundDiv.hasClass("pron-uk")) {
                    ukmp3 = "https://www.oxfordlearnersdictionaries.com" + mp3Src;
                } else if (soundDiv.hasClass("pron-us")) {
                    usmp3 = "https://www.oxfordlearnersdictionaries.com" + mp3Src;
                }
            }

            try {
                noun = li.getElementsByClass("pos").getFirst().text();
                lv = li.getElementsByClass("belong-to").getFirst().text();
            } catch (Exception ignored) {
            }

            String[] row = {english, noun, lv, meaningURL, ukmp3, usmp3};
            all.add(row);
            if (levelMap.containsKey(lv)) {
                levelMap.get(lv).add(row);
            }
            if (lv.equals(""))
                System.out.println(english + "\t" + noun + "\t" + lv);
        }
        writeListToExcel(path + "/All.xlsx", all);
        for (Map.Entry<String, List<String[]>> entry : levelMap.entrySet()) {
            String level = entry.getKey();
            List<String[]> rows = entry.getValue();
            if (!rows.isEmpty()) {
                writeListToExcel(path + "/" + level.toUpperCase() + ".xlsx", rows);
            }
        }
    }

    public static void writeListToExcel(String excelFilePath, List<String[]> rows) {
        try {
            File file = new File(excelFilePath);
            File parentDir = file.getParentFile();

            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs(); // tạo thư mục nếu chưa có
            }

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Topics");

            int rowNum = 0;
            for (String[] data : rows) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < data.length; i++) {
                    row.createCell(i).setCellValue(data[i]);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addLineToExcelByLevel(String basePath, String english, String pos, String level) {
        String allFile = basePath + "/All.xlsx";
        addLineToExcell(allFile, english, pos, level);

        if (level != null && !level.isEmpty()) {
            String levelFile = basePath + "/" + level.toUpperCase() + ".xlsx";
            addLineToExcell(levelFile, english, pos, level);
        }
    }

    public static void createOxfordPhraseListExcel(HashMap<String, HashMap<String, HashSet<String>>> words, String html, String path) {
        Document document = Jsoup.parse(html);
        Element ulElement = document.selectFirst("ul.top-g");

        if (ulElement == null) {
            System.out.println("Không tìm thấy phần tử ul với class 'top-g'.");
            return;
        }

        Elements lis = ulElement.getElementsByTag("li");

        for (Element li : lis) {
            try {
                if (li.hasClass("hidden")) continue;

                var a = li.getElementsByTag("a").getFirst();
                String english = a.text().trim();
                String pos = "phrase";
                String level = li.getElementsByClass("belong-to").getFirst().text().trim().toLowerCase();
                if (words.containsKey(english)) {
                    for (var p : words.get(english).keySet()) {
                        for (var lv : words.get(english).get(p))
                            if (level.equals(lv)) {
                                addLineToExcelByLevel(path, english, p, level);
                            }
                    }
                } else {
                    addLineToExcelByLevel(path, english, pos, level);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("✅ Hoàn thành tạo danh sách phrase theo cấp độ.");
    }

    public static void createOPALExcel(HashMap<String, HashMap<String, HashSet<String>>> words, String html, String path) {
        Document document = Jsoup.parse(html);
        Element ulElement = document.selectFirst("ul.top-g");

        if (ulElement == null) {
            System.out.println("Không tìm thấy phần tử ul với class 'top-g'.");
            return;
        }

        Elements lis = ulElement.getElementsByTag("li");

        for (Element li : lis) {
            try {
                if (li.hasClass("hidden")) continue;
                var a = li.getElementsByTag("a").getFirst();
                String english = a.text().trim();
                var level = "b1";
                var parts_of_speech = "phrase";
                try {
                    parts_of_speech = li.getElementsByClass("pos").getFirst().text();
                } catch (Exception _) {
                }
                if (words.containsKey(english)) {
                    if (words.get(english).get(parts_of_speech) != null) {
                        for (var lv : words.get(english).get(parts_of_speech))
                            addLineToExcelByLevel(path, english, parts_of_speech, lv);
                    } else {
                        for (var p : words.get(english).keySet()) {
                            for (var lv : words.get(english).get(p))
                                addLineToExcelByLevel(path, english, p, lv);
                        }
                    }
                } else {
                    addLineToExcelByLevel(path, english, parts_of_speech, level);
                }

            } catch (Exception e) {
                System.out.println("Lỗi xử lý phrase: " + li);
                e.printStackTrace();
            }
        }
    }

    public static void createPhraseOPALExcel(HashMap<String, HashMap<String, HashSet<String>>> words, String html, String path) {
        Document document = Jsoup.parse(html);
        Element ulElement = document.selectFirst("ul.top-g");

        if (ulElement == null) {
            System.out.println("Không tìm thấy phần tử ul với class 'top-g'.");
            return;
        }

        Elements lis = ulElement.getElementsByTag("li");

        for (Element li : lis) {
            try {
                if (li.hasClass("hidden")) continue;

                var a = li.getElementsByTag("a").getFirst();
                String english = a.text().trim();
                String level = "abc";
                var parts_of_speech = li.getElementsByClass("pos").getFirst().text();
                if (words.containsKey(english)) {
                    if (words.get(english).get(parts_of_speech) != null) {
                        for (var lv : words.get(english).get(parts_of_speech))
                            addLineToExcelByLevel(path, english, parts_of_speech, lv);
                    } else {
                        addLineToExcelByLevel(path, english, parts_of_speech, level);
                        System.out.println(english + "\t" + level + "\t" + level);
                    }
                } else {
                    addLineToExcelByLevel(path, english, parts_of_speech, level);
                    System.out.println(english + "\t" + level + "\t" + parts_of_speech);
                }

            } catch (Exception e) {
                System.out.println("Lỗi xử lý phrase: " + li);
                e.printStackTrace();
            }
        }
    }

    public static void createWordListsExcel() {
        String directoryPath = "Topics of Oxford/Word Lists";
        createOxford3000and5000Excel(Test.readFile("Oxford Word Lists data/Oxford 3000.txt"), directoryPath + "/" + "Oxford 3000");
        createOxford3000and5000Excel(Test.readFile("Oxford Word Lists data/Oxford 5000.txt"), directoryPath + "/" + "Oxford 5000");
        var words = allwords();
        createOPALExcel(words, Test.readFile("Oxford Word Lists data/OPAL written words.txt"), directoryPath + "/" + "OPAL written words");
        createOPALExcel(words, Test.readFile("Oxford Word Lists data/OPAL written phrases.txt"), directoryPath + "/" + "OPAL written phrases");
        createOPALExcel(words, Test.readFile("Oxford Word Lists data/OPAL spoken words.txt"), directoryPath + "/" + "OPAL spoken words");
        createOPALExcel(words, Test.readFile("Oxford Word Lists data/OPAL spoken phrases.txt"), directoryPath + "/" + "OPAL spoken phrases");
        createOPALExcel(words, Test.readFile("Oxford Word Lists data/OPAL Academic written words.txt"), directoryPath + "/" + "OPAL Academic written words");
        createOPALExcel(words, Test.readFile("Oxford Word Lists data/OPAL Academic written phrases.txt"), directoryPath + "/" + "OPAL Academic written phrases");
        createOPALExcel(words, Test.readFile("Oxford Word Lists data/OPAL Academic spoken words.txt"), directoryPath + "/" + "OPAL Academic spoken words");
        createOPALExcel(words, Test.readFile("Oxford Word Lists data/OPAL Academic spoken phrases.txt"), directoryPath + "/" + "OPAL Academic spoken phrases");
    }


    public static void addLineToExcell(String excelFilePath, String en, String noun, String lv) {
        File excelFile = new File(excelFilePath);
        Workbook workbook = null;
        Sheet sheet;

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
//            for (Row row : sheet) {
//                Cell cell = row.getCell(0);
//                Cell cell1 = row.getCell(1);
//                if (cell != null && cell.getStringCellValue().equals(en) && cell1.getStringCellValue().equals(noun)) {
//                    exists = true;
//                    break;
//                }
//            }

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
        assert files != null;
        for (File file : files) {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                Workbook workbook = new XSSFWorkbook(fileInputStream);
                workbooks.add(workbook);
            } catch (Exception e) {
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

    public static void createTopicsOfOxfordExcel() {
        SeleniumHelper.setup();
        var root = "Topics of Oxford";
        createDirectory(System.getProperty("user.dir"), root);
        root = System.getProperty("user.dir") + "/" + root;
        var topics = getRootTopics();
        for (String name : topics.keySet()) {
            createDirectory(root, name);
            System.out.println(name);
            var secondTopics = getSecondTopics(topics.get(name));
            for (var sn : secondTopics.keySet()) {
                createDirectory(root + "/" + name, sn);
                for (var tn : secondTopics.get(sn).keySet()) {
                    if (!tn.equals("All"))
                        System.out.println(name + " > " + sn + " > " + tn);
                    var path = root + "/" + name + "/" + sn + "/" + tn.replaceAll("/", " or ") + ".xlsx";
                    var vocabularies = getVocabularies(secondTopics.get(sn).get(tn));
                    writeVocabulariesToExcel(path, vocabularies);
                }
            }
        }
    }
}
