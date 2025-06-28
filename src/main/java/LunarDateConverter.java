import java.util.Calendar;
import java.util.GregorianCalendar;
import com.github.heqiao2010.lunar.LunarCalendar;

public class LunarDateConverter {

    /**
     * Chuyển đổi ngày dương lịch sang âm lịch
     * @param solarYear năm dương lịch
     * @param solarMonth tháng dương lịch (1-12)
     * @param solarDay ngày dương lịch
     * @return mảng chứa [năm âm lịch, tháng âm lịch, ngày âm lịch, có phải tháng nhuận không]
     */
    public static int[] convertSolarToLunar(int solarYear, int solarMonth, int solarDay) {
        Calendar calendar = new GregorianCalendar(solarYear, solarMonth - 1, solarDay);
        LunarCalendar lunar = LunarCalendar.solar2Lunar(calendar);

        return new int[] {
                lunar.getLunarYear(),
                lunar.getLunarMonth(),
                lunar.getDayOfLunarMonth(),
                lunar.isLeapMonth() ? 1 : 0
        };
    }

    /**
     * Chuyển đổi ngày âm lịch sang dương lịch, tự động xác định tháng nhuận
     * @param lunarYear năm âm lịch
     * @param lunarMonth tháng âm lịch (1-12)
     * @param lunarDay ngày âm lịch
     * @return mảng chứa [năm dương, tháng dương, ngày dương]
     * @throws IllegalArgumentException nếu ngày âm không hợp lệ
     */
    public static int[][] convertLunarToSolar(int lunarYear, int lunarMonth, int lunarDay) {
        int[][] rs=new int[2][3];
        try {
           Calendar solarDate = LunarCalendar.lunar2Solar(lunarYear, lunarMonth, lunarDay, true);
            rs[0]= new int[] {
                    solarDate.get(Calendar.YEAR),
                    solarDate.get(Calendar.MONTH) + 1,
                    solarDate.get(Calendar.DAY_OF_MONTH)
            };
        } catch (IllegalArgumentException e1) {
        }
        try {
            Calendar solarDate = LunarCalendar.lunar2Solar(lunarYear, lunarMonth, lunarDay, false);
            rs[1]= new int[] {
                    solarDate.get(Calendar.YEAR),
                    solarDate.get(Calendar.MONTH) + 1,
                    solarDate.get(Calendar.DAY_OF_MONTH)
            };
        } catch (IllegalArgumentException e2) {
        }
        return rs;
    }

    /**
     * Kiểm tra xem tháng âm lịch có phải là tháng nhuận không
     */
    private static boolean isLeapMonth(int lunarYear, int lunarMonth) {
        LunarCalendar testDate = new LunarCalendar(lunarYear, lunarMonth, 1, false);
        return testDate.isLeapYear(lunarYear) && testDate.getLeapMonth() == lunarMonth;
    }
    public static String convertSolarToLunarString(int solarYear, int solarMonth, int solarDay) {
        Calendar calendar = new GregorianCalendar(solarYear, solarMonth - 1, solarDay);
        LunarCalendar lunar = LunarCalendar.solar2Lunar(calendar);

        return (lunar.isLeapMonth() ?"Leap ": "" )+lunar.getDayOfLunarMonth() +"/"+ lunar.getLunarMonth() +"/"+  lunar.getLunarYear();
    }
    public static void main(String[] args) {
        // Test cases
        try {
            // Ngày bình thường (15/8/2023 âm lịch)
            var solar = convertLunarToSolar(2025, 6, 23);
            for(var i: solar)
            System.out.println( i[2] + "/" + i[1] + "/" + i[0]);
            for(var i: solar){
            var lunar =convertSolarToLunar(i[0],i[1],i[2]);
            System.out.println(lunar[0]+"/"+lunar[1]+"/"+lunar[2]);}
        } catch (IllegalArgumentException e) {
            System.out.println("Lỗi: " + e.getMessage());
        }
    }
}