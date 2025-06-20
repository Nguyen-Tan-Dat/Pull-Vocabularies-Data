package example;

import java.io.*;
import java.nio.file.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.*;

public class Cambridge {
    public static void main(String[] args) {
//        var cams = readOnlineWords();
//        System.out.println(cams.size());
//        writeTopics();
//        vocabulariesElementary();
//        writeEnglishVocabulariesForIELTS(1,21);
        writeElementaryTopics();
        writePreIntermediateTopics();
        writeUpperIntermediateTopics();
        writeAdvancedTopics();
//        writeIELTSTopics();
    }

    private static void writeAdvancedTopics() {
        var ens = Test.databaseEnglish();
        String filePath = "Cambridge vocabularies in use data/Advanced.txt";
        HashMap<String, Set<String>> vocabMap = readAdvancedVocabularies(filePath);
        HashSet<String> exist = new HashSet<>();
        HashSet<String> unexist = new HashSet<>();
        ArrayList<Object> data = new ArrayList<>();
        int count = 0;
        for (var i : vocabMap.keySet()) {
            for (var j : vocabMap.get(i)) {
                if (!ens.contains(j)) {
                    unexist.add(j);
                } else {
                    exist.add(j);
                }
            }
            HashMap<String, Object> row = new HashMap<>();
            row.put("name", i);
            row.put("vs", vocabMap.get(i));
            data.add(row);
            count += vocabMap.get(i).size();
        }
//        System.out.println(count);
//        System.out.println("Có " + unexist.size() + " từ không có trong database");
        for (var i : unexist) {
            System.out.println(i);
        }
//        HashMap<String, Object> row = new HashMap<>();
//        row.put("name", "Advanced");
//        row.put("vs", exist);
//        data.add(row);
//        Test.writeTopics(data, "Vocabulary in use Advanced");
    }

    private static void writeUpperIntermediateTopics() {
        var ens = Test.databaseEnglish();
        String filePath = "Cambridge vocabularies in use data/Upper-Intermediate.txt";
        HashMap<String, Set<String>> vocabMap = readUpperIntermediateVocabularies(filePath);
        HashSet<String> exist = new HashSet<>();
        HashSet<String> unexist = new HashSet<>();
        ArrayList<Object> data = new ArrayList<>();
        int count = 0;
        for (var i : vocabMap.keySet()) {
            for (var j : vocabMap.get(i)) {
                if (!ens.contains(j)) {
                    unexist.add(j);
                } else {
                    exist.add(j);
                }
            }
            HashMap<String, Object> row = new HashMap<>();
            row.put("name", i);
            row.put("vs", vocabMap.get(i));
            data.add(row);
            count += vocabMap.get(i).size();
        }
//        System.out.println(count);
//        System.out.println("Có " + unexist.size() + " từ không có trong database");
        for (var i : unexist) {
            System.out.println(i);
        }
//        HashMap<String, Object> row = new HashMap<>();
//        row.put("name", "Upper-intermediate");
//        row.put("vs", exist);
//        data.add(row);
//        Test.writeTopics(data, "Vocabulary in use Upper-intermediate");
    }

    private static void writePreIntermediateTopics() {
        var ens = Test.databaseEnglish();
        String filePath = "Cambridge vocabularies in use data/Pre-Intermediate.txt";
        HashMap<String, Set<String>> vocabMap = readPreIntermediateVocabularies(filePath);

        HashSet<String> exist = new HashSet<>();
        HashSet<String> unexist = new HashSet<>();
        ArrayList<Object> data = new ArrayList<>();
        int count = 0;
        for (var i : vocabMap.keySet()) {
//            if(!i.contains("Pre-Intermediate > Unit 046"))continue;
            for (var j : vocabMap.get(i)) {
                if (!ens.contains(j)) {
                    unexist.add(j);
                } else {
                    exist.add(j);
                }

            }
            HashMap<String, Object> row = new HashMap<>();
            row.put("name", i);
            row.put("vs", vocabMap.get(i));
            data.add(row);
            count += vocabMap.get(i).size();
        }
//        System.out.println(exist.size());
//        System.out.println("Có " + unexist.size() + " từ không có trong database");
        for (var i : unexist) {
            System.out.println(i);
        }
//        HashMap<String, Object> row = new HashMap<>();
//        row.put("name", "Pre-Intermediate");
//        row.put("vs", exist);
//        data.add(row);
//        Test.writeTopics(data, "Vocabulary in use Pre-Intermediate 46");
    }

    public static HashMap<String, Set<String>> readElementaryVocabularies(String filePath) {
        HashMap<String, Set<String>> unitMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String word, pronunciation, unit;
            while ((word = br.readLine()) != null &&
                    (pronunciation = br.readLine()) != null &&
                    (unit = br.readLine()) != null) {
                try {
                    String unitName = parseTopic("Elementary", Integer.parseInt(unit));
                    if (!unitMap.containsKey(unitName)) {
                        unitMap.put(unitName, new HashSet<>());
                    }
                    word = word.replaceAll("'", "'");
                    unitMap.get(unitName).add(word);
                } catch (Exception e) {
                    var n = unit.split(",");
                    for (var i : n) {
                        try {
                            String unitName = parseTopic("Elementary", Integer.parseInt(i.trim()));
                            if (!unitMap.containsKey(unitName)) {
                                unitMap.put(unitName, new HashSet<>());
                            }
                            word = word.replaceAll("'", "'");
                            word = word.replaceAll("…", "...");
                            unitMap.get(unitName).add(word);
                        } catch (Exception e1) {
                            System.out.println(unit);
                            return null;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return unitMap;
    }


    public static HashMap<String, Set<String>> readUpperIntermediateVocabularies(String filePath) {
        HashMap<String, Set<String>> unitMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                var data = line.split("/");
                if (data.length == 3) {
                    String word = data[0].trim();
                    String pronunciation = data[1];
                    String unit = data[2];
                    try {
                        String unitName = parseTopic("Upper-intermediate", Integer.parseInt(unit));
                        if (!unitMap.containsKey(unitName)) {
                            unitMap.put(unitName, new HashSet<>());
                        }
                        unitMap.get(unitName).add(word);
                    } catch (Exception e) {
                        var n = unit.split(",");
                        for (var i : n) {
                            try {
                                String unitName = parseTopic("Upper-intermediate", Integer.parseInt(i.trim()));
                                if (!unitMap.containsKey(unitName)) {
                                    unitMap.put(unitName, new HashSet<>());
                                }
                                unitMap.get(unitName).add(word);
                            } catch (Exception e1) {
                                System.out.println(unit);
                                return null;
                            }
                        }
                    }
                } else {
                    System.out.println(line);
                    return unitMap;
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return unitMap;
    }

    public static HashMap<String, Set<String>> readAdvancedVocabularies(String filePath) {
        HashMap<String, Set<String>> unitMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String word, unit;
                if (line.contains("/")) {
                    // Trường hợp thông thường: "word / pronunciation / unit"
                    var data = line.split("/");
                    if (data.length == 3) {
                        word = data[0].trim();
                        unit = data[2].trim();
                    } else {
                        System.out.println("Lỗi định dạng: " + line);
                        continue;
                    }
                } else {
                    // Trường hợp thiếu phát âm: "word unit"
                    int lastSpaceIndex = line.lastIndexOf(" ");
                    if (lastSpaceIndex == -1) {
                        System.out.println("Lỗi định dạng: " + line);
                        continue;
                    }
                    word = line.substring(0, lastSpaceIndex).trim();
                    unit = line.substring(lastSpaceIndex + 1).trim();
                }

                try {
                    // Xử lý unit (có thể là một hoặc nhiều unit cách nhau bởi dấu ",")
                    for (String u : unit.split(",")) {
                        try {
                            String unitName = parseTopic("Advanced", Integer.parseInt(u.trim()));
                            unitMap.computeIfAbsent(unitName, k -> new HashSet<>()).add(word);
                        } catch (NumberFormatException e1) {
                            System.out.println("Lỗi unit: " + unit);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Lỗi khi xử lý unit: " + unit);
                    return null;
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return unitMap;
    }

    public static HashMap<String, Set<String>> readIntermediateVocabularies(String filePath) {
        HashMap<String, Set<String>> unitMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String word, pronunciation, unit;
            while ((word = br.readLine()) != null &&
                    (pronunciation = br.readLine()) != null &&
                    (unit = br.readLine()) != null) {
                try {
                    String unitName = parseTopic("Intermediate", Integer.parseInt(unit));
                    if (!unitMap.containsKey(unitName)) {
                        unitMap.put(unitName, new HashSet<>());
                    }
                    unitMap.get(unitName).add(word);
                } catch (Exception e) {
                    var n = unit.split(",");
                    for (var i : n) {
                        try {
                            String unitName = parseTopic("Intermediate", Integer.parseInt(i.trim()));
                            if (!unitMap.containsKey(unitName)) {
                                unitMap.put(unitName, new HashSet<>());
                            }
                            unitMap.get(unitName).add(word);
                        } catch (Exception e1) {
                            System.out.println(unit);
                            return null;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return unitMap;
    }

    public static HashMap<String, Set<String>> readPreIntermediateVocabularies(String filePath) {
        HashMap<String, Set<String>> unitMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String word, pronunciation, unit;
            while ((word = br.readLine()) != null &&
                    (pronunciation = br.readLine()) != null &&
                    (unit = br.readLine()) != null) {
                try {
                    String unitName = parseTopic("Pre-Intermediate", Integer.parseInt(unit));
                    if (!unitMap.containsKey(unitName)) {
                        unitMap.put(unitName, new HashSet<>());
                    }
                    unitMap.get(unitName).add(word);
                } catch (Exception e) {
                    var n = unit.split(",");
                    for (var i : n) {
                        try {
                            String unitName = parseTopic("Pre-Intermediate", Integer.parseInt(i.trim()));
                            if (!unitMap.containsKey(unitName)) {
                                unitMap.put(unitName, new HashSet<>());
                            }
                            unitMap.get(unitName).add(word);
                        } catch (Exception e1) {
                            System.out.println(unit);
                            return null;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return unitMap;
    }

    public static String parseTopic(String root, int unitNumber) {
        return String.format(root + " > Unit %03d", unitNumber);
    }

    private static void writeTopics() {
        ArrayList<Object> data = new ArrayList<>();
        File[] subdirectories = Oxford.getDirectories("Cambridge word lists");

        for (File subdirectory : subdirectories) {
            var files = Oxford.getExcelFiles(subdirectory.getAbsolutePath());
            for (File file : files) {
                HashSet<String[]> list = new HashSet<>();
                var topic = Oxford.pathToTopicName(file.getAbsolutePath());
                if (topic.contains("Advanced") || topic.contains("Intermediate")) {
                    continue;
                }
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    Workbook workbook = new XSSFWorkbook(fileInputStream);
                    for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                        Sheet sheet = workbook.getSheetAt(i);
                        for (Row row : sheet) {
                            String english = row.getCell(0).getStringCellValue();
                            String parts_of_speech = row.getCell(1).getStringCellValue().toLowerCase();
                            if (parts_of_speech.equals("")) {
                                parts_of_speech = "phrase";
//                                continue;
                            }
                            list.add(new String[]{english, parts_of_speech});
                        }
                    }
                    workbook.close();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                HashMap<String, Object> row = new HashMap<>();
                row.put("name", topic);
                row.put("vs", list);
                data.add(row);
                System.out.println(topic);
            }
        }

        Test.writeTopics(data, "Topics of Cambridge");
    }

    public static HashMap<String, HashSet<String>> readOnlineWords() {
        HashMap<String, HashSet<String>> plist = new HashMap<>();
        for (int number = 1; number <= 1; number++) {
            String directoryPath = "Cambridge Vocabularies/" + number;
            Path path = Paths.get(directoryPath);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.xlsx")) {
                for (Path filePath : stream) {
                    try (FileInputStream fis = new FileInputStream(filePath.toString()); Workbook workbook = new XSSFWorkbook(fis)) {
                        Sheet sheet = workbook.getSheetAt(0);
                        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                            Row row = sheet.getRow(i);
                            if (row != null) {
                                Cell cell = row.getCell(0);
                                Cell cell1 = row.getCell(1);
//                                if (cell != null) {
                                // Lấy giá trị của ô
                                String cellValue = Test.getCellValueAsString(cell);
                                String cellValue1 = Test.getCellValueAsString(cell1);
                                cellValue = cellValue.trim();
                                cellValue1 = cellValue1.trim();
//                                    list.add(cellValue);
                                if (!plist.containsKey(cellValue)) {
                                    plist.put(cellValue, new HashSet<>());
//                                        plist.get(cellValue).add("");
                                }
                                plist.get(cellValue).add(cellValue1.toLowerCase());

//                                }
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException | DirectoryIteratorException e) {
                System.err.println("Lỗi khi đọc thư mục: " + e.getMessage());
            }
        }
        return plist;
    }


    public static void writeIELTSTopics() {
        // Đường dẫn đến file từ vựng
        String filePath = "Cambridge vocabularies in use/Vocabularies For IELTS.txt";
        HashSet<String> list = new HashSet<>();
        try {
            Map<String, HashSet<String>> vocabularyByLesson = extractVocabularyForIELTSByLesson(filePath);

            // In danh sách từ vựng theo từng chủ đề
            for (Map.Entry<String, HashSet<String>> entry : vocabularyByLesson.entrySet()) {
                System.out.println(entry.getKey().replaceFirst(":", "").toUpperCase());

                Test.writeTopic(entry.getKey().replaceFirst(":", "").toUpperCase(), entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, HashSet<String>> extractVocabularyForIELTSByLesson(String filePath) throws IOException {
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

    public static Set<String> all() {
        HashSet<String> list = new HashSet<>();
        var ens = Test.databaseEnglish();
        String filePath = "Cambridge vocabularies in use data/Elementary.txt";
        HashMap<String, Set<String>> vocabMap = readElementaryVocabularies(filePath);
        for (var i : vocabMap.keySet()) {
            for (var j : vocabMap.get(i)) {
                if (!ens.contains(j)) {
                    list.add(j);
                }
            }
        }
        filePath = "Cambridge vocabularies in use data/Pre-Intermediate.txt";
        vocabMap = readPreIntermediateVocabularies(filePath);
        for (var i : vocabMap.keySet()) {
            for (var j : vocabMap.get(i)) {
                if (!ens.contains(j)) {
                    list.add(j);
                }
            }
        }
//
//        filePath = "Cambridge vocabularies in use data/Upper-Intermediate.txt";
//        vocabMap = readUpperIntermediateVocabularies(filePath);
//        for (var i : vocabMap.keySet()) {
//            for (var j : vocabMap.get(i)) {
//                if (!ens.contains(j)) {
//                    list.add(j);
//                }
//            }
//        }
//
//        filePath = "Cambridge vocabularies in use data/Advanced.txt";
//        vocabMap = readAdvancedVocabularies(filePath);
//        for (var i : vocabMap.keySet()) {
//            for (var j : vocabMap.get(i)) {
//                if (!ens.contains(j)) {
//                    list.add(j);
//                }
//            }
//        }
        return list;
    }

    private static void writeElementaryTopics() {
        var ens = Test.databaseEnglish();
        String filePath = "Cambridge vocabularies in use data/Elementary.txt";
        HashMap<String, Set<String>> vocabMap = readElementaryVocabularies(filePath);
        HashSet<String> set = new HashSet<>();
        HashSet<String> exist = new HashSet<>();
        ArrayList<Object> data = new ArrayList<>();
        int count = 0;
        for (var i : vocabMap.keySet()) {
            for (var j : vocabMap.get(i)) {
                if (!ens.contains(j)) {
                    set.add(j);
                } else {
                    exist.add(j);
                }
            }
            HashMap<String, Object> row = new HashMap<>();
            row.put("name", i);
            row.put("vs", vocabMap.get(i));
            data.add(row);
            count += vocabMap.get(i).size();
        }
//        System.out.println(exist.size());
//        System.out.println("Có " + set.size() + " từ không có trong database");
        for (var i : set) {
            System.out.println(i);
        }
//        HashMap<String, Object> row = new HashMap<>();
//        row.put("vs", exist);
//        data.add(row);
//        Test.writeTopics(data, "Vocabulary in use Elementary");
    }

    private static void writeEnglishVocabulariesForIELTS(int start, int end) {
        var text = Test.readPdf("vocabularies clone/Cambridge Vocabularies/Word List.pdf"
        );
        var list = Elllo.extractWords(text);
        var ens = Test.databaseEnglish();
        HashSet<String> set = new HashSet<>();
        for (var i : ens) {
            if (list.contains(i)) {
                set.add(i);
            }
        }
        for (var i : list) {
            if (!set.contains(i)) {
                System.out.println(i);
//                Test.printVocabulariesLaban(i,"Vocabularies IELTS more.xlsx");
            }
        }
        System.out.println(set.size());
        Test.writeTopic("CAMBRIDGE VOCABULARY FOR IELTS" + end, list);
    }


    private static void writeGrammarTopic() {
        var ens = Test.databaseEnglish();
        ArrayList<Object> data = new ArrayList<>();
        File[] oxfSubdirectories = Oxford.getDirectories("Oxford topics");
        File[] camSubdirectories = Oxford.getDirectories("Cambridge word lists");
        File[] subdirectories = new File[oxfSubdirectories.length + camSubdirectories.length];
        System.arraycopy(oxfSubdirectories, 0, subdirectories, 0, oxfSubdirectories.length);
        System.arraycopy(camSubdirectories, 0, subdirectories, oxfSubdirectories.length, camSubdirectories.length);
        int start = 305, end = 305;
        var cams = Elllo.extractWords(Test.readPdf("vocabularies clone/Cambridge Vocabularies/English Vocabulary in Use Elementary  (2017).pdf", 160, 170).toLowerCase());
//        var cams = Elllo.extractWords(Test.readPdf(
//                "vocabularies clone/Vocabularies/English Grammar in Use.pdf"
//                ,start,
//                end
//        ).toLowerCase());
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
                                    if (cams.contains(en.toLowerCase())) list.add(english);
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
        String name = "English Vocabularies in Use " + start + "-" + end;
//        String name = "English Grammar in Use";
        HashMap<String, Object> row = new HashMap<>();
        row.put("name", name);
        row.put("vs", list);
        data.add(row);
        Test.writeTopics(data, "Oxford topics json/" + name);
    }

}
