package example;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

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
        int i=0;
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
                    if (row.getCell(1) != null && i==row.getCell(1) .getNumericCellValue() ) {
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

    public static void main(String[] args) {
        partArrayCode("sft-wfa-boys-z-0-5.xlsx");
        partArrayCode("sft-wfa-girls-z-0-5.xlsx");
        partArrayCode("sft_lhfa_boys_z_0_5.xlsx");
        partArrayCode("sft_lhfa_girls_z_0_5.xlsx");
    }
}
