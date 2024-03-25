package example;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static String readFile(String fileName) throws IOException {
        StringBuilder content = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        return content.toString();
    }

    private static void partArrayCode(String fileName) {
        int i = 0;
        try (FileInputStream inputStream = new FileInputStream(fileName);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            String array = "const " + fileName.replaceAll("-", "_").replaceAll("_z_0_5.xlsx", "") + "=[";
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    // Bắt đầu từ cột thứ 2
//                    for (int colIndex = 1; colIndex < row.getLastCellNum(); colIndex++) {
//                        Cell cell = row.getCell(1);

//                        if (cell != null) {
//                            System.out.print(cell.toString() + "\t");
//                        }
//                    }
//                    array+=row.getCell(1)+":["
                    if (row.getCell(1) != null && i == row.getCell(1).getNumericCellValue()) {
                        array += "["
                                + row.getCell(2) + ","
                                + row.getCell(3) + ","
                                + row.getCell(4) + ","
                                + row.getCell(5) + ","
                                + row.getCell(6) + ","
                                + row.getCell(7) + ","
                                + row.getCell(8) + "],"
                        ;
                        i++;
                    }
                }
            }
            array += "];";
            System.out.println(array);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    public static void main(String[] args) {
//        partArrayCode("sft-wfa-boys-z-0-5.xlsx");
//        partArrayCode("sft-wfa-girls-z-0-5.xlsx");
//        partArrayCode("sft_lhfa_boys_z_0_5.xlsx");
//        partArrayCode("sft_lhfa_girls_z_0_5.xlsx");
//    }
    public static void main(String[] args) {
        byte count = 0;
        byte countTopic = 0;
        int countVocab = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
            String line;
            int lineNumber = 1;
            BufferedWriter writer = null;
            while ((line = br.readLine()) != null) {

                if (!containsNumber(line)) {
                    count++;
                    System.out.println(line);
//                    writer=new BufferedWriter(new FileWriter("data_group"+count+".txt"));
                } else {
                    if (!startsWithDigit(line)) {
                        System.out.println(line);
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
//                            System.out.println(item.replaceFirst(". ", ".\t"));
//                            if(item.trim().contains(". "))
                            String row = item.toLowerCase().trim().replaceFirst(" ", "\t");
                            row = removeNumberAndDot(row).replaceAll("\t", "");
                            String[] parts = row.split("\\s*/\\s*");
                            if (parts.length > 3) {
                                String e=parts[0].replaceAll(":", "").replace(".", "").trim();
                                if (isEnglish(e)) {
                                    row = e + " => " + parts[parts.length - 1].replaceAll(":","");
                                    countVocab++;
                                    System.out.println(row);
                                }
//                                else {
//                                    countVocab++;
//                                    System.out.println(parts[0]);
//                                }
                            } else if (parts.length == 3) {
                                String e=parts[0].replaceAll(":", "").trim();
                                if (isEnglish(e)) {
                                    row = e + " => " + parts[2].replaceAll(":","");
                                    countVocab++;
                                    System.out.println(row);
                                }
                                else {
                                    row = parts[2] + " => " + parts[0];
                                    countVocab++;
                                    System.out.println(row);
                                }

                            } else {
                                parts = row.split(":");
                                if (parts.length > 2) {
                                    row = parts[0].trim() + " => " + parts[parts.length-1].trim();
                                    System.out.println(row);
                                    countVocab++;
                                }
                                else if (parts.length == 2) {
                                    row = parts[0].trim() + " => " + parts[1].trim();
                                    System.out.println(row);
                                    countVocab++;
                                } else {
                                    parts = row.split("\\(.*?\\)");
                                    if (parts.length == 2) {
                                        row = parts[0].trim() + " => " + parts[1].trim();
                                        System.out.println(row);
                                        countVocab++;
                                    }
                                }

                            }
                        }
                    }


                }
                lineNumber++;
            }
            System.out.println(countVocab);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean containsNumber(String line) {
        for (char c : line.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    public static String removeNumberAndDot(String text) {
        // Biểu thức chính quy để tìm số và dấu chấm ở đầu dòng
        String regex = "^\\d+\\.\\s";
        // Thực hiện thay thế các số và dấu chấm bằng chuỗi rỗng
        return text.replaceAll(regex, "");
    }

    private static boolean startsWithDigit(String line) {
        if (line.length() > 0 && Character.isDigit(line.charAt(0))) {
            return true;
        }
        return false;
    }

    public static boolean isEnglish(String text) {
        // Kiểm tra từng ký tự trong chuỗi
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            // Kiểm tra nếu ký tự không nằm trong phạm vi chữ cái tiếng Anh (a-z, A-Z)
            if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == ' ' || ch == '-'|| ch == '('|| ch == ')'|| ch == '&')) {
                return false;
            }
        }
        // Nếu không có ký tự nào không phải tiếng Anh, trả về true
        return true;
    }

    public static int getNumberBeforeDot(String text) {
        int dotIndex = text.indexOf('.');

        if (dotIndex != -1 && dotIndex > 0) {
            String numberString = text.substring(0, dotIndex).trim();
            try {
                return Integer.parseInt(numberString);
            } catch (NumberFormatException e) {
                return -1;
            }
        } else {
            return -1;
        }
    }
}
