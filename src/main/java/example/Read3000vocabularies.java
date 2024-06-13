package example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
public class Read3000vocabularies {
    public static void main(String[] args) {
        String excelFilePath ="3000-vocabularies.xlsx" ;
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
                    else
                        rs+="\"],[\"en\"=>\""+vocab + "\",\"vi\"=>\"" + meaning;
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
    }
}
