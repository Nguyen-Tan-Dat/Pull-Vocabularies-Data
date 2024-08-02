package example;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class HashMapToJson {
    public static void writeTopic(String name, HashSet<String> vocabularies,String filename){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("vs",vocabularies);
        hashMap.put("name", name);
        ArrayList<Object> list=new ArrayList<>();
        list.add(hashMap);
        String jsonString = convertHashMapToJson(list);
        Test.writeFile(filename+".json",jsonString);
    }
    public static void writeTopics( ArrayList<Object>  list ,String filename){
        String jsonString = convertHashMapToJson(list);
        Test.writeFile(filename+".json",jsonString);
    }



    private static String convertHashMapToJson(ArrayList<Object> hashMap) {
        Gson gson = new Gson();
        return gson.toJson(hashMap);
    }
}
