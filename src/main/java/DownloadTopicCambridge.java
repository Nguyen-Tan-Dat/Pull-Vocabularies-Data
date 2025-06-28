import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class DownloadTopicCambridge {

    public static String readFile(String fileName) {
        StringBuilder content = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("https://dictionary.cambridge.org/plus/wordlist/"))
                    content.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return content.toString();
    }

    static ChromeDriver driver;

    public static void setup() {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");

        // Tùy chỉnh Chrome options để sử dụng Brave browser
        ChromeOptions options = new ChromeOptions();

        // Đặt đường dẫn tới Brave Browser
        options.setBinary("C:\\Program Files\\BraveSoftware\\Brave-Browser\\Application\\brave.exe"); // Đảm bảo rằng đường dẫn này đúng

        // Cấu hình các tùy chọn khác nếu cần thiết
        options.addArguments("--window-size=1920,1080");

        // Khởi tạo WebDriver với các tùy chọn Brave
        driver = new ChromeDriver(options);
    }

    public static String getHTML(String url) {
        try {
            driver.get(url);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            for (int i = 0; i < 3; i++) {
                js.executeScript("window.scrollTo(0, " + 700 + ");");
                Thread.sleep(500);
            }
            Thread.sleep(1000);
            return driver.getPageSource();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static String fileName="";
    public static List<List<String>> htmlToData(String html) {
        List<List<String>> result = new ArrayList<>();

        Document doc = Jsoup.parse(html);
        Elements wordEntries = doc.select("#wordlistEntries li.wordlistentry-row");
        fileName=doc.title();
        // Duyệt qua các mục từ và lấy dữ liệu
        for (Element entry : wordEntries) {
            List<String> rowData = new ArrayList<>();

            // Lấy từ
            String word = entry.select(".phrase.haxa").text();
            rowData.add(word);

            // Lấy Part of Speech (Loại từ)
            String pos = "unknown";
            try {
                pos = entry.select(".pos").text();
            } catch (Exception e) {
                pos = ""; // Nếu không có loại từ thì để trống
            }
            rowData.add(pos);
            String level = "";
            try {
                Element levelElement = entry.select(".def-info .epp-xref").first();
                level = levelElement.text();
                rowData.add(level);
                result.add(rowData);
            } catch (Exception e) {
            }
        }

        return result;
    }

    public static void writeDataToExcel(String filePath, List<List<String>> data) {
        File file = new File(filePath);

        // Kiểm tra và tạo thư mục nếu chưa tồn tại
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                System.err.println("Không thể tạo thư mục: " + parentDir.getAbsolutePath());
                return;
            }
        }

        // Tạo workbook và sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Vocabulary Data");

        // Duyệt qua từng hàng dữ liệu và ghi vào sheet
        int rowNum = 0;
        for (List<String> rowData : data) {
            Row row = sheet.createRow(rowNum++);
            for (int col = 0; col < rowData.size(); col++) {
                row.createCell(col).setCellValue(rowData.get(col));
            }
        }

        // Ghi workbook ra file
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
            System.out.println("Đã tạo file Excel thành công tại: " + filePath);
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file Excel: " + e.getMessage());
        } finally {
            // Đóng workbook để giải phóng tài nguyên
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main1(String[] args) {
        setup();
        pullData("https://dictionary.cambridge.org/plus/wordlist/32985378_vocabulary-in-use-advanced-food");
    }

    public static void  pullData(String url) {
        var html = getHTML(url);
        List<List<String>> data = htmlToData(html);
        for (List<String> row : data) {
            System.out.println("Word: " + row.get(0));
            System.out.println("Part of Speech: " + row.get(1));
            System.out.println("Level: " + row.get(2));
            System.out.println("--------------------");
        }
        writeDataToExcel(convertToPath(fileName), data);
    }
    public static String convertToPath(String input) {
        // Tách chuỗi từ dấu '|' để lấy phần trước dấu này
        String[] parts = input.split("\\|");
        if (parts.length > 0) {
            // Xóa khoảng trắng ở đầu cuối của chuỗi
            String relevantPart = parts[0].trim();

            // Tìm vị trí dấu phẩy cuối cùng
            int lastCommaIndex = relevantPart.lastIndexOf(",");
            if (lastCommaIndex != -1) {
                // Thay dấu phẩy cuối cùng bằng dấu '\'
                relevantPart = relevantPart.substring(0, lastCommaIndex) + "\\" + relevantPart.substring(lastCommaIndex + 1).trim();
            }

            // Tạo đường dẫn với đuôi ".xlsx"
            return "Cambridge word lists\\"+ relevantPart + ".xlsx";
        }
        return "";
    }
    public static void main(String[] args) {
        setup();
        try {
            String html = readFile("list.txt");
            Document doc = Jsoup.parse(html);
            var ls = doc.getElementsByTag("a");
            HashSet<String> links = new HashSet<>();
            for (Element link : ls) {
                String href = link.attr("href");
                links.add(href);
            }
            int count = 0;
            for (var i : links) {
//            var ht=DownloadTopic.getHTML(i);
//            var d=Jsoup.parse(ht);
//                System.out.println(i.split("_")[0] + "/export");
//                pullData(i.split("_")[0]);
                count++;
//                break;
            }
            System.out.println(count);
        } finally {
//            driver.quit();
        }
    }
}
