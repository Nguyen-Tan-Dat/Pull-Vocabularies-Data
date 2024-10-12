    package example;

    import org.apache.poi.ss.usermodel.*;
    import org.apache.poi.xssf.usermodel.XSSFWorkbook;

    import java.io.File;
    import java.io.FileInputStream;
    import java.io.IOException;
    import java.util.*;

    public class ReadExcel {
        public static void main(String[] args) {
            read33Topics();
            read3000Vocabularies();
        }
        public static void read3000Vocabularies() {
            var db=Test.databaseEnglish();
            int count=0;
            String excelFilePath ="vocabularies clone/Vocabularies/New folder/3000 từ vựng Tiếng Anh.xlsx" ;
            try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet
                Iterator<Row> iterator = sheet.iterator();
                String rs="[";
                while (iterator.hasNext()) {
                    Row currentRow = iterator.next();
                    Cell vocabCell0 = currentRow.getCell(0);
                    Cell vocabCell = currentRow.getCell(1);
                    Cell meaningCell = currentRow.getCell(2);
                    try {
                        if(!vocabCell0.equals("STT"))
                            if (vocabCell != null && meaningCell != null) {
                                String vocab = vocabCell.getStringCellValue();
                                String meaning = meaningCell.getStringCellValue();
                                if(vocab.equals(""))
                                    rs+=meaning;
                                else {
                                    rs+="\"],[\"en\"=>\""+vocab + "\",\"vi\"=>\"" + meaning;
                                    if(db.contains(vocab)){
                                        count++;
                                    }

                                }
                            }else {

                                if (vocabCell0.getStringCellValue().contains("("))
                                    rs += "\"]]],\n[\"name\"=>\"" + vocabCell0 + "\",\"vs\"=>[[";

                            } }catch (Exception e){}
                }
                rs.replaceAll("[\"],", "");
                System.out.println(rs);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(count);
        }
        public static void read33Topics() {
            String excelFilePath ="vocabularies clone/Vocabularies/New folder/Tu-vung-IELTS-33-chu-de.xlsx" ;
            HashSet<String[]> list=new HashSet<>();
            var db=Test.databaseEnglish();
            int count=0;
            try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet
                Iterator<Row> iterator = sheet.iterator();
                String rs="[";
                while (iterator.hasNext()) {
                    Row currentRow = iterator.next();
                    Cell vocabCell = currentRow.getCell(0);
                    Cell phoneticCell = currentRow.getCell(1);
                    Cell meaningCell = currentRow.getCell(2);

                    if (vocabCell != null && phoneticCell != null && meaningCell != null) {
                        String vocab = vocabCell.getStringCellValue().toLowerCase();
                        String meaning = meaningCell.getStringCellValue();
                        if(vocab.equals(""))
                            rs+=meaning;
                        else{
                            if   (db.contains(vocab)){
                                count++;
                                System.out.println(vocab);
                            }
                        }
                    }else {
                        if(vocabCell.getStringCellValue().contains("."))
                            rs+="\"]]],\n[\"name\"=>\""+vocabCell+"\",\"vs\"=>[[";
                    }
                }
                rs.replaceAll("[\"],", "");
//            System.out.println(rs);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(count);
        }
    }
