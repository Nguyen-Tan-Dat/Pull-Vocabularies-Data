package example;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;

public class ReadPDFsInDirectory {
    public static void main(String[] args) {
        // Đường dẫn tới thư mục chứa các tệp PDF
        String directoryPath = "Topics";

        // Tạo đối tượng Path cho thư mục
        Path path = Paths.get(directoryPath);

        // Sử dụng DirectoryStream để duyệt qua các tệp trong thư mục
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.pdf")) {
            int count=0;
            System.out.println("[");
            for (Path filePath : stream) {
                readPDFContent(filePath.toFile());
                System.out.println(",");
                count++;
            }
            System.out.println("]");
            System.out.println(count);
        } catch (IOException | DirectoryIteratorException e) {
            System.err.println("Lỗi khi đọc thư mục: " + e.getMessage());
        }
    }

    // Hàm đọc nội dung của một tệp PDF
    public static void readPDFContent(File file) {
        var database=Test.databaseEnglish();
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(file));
//            StringBuilder text = new StringBuilder();
            HashSet<String> list=new HashSet<>();
            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                var line=PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i));
                if(!line.isEmpty()&&!line.equals(" ")) {
                    for(var en: database){
                         line=line.toLowerCase();
                        if(en.length()>3&&line.contains(en.toLowerCase())){
                            list.add(en);
                        }
                    }
                }
            }
            pdfDoc.close();
            String filename=file.getName();
            filename=filename.replaceAll("-"," ");
            filename=filename.split(".pdf")[0].trim();
            System.out.print("['name'=>'"+filename+"','vs'=>[");
            for(var i: list){
                System.out.print("\""+i+"\",");
            }
            System.out.print("]]");
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc tệp PDF: " + e.getMessage());
        }
    }
}
