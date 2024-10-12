package example;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ClearData {
    public static void main(String[] args) {
//        ArrayList<String[]> data1 = parseFile("vocabularies clone/3000vocabularies.txt");  // Đường dẫn đến file .txt
//        var data = parseFileExcel();
//        writeExcel("vocabularies clone/vocabularies.xlsx", data);
//        writeExcel("vocabularies clone/3000vocabularies.xlsx", data1);
//        toData("3000vocabularies.xlsx");
        toData("vocabularies.xlsx");
//        toData("3000zim_vocabularies.xlsx");
//        toData("vocabulariesIELTS.xlsx");

        var vs = Test.readDataVocabularies("vocabularies clone/data_vocabularies.xlsx");
        var db=Test.databaseEnglish();
    //        var cams=Test.dataBook();
            ArrayList<String[]> myWords = new ArrayList<>();
            int count=0;
            for (var i = 0; i < vs.size(); i++) {
                var en=extractWord(vs.get(i)[0]);

                if(!db.contains(en)){
                    count++;
                    myWords.add(vs.get(i));
                }
            }
            System.out.println(count);
        Test.writeDataVocabularies(myWords, "vocabularies clone/more vocabularies_data.xlsx");
    }
    public static String extractWord(String input) {
        // Tìm vị trí của dấu cách đầu tiên trong chuỗi
        int firstSpaceIndex = input.indexOf('(');

        // Nếu không tìm thấy dấu cách, trả về toàn bộ chuỗi
        if (firstSpaceIndex == -1) {
            return input;
        }

        // Trả về phần trước dấu cách
        return input.substring(0, firstSpaceIndex).trim();
    }
    public static void writeExcel(String filePath, ArrayList<String[]> data) {
        // Tạo workbook và sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Từ vựng");

        // Tạo hàng tiêu đề
        String[] headers = {"Từ vựng", "Từ loại", "Phiên âm", "Dịch Nghĩa"};
        Row headerRow = sheet.createRow(0);

        // Ghi tiêu đề vào hàng đầu tiên
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // Ghi dữ liệu vào các hàng tiếp theo
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < data.get(i).length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(data.get(i)[j]);
            }
        }

        // Ghi file ra đĩa
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
            System.out.println("File Excel đã được ghi thành công!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Đóng workbook
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String[]> parseFile(String fileName) {
        ArrayList<String[]> result = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;

            while ((line = reader.readLine()) != null) {
                // Loại bỏ số thứ tự ở đầu dòng
                line = line.replaceAll("^\\d+\\.\\s*", "");

                // Tách dữ liệu bằng dấu cách và dấu chấm
                String[] parts = line.split("\\s*/\\s*");

                // Kiểm tra nếu đủ 3 phần tử: từ, phiên âm, và nghĩa
                if (parts.length == 3) {
                    String wordAndType = parts[0].trim();  // Từ và loại từ
                    String pronunciation = "/" + parts[1].trim() + "/";  // Phiên âm
                    String meaning = parts[2].trim();  // Nghĩa
                    byte loop = 0;
                    HashSet<String> ps = new HashSet<>();
                    while ((wordAndType.contains(".") || wordAndType.contains(",")) && loop < 5) {
                        // Tìm vị trí của từ loại
                        loop++;
                        int lastSpaceIndex = wordAndType.lastIndexOf(' ');
                        if (!wordAndType.contains(' ' + "")) {
                            lastSpaceIndex = wordAndType.lastIndexOf(",");
                        }
                        if (lastSpaceIndex != -1) {
                            String word = wordAndType.substring(0, lastSpaceIndex).trim(); // Từ tiếng Anh
                            String partOfSpeech = wordAndType.substring(lastSpaceIndex + 1).trim(); // Từ loại

                            // Loại bỏ dấu '.' sau mỗi từ loại và tách từ loại ra nếu có nhiều từ loại
                            String[] partOfSpeechArray = partOfSpeech.replace(".", "").split("\\s*,\\s*");
                            wordAndType = word;
                            for (String pos : partOfSpeechArray) {
                                // Kiểm tra nếu từ loại còn có dấu chấm thì tiếp tục tách
                                String[] subParts = pos.split("\\s*\\.\\s*");
                                for (String subPos : subParts) {
                                    ps.add(subPos.trim());
                                    if (!word.contains(".") && !word.contains(",")) {
                                        for (var p : ps)
                                            result.add(new String[]{word, p.trim(), pronunciation, meaning});
                                    }
                                }
                            }
                        }
                    }
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ArrayList<String[]> parseFileExcel() {
        ArrayList<String[]> result = new ArrayList<>();
        String excelFilePath = "vocabularies clone/vocabularies3000-5000.xlsx";
        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(0);
                    Cell cell1 = row.getCell(1);
                    if (cell != null && cell1 != null) {
                        String v1 = cell.getStringCellValue().trim();
                        String v2 = cell1.getStringCellValue().trim();
                        String wordAndType = v1.toLowerCase();
                        String meaning = v2.toLowerCase();  // Nghĩa
                        byte loop = 0;
                        HashSet<String> ps = new HashSet<>();
                        while ((wordAndType.contains(".") || wordAndType.contains(",")) && loop < 5) {
                            // Tìm vị trí của từ loại
                            loop++;
                            int lastSpaceIndex = wordAndType.lastIndexOf(' ');
                            if (!wordAndType.contains(' ' + "")) {
                                lastSpaceIndex = wordAndType.lastIndexOf(",");
                            }
                            if (lastSpaceIndex != -1) {
                                String word = wordAndType.substring(0, lastSpaceIndex).trim(); // Từ tiếng Anh
                                String partOfSpeech = wordAndType.substring(lastSpaceIndex + 1).trim(); // Từ loại

                                // Loại bỏ dấu '.' sau mỗi từ loại và tách từ loại ra nếu có nhiều từ loại
                                String[] partOfSpeechArray = partOfSpeech.replace(".", "").split("\\s*,\\s*");
                                wordAndType = word;
                                for (String pos : partOfSpeechArray) {
                                    // Kiểm tra nếu từ loại còn có dấu chấm thì tiếp tục tách
                                    String[] subParts = pos.split("\\s*\\.\\s*");
                                    for (String subPos : subParts) {
                                        ps.add(subPos.trim());
                                        if (!word.contains(".") && !word.contains(",")) {
                                            for (var p : ps) {
                                                result.add(new String[]{word, p, "", meaning});
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static void toData(String name) {
        String filePath = "vocabularies clone/" + name; // Đường dẫn tới file Excel
        HashSet<String> speech_of_parts = new HashSet<>();
        ArrayList<String[]> most = new ArrayList<>();
        try (FileInputStream file = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(file)) {

            Sheet sheet = workbook.getSheetAt(0); // Đọc sheet đầu tiên (index = 0)

            for (Row row : sheet) {
                Cell cell1 = row.getCell(0);
                Cell cell2 = row.getCell(1);
                Cell cell3 = row.getCell(2);
                Cell cell4 = row.getCell(3);
                String en = " ", speech_of_part = " ", pronunciation = " ", vi = " ";
                if (cell1 != null) {
                    try {
                        en = cell1.getStringCellValue().toLowerCase();
                        en=extractWord(en);
                        if (cell2 != null) {
                            try {
                                speech_of_part = cell2.getStringCellValue();
                                speech_of_part = speech_of_part.trim().toLowerCase();
                                speech_of_part=speech_of_part.replaceAll(", ","/").trim();
                                speech_of_part=speech_of_part.replaceAll(",","").trim();
                                var pl=speech_of_part.split("/");
                                for(var p:pl){
                                speech_of_part = deSpeechOfParts(p);
                                if (!speech_of_part.equals("Từ loại") && speech_of_part != null) {
                                    if (cell3 != null) {
                                        if (!speech_of_part.isEmpty())
                                            try {
                                                pronunciation = cell3.getStringCellValue();
                                                if (cell4 != null) {
                                                    try {
                                                        vi = cell4.getStringCellValue().toLowerCase();
//                                                        if(!pronunciation.isEmpty()){
                                                        most.add(new String[]{en, vi, speech_of_part, pronunciation});
//                                                        }

                                                    } catch (Exception e) {
                                                    }
                                                }

                                            } catch (Exception e) {

                                            }
                                    }
                                }}
                            } catch (Exception e) {

                            }
                        }
                    } catch (Exception e) {
                        en = cell1.getNumericCellValue() + "";

                    }
                }


            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String speech_of_part : speech_of_parts) {
            System.out.println(speech_of_part);
        }
        Test.writeDataVocabularies(most, "vocabularies clone/data_" + name);
    }

    public static String deSpeechOfParts(String speechOfPart) {

        Map<String, String> speechPartsMap = new HashMap<>();
        speechPartsMap.put("adj", "adjective");
        speechPartsMap.put("unknown", "unknown");
        speechPartsMap.put("adj:", "adjective");
        speechPartsMap.put("n", "noun");
        speechPartsMap.put("v", "verb");
        speechPartsMap.put(")v", "verb");
        speechPartsMap.put("adv", "adverb");
        speechPartsMap.put("pre", "preposition");
        speechPartsMap.put("prep", "preposition");
        speechPartsMap.put("phrasal v", "phrasal verb");
        speechPartsMap.put("n/v", "noun/verb");
        speechPartsMap.put("v/n", "noun/verb");
        speechPartsMap.put("adj/n", "adjective/noun");
        speechPartsMap.put("adj/v", "adjective/verb");
        speechPartsMap.put("n. phr", "noun phrase");
        speechPartsMap.put("v. phr", "verb phrase");
        speechPartsMap.put("n/adj", "noun/adjective");
        speechPartsMap.put("det", "determiner");
        speechPartsMap.put("conj", "conjunction");
        speechPartsMap.put("pron", "pronoun");
        speechPartsMap.put("exclamation", "exclamation");
        speechPartsMap.put("disk", "disk");
        speechPartsMap.put("bre", "bre");
        speechPartsMap.put("abbr", "abbreviation");
        speechPartsMap.put("(abbr", "abbreviation");
        speechPartsMap.put("number", "number");
        speechPartsMap.put("det/pron", "determiner/pronoun");
        speechPartsMap.put("adv/prep", "adverb/preposition");
        speechPartsMap.put("prep/adv", "preposition/adverb");
        speechPartsMap.put("adv/prep", "adverb/preposition");
        speechPartsMap.put("prep/conj/adv", "preposition/conjunction/adverb");
        speechPartsMap.put("pron/adv", "pronoun/adverb");
        speechPartsMap.put("adj/adv", "adjective/adverb");
        speechPartsMap.put("prep/adv", "preposition/adverb");
        speechPartsMap.put("det/pron", "determiner/pronoun");
        speechPartsMap.put("detpron/adv", "determiner/pronoun/adverb");
        speechPartsMap.put("of/v", "of/verb");
        speechPartsMap.put("adj/to", "adjective/to");
        speechPartsMap.put("adv/prep", "adverb/preposition");
        speechPartsMap.put("adv/conj/prep", "adverb/conjunction/preposition");
        speechPartsMap.put("n/adj/adv/v", "noun/adjective/adverb/verb");
        speechPartsMap.put("prep/conj/adv", "preposition/conjunction/adverb");
        speechPartsMap.put("det/pron", "determiner/pronoun");
        speechPartsMap.put("businesswoman", "businesswoman");
        speechPartsMap.put("adj/v/n", "adjective/verb/noun");
        speechPartsMap.put("modal/v/n", "modal/verb/noun");
        speechPartsMap.put("of/adj", "of/adjective");
        speechPartsMap.put("adj/pron", "adjective/pronoun");
        speechPartsMap.put("modal/v", "modal/verb");
        speechPartsMap.put("adj/adv", "adjective/adverb");
        speechPartsMap.put("on/v", "on/verb");
        speechPartsMap.put("of/v", "of/verb");
        speechPartsMap.put("vauxiliary/v", "verb/auxiliary/verb");
        speechPartsMap.put("v adj/det/adv/n", "verb/adjective/determiner/adverb/noun");
        speechPartsMap.put("adv/prep", "adverb/preposition");
        speechPartsMap.put("adv/adj/n", "adverb/adjective/noun");
        speechPartsMap.put("ndet", "ndet");
        speechPartsMap.put("n/adj/v", "noun/adjective/verb");
        speechPartsMap.put("n/pro", "noun/pronoun");
        speechPartsMap.put("det/adv/n", "determiner/adverb/noun");
        speechPartsMap.put("v/adj", "verb/adjective");
        speechPartsMap.put("adj/prep", "adjective/preposition");
        speechPartsMap.put("adj/v/adv", "adjective/verb/adverb");
        speechPartsMap.put("adjv", "adjective verb");
        speechPartsMap.put("exclamation/n", "exclamation/noun");
        speechPartsMap.put("det/pron/adv", "determiner/pronoun/adverb");
        speechPartsMap.put("adj/adv", "adjective/adverb");
        speechPartsMap.put("modal/v", "modal/verb");
        speechPartsMap.put("n/pro", "noun/pronoun");
        speechPartsMap.put("prep/conj", "preposition/conjunction");
        speechPartsMap.put("v/adj", "verb/adjective");
        speechPartsMap.put("nv", "noun/verb");
        speechPartsMap.put("adj/n/adv", "adjective/noun/adverb");
        speechPartsMap.put("v/adj", "verb/adjective");
        speechPartsMap.put("adv/adj", "adverb/adjective");
        speechPartsMap.put("det/adj/pron", "determiner/adjective/pronoun");
        speechPartsMap.put("n/adj/adv", "noun/adjective/adverb");
        speechPartsMap.put("det/adv/n", "determiner/adverb/noun");
        speechPartsMap.put("prep/adv", "preposition/adverb");
        speechPartsMap.put("adj/n/adj", "adjective/noun/adjective");
        speechPartsMap.put("adj/n/v", "adjective/noun/verb");
        speechPartsMap.put("et", "et");
        speechPartsMap.put("pron/det", "pronoun/determiner");
        speechPartsMap.put("n/adv", "noun/adverb");
        speechPartsMap.put("prep/adv/n/adj", "preposition/adverb/noun/adjective");
        speechPartsMap.put("n/det/pro", "noun/determiner/pronoun");
        speechPartsMap.put("(abbr kg)", "abbreviation kg");
        speechPartsMap.put("of/n/v", "of/noun/verb");
        speechPartsMap.put("det//adv/n/v", "determiner/adverb/noun/verb");
        speechPartsMap.put("adj/adv/n", "adjective/adverb/noun");
        speechPartsMap.put("prep/vconj", "preposition/verb/conjunction");
        speechPartsMap.put("to/v", "to/verb");
        speechPartsMap.put("adj/det/pron", "adjective/determiner/pronoun");
        speechPartsMap.put("pron/det//adv", "pronoun/determiner/adverb");
        speechPartsMap.put("v/modal", "verb/modal");
        speechPartsMap.put("pron/n", "pronoun/noun");
        speechPartsMap.put("det/pro/n/adv", "determiner/pronoun/noun/adverb");
        speechPartsMap.put("v/modal", "verb/modal");
        speechPartsMap.put("adj/adv/prep", "adjective/adverb/preposition");
        speechPartsMap.put("v/modal verb/n", "verb/modal verb/noun");
        speechPartsMap.put("adj/adv/n", "adjective/adverb/noun");
        speechPartsMap.put("exclamation/det", "exclamation/determiner");
        speechPartsMap.put("adv/conj", "adverb/conjunction");
        speechPartsMap.put("no/n", "no/noun");
        speechPartsMap.put("adv/conj", "adverb/conjunction");
        speechPartsMap.put("adj/adv/nprep", "adjective/adverb/noun/preposition");
        speechPartsMap.put("v/modal", "verb/modal");
        speechPartsMap.put("of/adv/prep", "of/adverb/preposition");
        speechPartsMap.put("n/adj/prep/adv", "noun/adjective/preposition/adverb");
        speechPartsMap.put("adj/pron/v", "adjective/pronoun/verb");
        speechPartsMap.put("pip'emз", "pip'emз");
        speechPartsMap.put("adj/n/prep/adv", "adjective/noun/preposition/adverb");
        speechPartsMap.put("usn/adj/adv", "usn/adjective/adverb");
        speechPartsMap.put("exclamation/v", "exclamation/verb");
        speechPartsMap.put("n/adv/n/det", "noun/adverb/noun/determiner");
        speechPartsMap.put("n/adj conj/prep", "noun/adjective/conjunction/preposition");
        speechPartsMap.put("adj/n/v", "adjective/noun/verb");
        speechPartsMap.put("to/adj", "to/adjective");
        speechPartsMap.put("adj/adv/n", "adjective/adverb/noun");
        speechPartsMap.put("adj/adv/prep/n", "adjective/adverb/preposition/noun");
        speechPartsMap.put("v/modal", "verb/modal");
        speechPartsMap.put("v/modal", "verb/modal");
        speechPartsMap.put("adv/conj", "adverb/conjunction");
        speechPartsMap.put("pron/conj/det", "pronoun/conjunction/determiner");
        speechPartsMap.put("n/det/pro", "noun/determiner/pronoun");
        speechPartsMap.put("adv/conj", "adverb/conjunction");
        speechPartsMap.put("adv/n", "adverb/noun");
        speechPartsMap.put("adv/n", "adverb/noun");
        speechPartsMap.put("adv/n", "adverb/noun");
        speechPartsMap.put("prep/adj", "preposition/adjective");
        speechPartsMap.put("conj/prep", "conjunction/preposition");
        speechPartsMap.put("v/n/adj", "verb/noun/adjective");
        speechPartsMap.put("v/adj/n", "verb/adjective/noun");
        speechPartsMap.put("n/det/pro", "noun/determiner/pronoun");
        speechPartsMap.put("n/det/pro", "noun/determiner/pronoun");
        speechPartsMap.put("adv/pron/conj", "adverb/pronoun/conjunction");
        speechPartsMap.put("adv/conj", "adverb/conjunction");
        speechPartsMap.put("n/det/pro", "noun/determiner/pronoun");
        speechPartsMap.put("n/conj", "noun/conjunction");
        speechPartsMap.put("n/det/pro", "noun/determiner/pronoun");
        speechPartsMap.put("v/n/modal", "verb/noun/modal");
        speechPartsMap.put("v/modal", "verb/modal");
        speechPartsMap.put("n/exclamation", "noun/exclamation");
        speechPartsMap.put("adv/n", "adverb/noun");
        speechPartsMap.put("modal", "modal");
        speechPartsMap.put("pro", "pronoun");
        speechPartsMap.put("pro", "pronoun");
        speechPartsMap.put("pro", "pronoun");
        speechPartsMap.put("pro", "pronoun");
        speechPartsMap.put("pro", "pronoun");
        speechPartsMap.put("modal verb", "modal verb");
        speechPartsMap.put("detpron", "determiner/pronoun");
        speechPartsMap.put("of", "preposition");
        speechPartsMap.put("to", "preposition");
        speechPartsMap.put("on", "preposition");
        speechPartsMap.put("vauxiliary", "auxiliary verb");
        speechPartsMap.put("v adj", "verb/adjective");
        speechPartsMap.put("vauxiliary", "auxiliary verb");
        speechPartsMap.put("vconj", "verb/conjunction");
        speechPartsMap.put("no", "unknown");
        speechPartsMap.put("nprep", "unknown");
        speechPartsMap.put("adj conj", "adjective/conjunction");
        // Loại bỏ khoảng trắng thừa và chuyển về chữ thường

        // Kiểm tra xem từ loại có tồn tại trong Map không
        if (speechPartsMap.containsKey(speechOfPart)) {
            return speechPartsMap.get(speechOfPart);  // Trả về từ loại đầy đủ
        }

        // Nếu không phải từ loại hợp lệ, trả về null
        System.out.println(speechOfPart);
        return null;
    }
}
