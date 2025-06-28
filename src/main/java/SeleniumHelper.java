import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.By;
import java.time.Duration;

public class SeleniumHelper {

    private static WebDriver driver;

    // Constructor: Khởi tạo driver với ChromeDriver đã có sẵn
    public static void setup() {
        // Chỉ định đường dẫn đến ChromeDriver của bạn
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");

        // Cấu hình ChromeOptions
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Chạy không giao diện người dùng
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");

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

    // Main method để thử nghiệm
    public static        void main(String[] args) {
        SeleniumHelper helper = new SeleniumHelper();
        String url = "https://www.oxfordlearnersdictionaries.com";
        String html = helper.getHTML(url);
        System.out.println(html);

        helper.close();
    }
}
