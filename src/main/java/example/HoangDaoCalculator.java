package example;

import java.util.*;
import com.github.heqiao2010.lunar.LunarCalendar;

public class HoangDaoCalculator {

    // Danh sách 28 chòm sao (Nhị Thập Bát Tú)
    private static final String[] NHI_THAP_BAT_TU = {
            "Giác", "Cang", "Đê", "Phòng", "Tâm", "Vĩ", "Cơ",  // 7 sao phương Đông
            "Đẩu", "Ngưu", "Nữ", "Hư", "Nguy", "Thất", "Bích", // 7 sao phương Bắc
            "Khuê", "Lâu", "Vị", "Mão", "Tất", "Chủy", "Sâm", // 7 sao phương Tây
            "Tỉnh", "Quỷ", "Liễu", "Tinh", "Trương", "Dực", "Chẩn" // 7 sao phương Nam
    };

    // 6 ngày Hoàng Đạo (tốt)
    private static final Set<String> HOANG_DAO = new HashSet<>(Arrays.asList(
            "Thanh Long", "Minh Đường", "Kim Quỹ", "Bảo Quang",
            "Ngọc Đường", "Tư Mệnh"
    ));

    // 6 ngày Hắc Đạo (xấu)
    private static final Set<String> HAC_DAO = new HashSet<>(Arrays.asList(
            "Bạch Hổ", "Thiên Hình", "Chu Tước", "Thiên Lao",
            "Nguyên Vũ", "Huyền Vũ"
    ));

    /**
     * Tính thông tin Hoàng Đạo cho 1 ngày âm lịch
     * @param lunarYear Năm âm lịch
     * @param lunarMonth Tháng âm lịch
     * @param lunarDay Ngày âm lịch
     * @return Map chứa thông tin: sao, loại ngày (Hoàng Đạo/Hắc Đạo)
     */
    public static Map<String, String> calculateHoangDao(int lunarYear, int lunarMonth, int lunarDay) {
        Map<String, String> result = new HashMap<>();

        try {
            // 1. Chuyển sang ngày dương để tính can chi

               Calendar solarDate = LunarCalendar.lunar2Solar(lunarYear, lunarMonth, lunarDay, false);


            // 2. Tính vị trí sao trong 28 chòm sao
            int daysSinceBase = calculateDaysSinceBase(solarDate);
            int saoIndex = daysSinceBase % 28;
            String sao = NHI_THAP_BAT_TU[saoIndex];

            // 3. Xác định Hoàng Đạo hay Hắc Đạo
            String loaiNgay = HOANG_DAO.contains(sao) ? "Hoàng Đạo" :
                    HAC_DAO.contains(sao) ? "Hắc Đạo" : "Bình thường";

            result.put("Sao", sao);
            result.put("Loại ngày", loaiNgay);
            result.put("Ngày âm", lunarDay + "/" + lunarMonth + "/" + lunarYear);
            result.put("Ngày dương", solarDate.get(Calendar.DAY_OF_MONTH) + "/" +
                    (solarDate.get(Calendar.MONTH)+1) + "/" + solarDate.get(Calendar.YEAR));

        } catch (Exception e) {
            result.put("Lỗi", "Ngày âm lịch không hợp lệ");
        }

        return result;
    }

    // Tính số ngày từ ngày cơ sở (dùng để xác định vị trí sao)
    private static int calculateDaysSinceBase(Calendar date) {
        Calendar baseDate = new GregorianCalendar(2000, 0, 1); // Ngày cơ sở
        long diff = date.getTimeInMillis() - baseDate.getTimeInMillis();
        return (int) (diff / (24 * 60 * 60 * 1000));
    }

    public static void main(String[] args) {
        // Test tính ngày Hoàng Đạo
        Map<String, String> ketQua = calculateHoangDao(2025, 6, 18); // 15/6 âm lịch 2025
        System.out.println("Kết quả tra cứu:");
        ketQua.forEach((key, value) -> System.out.println(key + ": " + value));
    }
}