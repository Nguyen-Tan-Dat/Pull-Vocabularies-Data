import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.By;
import java.time.Duration;
import java.util.List;

public class SeleniumHelper {

    private static WebDriver driver;

    // Constructor: Khởi tạo driver với ChromeDriver đã có sẵn
    public static void setup() {
        // Chỉ định đường dẫn đến ChromeDriver của bạn
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");

        // Cấu hình ChromeOptions
        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless"); // Chạy không giao diện người dùng
//        options.addArguments("--disable-gpu");
//        options.addArguments("--no-sandbox");

        // Khởi tạo WebDriver
        driver = new ChromeDriver(options);
    }

    // Hàm lấy HTML sau khi JavaScript xử lý
    public static String getHTML(String url) {
        // Mở trang web
        driver.get(url);

        // Chờ cho trang tải xong
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));  // Chờ phần tử body xuất hiện

        // Lấy mã nguồn HTML
        return driver.getPageSource();
    }

    // Hàm đóng driver
    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
    public static void getPartOfSpeech(String word) {
        try {
            // Nhập từ khóa vào ô tìm kiếm
            WebElement searchBox = driver.findElement(By.id("q"));
            searchBox.sendKeys(word);
            searchBox.submit();

            // Đợi trang kết quả tải xong
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(50));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.webtop span.pos")));

            // Lấy phần tử chứa từ loại
            WebElement posElement = driver.findElement(By.cssSelector("div.webtop span.pos"));
            System.out.println(word+"\t"+posElement.getText());
            List<WebElement> arl1Elements = driver.findElements(By.cssSelector("ul.list-col li span.arl1"));
            for (WebElement element : arl1Elements) {
                try {
                    // Lấy nội dung trong <pos> bên trong <pos-g> trong <span class="arl1">
                    WebElement posEl = element.findElement(By.cssSelector("pos-g pos"));
                    String posText = posEl.getText();  // ví dụ: noun, verb, ...

                    // Hoặc lấy từ chính (nếu bạn cần) → String wordForm = element.getText().replace(posText, "").trim();
                    System.out.println(word + "\t" + posText);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }
    }
    // Main method để thử nghiệm
    public static  void main(String[] args) {
        SeleniumHelper.setup();
        driver.get("https://www.oxfordlearnersdictionaries.com/");
        SeleniumHelper helper = new SeleniumHelper();
        var list = Elllo.reafile("list.txt");
        int d=0;
        for (var i : list){
            getPartOfSpeech(i);
//            d++;
//            if(d>10)break;
        }
        helper.close();
    }
}
