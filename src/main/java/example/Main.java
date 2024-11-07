package example;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static String readFile(String filePath) {
        StringBuilder content = new StringBuilder(); // Dùng StringBuilder để lưu nội dung file

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("Trang ") && !line.startsWith("\t")) {
                    if (containsNumber(line)) {
                        line = line.replaceFirst("\\s", "\n").trim();
                    }
                    content.append(line).append("\n"); // Thêm từng dòng vào StringBuilder nếu không bắt đầu bằng "Trang "
                }
                ; // Thêm từng dòng vào StringBuilder
            }
        } catch (IOException e) {
            e.printStackTrace(); // Xử lý lỗi khi đọc file
        }

        return content.toString(); // Trả về nội dung của file dưới dạng chuỗi
    }

    public static boolean containsNumber(String str) {
        // Biểu thức chính quy để kiểm tra sự xuất hiện của số trong chuỗi
        return str.matches("\\d.*");
    }

    public static ArrayList<String[]> splitTextByNumber(String text) {
        ArrayList<String[]> result = new ArrayList<>();
        // Biểu thức chính quy để tìm các mục bắt đầu bằng số
        Pattern pattern = Pattern.compile("(\\d+)[\\s\\S]+?(?=\\d|$)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            // Đoạn văn bản giữa hai số tự nhiên
            String entry = matcher.group();
            String[] lines = entry.split("\n");

            if (lines.length > 4) { // Kiểm tra xem có đủ các dòng dữ liệu không
                String word = lines[1].trim().toLowerCase();        // Từ vựng nằm ở dòng thứ 2
                String type = lines[2].trim();        // Loại từ nằm ở dòng thứ 3
                String pronounce = lines[3].trim();   // Phát âm nằm ở dòng thứ 4
                String meaning = String.join(" ", Arrays.copyOfRange(lines, 4, lines.length)).trim(); // Nghĩa từ dòng 5 trở đi

                result.add(new String[]{word, type, pronounce, meaning.toLowerCase()});
            } else if (lines.length == 3) {
                result.add(new String[]{lines[1].trim().toLowerCase(), "unknown", "", lines[2].trim().toLowerCase()});
            } else if (lines.length == 4){
                String word = lines[1].trim().toLowerCase();        // Từ vựng nằm ở dòng thứ 2
                String type = lines[2].trim();
                String phonetic="";
                if(isPhonetic(type)){
                    phonetic = type;
                    type="unknown";
                }
                String meaning = lines[3].trim();
                result.add(new String[]{word, type, phonetic, meaning.toLowerCase()});
            }

        }
        return result;
    }
    private static boolean isPhonetic(String str) {
        // Biểu thức chính quy để kiểm tra sự xuất hiện của ký tự phiên âm
        return str.matches(".*[ˈˌː'´]+.*") || // Dấu nhấn và dấu phân cách
                str.matches(".*[a-zA-Z]+.*") || // Chứa chữ cái
                str.matches(".*[ʊʌɛɔɪɒʊʔʧʤʃʁʋɹɪʔʍʔ]+.*"); // Các ký tự phiên âm khác
    }
    public static void main1(String[] args) {
        var content = readFile("vocabularies clone/3000_Oxford_vocabularies.txt");
//        System.out.println(content);
        ArrayList<String[]> splitData = splitTextByNumber(content);
        ClearData.writeExcel("vocabularies clone/3000_Oxford_vocabularies.xlsx",splitData);
        ClearData.toData("3000_Oxford_vocabularies.xlsx");
    }

    public static void main(String[] args) {
        var db=Test.databaseEnglish();
        var cams = Test.readPdfsInFolder("vocabularies clone/Vocabularies");
//        var cams = Test.readPdfsInFolder("vocabularies clone/Vocabularies");
        var oxf=Oxford.readOxfordVocabularies();
//        var cams=Test.dataBook();
        HashSet<String> myWords = new HashSet<>();
        int count=0;
        for (var i:db) {
            var add=false;
            for (var j = 0; j < cams.size(); j++) {
                if (countOccurrences(cams.get(j),i)>=30) {
                    myWords.add(i);
                    add=true;
                    break;
                }
            }
//            if(oxf.contains(i)){
//                add=true;
//            }
            if(!add){
                count++;
            }
        }
        System.out.println("Vocabularies: "+db.size());
        System.out.println("List: "+myWords.size());
        System.out.println(count);
        System.out.println(db.size()-count);
        Test.writeTopic("English Grammar in Use",myWords,"Topics json/English Grammar in Use");
    }
    public static int countOccurrences(String s1, String s2) {
        if (s2.isEmpty()) {
            return 0; // Nếu s2 rỗng, không có gì để đếm.
        }

        int count = 0;
        int index = 0;

        // Duyệt qua s1 để tìm các lần xuất hiện của s2
        while ((index = s1.indexOf(s2, index)) != -1) {
            count++;
            index += s2.length(); // Tăng index để tránh đếm trùng lặp
        }

        return count;
    }
}
