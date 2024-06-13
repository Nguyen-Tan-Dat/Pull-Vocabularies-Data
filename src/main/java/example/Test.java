package example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void printVocabularies(String en,int number) {
        var html = DownloadTopic.getHTML("https://dictionary.cambridge.org/dictionary/english-vietnamese/" + en);
        Document doc = Jsoup.parse(html);
        var el = doc.getElementsByClass("d pr di english-vietnamese kdic");
        String phonetic="";
        for (var e : el) {
            var ps = e.getElementsByClass("di-info");
            if (ps.size() > 0){
                var pns=ps.first().getElementsByClass("pos dpos");
                if(pns.size()>0) {
                    String part = pns.first().text();
                    var p=ps.first().getElementsByClass("ipa dipa");
                    if(p.size()>0) {
                        phonetic = p.first().text();
                    }

                    var vis = e.getElementsByClass("trans dtrans");
                    for (var vi : vis) {
                        System.out.println(en + "\t" + vi.text() + "\t" + part+"\t"+phonetic);
                        String filePath = "cambridge-vocabularies-"+number+".xlsx";

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
        }
    }

    public static void main(String[] args) {
        var data = databaseEnglish();
        var list = dataBook();
        System.out.println("Database : " + data.size());
        System.out.println("Read total: " + list.size());
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
        list = toLo;
        System.out.println("Get total: " + toLo.size());
        int count=0;
        for (var i : list) {
            if(!data.contains(i)) {
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

    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/cic";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "CpqaFVYJ9Mkz6pOj";

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

        try (BufferedReader br = new BufferedReader(new FileReader("new 2.txt"))) {
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

        try (BufferedReader br = new BufferedReader(new FileReader("new 3.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("/")) {
                    var dataline = line.split("/");
                    String w = dataline[0].trim();
                    if (w != null) {
                        list.add(w);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (BufferedReader br = new BufferedReader(new FileReader("new 4.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("/")) {
                    var dataline = line.split("/");
                    String w = dataline[0].trim();
                    if (w != null) {
                        list.add(w);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
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

    //    public static void main(String[] args) {
//        partArrayCode("sft-wfa-boys-z-0-5.xlsx");
//        partArrayCode("sft-wfa-girls-z-0-5.xlsx");
//        partArrayCode("sft_lhfa_boys_z_0_5.xlsx");
//        partArrayCode("sft_lhfa_girls_z_0_5.xlsx");
//    }
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

    public static void main1(String[] args) {
        int id = 0;
        String topic = "No name";
        HashMap<String, Integer> topics = new HashMap<>();
        HashMap<String, Integer> english = new HashMap<>();
        int enMax = 1;
        int viMax = 1;
        HashMap<String, Integer> vietnamese = new HashMap<>();
        Set<String> vocab = new HashSet<>();
        Set<Vocab> vList = new HashSet<>();
        byte count = 0;
        byte countTopic = 0;
        int countVocab = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
            String line;
            int lineNumber = 1;
            BufferedWriter writer = null;
            while ((line = br.readLine()) != null) {

                if (!containsNumber(line)) {
                    count++;
//                    System.out.println(line);
//                    writer=new BufferedWriter(new FileWriter("data_group"+count+".txt"));
                } else {
                    if (!startsWithDigit(line)) {
//                        System.out.println(line);
                        topic = line;
                        id++;
                        topics.put(line, id);
                        countTopic++;
                    } else {
                        String regex = "(\\d+\\.\\s.*?)(?=(\\d+\\.\\s|$))";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(line);
                        ArrayList<String> result = new ArrayList<>();
                        while (matcher.find()) {
                            result.add(matcher.group(1));
                        }
                        for (String item : result) {
                            String en = "";
                            String vi = "";
//                            System.out.println(item.replaceFirst(". ", ".\t"));
//                            if(item.trim().contains(". "))
                            String row = item.toLowerCase().trim().replaceFirst(" ", "\t");
                            row = removeNumberAndDot(row).replaceFirst("\t", " ");
                            String[] parts = row.split("\\s*/\\s*");
                            if (parts.length > 3) {
                                String e = parts[0].replaceAll(":", "").replace(".", "").trim();
                                if (isEnglish(e)) {
                                    countVocab++;
                                    en = e;
                                    vi = parts[parts.length - 1].replaceAll(":", "");
                                }
//                                else {
//                                    countVocab++;
//                                    System.out.println(parts[0]);
//                                }
                            } else if (parts.length == 3) {
                                String e = parts[0].replaceAll(":", "").trim();
                                if (isEnglish(e)) {
                                    en = e;
                                    vi = parts[2].replaceAll(":", "");
                                    countVocab++;
                                } else {
                                    en = parts[2];
                                    vi = parts[0];
                                    countVocab++;
                                }

                            } else {
                                parts = row.split(":");
                                if (parts.length > 2) {
                                    en = parts[0].trim();
                                    vi = parts[parts.length - 1].trim();
                                    countVocab++;
                                } else if (parts.length == 2) {
                                    en = parts[0].trim();
                                    vi = parts[1].trim();
                                    countVocab++;
                                } else {
                                    parts = row.split("\\(.*?\\)");
                                    if (parts.length == 2) {
                                        en = parts[0].trim();
                                        vi = parts[1].trim();
                                        countVocab++;
                                    }
                                }

                            }
                            en = removeParenthesesContent(en).trim();
                            en = en.split("/")[0];
                            int enID = 0;
                            if (english.containsKey(en)) {
                                enID = english.get(en);
                            } else {
                                enID = enMax;
                                english.put(en, enMax++);
                            }
                            vi = removeParenthesesContent(vi).trim();
                            int viID = 0;
                            if (vietnamese.containsKey(vi)) {
                                viID = vietnamese.get(vi);
                            } else {
                                viID = viMax;
                                vietnamese.put(vi, viMax++);
                            }
                            if (vocab.contains(enID + "," + viID)) {
//                                System.out.println(enID);
                            } else {
                                vList.add(new Vocab(enID, viID, id));
                                vocab.add(enID + "," + viID);
                            }
//                            System.out.println(en+" => "+vi);
                        }
                    }


                }
                lineNumber++;
            }
            System.out.println(countVocab);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (var i : topics.keySet()) {
            System.out.println("INSERT INTO topics value (" + topics.get(i) + ",'" + i + "',1);");
        }
        for (var i : english.keySet()) {
            System.out.println("INSERT INTO english value (" + english.get(i) + ",'" + i + "','" + Request.phonetic(i) + "','" + Request.pronunciationsDictionary(i) + "');");
        }
        for (var i : vietnamese.keySet()) {
            System.out.println("INSERT INTO vietnamese value (" + vietnamese.get(i) + ",'" + i + "');");
        }
        int voID = 1;
        for (var i : vList) {
            String en = "";
            for (var j : english.keySet()) {
                if (i.en == english.get(j)) en = j;
            }
            String vi = "";
            for (var k : vietnamese.keySet()) {
                if (i.vi == vietnamese.get(k)) vi = k;
            }
            System.out.println("INSERT INTO vocabularies (id,en,part_of_speech,img,vi,user) VALUE (" + voID + "," + i.getEn() + ",'',''," + i.getVi() + ",1);");
            System.out.println("INSERT INTO vocabularies_topics value (" + i.to + "," + voID + ");");
            voID++;
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

    private static boolean containsNumber(String line) {
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

    private static boolean startsWithDigit(String line) {
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
            if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')  || ch == '\'' || ch == ' ' || ch == ',' || ch == '-' ||  ch == '&' || ch == '.')) {
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
}
