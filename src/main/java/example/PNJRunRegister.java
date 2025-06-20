package example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;

public class PNJRunRegister {

    // Hàm đăng ký người dùng
    public static void  registerUser(WebDriver driver, String email, String fullName, String phoneNumber, String gender) {
        try {
//            // Đợi một chút để trang tải xong
//            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
//
//            // Tìm nút bằng class name
//            WebElement button = driver.findElement(By.cssSelector(".btn.btn-default.secondary"));
//
//            // Click vào nút
//            button.click();
//
//            // Giữ trình duyệt mở 5 giây để kiểm tra
//            Thread.sleep(1000);
//            // Đợi trang tải và tìm tab "TẠO TÀI KHOẢN" bằng văn bản hiển thị
//            WebElement createAccountTab = driver.findElement(By.xpath("//a[contains(text(),'TẠO TÀI KHOẢN')]"));
//
//// Click vào tab
//            createAccountTab.click();
//            Thread.sleep(1000);
//            // Đợi trang tải và tìm ô nhập email
//            WebElement emailInput = driver.findElement(By.name("email"));
//
//// Nhập email
//            emailInput.sendKeys(email);
//// Tìm ô nhập mật khẩu
//            WebElement passwordInput = driver.findElement(By.name("password2"));
//
//// Nhập mật khẩu
//            passwordInput.sendKeys("MySecurePassword123");
//// Tìm ô nhập lại mật khẩu
//            WebElement confirmPasswordInput = driver.findElement(By.name("confirmPassword"));
//
//// Nhập lại mật khẩu
//            confirmPasswordInput.sendKeys("MySecurePassword123");
//// Tìm ô nhập họ tên
//            WebElement fullNameInput1 = driver.findElement(By.name("fname"));
//
//// Nhập họ tên
//            fullNameInput1.sendKeys(fullName);
//            // Tìm nút "ĐĂNG KÝ" và nhấn vào
//            WebElement registerButton = driver.findElement(By.xpath("//button[contains(text(),'ĐĂNG KÝ')]"));
//            registerButton.click();


            // Tìm ô nhập email và nhập địa chỉ email
            WebElement emailInput = driver.findElement(By.name("email"));
            emailInput.sendKeys(email);

            // Tìm ô nhập mật khẩu và nhập mật khẩu
            WebElement passwordInput = driver.findElement(By.name("password1"));
            passwordInput.sendKeys("MySecurePassword123");

            // Tìm nút "ĐĂNG NHẬP" và click vào
            WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'ĐĂNG NHẬP')]"));
            loginButton.click();

            Thread.sleep(5000);

            WebElement genderDropdown = driver.findElement(By.name("gender"));
            // Chọn "Nam" từ dropdown
            Select selectGender = new Select(genderDropdown);
            selectGender.selectByVisibleText(gender);

            // Chọn nhóm tuổi
            WebElement ageDropdown = driver.findElement(By.xpath("//select[contains(@ng-model, '66024708564e8805f8c19d6c')]"));
            Select selectAge = new Select(ageDropdown);
            selectAge.selectByValue("66024708564e8805f8c19d6d");

            // Chọn đơn vị
            WebElement unitDropdown = driver.findElement(By.xpath("//select[contains(@ng-model, '67e0cd1e1a0b561798acd7a9')]"));
            Select selectUnit = new Select(unitDropdown);
            selectUnit.selectByValue("67e0cd1e1a0b561798acd7aa"); // Chọn "Khối Văn phòng Hội sở PNJ"

            // Nhập họ tên
            WebElement fullNameInput = driver.findElements(By.tagName("input")).get(2);
            fullNameInput.sendKeys(fullName.toLowerCase());

            // Nhập số điện thoại
            WebElement phoneNumberInput = driver.findElements(By.tagName("input")).get(3);
            phoneNumberInput.sendKeys(phoneNumber);

            // Nhập mã số nhân viên
            WebElement employeeCodeInput = driver.findElements(By.tagName("input")).get(4);
            employeeCodeInput.sendKeys("Không có");

            // Chọn khoảng cách
            WebElement distanceDropdown = driver.findElement(By.cssSelector("select[ng-model='form.distance']"));
            Select selectDistance = new Select(distanceDropdown);
            selectDistance.selectByVisibleText("37KM");

            // Nhập mã code
            WebElement joinCodeInput = driver.findElement(By.name("joinCode"));
            joinCodeInput.sendKeys("PNJ@2025");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Chỉ định đường dẫn đến ChromeDriver của bạn
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");

        // Khởi tạo WebDriver
        WebDriver driver = new ChromeDriver();

        try {
            // Mở trang web
            driver.get("https://pnjrun.com/");

            // Đợi một chút để trang tải xong
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

            // Tìm nút bằng class name và click vào
            WebElement button = driver.findElement(By.cssSelector(".btn.btn-default.secondary"));
            button.click();

            registerUser(driver, "huongkimtran42@gmail.com", "Trần Kim Hường", "0902402782","Nữ");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Đóng trình duyệt
//            driver.quit();
        }
    }
}
