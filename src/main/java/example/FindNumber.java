package example;

public class FindNumber {

    public static boolean laSoHoanHao(int so) {
        if (so <= 1) {
            return false; // Số 1 và các số âm không phải là số hoàn hảo
        }

        int tongUoc = 0;

        for (int i = 1; i <= so / 2; i++) {
            if (so % i == 0) {
                tongUoc += i;
            }
        }

        return tongUoc == so;
    }

    public static void main(String[] args) {
        for(int i=0;i<1000000000;i++){
            if(laSoHoanHao(i)) System.out.println(i);
        }
    }
}
