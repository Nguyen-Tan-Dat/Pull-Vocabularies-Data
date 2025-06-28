import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OtherTopics {
    public static void main(String[] args) {
        ArrayList<Object> data = new ArrayList<>();
        HashSet<String> list = new HashSet<>();
        int id = 0;
        String topic = "";
        HashMap<String, Integer> topics = new HashMap<>();
        HashMap<String, Integer> english = new HashMap<>();
        int enMax = 1;
        int viMax = 1;
        HashMap<String, Integer> vietnamese = new HashMap<>();
        Set<String> vocab = new HashSet<>();
        Set<Test.Vocab> vList = new HashSet<>();
        byte count = 0;
        byte countTopic = 0;
        int countVocab = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
            String line;
            int lineNumber = 1;
            BufferedWriter writer = null;
            while ((line = br.readLine()) != null) {

                if (!Test.containsNumber(line)) {

//                    System.out.println(line);
//                    writer=new BufferedWriter(new FileWriter("data_group"+count+".txt"));
                } else {
                    if (!Test.startsWithDigit(line)) {
//                        System.out.println(line);
                        if (!topic.isEmpty()) {
                            HashMap<String, Object> row = new HashMap<>();
                            row.put("name", topic);
                            row.put("vs", list);
                            data.add(row);
                            list = new HashSet<>();
                        }
                        topic =processString(line);
                        System.out.println("]],");
                        System.out.print("['name'=>'" + topic + "','vs'=>[");

                        id++;
                        topics.put(line, id);
                        countTopic++;
                    } else {
                        String regex = "(\\d+\\.\\s.*?)(?=(\\d+\\.\\s|$))";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(line);
                        ArrayList<String> result = new ArrayList<>();
                        while (matcher.find()) {
                            result.add(matcher.group(1));
                        }
                        for (String item : result) {
                            String en = "";
                            String vi = "";
//                            System.out.println(item.replaceFirst(". ", ".\t"));
//                            if(item.trim().contains(". "))
                            String row = item.toLowerCase().trim().replaceFirst(" ", "\t");
                            row = Test.removeNumberAndDot(row).replaceFirst("\t", " ");
                            String[] parts = row.split("\\s*/\\s*");
                            if (parts.length > 3) {
                                String e = parts[0].replaceAll(":", "").replace(".", "").trim();
                                if (Test.isEnglish(e)) {
                                    countVocab++;
                                    en = e;
                                    vi = parts[parts.length - 1].replaceAll(":", "");
                                }
//                                else {
//                                    countVocab++;
//                                    System.out.println(parts[0]);
//                                }
                            } else if (parts.length == 3) {
                                String e = parts[0].replaceAll(":", "").trim();
                                if (Test.isEnglish(e)) {
                                    en = e;
                                    vi = parts[2].replaceAll(":", "");
                                    countVocab++;
                                } else {
                                    en = parts[2];
                                    vi = parts[0];
                                    countVocab++;
                                }

                            } else {
                                parts = row.split(":");
                                if (parts.length > 2) {
                                    en = parts[0].trim();
                                    vi = parts[parts.length - 1].trim();
                                    countVocab++;
                                } else if (parts.length == 2) {
                                    en = parts[0].trim();
                                    vi = parts[1].trim();
                                    countVocab++;
                                } else {
                                    parts = row.split("\\(.*?\\)");
                                    if (parts.length == 2) {
                                        en = parts[0].trim();
                                        vi = parts[1].trim();
                                        countVocab++;
                                    }
                                }

                            }
                            en = Test.removeParenthesesContent(en).trim();
                            en = en.split("/")[0];
                            int enID = 0;
                            if (english.containsKey(en)) {
                                enID = english.get(en);
                            } else {
                                enID = enMax;
                                english.put(en, enMax++);
                            }
                            vi = Test.removeParenthesesContent(vi).trim();
                            int viID = 0;
                            if (vietnamese.containsKey(vi)) {
                                viID = vietnamese.get(vi);
                            } else {
                                viID = viMax;
                                vietnamese.put(vi, viMax++);
                            }
                            if (vocab.contains(enID + "," + viID)) {
//                                System.out.println(enID);
                            } else {
                                vList.add(new Test.Vocab(enID, viID, id));
                                vocab.add(enID + "," + viID);
                            }
                            System.out.print("\"" + en + "\",");
                            list.add(en);
                        }
                    }


                }
                lineNumber++;
            }
            System.out.println(countVocab);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Test.writeTopics(data, "Topics json/3000-tu-vung-tieng-anh-theo-chu-de-qts-english");
    }
    public static String processString(String input) {
        // Bỏ phần "Chủ đề " và tìm vị trí của dấu ":" để lấy số
        String[] parts = input.replace("Chủ đề ", "").split(": ", 2);

        // Nếu không tìm được đúng định dạng thì trả về chuỗi gốc
        if (parts.length < 2) {
            return input;
        }

        // Lấy phần số và phần nội dung
        String numberPart = parts[0].trim();
        String contentPart = parts[1].trim();

        // Chuyển số thành dạng hai chữ số
        if (numberPart.length() == 1) {
            numberPart = "0" + numberPart;
        }

        // Ghép lại chuỗi theo định dạng yêu cầu
        return numberPart + ": " + contentPart;
    }
}
