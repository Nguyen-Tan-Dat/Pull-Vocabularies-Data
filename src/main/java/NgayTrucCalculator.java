import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class NgayTrucCalculator {

    private static final String[] TRUC_NAMES = {
            "Khai", "Bế" ,  "Kiến", "Trừ", "Mãn", "Bình", "Định", "Chấp",
            "Phá", "Nguy", "Thành", "Thâu"
    };

    // Mốc gốc: ngày 1/1/1900 là ngày trực "Kiến"
    private static final LocalDate BASE_DATE = LocalDate.of(1950, 1, 3);

    public static String getNgayTruc(LocalDate date) {
        long daysBetween = ChronoUnit.DAYS.between(BASE_DATE, date);
        int trucIndex = (int) (daysBetween % 12);
        if (trucIndex < 0) trucIndex += 12; // Trường hợp ngày trước mốc
        return TRUC_NAMES[trucIndex];
    }

    public static void main(String[] args) {
        // Ví dụ tính ngày trực từ 13/07/2025 đến 03/08/2025
        LocalDate start = LocalDate.of(2025, 6, 13);
        LocalDate end = LocalDate.of(2025, 8, 3);

        while (!start.isAfter(end)) {
            String truc = getNgayTruc(start);
            System.out.println(start + " → Trực " + truc);
            start = start.plusDays(1);
        }
    }
}
