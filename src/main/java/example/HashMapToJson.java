package example;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class HashMapToJson {

    public static void main(String[] args) {
        // Tạo một HashMap ví dụ
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("key1", "value1");
        hashMap.put("key2", 123);
        hashMap.put("key3", true);

        // Chuyển đổi HashMap thành JSON
        String jsonString = convertHashMapToJson(hashMap);

        // In kết quả
        System.out.println(jsonString);
    }

    private static String convertHashMapToJson(Map<String, Object> hashMap) {
        // Sử dụng thư viện Gson
        Gson gson = new Gson();

        // Chuyển đổi HashMap thành JSON
        return gson.toJson(hashMap);
    }
}
