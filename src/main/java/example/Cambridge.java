package example;

import java.io.*;
import java.nio.file.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

public class Cambridge {

    public static HashMap<String, HashSet<String>> readOnlineWords() {
        HashMap<String, HashSet<String>> plist = new HashMap<>();
        for (int number = 1; number <= 4; number++) {
            String directoryPath = "Cambridge Vocabularies/" + number;
            Path path = Paths.get(directoryPath);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.xlsx")) {
                for (Path filePath : stream) {
                    try (FileInputStream fis = new FileInputStream(filePath.toString()); Workbook workbook = new XSSFWorkbook(fis)) {
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

    public static void main2(String[] args) {
        var cams = readOnlineWords();
        System.out.println(cams.size());
    }

    public static void main(String[] args) {
//        writeGrammarTopic();
//        vocabulariesElementary();
//        for(var i=1;i<22;i++)
        writeEnglishVocabulariesForIELTS(1,21);
    }

    private static void writeEnglishVocabulariesForIELTS(int start, int end) {
        var text=Test.readPdf("vocabularies clone/Cambridge Vocabularies/Word List.pdf"
        );
        var list=Elllo.extractWords(text);
        var ens=Test.databaseEnglish();
        HashSet<String> set=new HashSet<>();
        for(var i:ens){
            if(list.contains(i)){
                set.add(i);
            }
        }
        for(var i:list){
            if(!set.contains(i)){
                System.out.println(i);
//                Test.printVocabulariesLaban(i,"Vocabularies IELTS more.xlsx");
            }
        }
        System.out.println(set.size());
        Test.writeTopic("CAMBRIDGE VOCABULARY FOR IELTS"+end,list);
    }

    public static void vocabulariesElementary(){
        var text=Test.readPdf("vocabularies clone/Cambridge Vocabularies/English Vocabulary in Use Elementary  (2017).pdf",160,170);
        var words=Elllo.extractWords(text);
        words.size();
        System.out.println(text);
        System.out.println(words.size());
        var txt=Test.dataBook();
        System.out.println(txt.size());
        var count=0;
        for(var i:txt){
            if(!words.contains(i.toLowerCase())){
                System.out.println(i);
                count++;
            }
        }
        System.out.println(count);
    }

    private static void writeGrammarTopic() {
        var ens = Test.databaseEnglish();
        ArrayList<Object> data = new ArrayList<>();
        File[] oxfSubdirectories = Oxford.getDirectories("Oxford topics");
        File[] camSubdirectories = Oxford.getDirectories("Cambridge word lists");
        File[] subdirectories = new File[oxfSubdirectories.length + camSubdirectories.length];
        System.arraycopy(oxfSubdirectories, 0, subdirectories, 0, oxfSubdirectories.length);
        System.arraycopy(camSubdirectories, 0, subdirectories, oxfSubdirectories.length, camSubdirectories.length);
        int start=305,end=305;
        var cams = Elllo.extractWords(Test.readPdf("vocabularies clone/Cambridge Vocabularies/English Vocabulary in Use Elementary  (2017).pdf",160,170).toLowerCase());
//        var cams = Elllo.extractWords(Test.readPdf(
//                "vocabularies clone/Vocabularies/English Grammar in Use.pdf"
//                ,start,
//                end
//        ).toLowerCase());
        HashSet<String> list = new HashSet<>();
        for (File subdirectory : subdirectories) {
            List<Workbook> workbooks = Oxford.readExcelFiles(subdirectory.getAbsolutePath());
            for (Workbook workbook : workbooks) {
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    for (Row row : sheet) {
                        String english = row.getCell(0).getStringCellValue();
                        String lv = row.getCell(2).getStringCellValue().toLowerCase();
                        if (lv.equals("a1") || lv.equals("a2") || lv.equals("b1") || lv.equals("b2")
//                                || lv.equals("c1")
//                                || lv.equals("c2")
                        ) {
                            for (var en : ens) {
                                if (en.equals(english)) {
                                    if (cams.contains(en.toLowerCase())) list.add(english);
                                }
                            }
                        }
                    }
                }
            }
            for (Workbook workbook : workbooks) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String name = "English Vocabularies in Use "+start+"-"+end;
//        String name = "English Grammar in Use";
        HashMap<String, Object> row = new HashMap<>();
        row.put("name", name);
        row.put("vs", list);
        data.add(row);
        Test.writeTopics(data, "Oxford topics json/"+name);
    }

    public static void main1(String[] args) {
        var pdfCamBooks = Test.dataBook();
        var onlineCambridgeWords = readOnlineWords();
        System.out.println("Book online: " + onlineCambridgeWords.size());
        System.out.println("PDF books: " + pdfCamBooks.size());

        for (var i : onlineCambridgeWords.keySet()) {
//            if(!pdfCamBooks.contains(i)){
            pdfCamBooks.add(i);
//            }
        }
        System.out.println("Cambridge vocabularies: " + pdfCamBooks.size());
        var oxfordVs = Oxford.readOxfordVocabularies();
        System.out.println(oxfordVs.size());
        int count = 0;
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
