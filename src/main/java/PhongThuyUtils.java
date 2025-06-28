import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class PhongThuyUtils {


    private static final String[] THIEN_CAN = {"Canh", "Tân", "Nhâm", "Quý", "Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ"};
    private static final String[] DIA_CHI = {"Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi", "Tý"};

    public static String tinhCanChi(int year) {
        int canIndex = year % 10;
        String thienCan = THIEN_CAN[canIndex];
        int chiIndex = (year + 7) % 12;
        String diaChi = DIA_CHI[chiIndex];
        return thienCan + " " + diaChi;
    }

    public static String tinhCanChiThang(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }
        String diaChi = DIA_CHI[month % 12];
        int canNam = year % 10;
        int nhomCan = (canNam + 6) % 10;
        String canThangGieng = switch (nhomCan) {
            case 0, 5 -> "Bính";
            case 1, 6 -> "Mậu";
            case 2, 7 -> "Canh";
            case 3, 8 -> "Nhâm";
            case 4, 9 -> "Giáp";
            default -> "";
        };

        int indexCanThangGieng = indexOf(THIEN_CAN, canThangGieng);
        int indexCanThang = (indexCanThangGieng + month - 1) % 10;
        String thienCan = THIEN_CAN[indexCanThang];

        return thienCan + " " + diaChi;
    }

    // Hàm phụ trợ tìm index trong mảng
    private static int indexOf(String[] arr, String value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    private static final LocalDate NGAY_MOC = LocalDate.of(1, 1, 1);
    private static final int CAN_NGAY_MOC = 8; // Nhâm (index 8 trong THIEN_CAN)
    private static final int CHI_NGAY_MOC = 0; // Tý (index 0 trong DIA_CHI)

    public static String tinhCanChiNgay(LocalDate date) {
        long soNgay = ChronoUnit.DAYS.between(NGAY_MOC, date);

        int canIndex = (CAN_NGAY_MOC + (int) (soNgay % 10) + 1) % 10;
        int chiIndex = (CHI_NGAY_MOC + (int) (soNgay % 12) + 2) % 12;

        return THIEN_CAN[canIndex] + " " + DIA_CHI[chiIndex];
    }

    public static String tinhCanChiNgay(int day, int month, int year) {
        LocalDate date = LocalDate.of(year, month, day);
        return tinhCanChiNgay(date);
    }

    private static final String[] DANH_SACH_TRUC = {"Kiến", "Trừ", "Mãn", "Bình", "Định", "Chấp", "Phá", "Nguy", "Thành", "Thu", "Khai", "Bế"};


    public static String tinhNgayTruc(int year, int month, int day) {
        var lunar = LunarDateConverter.convertSolarToLunar(year, month, day);
        var thangAm = lunar[1];
        var ngayAm = lunar[2];
        if (thangAm < 1 || thangAm > 12) {
            throw new IllegalArgumentException("Tháng Âm lịch phải từ 1 đến 12");
        }
        if (ngayAm < 1 || ngayAm > 30) {
            throw new IllegalArgumentException("Ngày Âm lịch phải từ 1 đến 30");
        }
        LocalDate date = LocalDate.of(year, month, day);
        long soNgay = ChronoUnit.DAYS.between(NGAY_MOC, date);


        int chiIndex = (CHI_NGAY_MOC + (int) (soNgay % 12) + 2) % 12;
        if (chiIndex < 0) chiIndex += 12;
        var diaChiNgay = DIA_CHI[chiIndex];
        String diaChiThang = DIA_CHI[thangAm % 12];

        int indexTrucKien = timIndexDiaChi(diaChiThang);
        int indexNgay = (timIndexDiaChi(diaChiNgay) + 12);
        int indexTruc = (indexNgay - indexTrucKien) % 12;
        if (indexTruc < 0) indexTruc += 12;

        return DANH_SACH_TRUC[indexTruc];
    }

    private static int timIndexDiaChi(String diaChi) {
        for (int i = 0; i < DIA_CHI.length; i++) {
            if (DIA_CHI[i].equals(diaChi)) {
                return i;
            }
        }
        return -1;
    }

    private static final String[][] CAC_CAP_TUONG_HOP = {
            {"Tý", "Sửu"}, {"Dần", "Hợi"}, {"Mão", "Tuất"},
            {"Thìn", "Dậu"}, {"Tỵ", "Thân"}, {"Ngọ", "Mùi"}
    };

    // Các cặp Địa Chi tương xung
    private static final String[][] CAC_CAP_TUONG_XUNG = {
            {"Tý", "Ngọ"}, {"Sửu", "Mùi"}, {"Dần", "Thân"},
            {"Mão", "Dậu"}, {"Thìn", "Tuất"}, {"Tỵ", "Hợi"}
    };

    // Các sao tốt (Cát tinh)
    private static final String[] CAT_TINH = {
            "Thiên Đức", "Nguyệt Đức", "Thiên Hỷ", "Nguyệt Hỷ",
            "Thiên Quý", "Nguyệt Quý", "Thiên Phúc", "Nguyệt Phúc",
            "Thiên Mã", "Nguyệt Ân", "Tam Hợp", "Lục Hợp"
    };

    // Các sao xấu (Hung tinh)
    private static final String[] HUNG_TINH = {
            "Sát Chủ", "Nguyệt Kiến", "Thiên Cương", "Nguyệt Hình",
            "Nguyệt Phá", "Nguyệt Yểm", "Tứ Ly", "Tứ Tuyệt",
            "Hắc Đạo", "Bạch Hổ", "Huyền Vũ", "Chu Tước"
    };

    // Danh sách các ngày Hoàng Đạo (tốt) và Hắc Đạo (xấu)
    private static final String[] HOANG_DAO = {
            "Tý", "Sửu", "Thìn", "Tỵ", "Mùi", "Thân", "Dậu", "Tuất"
    };

    private static final String[] HAC_DAO = {
            "Dần", "Mão", "Ngọ", "Hợi"
    };

    // Hàm kiểm tra ngày Hoàng Đạo
    public static boolean laNgayHoangDao(LocalDate date) {
        String canChiNgay = tinhCanChiNgay(date);
        String diaChiNgay = canChiNgay.split(" ")[1];

        for (String chi : HOANG_DAO) {
            if (chi.equals(diaChiNgay)) {
                return true;
            }
        }
        return false;
    }

    // Hàm kiểm tra ngày Hắc Đạo
    public static boolean laNgayHacDao(LocalDate date) {
        String canChiNgay = tinhCanChiNgay(date);
        String diaChiNgay = canChiNgay.split(" ")[1];

        for (String chi : HAC_DAO) {
            if (chi.equals(diaChiNgay)) {
                return true;
            }
        }
        return false;
    }

    // Hàm kiểm tra sự tương hợp giữa 2 Địa Chi
    public static boolean kiemTraTuongHop(String diaChi1, String diaChi2) {
        for (String[] cap : CAC_CAP_TUONG_HOP) {
            if ((cap[0].equals(diaChi1) && cap[1].equals(diaChi2)) ||
                    (cap[0].equals(diaChi2) && cap[1].equals(diaChi1))) {
                return true;
            }
        }
        return false;
    }

    // Hàm kiểm tra sự tương xung giữa 2 Địa Chi
    public static boolean kiemTraTuongXung(String diaChi1, String diaChi2) {
        for (String[] cap : CAC_CAP_TUONG_XUNG) {
            if ((cap[0].equals(diaChi1) && cap[1].equals(diaChi2)) ||
                    (cap[0].equals(diaChi2) && cap[1].equals(diaChi1))) {
                return true;
            }
        }
        return false;
    }

    // Hàm lấy danh sách sao tốt (Cát tinh) trong ngày
    public static List<String> layCatTinh(LocalDate date) {
        List<String> catTinh = new ArrayList<>();
        String canChiNgay = tinhCanChiNgay(date);
        String diaChiNgay = canChiNgay.split(" ")[1];

        // Thêm một số sao tốt dựa trên Địa Chi ngày
        if (diaChiNgay.equals("Tý") || diaChiNgay.equals("Thân") || diaChiNgay.equals("Thìn")) {
            catTinh.add("Tam Hợp");
        }
        if (diaChiNgay.equals("Sửu")) {
            catTinh.add("Thiên Đức");
        }
        if (diaChiNgay.equals("Dần")) {
            catTinh.add("Thiên Mã");
        }

        // Thêm ngẫu nhiên một số sao tốt khác (ví dụ)
        Random random = new Random(date.toEpochDay());
        int soSaoTot = random.nextInt(3) + 1;
        for (int i = 0; i < soSaoTot; i++) {
            String sao = CAT_TINH[random.nextInt(CAT_TINH.length)];
            if (!catTinh.contains(sao)) {
                catTinh.add(sao);
            }
        }

        return catTinh;
    }

    // Hàm lấy danh sách sao xấu (Hung tinh) trong ngày
    public static List<String> layHungTinh(LocalDate date) {
        List<String> hungTinh = new ArrayList<>();
        String canChiNgay = tinhCanChiNgay(date);
        String diaChiNgay = canChiNgay.split(" ")[1];

        // Thêm một số sao xấu dựa trên Địa Chi ngày
        if (diaChiNgay.equals("Ngọ")) {
            hungTinh.add("Bạch Hổ");
        }
        if (diaChiNgay.equals("Hợi")) {
            hungTinh.add("Huyền Vũ");
        }
        if (laNgayHacDao(date)) {
            hungTinh.add("Hắc Đạo");
        }

        // Thêm ngẫu nhiên một số sao xấu khác (ví dụ)
        Random random = new Random(date.toEpochDay());
        int soSaoXau = random.nextInt(2) + 1;
        for (int i = 0; i < soSaoXau; i++) {
            String sao = HUNG_TINH[random.nextInt(HUNG_TINH.length)];
            if (!hungTinh.contains(sao)) {
                hungTinh.add(sao);
            }
        }

        return hungTinh;
    }

    // Hàm đánh giá tổng quan ngày (tốt/xấu)
    public static String danhGiaNgay(LocalDate date) {
        boolean hoangDao = laNgayHoangDao(date);
        List<String> catTinh = layCatTinh(date);
        List<String> hungTinh = layHungTinh(date);

        int diem = 0;
        if (hoangDao) diem += 2;
        diem += catTinh.size();
        diem -= hungTinh.size();

        if (diem >= 3) return "Rất tốt";
        if (diem >= 1) return "Tốt";
        if (diem == 0) return "Bình thường";
        return "Xấu";
    }

    public static boolean laNgayKimDuong(LocalDate date) {
        // Chuyển đổi ngày dương sang âm lịch
        int[] lunarDate = LunarDateConverter.convertSolarToLunar(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth()
        );

        int lunarMonth = lunarDate[1]; // Tháng âm lịch
        int lunarDay = lunarDate[2];   // Ngày âm lịch

        // Lấy Địa Chi của ngày
        String canChiNgay = tinhCanChiNgay(date);
        String diaChiNgay = canChiNgay.split(" ")[1]; // Phần Địa Chi

        // Danh sách các Địa Chi tương ứng với Kim Đường theo từng tháng
        Map<Integer, String> kimDuongChi = new HashMap<>();
        kimDuongChi.put(1, "Tỵ");
        kimDuongChi.put(2, "Mùi");
        kimDuongChi.put(3, "Dậu");
        kimDuongChi.put(4, "Hợi");
        kimDuongChi.put(5, "Sửu");
        kimDuongChi.put(6, "Mão");
        kimDuongChi.put(7, "Tỵ");
        kimDuongChi.put(8, "Mùi");
        kimDuongChi.put(9, "Dậu");
        kimDuongChi.put(10, "Hợi");
        kimDuongChi.put(11, "Sửu");
        kimDuongChi.put(12, "Mão");

        // Kiểm tra nếu Địa Chi ngày trùng với Địa Chi Kim Đường của tháng
        return diaChiNgay.equals(kimDuongChi.get(lunarMonth));
    }

    public static boolean laNgayTuMenhHoangDao(LocalDate date) {
        // Chuyển đổi ngày dương sang âm lịch
        int[] lunarDate = LunarDateConverter.convertSolarToLunar(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth()
        );

        int lunarMonth = lunarDate[1]; // Tháng âm lịch
        int lunarDay = lunarDate[2];   // Ngày âm lịch

        // Lấy Địa Chi của ngày
        String canChiNgay = tinhCanChiNgay(date);
        String diaChiNgay = canChiNgay.split(" ")[1]; // Phần Địa Chi

        // Danh sách các Địa Chi tương ứng với Tư Mệnh theo từng tháng
        Map<Integer, String> tuMenhChi = new HashMap<>();
        tuMenhChi.put(1, "Tuất");
        tuMenhChi.put(2, "Tý");
        tuMenhChi.put(3, "Dần");
        tuMenhChi.put(4, "Thìn");
        tuMenhChi.put(5, "Ngọ");
        tuMenhChi.put(6, "Thân");
        tuMenhChi.put(7, "Tuất");
        tuMenhChi.put(8, "Tý");
        tuMenhChi.put(9, "Dần");
        tuMenhChi.put(10, "Thìn");
        tuMenhChi.put(11, "Ngọ");
        tuMenhChi.put(12, "Thân");

        // Kiểm tra nếu Địa Chi ngày trùng với Địa Chi Tư Mệnh của tháng
        return diaChiNgay.equals(tuMenhChi.get(lunarMonth));
    }
    public static boolean laNgayKimQuyHoangDao(LocalDate date) {
        // Chuyển đổi ngày dương sang âm lịch
        int[] lunarDate = LunarDateConverter.convertSolarToLunar(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth()
        );

        int lunarMonth = lunarDate[1]; // Tháng âm lịch

        // Lấy Địa Chi của ngày
        String canChiNgay = tinhCanChiNgay(date);
        String diaChiNgay = canChiNgay.split(" ")[1]; // Phần Địa Chi

        // Danh sách các Địa Chi tương ứng với Kim Quỹ theo từng tháng
        Map<Integer, String> kimQuyChi = Map.ofEntries(
                Map.entry(1, "Thìn"),
                Map.entry(2, "Ngọ"),
                Map.entry(3, "Thân"),
                Map.entry(4, "Tuất"),
                Map.entry(5, "Tý"),
                Map.entry(6, "Dần"),
                Map.entry(7, "Thìn"),
                Map.entry(8, "Ngọ"),
                Map.entry(9, "Thân"),
                Map.entry(10, "Tuất"),
                Map.entry(11, "Tý"),
                Map.entry(12, "Dần")
        );

        // Kiểm tra nếu Địa Chi ngày trùng với Địa Chi Kim Quỹ của tháng
        return diaChiNgay.equals(kimQuyChi.get(lunarMonth));
    }
    public static boolean laNgayThanhLongHoangDao(LocalDate date) {
        // Chuyển đổi ngày dương sang âm lịch
        int[] lunarDate = LunarDateConverter.convertSolarToLunar(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth()
        );

        int lunarMonth = lunarDate[1]; // Tháng âm lịch

        // Lấy Địa Chi của ngày
        String canChiNgay = tinhCanChiNgay(date);
        String diaChiNgay = canChiNgay.split(" ")[1]; // Phần Địa Chi

        // Danh sách các Địa Chi tương ứng với Thanh Long theo từng tháng
        Map<Integer, String> thanhLongChi = Map.ofEntries(
                Map.entry(1, "Tý"),
                Map.entry(2, "Dần"),
                Map.entry(3, "Thìn"),
                Map.entry(4, "Ngọ"),
                Map.entry(5, "Thân"),
                Map.entry(6, "Tuất"),
                Map.entry(7, "Tý"),
                Map.entry(8, "Dần"),
                Map.entry(9, "Thìn"),
                Map.entry(10, "Ngọ"),
                Map.entry(11, "Thân"),
                Map.entry(12, "Tuất")
        );

        // Kiểm tra nếu Địa Chi ngày trùng với Địa Chi Thanh Long của tháng
        return diaChiNgay.equals(thanhLongChi.get(lunarMonth));
    }
    public static boolean laNgayNgocDuongHoangDao(LocalDate date) {
        // Chuyển đổi ngày dương sang âm lịch
        int[] lunarDate = LunarDateConverter.convertSolarToLunar(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth()
        );

        int lunarMonth = lunarDate[1]; // Tháng âm lịch

        // Lấy Địa Chi của ngày
        String canChiNgay = tinhCanChiNgay(date);
        String diaChiNgay = canChiNgay.split(" ")[1]; // Phần Địa Chi

        // Danh sách các Địa Chi tương ứng với Ngọc Đường theo từng tháng
        Map<Integer, String> ngocDuongChi = Map.ofEntries(
                Map.entry(1, "Sửu"),
                Map.entry(2, "Mão"),
                Map.entry(3, "Tỵ"),
                Map.entry(4, "Mùi"),
                Map.entry(5, "Dậu"),
                Map.entry(6, "Hợi"),
                Map.entry(7, "Sửu"),
                Map.entry(8, "Mão"),
                Map.entry(9, "Tỵ"),
                Map.entry(10, "Mùi"),
                Map.entry(11, "Dậu"),
                Map.entry(12, "Hợi")
        );

        // Kiểm tra nếu Địa Chi ngày trùng với Địa Chi Ngọc Đường của tháng
        return diaChiNgay.equals(ngocDuongChi.get(lunarMonth));
    }

    public static boolean laNgayMinhDuongHoangDao(LocalDate date) {
        // Chuyển đổi ngày dương sang âm lịch
        int[] lunarDate = LunarDateConverter.convertSolarToLunar(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth()
        );

        int lunarMonth = lunarDate[1]; // Tháng âm lịch

        // Lấy Địa Chi của ngày
        String canChiNgay = tinhCanChiNgay(date);
        String diaChiNgay = canChiNgay.split(" ")[1]; // Phần Địa Chi

        // Danh sách các Địa Chi tương ứng với Minh Đường theo từng tháng
        Map<Integer, String> minhDuongChi = Map.ofEntries(
                Map.entry(1, "Sửu"),
                Map.entry(2, "Mão"),
                Map.entry(3, "Tỵ"),
                Map.entry(4, "Mùi"),
                Map.entry(5, "Dậu"),
                Map.entry(6, "Hợi"),
                Map.entry(7, "Sửu"),
                Map.entry(8, "Mão"),
                Map.entry(9, "Tỵ"),
                Map.entry(10, "Mùi"),
                Map.entry(11, "Dậu"),
                Map.entry(12, "Hợi")
        );

        // Kiểm tra nếu Địa Chi ngày trùng với Địa Chi Minh Đường của tháng
        return diaChiNgay.equals(minhDuongChi.get(lunarMonth));
    }
    public static boolean laNgayBachHoHacDao(LocalDate date) {
        // Chuyển đổi ngày dương sang âm lịch
        int[] lunarDate = LunarDateConverter.convertSolarToLunar(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth()
        );

        int lunarMonth = lunarDate[1]; // Tháng âm lịch

        // Lấy Địa Chi của ngày
        String canChiNgay = tinhCanChiNgay(date);
        String diaChiNgay = canChiNgay.split(" ")[1]; // Phần Địa Chi

        // Danh sách các Địa Chi tương ứng với Bạch Hổ theo từng tháng
        Map<Integer, String> bachHoChi = Map.ofEntries(
                Map.entry(1, "Ngọ"),
                Map.entry(2, "Thân"),
                Map.entry(3, "Tuất"),
                Map.entry(4, "Tý"),
                Map.entry(5, "Dần"),
                Map.entry(6, "Thìn"),
                Map.entry(7, "Ngọ"),
                Map.entry(8, "Thân"),
                Map.entry(9, "Tuất"),
                Map.entry(10, "Tý"),
                Map.entry(11, "Dần"),
                Map.entry(12, "Thìn")
        );

        // Kiểm tra nếu Địa Chi ngày trùng với Địa Chi Bạch Hổ của tháng
        return diaChiNgay.equals(bachHoChi.get(lunarMonth));
    }
    public static boolean laNgayChuTocHacDao(LocalDate date) {
        // Chuyển đổi ngày dương sang âm lịch
        int[] lunarDate = LunarDateConverter.convertSolarToLunar(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth()
        );

        int lunarMonth = lunarDate[1]; // Tháng âm lịch

        // Lấy Địa Chi của ngày
        String canChiNgay = tinhCanChiNgay(date);
        String diaChiNgay = canChiNgay.split(" ")[1]; // Phần Địa Chi

        // Danh sách các Địa Chi tương ứng với Chu Tước theo từng tháng
        Map<Integer, String> chuTocChi = Map.ofEntries(
                Map.entry(1, "Mão"),
                Map.entry(2, "Tỵ"),
                Map.entry(3, "Mùi"),
                Map.entry(4, "Dậu"),
                Map.entry(5, "Hợi"),
                Map.entry(6, "Sửu"),
                Map.entry(7, "Mão"),
                Map.entry(8, "Tỵ"),
                Map.entry(9, "Mùi"),
                Map.entry(10, "Dậu"),
                Map.entry(11, "Hợi"),
                Map.entry(12, "Sửu")
        );

        // Kiểm tra nếu Địa Chi ngày trùng với Địa Chi Chu Tước của tháng
        return diaChiNgay.equals(chuTocChi.get(lunarMonth));
    }
    public static boolean laNgayCauTranHacDao(LocalDate date) {
        // Chuyển đổi ngày dương sang âm lịch
        int[] lunarDate = LunarDateConverter.convertSolarToLunar(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth()
        );

        int lunarMonth = lunarDate[1]; // Tháng âm lịch

        // Lấy Địa Chi của ngày
        String canChiNgay = tinhCanChiNgay(date);
        String diaChiNgay = canChiNgay.split(" ")[1]; // Phần Địa Chi

        // Danh sách các Địa Chi tương ứng với Câu Trần theo từng tháng
        Map<Integer, String> cauTranChi = Map.ofEntries(
                Map.entry(1, "Hợi"),
                Map.entry(2, "Sửu"),
                Map.entry(3, "Mão"),
                Map.entry(4, "Tỵ"),
                Map.entry(5, "Mùi"),
                Map.entry(6, "Dậu"),
                Map.entry(7, "Hợi"),
                Map.entry(8, "Sửu"),
                Map.entry(9, "Mão"),
                Map.entry(10, "Tỵ"),
                Map.entry(11, "Mùi"),
                Map.entry(12, "Dậu")
        );

        // Kiểm tra nếu Địa Chi ngày trùng với Địa Chi Câu Trần của tháng
        return diaChiNgay.equals(cauTranChi.get(lunarMonth));
    }
    public static boolean laNgayThienLaoHacDao(LocalDate date) {
        // Chuyển đổi ngày dương sang âm lịch
        int[] lunarDate = LunarDateConverter.convertSolarToLunar(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth()
        );

        int lunarMonth = lunarDate[1]; // Tháng âm lịch

        // Lấy Địa Chi của ngày
        String canChiNgay = tinhCanChiNgay(date);
        String diaChiNgay = canChiNgay.split(" ")[1]; // Phần Địa Chi

        // Danh sách các Địa Chi tương ứng với Thiên Lao theo từng tháng
        Map<Integer, String> thienLaoChi = Map.ofEntries(
                Map.entry(1, "Thân"),
                Map.entry(2, "Tuất"),
                Map.entry(3, "Tý"),
                Map.entry(4, "Dần"),
                Map.entry(5, "Thìn"),
                Map.entry(6, "Ngọ"),
                Map.entry(7, "Thân"),
                Map.entry(8, "Tuất"),
                Map.entry(9, "Tý"),
                Map.entry(10, "Dần"),
                Map.entry(11, "Thìn"),
                Map.entry(12, "Ngọ")
        );

        // Kiểm tra nếu Địa Chi ngày trùng với Địa Chi Thiên Lao của tháng
        return diaChiNgay.equals(thienLaoChi.get(lunarMonth));
    }
    public static boolean laNgayThienHinhHacDao(LocalDate date) {
        // Chuyển đổi ngày dương sang âm lịch
        int[] lunarDate = LunarDateConverter.convertSolarToLunar(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth()
        );

        int lunarMonth = lunarDate[1]; // Tháng âm lịch

        // Lấy Địa Chi của ngày
        String canChiNgay = tinhCanChiNgay(date);
        String diaChiNgay = canChiNgay.split(" ")[1]; // Phần Địa Chi

        // Bản đồ tháng âm lịch -> Địa Chi Thiên Hình hắc đạo
        Map<Integer, String> thienHinhChi = Map.ofEntries(
                Map.entry(1, "Tuất"),
                Map.entry(2, "Thìn"),
                Map.entry(3, "Ngọ"),
                Map.entry(4, "Thân"),
                Map.entry(5, "Tuất"),
                Map.entry(6, "Tỵ"),
                Map.entry(7, "Dần"),
                Map.entry(8, "Thìn"),
                Map.entry(9, "Ngọ"),
                Map.entry(10, "Thân"),
                Map.entry(11, "Tuất"),
                Map.entry(12, "Tỵ")
        );

        // So sánh địa chi ngày với địa chi Thiên Hình hắc đạo theo tháng
        return diaChiNgay.equals(thienHinhChi.get(lunarMonth));
    }
    public static boolean laNgayNguyenVuHacDao(LocalDate date) {
        // Chuyển đổi ngày dương sang âm lịch
        int[] lunarDate = LunarDateConverter.convertSolarToLunar(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth()
        );

        int lunarMonth = lunarDate[1]; // Tháng âm lịch

        // Lấy Địa Chi của ngày
        String canChiNgay = tinhCanChiNgay(date);
        String diaChiNgay = canChiNgay.split(" ")[1]; // Phần Địa Chi

        // Bản đồ tháng âm lịch -> Địa Chi Nguyên Vu hắc đạo
        Map<Integer, String> nguyenVuChi = Map.ofEntries(
                Map.entry(1, "Tỵ"),     // Tháng Một
                Map.entry(2, "Hợi"),    // Tháng Hai
                Map.entry(3, "Sửu"),    // Tháng Ba
                Map.entry(4, "Mão"),    // Tháng Tư
                Map.entry(5, "Tỵ"),     // Tháng Năm
                Map.entry(6, "Mùi"),    // Tháng Sáu
                Map.entry(7, "Dậu"),    // Tháng Bảy
                Map.entry(8, "Hợi"),    // Tháng Tám
                Map.entry(9, "Sửu"),    // Tháng Chín
                Map.entry(10, "Mão"),   // Tháng Mười
                Map.entry(11, "Tỵ"),    // Tháng Mười Một
                Map.entry(12, "Mùi")    // Tháng Chạp
        );

        // Kiểm tra nếu địa chi ngày trùng với quy định của Nguyên Vu hắc đạo
        return diaChiNgay.equals(nguyenVuChi.get(lunarMonth));
    }
    private static final String[] LUC_DIEU = {
            "Đại An", "Lưu Niên", "Tốc Hỷ", "Xích Khẩu", "Tiểu Cát", "Không Vong"
    };

    // Bảng cung khởi đầu của mỗi tháng âm lịch (tháng 1-12)
    private static final Map<Integer, Integer> THANG_AM_START_CUNG_INDEX = Map.ofEntries(
            Map.entry(1, 0),   // Đại An
            Map.entry(2, 1),   // Lưu Niên
            Map.entry(3, 2),   // Tốc Hỷ
            Map.entry(4, 3),   // Xích Khẩu
            Map.entry(5, 4),   // Tiểu Cát
            Map.entry(6, 5),   // Không Vong
            Map.entry(7, 0),   // Đại An
            Map.entry(8, 1),   // Lưu Niên
            Map.entry(9, 2),   // Tốc Hỷ
            Map.entry(10, 3),  // Xích Khẩu
            Map.entry(11, 4),  // Tiểu Cát
            Map.entry(12, 5)   // Không Vong
    );

    /**
     * Trả về tên cung Lục Diệu tương ứng với ngày dương lịch.
     *
     * @param date ngày dương lịch (LocalDate)
     * @return tên cung Lục Diệu
     */
    public static String layLucDieu(LocalDate date) {
        // Chuyển sang ngày âm lịch
        int[] lunarDate = LunarDateConverter.convertSolarToLunar(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth()
        );

        int lunarDay = lunarDate[2];   // Ngày âm
        int lunarMonth = lunarDate[1]; // Tháng âm

        // Lấy chỉ số cung bắt đầu của tháng
        int startIndex = THANG_AM_START_CUNG_INDEX.getOrDefault(lunarMonth, 0);

        // Tính chỉ số của ngày
        int lucDieuIndex = (startIndex + (lunarDay - 1)) % 6;

        return LUC_DIEU[lucDieuIndex];
    }
//    private static final String[] LUC_DIEU = {
//            "Đại An", "Lưu Niên", "Tốc Hỷ", "Xích Khẩu", "Tiểu Cát", "Không Vong"
//    };
//
//    private static final Map<Integer, Integer> THANG_AM_START_CUNG_INDEX = Map.ofEntries(
//            Map.entry(1, 0), Map.entry(2, 1), Map.entry(3, 2),
//            Map.entry(4, 3), Map.entry(5, 4), Map.entry(6, 5),
//            Map.entry(7, 0), Map.entry(8, 1), Map.entry(9, 2),
//            Map.entry(10, 3), Map.entry(11, 4), Map.entry(12, 5)
//    );

    private static final String[] GIO_CAN_CHI = {
            "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ",
            "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi"
    };

//    public static String layLucDieu(LocalDate date) {
//        int[] lunarDate = LunarDateConverter.convertSolarToLunar(
//                date.getYear(), date.getMonthValue(), date.getDayOfMonth()
//        );
//        int lunarDay = lunarDate[2];
//        int lunarMonth = lunarDate[1];
//        int startIndex = THANG_AM_START_CUNG_INDEX.getOrDefault(lunarMonth, 0);
//        int lucDieuIndex = (startIndex + (lunarDay - 1)) % 6;
//        return LUC_DIEU[lucDieuIndex];
//    }

    /**
     * Lấy Lục Diệu của một giờ cụ thể trong ngày
     *
     * @param date Ngày dương lịch
     * @param hour Giờ trong ngày (0–23)
     * @return Tên cung Lục Diệu ứng với giờ
     */
    public static String layLucDieuGio(LocalDate date, int hour) {
        String lucDieuNgay = layLucDieu(date); // ví dụ: "Lưu Niên"
        int startIndex = Arrays.asList(LUC_DIEU).indexOf(lucDieuNgay);

        // Xác định giờ theo chi (0: Tý, 1: Sửu, ..., 11: Hợi)
        int gioIndex = getChiIndexFromHour(hour);

        int gioLucDieuIndex = (startIndex + gioIndex) % 6;

        return LUC_DIEU[gioLucDieuIndex];
    }

    /**
     * Chuyển giờ (0–23) sang thứ tự chi giờ (0: Tý, 1: Sửu, ..., 11: Hợi)
     */
    private static int getChiIndexFromHour(int hour) {
        if (hour >= 23 || hour < 1) return 0;   // Tý
        else if (hour < 3) return 1;            // Sửu
        else if (hour < 5) return 2;            // Dần
        else if (hour < 7) return 3;            // Mão
        else if (hour < 9) return 4;            // Thìn
        else if (hour < 11) return 5;           // Tỵ
        else if (hour < 13) return 6;           // Ngọ
        else if (hour < 15) return 7;           // Mùi
        else if (hour < 17) return 8;           // Thân
        else if (hour < 19) return 9;           // Dậu
        else if (hour < 21) return 10;          // Tuất
        else return 11;                         // Hợi
    }
    private static final String[] NHI_THAP_BAT_TU = {
            "Giác", "Cang", "Đê", "Phòng", "Tâm", "Vĩ", "Cơ",       // Thanh Long
            "Đẩu", "Ngưu", "Nữ", "Hư", "Nguy", "Thất", "Bích",     // Huyền Vũ
            "Khuê", "Lâu", "Vị", "Mão", "Tất", "Chuỷ", "Sâm",       // Bạch Hổ
            "Tỉnh", "Quỷ", "Liễu", "Tinh", "Trương", "Dực", "Chẩn"  // Chu Tước
    };

    // Mốc gốc: Ngày 1/1/2000 được quy ước là sao "Giác"
    private static final LocalDate BASE_DATE = LocalDate.of(1, 1, 1);

    public static String getSaoNhiThapBatTu(LocalDate date) {
        long daysBetween = ChronoUnit.DAYS.between(BASE_DATE, date);
        int index = (int) ((daysBetween+25 )% 28);
        if (index < 0) index += 28; // đảm bảo dương
        return NHI_THAP_BAT_TU[index];
    }

    public static void main(String[] args) {
        LocalDate inputDate = LocalDate.of(2000, 5, 16);
        String sao = getSaoNhiThapBatTu(inputDate);
        System.out.println("Ngày " + inputDate + " ứng với sao Nhị Thập Bát Tú: " + sao);
    }
    public static void main2(String[] args) {
        LocalDate date = LocalDate.of(2025, 7, 21); // Ngày dương
        int hour = 13; // 10 giờ sáng

        String lucDieuGio = layLucDieuGio(date, hour);
        System.out.println("Lục Diệu của giờ là: " + lucDieuGio);
    }

    public static void main1(String[] args) {
        int nam = 2025;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate ngayBatDau = LocalDate.of(nam, 1, 1);
        LocalDate ngayKetThuc = LocalDate.of(nam, 12, 31);

        System.out.println("Duyệt các ngày trong năm " + nam + ":");
        System.out.println("--------------------------------------------------------------------------------------------------");
        System.out.println("|   Ngày Dương  |   Ngày Âm   |      Can Chi      |  Trực  | Hoàng Đạo |  Đánh giá |  Đánh giá | |  Lục Diệu |");
        System.out.println("--------------------------------------------------------------------------------------------------");

        LocalDate ngayHienTai = ngayBatDau;
        while (!ngayHienTai.isAfter(ngayKetThuc)) {
            int d = ngayHienTai.getDayOfMonth();
            int m = ngayHienTai.getMonthValue();
            int y = ngayHienTai.getYear();

            String amLich = LunarDateConverter.convertSolarToLunarString(y, m, d);
            String canChiNgay = tinhCanChiNgay(d, m, y);
            String truc = tinhNgayTruc(y, m, d);

            boolean hoangDao = laNgayHoangDao(ngayHienTai);
            String danhGia = danhGiaNgay(ngayHienTai);

            System.out.printf("| %13s | %11s | %17s | %6s | %9s | %9s | %17s | %17s \n",
                    ngayHienTai.format(formatter),
                    amLich,
                    canChiNgay,
                    truc,
                    hoangDao ? "Có" : "Không",
                    danhGia,
                    laNgayKimDuong(ngayHienTai) ? "Kim đường hoàn đạo" :
                            laNgayTuMenhHoangDao(ngayHienTai) ? "Tư mệnh hoàng đạo" :
                                    laNgayThanhLongHoangDao(ngayHienTai) ? "Thanh long hoàng đạo" :
                                            laNgayKimQuyHoangDao(ngayHienTai)?"Kim quỷ hoàng đạo" :
                                                    laNgayNgocDuongHoangDao(ngayHienTai)?"Ngọc đường hoàng đạo":
                                                            laNgayMinhDuongHoangDao(ngayHienTai)?"Minh đường hoàng đạo":
                                                                    laNgayBachHoHacDao(ngayHienTai)?"Bach hổ hắc đạo":
                                                                            laNgayChuTocHacDao(ngayHienTai)?"Chu tước hắc đạo":
                                                                                    laNgayCauTranHacDao(ngayHienTai)?"Câu Trần Hắc Đạo":
                                                                                            laNgayThienLaoHacDao(ngayHienTai)?"Thiên Lao Hắc Đạo":
                                                                                                    laNgayThienHinhHacDao(ngayHienTai)?"Thiên Hình Hắc Đạo":
                                                                                                            laNgayNguyenVuHacDao(ngayHienTai)? "Thiên Vu Hắc Đạo":
                                                                                                                    " "
                    ,
                    layLucDieu(ngayHienTai)
            );

            ngayHienTai = ngayHienTai.plusDays(1);
        }
        System.out.println("--------------------------------------------------------------------------------------------------");
    }
}