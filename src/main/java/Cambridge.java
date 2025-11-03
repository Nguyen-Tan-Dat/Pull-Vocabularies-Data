import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

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
        writeAcademicTopics();
//        writeIELTSTopics();

//        printPartsOfSpeech("get dressed");
    }

    public static void printPartsOfSpeech(String word) {
        try {
            String urlStr = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {return;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseContent = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                responseContent.append(inputLine);
            }

            in.close();

            JSONArray jsonArray = new JSONArray(responseContent.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject entry = jsonArray.getJSONObject(i);
                JSONArray meanings = entry.getJSONArray("meanings");

                for (int j = 0; j < meanings.length(); j++) {
                    JSONObject meaning = meanings.getJSONObject(j);
                    String partOfSpeech = meaning.getString("partOfSpeech");
                    System.out.println(word+ "\tnone\t" + partOfSpeech);
                }
            }

        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
    }
    private static void writeAdvancedTopics() {
        var ens = Test.databaseEnglish();
        String filePath = "Cambridge vocabularies in use data/Advanced.txt";
        HashMap<Integer, Set<String>> vocabMap = readAcademicVocabularies(filePath);
        var topics= getAdvancedTopics();
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
            row.put("name",String.format("Advanced > Unit %03d: ",i)+topics.get(i));
             row.put("vs", vocabMap.get(i));
            data.add(row);
            count += vocabMap.get(i).size();
        }
//        System.out.println(count);
        System.out.println("Có " + unexist.size() + " từ không có trong database");
        for (var i : unexist) {
            System.out.println(i);
        }
        HashMap<String, Object> row = new HashMap<>();
        row.put("name", "Cambridge Advanced");
        row.put("vs", exist);
        data.add(row);

        HashMap<String, Object> row1 = new HashMap<>();
        row1.put("name", "Cambridge all");
        row1.put("vs", exist);
        data.add(row1);


        HashMap<String, Object> row3 = new HashMap<>();
        row3.put("name", "Cambridge vocabulary in use");
        row3.put("vs", exist);
        data.add(row3);
        Test.writeTopics(data, "Vocabulary in use Advanced");
    }
    public static Map<Integer, String> getAdvancedTopics() {
        Map<Integer, String> topics = new LinkedHashMap<>();

        // Work and study
        topics.put(1, "Cramming for success: study and academic work");
        topics.put(2, "Education: debates and issues");
        topics.put(3, "Applying for a job");
        topics.put(4, "Job interviews");
        topics.put(5, "At work: colleagues and routines");
        topics.put(6, "At work: job satisfaction");
        topics.put(7, "At work: careers");

        // People and relationships
        topics.put(8, "Describing people: positive and negative qualities");
        topics.put(9, "Describing people: appearance and mannerisms");
        topics.put(10, "Describing people: personality and character traits");
        topics.put(11, "Relationships: friends forever");
        topics.put(12, "Relationships: ups and downs");
        topics.put(13, "Emotions and reactions");
        topics.put(14, "Negative feelings");
        topics.put(15, "Birth and death: from cradle to grave");

        // Leisure and lifestyle
        topics.put(16, "Free time: relaxation and leisure");
        topics.put(17, "All the rage: clothes and fashion");
        topics.put(18, "Home styles, lifestyles");
        topics.put(19, "Socialising and networking");
        topics.put(20, "The performance arts: reviews and critiques");
        topics.put(21, "The visual arts");
        topics.put(22, "Talking about books");
        topics.put(23, "Food: a recipe for disaster");
        topics.put(24, "Dinner’s on me: entertaining and eating out");

        // Travel
        topics.put(25, "On the road: traffic and driving");
        topics.put(26, "Travel and accommodation");
        topics.put(27, "Attracting tourists");

        // Environment
        topics.put(28, "Describing the world");
        topics.put(29, "Weather and climate");
        topics.put(30, "Brick walls and glass ceilings");
        topics.put(31, "Taking root and reaping rewards");
        topics.put(32, "The animal kingdom");
        topics.put(33, "Our endangered world");

        // Society and institutions
        topics.put(34, "Here to help: customer service");
        topics.put(35, "Authorities: customs and police");
        topics.put(36, "Beliefs");
        topics.put(37, "Festivals in their cultural context");
        topics.put(38, "Talking about language");
        topics.put(39, "History: since the dawn of civilisation");
        topics.put(40, "The haves and the have-nots");
        topics.put(41, "British politics");
        topics.put(42, "International politics");
        topics.put(43, "The letter of the law");
        topics.put(44, "War and peace");
        topics.put(45, "Economy and finance");
        topics.put(46, "Personal finance: making ends meet");

        // Media
        topics.put(47, "The media: in print");
        topics.put(48, "The media: internet and email");
        topics.put(49, "Advertising");
        topics.put(50, "The news: gathering and delivering");

        // Health
        topics.put(51, "Healthcare");
        topics.put(52, "Illness: feeling under the weather");
        topics.put(53, "Medical language");
        topics.put(54, "Diet, sport and fitness");

        // Technology
        topics.put(55, "Industries: from manufacturing to service");
        topics.put(56, "Technology and its impact");
        topics.put(57, "Technology of the future");
        topics.put(58, "Energy: from fossil fuels to windmills");

        // Basic concepts
        topics.put(59, "Space: no room to swing a cat");
        topics.put(60, "Time: once in a blue moon");
        topics.put(61, "Motion: taking steps");
        topics.put(62, "Manner: behaviour and body language");
        topics.put(63, "Sounds: listen up!");
        topics.put(64, "Weight and density");
        topics.put(65, "All the colours of the rainbow");
        topics.put(66, "Speed: fast and slow");
        topics.put(67, "Cause and effect");
        topics.put(68, "Spot the difference: making comparisons");
        topics.put(69, "Difficulties and dilemmas");
        topics.put(70, "Modality: expressing facts, opinions, desires");
        topics.put(71, "Number: statistics and quantity");

        // Functional vocabulary
        topics.put(72, "Permission: getting the go-ahead");
        topics.put(73, "Complaining and protesting");
        topics.put(74, "Apology, regret and reconciliation");
        topics.put(75, "A pat on the back: complimenting and praising");
        topics.put(76, "Promises and bets");
        topics.put(77, "Reminiscences and regrets");
        topics.put(78, "Agreement, disagreement and compromise");
        topics.put(79, "Academic writing: making sense");
        topics.put(80, "Academic writing: text structure");
        topics.put(81, "Writing: style and format");
        topics.put(82, "Whatchamacallit: being indirect");
        topics.put(83, "Give or take: more vague expressions");
        topics.put(84, "The way you say it");

        // Words and meanings
        topics.put(85, "Abbreviations and acronyms");
        topics.put(86, "Prefixes: creating new meanings");
        topics.put(87, "Suffixes: forming new words");
        topics.put(88, "Word-building and word-blending");
        topics.put(89, "English: a global language");
        topics.put(90, "Easily confused words");
        topics.put(91, "One word, many meanings");

        // Fixed expressions and figurative language
        topics.put(92, "Collocation: which words go together");
        topics.put(93, "Metaphor: seeing the light");
        topics.put(94, "Idioms for everyday situations and feelings");
        topics.put(95, "Brushing up on phrasal verbs");
        topics.put(96, "Connotation: making associations");

        // Language variation
        topics.put(97, "Register: degrees of formality");
        topics.put(98, "Divided by a common language");
        topics.put(99, "Language and gender");
        topics.put(100, "In the headlines");
        topics.put(101, "Red tape");

        return topics;
    }

    private static void writeAcademicTopics() {
        var ens = Test.databaseEnglish();
        String filePath = "Cambridge vocabularies in use data/Academic 1.txt";
        HashMap<Integer, Set<String>> vocabMap = readAcademicVocabularies(filePath);
        var topics=getAcademicTopics();
        String filePath1 = "Cambridge vocabularies in use data/Academic.txt";
        HashMap<Integer, Set<String>> vocabMap1 = readAcademicVocabularies(filePath1);
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
                if(vocabMap1.get(i)==null){
                    System.out.println(i);
                }else if(vocabMap1.get(i).contains(j)){
                    count++;
                }else if(!vocabMap1.get(i).contains(j)){
                    System.out.println(j);
                }else if(!vocabMap1.get(i).contains(j)){
                    System.out.println(j);
                }
            }
            HashMap<String, Object> row = new HashMap<>();
            row.put("name",String.format("Academic > Unit %02d: ",i)+topics.get(i));

            row.put("vs", vocabMap.get(i));
            data.add(row);
//            count += vocabMap.get(i).size();
        }
        for (var i : unexist) {
            System.out.println(i);
        }
        System.out.println(exist.size());
        HashMap<String, Object> row = new HashMap<>();
        row.put("name", "Cambridge Academic");
        row.put("vs", exist);
        data.add(row);
        HashMap<String, Object> row1 = new HashMap<>();
        row1.put("name", "Cambridge all");
        row1.put("vs", exist);
        data.add(row1);
        Test.writeTopics(data, "Academic Vocabulary in Use");
    }

    private static void writeUpperIntermediateTopics() {
        var ens = Test.databaseEnglish();
        String filePath = "Cambridge vocabularies in use data/Upper-Intermediate.txt";
        HashMap<Integer, Set<String>> vocabMap = readUpperIntermediateVocabularies(filePath);
        HashSet<String> exist = new HashSet<>();
        var topics =getUpperIntermediateTopics();
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
            row.put("name",String.format("Upper-Intermediate > Unit %03d: ",i)+topics.get(i));

            row.put("vs", vocabMap.get(i));
            data.add(row);
            count += vocabMap.get(i).size();
        }
//        System.out.println(count);
//        System.out.println("Có " + unexist.size() + " từ không có trong database");
        for (var i : unexist) {
            System.out.println(i);
        }
        HashMap<String, Object> row = new HashMap<>();
        row.put("name", "Cambridge Upper-Intermediate");
        row.put("vs", exist);
        data.add(row);
        HashMap<String, Object> row1 = new HashMap<>();
        row1.put("name", "Cambridge all");
        row1.put("vs", exist);
        data.add(row1);

        HashMap<String, Object> row3 = new HashMap<>();
        row3.put("name", "Cambridge vocabulary in use");
        row3.put("vs", exist);
        data.add(row3);
        Test.writeTopics(data, "Vocabulary in use Upper-intermediate");
    }

    private static void writePreIntermediateTopics() {
        var ens = Test.databaseEnglish();
        String filePath = "Cambridge vocabularies in use data/Pre-Intermediate.txt";
        HashMap<Integer, Set<String>> vocabMap = readPreIntermediateVocabularies(filePath);
        var topics = getPreIntermediateTopics();
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

            row.put("name",String.format("Pre-Intermediate > Unit %03d: ",i)+topics.get(i));
            row.put("vs", vocabMap.get(i));
            data.add(row);
            count += vocabMap.get(i).size();
        }
//        System.out.println(exist.size());
//        System.out.println("Có " + unexist.size() + " từ không có trong database");
        for (var i : unexist) {
            System.out.println(i);
        }
        HashMap<String, Object> row = new HashMap<>();
        row.put("name", "Cambridge Pre-Intermediate ");
        row.put("vs", exist);
        data.add(row);
        HashMap<String, Object> row1 = new HashMap<>();
        row1.put("name", "Cambridge all");
        row1.put("vs", exist);
        data.add(row1);
        HashMap<String, Object> row3 = new HashMap<>();
        row3.put("name", "Cambridge vocabulary in use");
        row3.put("vs", exist);
        data.add(row3);
        Test.writeTopics(data, "Vocabulary in use Pre-Intermediate");
    }

    public static HashMap<Integer, Set<String>> readElementaryVocabularies(String filePath) {
        HashMap<Integer, Set<String>> unitMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String word, pronunciation, unit;
            while ((word = br.readLine()) != null &&
                    (pronunciation = br.readLine()) != null &&
                    (unit = br.readLine()) != null) {
                try {
                    int unitName = Integer.parseInt(unit);
                    if (!unitMap.containsKey(unitName)) {
                        unitMap.put(unitName, new HashSet<>());
                    }
                    word = word.replaceAll("'", "'");
                    unitMap.get(unitName).add(word);
                } catch (Exception e) {
                    var n = unit.split(",");
                    for (var i : n) {
                        try {
                            int unitName = Integer.parseInt(i.trim());
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


    public static HashMap<Integer, Set<String>> readUpperIntermediateVocabularies(String filePath) {
        HashMap<Integer, Set<String>> unitMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                var data = line.split("/");
                if (data.length == 3) {
                    String word = data[0].trim();
                    String pronunciation = data[1];
                    String unit = data[2];
                    try {
                        var unitName = Integer.parseInt(unit);
                        if (!unitMap.containsKey(unitName)) {
                            unitMap.put(unitName, new HashSet<>());
                        }
                        unitMap.get(unitName).add(word);
                    } catch (Exception e) {
                        var n = unit.split(",");
                        for (var i : n) {
                            try {
                                var unitName =  Integer.parseInt(i.trim());
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
    public static HashMap<Integer, Set<String>> readAcademicVocabularies(String filePath) {
        HashMap<Integer, Set<String>> unitMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int index=0;
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
                        System.out.println( line);
                        continue;
                    }
                } else {
                    // Trường hợp thiếu phát âm: "word unit"
                    int lastSpaceIndex = line.lastIndexOf(" ");
                    if (lastSpaceIndex == -1) {
                        System.out.println( line);
                        continue;
                    }
                    word = line.substring(0, lastSpaceIndex).trim();
                    unit = line.substring(lastSpaceIndex + 1).trim();
                }

                try {
                    // Xử lý unit (có thể là một hoặc nhiều unit cách nhau bởi dấu ",")
                    for (String u : unit.split(",")) {
                        try {
                            unitMap.computeIfAbsent(Integer.parseInt(u.trim()), k -> new HashSet<>()).add(word);
                        } catch (NumberFormatException e1) {
                            System.out.println("Lỗi unit: " + unit);
                            System.out.println(index);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Lỗi khi xử lý unit: " + unit);
                    return null;
                }
                index++;
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

    public static HashMap<Integer, Set<String>> readPreIntermediateVocabularies(String filePath) {
        HashMap<Integer, Set<String>> unitMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String word, pronunciation, unit;
            while ((word = br.readLine()) != null &&
                    (pronunciation = br.readLine()) != null &&
                    (unit = br.readLine()) != null) {
                try {
                    var unitName = Integer.parseInt(unit);
                    if (!unitMap.containsKey(unitName)) {
                        unitMap.put(unitName, new HashSet<>());
                    }
                    unitMap.get(unitName).add(word);
                } catch (Exception e) {
                    var n = unit.split(",");
                    for (var i : n) {
                        try {
                            var unitName = Integer.parseInt(i.trim());
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
        return String.format(root + " > Unit %02d: ", unitNumber);
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
        String filePath = "Vocabularies For IELTS.txt";
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


    private static void writeElementaryTopics() {
        var ens = Test.databaseEnglish();
        String filePath = "Cambridge vocabularies in use data/Elementary.txt";
        HashMap<Integer, Set<String>> vocabMap = readElementaryVocabularies(filePath);
        HashSet<String> set = new HashSet<>();
        HashSet<String> exist = new HashSet<>();
        ArrayList<Object> data = new ArrayList<>();
        var topics=getElementaryTopics();
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

            row.put("name",String.format("Elementary > Unit %02d: ",i)+topics.get(i));
            row.put("vs", vocabMap.get(i));
            data.add(row);
            count += vocabMap.get(i).size();
        }
        for (var i : set) {
            System.out.println(i);
        }
        System.out.println(exist.size());
        HashMap<String, Object> row = new HashMap<>();
        row.put("name", "Cambridge Elementary");
        row.put("vs", exist);
        data.add(row);
        HashMap<String, Object> row1 = new HashMap<>();
        row1.put("name", "Cambridge all");
        row1.put("vs", exist);
        data.add(row1);
        HashMap<String, Object> row3 = new HashMap<>();
        row3.put("name", "Cambridge vocabulary in use");
        row3.put("vs", exist);
        data.add(row3);
        Test.writeTopics(data, "Vocabulary in use Elementary");
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
    public static Map<Integer, String> getAcademicTopics() {
        Map<Integer, String> topics = new LinkedHashMap<>();

        topics.put(1, "What is special about academic English?");
        topics.put(2, "Key nouns");
        topics.put(3, "Key verbs");
        topics.put(4, "Key adjectives");
        topics.put(5, "Key adverbs");
        topics.put(6, "Phrasal verbs in academic English");
        topics.put(7, "Key quantifying expressions");
        topics.put(8, "Words with several meanings");
        topics.put(9, "Metaphors and idioms");
        topics.put(10, "Word combinations");
        topics.put(11, "Nouns and the words they combine with");
        topics.put(12, "Adjective and noun combinations");
        topics.put(13, "Verbs and the words they combine with");
        topics.put(14, "Prepositional phrases");
        topics.put(15, "Verbs and prepositions");
        topics.put(16, "Nouns and prepositions");
        topics.put(17, "Fixed expressions");
        topics.put(18, "Applications and application forms");
        topics.put(19, "College and university: the UK system");
        topics.put(20, "Systems compared: the US and the UK");
        topics.put(21, "Academic courses");
        topics.put(22, "Study habits and skills");
        topics.put(23, "Online learning");
        topics.put(24, "Sources");
        topics.put(25, "Facts, evidence and data");
        topics.put(26, "Numbers");
        topics.put(27, "Statistics");
        topics.put(28, "Graphs and diagrams");
        topics.put(29, "Money and education");
        topics.put(30, "Time");
        topics.put(31, "Cause and effect");
        topics.put(32, "Talking about ideas");
        topics.put(33, "Reporting what others say");
        topics.put(34, "Analysis of results");
        topics.put(35, "Talking about meaning");
        topics.put(36, "Research and study aims");
        topics.put(37, "Talking about points of view");
        topics.put(38, "Degrees of certainty");
        topics.put(39, "Presenting an argument");
        topics.put(40, "Organising your writing");
        topics.put(41, "Making a presentation");
        topics.put(42, "Describing research methods");
        topics.put(43, "Classifying");
        topics.put(44, "Making connections");
        topics.put(45, "Comparing and contrasting");
        topics.put(46, "Describing problems");
        topics.put(47, "Describing situations");
        topics.put(48, "Processes and procedures");
        topics.put(49, "Describing change");
        topics.put(50, "Evaluation and emphasis");

        return topics;
    }

    public static Map<Integer, String> getElementaryTopics() {
        Map<Integer, String> topics = new LinkedHashMap<>();

        topics.put(1, "The family");
        topics.put(2, "Birth, marriage and death");
        topics.put(3, "Parts of the body");
        topics.put(4, "Clothes");
        topics.put(5, "Describing people");
        topics.put(6, "Health and illness");
        topics.put(7, "Feelings");
        topics.put(8, "Conversations 1: Greetings and wishes");
        topics.put(9, "Conversations 2: Useful words and expressions");

        topics.put(10, "Food and drink");
        topics.put(11, "In the kitchen");
        topics.put(12, "In the bedroom and bathroom");
        topics.put(13, "In the living room");

        topics.put(14, "Jobs");
        topics.put(15, "At school and university");
        topics.put(16, "Communications");
        topics.put(17, "Your phone");

        topics.put(18, "Holidays");
        topics.put(19, "Shops and shopping");
        topics.put(20, "Online shopping");
        topics.put(21, "In a hotel");
        topics.put(22, "Eating out");
        topics.put(23, "Sports");
        topics.put(24, "Cinema");
        topics.put(25, "Free time at home");
        topics.put(26, "Music and musical instruments");

        topics.put(27, "Countries and nationalities");
        topics.put(28, "Weather");
        topics.put(29, "In the town");
        topics.put(30, "In the countryside");
        topics.put(31, "Animals");
        topics.put(32, "Travelling");
        topics.put(33, "UK culture");

        topics.put(34, "Crime");
        topics.put(35, "The media");
        topics.put(36, "Problems at home and work");
        topics.put(37, "Global problems");

        topics.put(38, "Have / had / had");
        topics.put(39, "Go / went / gone");
        topics.put(40, "Do / did / done");
        topics.put(41, "Make / made / made");
        topics.put(42, "Come / came / come");
        topics.put(43, "Take / took / taken");
        topics.put(44, "Bring / brought / brought");
        topics.put(45, "Get / got / got");

        topics.put(46, "Phrasal verbs");
        topics.put(47, "Everyday things");
        topics.put(48, "Talking");
        topics.put(49, "Moving");

        topics.put(50, "Conjunctions and connecting words");
        topics.put(51, "Days, months, seasons");
        topics.put(52, "Time words");
        topics.put(53, "Places");
        topics.put(54, "Manner");
        topics.put(55, "Common uncountable nouns");
        topics.put(56, "Common adjectives: Good and bad things");
        topics.put(57, "Words and prepositions");
        topics.put(58, "Prefixes");
        topics.put(59, "Suffixes");
        topics.put(60, "Words you may confuse");

        return topics;
    }
    public static Map<Integer, String> getPreIntermediateTopics() {
        Map<Integer, String> topics = new LinkedHashMap<>();

        topics.put(1, "Learning vocabulary");
        topics.put(2, "Keeping a vocabulary notebook");
        topics.put(3, "Using a dictionary");
        topics.put(4, "English language words");

        topics.put(5, "Country, nationality and language");
        topics.put(6, "The physical world");
        topics.put(7, "Weather");
        topics.put(8, "Animals and insects");

        topics.put(9, "The body and movement");
        topics.put(10, "Describing appearance");
        topics.put(11, "Describing character");
        topics.put(12, "Feelings");
        topics.put(13, "Family and friends");
        topics.put(14, "Growing up");
        topics.put(15, "Romance, marriage and divorce");

        topics.put(16, "Daily routines");
        topics.put(17, "The place where you live");
        topics.put(18, "Around the home");
        topics.put(19, "Money");
        topics.put(20, "Health");
        topics.put(21, "Clothes");
        topics.put(22, "Fashion and buying clothes");
        topics.put(23, "Shopping");
        topics.put(24, "Food");
        topics.put(25, "Cooking");
        topics.put(26, "City life");
        topics.put(27, "Life in the country");
        topics.put(28, "Transport");
        topics.put(29, "On the road");
        topics.put(30, "Notices and warnings");

        topics.put(31, "Classroom language");
        topics.put(32, "School education");
        topics.put(33, "Studying English and taking exams");
        topics.put(34, "University education");

        topics.put(35, "Jobs");
        topics.put(36, "Talking about your work");
        topics.put(37, "Making a career");
        topics.put(38, "Working in an office");
        topics.put(39, "Running a company");
        topics.put(40, "Business and finance");

        topics.put(41, "Sport and leisure");
        topics.put(42, "Competitive sport");
        topics.put(43, "Books and films");
        topics.put(44, "Music");
        topics.put(45, "Special events");

        topics.put(46, "Travel bookings");
        topics.put(47, "Air travel");
        topics.put(48, "Hotels and restaurants");
        topics.put(49, "Cafés");
        topics.put(50, "Sightseeing holidays");
        topics.put(51, "Holidays by the sea");

        topics.put(52, "Newspapers and television");
        topics.put(53, "Phoning and texting");
        topics.put(54, "Computers");
        topics.put(55, "Email and the Internet");

        topics.put(56, "Crime");
        topics.put(57, "Politics");
        topics.put(58, "Climate change");
        topics.put(59, "War and violence");

        topics.put(60, "Time");
        topics.put(61, "Numbers");
        topics.put(62, "Distance, dimensions and size");
        topics.put(63, "Objects, materials, shapes and colour");
        topics.put(64, "Containers and quantities");

        topics.put(65, "Apologies, excuses and thanks");
        topics.put(66, "Requests, permission and suggestions");
        topics.put(67, "Opinions, agreeing and disagreeing");
        topics.put(68, "Likes, dislikes, attitudes and preferences");
        topics.put(69, "Greetings, farewells and special expressions");

        topics.put(70, "Prefixes: changing meaning");
        topics.put(71, "Suffixes: forming nouns");
        topics.put(72, "Suffixes: forming adjectives");
        topics.put(73, "Compound nouns");

        topics.put(74, "Word partners");
        topics.put(75, "Fixed phrases");
        topics.put(76, "Fixed phrases in conversation");
        topics.put(77, "Verb or adjective + preposition");
        topics.put(78, "Prepositional phrases");

        topics.put(79, "Phrasal verbs 1: form and meaning");
        topics.put(80, "Phrasal verbs 2: grammar and style");

        topics.put(81, "Make, do and take: uses and phrases");
        topics.put(82, "Key verbs: give, keep and miss");
        topics.put(83, "Get: uses, phrases and phrasal verbs");
        topics.put(84, "Go: meanings and expressions");
        topics.put(85, "The senses");

        topics.put(86, "Uncountable nouns");
        topics.put(87, "Verb constructions 1");
        topics.put(88, "Verb constructions 2");
        topics.put(89, "Adjectives");
        topics.put(90, "Prepositions: place and movement");
        topics.put(91, "Adverbs");

        topics.put(92, "Time and sequence");
        topics.put(93, "Addition and contrast");
        topics.put(94, "Reason, purpose, result, condition");

        topics.put(95, "Formal and informal English");
        topics.put(96, "Completing forms and CVs");
        topics.put(97, "Writing an essay");
        topics.put(98, "Formal letters and emails");
        topics.put(99, "Informal emails and messages");
        topics.put(100, "Abbreviations");

        return topics;
    }
    public static Map<Integer, String> getUpperIntermediateTopics() {
        Map<Integer, String> topics = new LinkedHashMap<>();

        topics.put(1, "Learning vocabulary");
        topics.put(2, "Organising a vocabulary notebook");
        topics.put(3, "Using your dictionary");
        topics.put(4, "Guessing and explaining meaning");

        topics.put(5, "Countries, nationalities and languages");
        topics.put(6, "The weather");
        topics.put(7, "Describing people: appearance");
        topics.put(8, "Describing people: personality");
        topics.put(9, "Idioms describing people");
        topics.put(10, "Relationships");
        topics.put(11, "At home");
        topics.put(12, "Everyday minor problems");
        topics.put(13, "Global problems");
        topics.put(14, "Education");
        topics.put(15, "Higher education");
        topics.put(16, "Work");
        topics.put(17, "Business");
        topics.put(18, "Sport");
        topics.put(19, "Art and literature");
        topics.put(20, "Theatre and cinema");
        topics.put(21, "Music");
        topics.put(22, "Food");
        topics.put(23, "Physical geography");
        topics.put(24, "Environmental problems");
        topics.put(25, "Towns");
        topics.put(26, "The natural world");
        topics.put(27, "Clothes");
        topics.put(28, "Health and medicine");
        topics.put(29, "Medicine and technology");
        topics.put(30, "Health and lifestyle");
        topics.put(31, "Travel");
        topics.put(32, "Holidays");
        topics.put(33, "Science and technology");
        topics.put(34, "Computers");
        topics.put(35, "Communications and the Internet");
        topics.put(36, "Social media");
        topics.put(37, "The press and the media");
        topics.put(38, "Politics and public institutions");
        topics.put(39, "Crime");
        topics.put(40, "Money");
        topics.put(41, "Describing objects");

        topics.put(42, "Belief and opinion");
        topics.put(43, "Pleasant and unpleasant feelings");
        topics.put(44, "Like, dislike and desire");
        topics.put(45, "Speaking");
        topics.put(46, "The six senses");
        topics.put(47, "What your body does");
        topics.put(48, "Praising and criticising");
        topics.put(49, "Emotions and moods");
        topics.put(50, "Commenting on problematic situations");

        topics.put(51, "Number, quantity, degree and intensity");
        topics.put(52, "Numbers and shapes");
        topics.put(53, "Time");
        topics.put(54, "Distances and dimensions");
        topics.put(55, "Obligation, need, possibility and probability");
        topics.put(56, "Sound and light");
        topics.put(57, "Possession and giving");
        topics.put(58, "Movement and speed");
        topics.put(59, "Texture, brightness, weight and density");
        topics.put(60, "Success, failure and difficulty");

        topics.put(61, "Time: connecting words and expressions");
        topics.put(62, "Condition");
        topics.put(63, "Cause, reason, purpose and result");
        topics.put(64, "Concession and contrast");
        topics.put(65, "Addition");
        topics.put(66, "Referring words");
        topics.put(67, "Discourse markers in spoken English");
        topics.put(68, "Linking words in writing");
        topics.put(69, "Talking and communicating");

        topics.put(70, "Suffixes");
        topics.put(71, "Prefixes");
        topics.put(72, "Roots");
        topics.put(73, "Abstract nouns");
        topics.put(74, "Compound adjectives");
        topics.put(75, "Compound nouns 1: noun + noun");
        topics.put(76, "Compound nouns 2: verb + preposition");
        topics.put(77, "Binomials");
        topics.put(78, "Abbreviations and acronyms");
        topics.put(79, "Multi-word expressions");

        topics.put(80, "Words commonly mispronounced");
        topics.put(81, "Onomatopoeic words");
        topics.put(82, "Homophones and homographs");

        topics.put(83, "Uncountable nouns");
        topics.put(84, "Words that only occur in the plural");
        topics.put(85, "Countable and uncountable nouns with different meanings");
        topics.put(86, "Making uncountable nouns countable");
        topics.put(87, "Collective nouns");
        topics.put(88, "Containers and contents");

        topics.put(89, "Expressions with do and make");
        topics.put(90, "Expressions with bring and take");
        topics.put(91, "Expressions with get");
        topics.put(92, "Expressions with set and put");
        topics.put(93, "Expressions with come and go");
        topics.put(94, "Expressions with other common verbs");

        topics.put(95, "Formal and informal words 1");
        topics.put(96, "Formal and informal words 2");
        topics.put(97, "Similes");
        topics.put(98, "Proverbs");
        topics.put(99, "The language of signs and notices");
        topics.put(100, "Headline English");
        topics.put(101, "US English");

        return topics;
    }
}
