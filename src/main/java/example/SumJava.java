package example;

public class SumJava {
    public static void main(String[] args) {
        long startTime = System.nanoTime();

        long result = 0;
        for (long i = 1; i <= 20000000; i++) {
            if (isPrime(i)) {
//                result += i;
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000; // Đổi ra mili giây

        System.out.println("Java result: " + result);
        System.out.println("Java duration: " + duration + " milliseconds");
    }

    private static boolean isPrime(long n) {
        if (n < 2) {
            return false;
        }
        for (long i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }
}
