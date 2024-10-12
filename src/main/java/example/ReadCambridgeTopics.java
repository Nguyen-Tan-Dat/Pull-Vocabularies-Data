package example;

import java.io.*;
import java.nio.file.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

public class ReadCambridgeTopics {
    public static String readPdf(String filePath) {
        StringBuilder content = new StringBuilder();
        try {
            // Create a PdfReader instance
            PdfReader reader = new PdfReader(filePath);

            // Create a PdfDocument instance
            PdfDocument pdfDoc = new PdfDocument(reader);

            // Loop through all the pages and extract text
            int numberOfPages = pdfDoc.getNumberOfPages();
            for (int i = 1; i <= numberOfPages; i++) {
                content.append(PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i))).append("\n");
            }

            // Close the PdfDocument
            pdfDoc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString().toLowerCase();
    }

    public static HashMap<String, HashSet<String>> readOnlineWords() {

        HashMap<String, HashSet<String>> plist = new HashMap<>();
        for (int number = 1; number <= 3; number++) {
            String directoryPath = "Cambridge Vocabularies/" + number;
            Path path = Paths.get(directoryPath);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.xlsx")) {
                for (Path filePath : stream) {
                    try (FileInputStream fis = new FileInputStream(filePath.toString());
                         Workbook workbook = new XSSFWorkbook(fis)) {
                        Sheet sheet = workbook.getSheetAt(0);
                        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                            Row row = sheet.getRow(i);
                            if (row != null) {
                                Cell cell = row.getCell(0);
                                Cell cell1 = row.getCell(1);
//                                if (cell != null) {
                                // Lấy giá trị của ô
                                String cellValue = Test.getCellValueAsString(cell);
                                String cellValue1 = Test.getCellValueAsString(cell1);
                                cellValue = cellValue.trim();
                                cellValue1 = cellValue1.trim();
//                                    list.add(cellValue);
                                if (!plist.containsKey(cellValue)) {
                                    plist.put(cellValue, new HashSet<>());
//                                        plist.get(cellValue).add("");
                                }
                                plist.get(cellValue).add(cellValue1.toLowerCase());

//                                }
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException | DirectoryIteratorException e) {
                System.err.println("Lỗi khi đọc thư mục: " + e.getMessage());
            }
        }
        return plist;
    }


    public static void main(String[] args) {
//        HashSet<String> list = new HashSet<>();
        String ofdVs = readPdf("The_Oxford_3000.pdf");
        String ofdVs5000 = readPdf("The_Oxford_5000.pdf");
        var pdfCamBooks = Test.dataBook();
        var onlineCambridgeWords = readOnlineWords();
//
        int count = 0;
//        for (var i : plist.keySet()) {
//                count+=plist.get(i).size();
//        }
        System.out.println("Book online: " + onlineCambridgeWords.size());
        System.out.println("PDF books: " + pdfCamBooks.size());
        count = 0;
        for (var i : onlineCambridgeWords.keySet()) {
//            if(!pdfCamBooks.contains(i)){
            pdfCamBooks.add(i);
//            }
        }
        System.out.println("Cambridge vocabularies: " + pdfCamBooks.size());
        var oxfordVs = Oxford.readOxfordVocabularies();
        System.out.println(oxfordVs.size());
        count = 0;
        for (var i : oxfordVs) {
            if (pdfCamBooks.contains(i)) {
                count++;
            }
        }
        System.out.println(count);
//        HashSet<String> child=new HashSet<>();
//        for(var i:pdfCamBooks){
//            if(ofdVs.contains(i)
///*                    ||ofdVs5000.contains(i) */){
//                System.out.println(i);child.add(i);
//            }
//        }
//        System.out.println(child.size());
//        System.out.println(count);
//        System.out.println(eData.size());
//        HashSet<String> parts=new HashSet<>();
//        var database=Test.databaseEnglish();
//        for (int i=0;i< eData.size();i++ ) {
//            var en=eData.get(i)[0];
//            var part=eData.get(i)[2].trim();
//            if(part!=null &&!part.equals(""))
//            if (plist.containsKey(en)){
//                var ad=true;
//                for (var j:plist.get(en)){
//                    if(j.contains(part)||part.contains(j)){
//                        ad=false;
//                        break;
//                    }
//                }
//                if(ad)
//                eData.remove(i);
//            }
////                System.out.println(i[0]+"\t"+i[1]);
//        }
//        System.out.println(eData.size());
//        for(var i:eData){
//            parts.add(i[0]);
//        }
//        System.out.println(parts.size());
//        count=0;
//        for(var i:database){
//            if(!parts.contains(i)){
//                count++;
////                System.out.println(i);
//            }
//        }
//        System.out.println(count);
//        count=0;
//        for(var i:plist.keySet()){
//            if(!database.contains(i)){
//                count++;
////                System.out.println(i);
//            }
//        }
//        System.out.println(count);

    }


}
