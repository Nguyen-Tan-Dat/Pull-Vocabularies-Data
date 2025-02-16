package example;

import com.google.gson.Gson;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void writeDataVocabularies(ArrayList<String[]> list,String filePath) {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream(filePath)) {
            Sheet sheet = workbook.createSheet("data");
            for (int i = 0; i < list.size(); i++) {
                Row row = sheet.createRow(i);
                String[] data = list.get(i);
                for (int j = 0; j < data.length; j++) {
                    Cell cell = row.createCell(j + 2);
                    cell.setCellValue(data[j]);
                }
            }
            workbook.write(fileOut);
            System.out.println("Excel file has been generated successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String[]> readDataVocabularies(String excelFilePath) {
        ArrayList<String[]> eData = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(2);
                    Cell cell1 = row.getCell(3);
                    Cell cell2 = row.getCell(4);
                    Cell cell3 = row.getCell(5);
                    if (cell != null) {
                        String en = getCellValueAsString(cell);
                        String vi = getCellValueAsString(cell1);
                        String part = getCellValueAsString(cell2);
                        String phonetic = getCellValueAsString(cell3);
                        var add = true;
//                        for (int i1 = 0; i1 < eData.size(); i1++) {
//                            if (en == eData.get(i1)[0] && vi == eData.get(i1)[1] && part == eData.get(i1)[2]) {
//                                add = false;
//                            }
//                        }
                        if (add)
                            eData.add(new String[]{en, vi, part, phonetic});

                    } else {
                        System.out.println(i);
                    }
                } else {
                    System.out.println(i);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return eData;
    }
    public static void writeTopic(String name, HashSet<String> vocabularies){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("vs",vocabularies);
        hashMap.put("name", name);
        ArrayList<Object> list=new ArrayList<>();
        list.add(hashMap);
        String jsonString = convertHashMapToJson(list);
        Test.writeFile("output json/"+name+".json",jsonString);
    }
    public static void writeTopicHaveTypes(String name, HashSet<String[]> vocabularies){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("vs",vocabularies);
        hashMap.put("name", name);
        ArrayList<Object> list=new ArrayList<>();
        list.add(hashMap);
        String jsonString = convertHashMapToJson(list);
        Test.writeFile("output json/"+name+".json",jsonString);
    }
    public static void writeTopics( ArrayList<Object>  list ,String filename){
        String jsonString = convertHashMapToJson(list);
        Test.writeFile("output json/"+filename+".json",jsonString);
    }

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

    private static String convertHashMapToJson(ArrayList<Object> hashMap) {
        Gson gson = new Gson();
        return gson.toJson(hashMap);
    }
    public static String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    public static void printVocabulariesLaban(String english, String filePath) {
        String url = "https://dict.laban.vn/find?type=1&query=" + english;

        try {
            Document document = Jsoup.connect(url).get();
            Element ulElement = document.getElementById("content_selectable");
            String phonetic = "";
            try {
                phonetic = document.getElementsByClass("color-black").get(0).text();

            } catch (Exception e) {
            }
            if (ulElement != null) {
                String part_of_speech = "unknown";
                for (var i : ulElement.getElementsByTag("div")) {
                    if (i.attr("class").equals("bg-grey bold font-large m-top20"))
                        part_of_speech = i.text();
                    if (i.attr("class").equals("green bold margin25 m-top15")) {
                        System.out.println(english + "\t" + part_of_speech + "\t" + i.text() + "\t" + phonetic);
                        // Kiểm tra xem tệp đã tồn tại hay chưa
                        File file = new File(filePath);
                        Workbook workbook;
                        Sheet sheet;

                        if (file.exists()) {
                            try (FileInputStream fis = new FileInputStream(file)) {
                                // Mở workbook và sheet nếu tệp đã tồn tại
                                workbook = new XSSFWorkbook(fis);
                                sheet = workbook.getSheetAt(0);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                return;
                            }
                        } else {
                            // Tạo workbook và sheet mới nếu tệp chưa tồn tại
                            workbook = new XSSFWorkbook();
                            sheet = workbook.createSheet("Data");
                        }

                        // Tạo hàng mới ở cuối sheet
                        int lastRowNum = sheet.getLastRowNum();
                        Row row = sheet.createRow(lastRowNum + 1);

                        // Tạo các ô và đặt giá trị vào các ô
                        Cell cell1 = row.createCell(2);
                        cell1.setCellValue(english);

                        Cell cell2 = row.createCell(3);
                        cell2.setCellValue(i.text());

                        Cell cell3 = row.createCell(4);
                        cell3.setCellValue(part_of_speech);

                        Cell cell4 = row.createCell(5);
                        cell4.setCellValue(phonetic);
                        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                            workbook.write(fileOut);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                        try {
                            workbook.close();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                }
            } else {
                System.out.println("Không tìm thấy phần tử <ul> với class 'slide_img'");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printVocabularies(String en, String path) {
        var html = DownloadTopic.getHTML("https://dictionary.cambridge.org/dictionary/english-vietnamese/" + en);
        Document doc = Jsoup.parse(html);
        var el = doc.getElementsByClass("d pr di english-vietnamese kdic");
        String phonetic = "";
        for (var e : el) {
            var ps = e.getElementsByClass("di-info");
            String part = "unknown";
            if (ps.size() > 0) {
                var pns = ps.first().getElementsByClass("pos dpos");

                if (pns.size() > 0) {
                    part = pns.first().text();
                }
                var p = ps.first().getElementsByClass("ipa dipa");
                if (p.size() > 0) {
                    phonetic = p.first().text();
                }
            }
            var vis = e.getElementsByClass("trans dtrans");

            for (var vi : vis) {
                System.out.println(en + "\t" + vi.text() + "\t" + part + "\t" + phonetic);
                String filePath = path;

                // Kiểm tra xem tệp đã tồn tại hay chưa
                File file = new File(filePath);
                Workbook workbook;
                Sheet sheet;

                if (file.exists()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        // Mở workbook và sheet nếu tệp đã tồn tại
                        workbook = new XSSFWorkbook(fis);
                        sheet = workbook.getSheetAt(0);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        return;
                    }
                } else {
                    // Tạo workbook và sheet mới nếu tệp chưa tồn tại
                    workbook = new XSSFWorkbook();
                    sheet = workbook.createSheet("Data");
                }

                // Tạo hàng mới ở cuối sheet
                int lastRowNum = sheet.getLastRowNum();
                Row row = sheet.createRow(lastRowNum + 1);

                // Tạo các ô và đặt giá trị vào các ô
                Cell cell1 = row.createCell(2);
                cell1.setCellValue(en);

                Cell cell2 = row.createCell(3);
                cell2.setCellValue(vi.text());

                Cell cell3 = row.createCell(4);
                cell3.setCellValue(part);

                Cell cell4 = row.createCell(5);
                cell4.setCellValue(phonetic);

                // Ghi workbook vào file
                try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                    workbook.write(fileOut);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                // Đóng workbook để giải phóng tài nguyên
                try {
                    workbook.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

        }
    }

    public static void main1(String[] args) {
        var data = databaseEnglish();
        var list = dataBook();
        System.out.println("Database : " + data.size());
        System.out.println("Read total: " + list.size());
        int count = 0;
        for (var i : list) {
            if (!data.contains(i)) {
                System.out.println(i);
//                printVocabularies(i,0);
                count++;
            }

        }
        System.out.println(count);

//        System.out.println("Data: " + data.size());
//        System.out.println("CI size: " + list.size());
//        HashSet<String> learnHad = new HashSet<>();
//        HashSet<String> learnAdd = new HashSet<>();
//        for (var i : list) {
//            learnAdd.add(i.toLowerCase());
//        }
//        data.remove("");
//        data.remove(" ");
//        list.remove("");
//        list.remove(" ");
//        for (var i : data) {
//            for (var j : list) {
//                if (i.equals(j)) {
//                    learnHad.add(i);
//                    learnAdd.remove(i);
//                    break;
//                }
//            }
//        }
//        HashSet<String> drop = new HashSet<>();
//        for (var i : data) {
//            if (!learnHad.contains(i.toLowerCase())) {
//                drop.add(i);
//            }
//        }
//        System.out.println("Drop: " + drop.size());
//        System.out.print("[ ");
//        for (var array : drop) {
//            System.out.print("\"" + array + "\", ");
//        }
//        System.out.println("]");
//        System.out.println("Learn " + learnHad.size());
//        System.out.print("[ ");
//        for (var array : learnHad) {
//            System.out.print("\"" + array + "\", ");
//        }
//        System.out.println("]");
//        System.out.println("Learn Add: " + learnAdd.size());
//        String s = "";
//        for (var i : learnAdd) {
//
////            if (s.length() < 5000) {
//                s += i + "\n";
////            } else {
////                break;
////            }
//        }
//        System.out.println(s);
////        System.out.print("[ ");
////        for (var array : learnAdd) {
////            System.out.print("\"" + array + "\", ");
////        }
////        System.out.println("]");

    }

    public static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/cic";
    public static final String USERNAME = "postgres";
    public static final String PASSWORD = "CpqaFVYJ9Mkz6pOj";

    public static HashSet<String> databaseEnglish() {
        HashSet<String> data = new HashSet<>();
        try {
            // Kết nối với cơ sở dữ liệu
            Connection connection = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
            if (connection != null) {
                String sqlQuery = "SELECT * FROM english";
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlQuery);
                while (resultSet.next()) {
                    // Đọc các cột từ ResultSet bằng các phương thức get<kiểu dữ liệu cột>()
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("word");
                    // Đọc các cột khác tương tự
                    data.add(name);
                    // In ra dữ liệu
//                    System.out.println("ID: " + id + ", Name: " + name);
                }

                // Đóng ResultSet, Statement và Connection sau khi sử dụng xong
                resultSet.close();
                statement.close();
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Không thể kết nối đến cơ sở dữ liệu!");
            e.printStackTrace();
        }
        return data;
    }

    public static HashSet<String> dataBook() {
        HashSet<String> list = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("new 1.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (containsNumber(line)) {
                    String w = br.readLine();
                    if (w != null) {
                        w = w.trim();
                        list.add(w);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//
//        try (BufferedReader br = new BufferedReader(new FileReader("new 2.txt"))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                if (containsNumber(line)) {
//                    String w = br.readLine();
//                    if (w != null) {
//                        w = w.trim();
//                        list.add(w);
//                    }
//                }
//            }
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        try (BufferedReader br = new BufferedReader(new FileReader("new 3.txt"))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                if (line.contains("/")) {
//                    var dataline = line.split("/");
//                    String w = dataline[0].trim();
//                    if (w != null) {
//                        list.add(w);
//                    }
//                }
//            }
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        try (BufferedReader br = new BufferedReader(new FileReader("new 4.txt"))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                if (line.contains("/")) {
//                    var dataline = line.split("/");
//                    String w = dataline[0].trim();
//                    if (w != null) {
//                        list.add(w);
//                    }
//                }
//            }
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        HashSet<String> toLo = new HashSet<>();
        for (var i : list) {
            i = i.replaceAll("…", "...");
            i = i.replaceAll("’", "'");
            i = i.replaceAll("–", "-");
            i = i.replaceAll(" - ", "-");
            if (!containsNumber(i)
                    && isEnglish(i)
//            &&!i.contains("…")
//            &&!i.contains("’")
                    && !i.isEmpty()
            ) {
                toLo.add(i);
                if (i.contains("[")
//                        && !i.contains("]")
                ) toLo.remove(i);
                if (i.contains("(")
//                        && !i.contains(")")
                ) toLo.remove(i);
            }
        }
        return toLo;
    }


    public static String readFile(String fileName) {
        StringBuilder content = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return content.toString();
    }

    private static void partArrayCode(String fileName) {
        int i = 0;
        try (FileInputStream inputStream = new FileInputStream(fileName);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            String array = "const " + fileName.replaceAll("-", "_").replaceAll("_z_0_5.xlsx", "") + "=[";
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    // Bắt đầu từ cột thứ 2
//                    for (int colIndex = 1; colIndex < row.getLastCellNum(); colIndex++) {
//                        Cell cell = row.getCell(1);

//                        if (cell != null) {
//                            System.out.print(cell.toString() + "\t");
//                        }
//                    }
//                    array+=row.getCell(1)+":["
                    if (row.getCell(1) != null && i == row.getCell(1).getNumericCellValue()) {
                        array += "["
                                + row.getCell(2) + ","
                                + row.getCell(3) + ","
                                + row.getCell(4) + ","
                                + row.getCell(5) + ","
                                + row.getCell(6) + ","
                                + row.getCell(7) + ","
                                + row.getCell(8) + "],"
                        ;
                        i++;
                    }
                }
            }
            array += "];";
            System.out.println(array);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> readPdfsInFolder(String path) {
        List<String> pdfContents = new ArrayList<>();
        File folder = new File(path);

        // Kiểm tra xem đường dẫn có phải là thư mục không
        if (folder.isDirectory()) {
            // Lấy danh sách tất cả các file trong thư mục
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    // Kiểm tra xem file có đuôi .pdf không
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
                        // Đọc nội dung file PDF và thêm vào danh sách
                        String content = readPdf(file.getAbsolutePath());
                        pdfContents.add(content);
                    }
                }
            }
        } else {
            System.out.println("Đường dẫn không phải là thư mục.");
        }

        return pdfContents;
    }

    public static String readPdf(String filePath) {
        StringBuilder content = new StringBuilder();
        try {
            PdfReader reader = new PdfReader(filePath);
            PdfDocument pdfDoc = new PdfDocument(reader);
            int numberOfPages = pdfDoc.getNumberOfPages();
            for (int i = 1; i <= numberOfPages; i++) {
                content.append(PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i))).append("\n");
            }
            pdfDoc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString().toLowerCase();
    }

    public static String readPdf(String filePath, int startPage, int endPage) {
        StringBuilder content = new StringBuilder();

        try {
            PdfReader reader = new PdfReader(filePath);
            PdfDocument pdfDoc = new PdfDocument(reader);
            int numberOfPages = pdfDoc.getNumberOfPages();

            // Điều chỉnh startPage và endPage nếu vượt quá số trang của tài liệu
            startPage = Math.max(startPage, 1);  // đảm bảo startPage không nhỏ hơn 1
            endPage = Math.min(endPage, numberOfPages); // đảm bảo endPage không lớn hơn số trang tổng

            // Đọc từ startPage đến endPage
            for (int i = startPage; i <= endPage; i++) {
                content.append(PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i))).append("\n");
            }

            pdfDoc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Trả về nội dung đã chuyển sang chữ thường
        return content.toString().toLowerCase();
    }

    public static void main11(String[] args) {

        ArrayList<String[]> vsData = new ArrayList<>();
        var oxford= Oxford.readOxfordVocabularies();
        vsData.addAll(Test.readDataVocabularies("All my words.xlsx"));
        vsData.addAll(Test.readDataVocabularies("All my words part laban clean.xlsx"));
        var ens=databaseEnglish();
        var books=dataBook();

        ArrayList<String[]> most = new ArrayList<>();
        for(var i: vsData){
            var en=i[0];
            if(books.contains(en)&&!ens.contains(en)&&oxford.contains(en)){
                most.add(i);
            }
        }

        Test.writeDataVocabularies(most, "All my words more.xlsx");
    }


    static class Vocab {
        private int en, vi, to;

        public Vocab(int en, int vi, int to) {
            this.en = en;
            this.vi = vi;
            this.to = to;
        }

        public int getEn() {
            return en;
        }

        public void setEn(int en) {
            this.en = en;
        }

        public int getVi() {
            return vi;
        }

        public void setVi(int vi) {
            this.vi = vi;
        }

        public int getTo() {
            return to;
        }

        public void setTo(int to) {
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            Vocab vocab = (Vocab) o;
            return en == vocab.en && vi == vocab.vi;
        }

        @Override
        public int hashCode() {
            return Objects.hash(en, vi, to);
        }
    }




    public static String removeParenthesesContent(String input) {
        // Tạo mẫu regular expression
        Pattern pattern = Pattern.compile("\\([^\\)]+\\)");

        // Tạo một Matcher để tìm kiếm chuỗi trong ngoặc đơn
        Matcher matcher = pattern.matcher(input);

        // Thực hiện thay thế chuỗi trong ngoặc đơn bằng chuỗi trống
        String result = matcher.replaceAll("").trim();

        return result;
    }

    public static boolean containsNumber(String line) {
        for (char c : line.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    public static String removeNumberAndDot(String text) {
        // Biểu thức chính quy để tìm số và dấu chấm ở đầu dòng
        String regex = "^\\d+\\.\\s";
        // Thực hiện thay thế các số và dấu chấm bằng chuỗi rỗng
        return text.replaceAll(regex, "");
    }

    public static boolean startsWithDigit(String line) {
        if (line.length() > 0 && Character.isDigit(line.charAt(0))) {
            return true;
        }
        return false;
    }

    public static boolean isEnglish(String text) {
        // Kiểm tra từng ký tự trong chuỗi
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            // Kiểm tra nếu ký tự không nằm trong phạm vi chữ cái tiếng Anh (a-z, A-Z)
            if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '\'' || ch == ' ' || ch == ',' || ch == '-' || ch == '&' || ch == '.')) {
                return false;
            }
        }
        // Nếu không có ký tự nào không phải tiếng Anh, trả về true
        return true;
    }

    public static int getNumberBeforeDot(String text) {
        int dotIndex = text.indexOf('.');

        if (dotIndex != -1 && dotIndex > 0) {
            String numberString = text.substring(0, dotIndex).trim();
            try {
                return Integer.parseInt(numberString);
            } catch (NumberFormatException e) {
                return -1;
            }
        } else {
            return -1;
        }
    }
    public static void writeFile(String filePath, String content) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(content);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
