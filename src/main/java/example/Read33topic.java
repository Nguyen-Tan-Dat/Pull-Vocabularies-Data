package example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
public class Read33topic {
    public static void main(String[] args) {
        String excelFilePath ="vocabularies.xlsx" ;
        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet
            Iterator<Row> iterator = sheet.iterator();
            String rs="[";
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                Cell vocabCell = currentRow.getCell(0);
                Cell meaningCell = currentRow.getCell(1);

                if (vocabCell != null && meaningCell != null) {
                    String vocab = vocabCell.getStringCellValue();
                    String meaning = meaningCell.getStringCellValue();
                    if(vocab.equals(""))
                        rs+=meaning;
                    else
                        rs+="\"],[\"en\"=>\""+vocab + "\",\"vi\"=>\"" + meaning;
                }else {
                    if(vocabCell.getStringCellValue().contains("."))
                    rs+="\"]]],\n[\"name\"=>\""+vocabCell+"\",\"vs\"=>[[";
                }
            }
            rs.replaceAll("[\"],", "");
            System.out.println(rs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
