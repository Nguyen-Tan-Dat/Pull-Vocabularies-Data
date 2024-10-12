package example;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;

public class TopicExams {

    // Hàm đọc nội dung của một tệp PDF
    public static void readPDFContent(File file) {
        var database=Test.databaseEnglish();
        HashSet<String> list=new HashSet<>();
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(file));
            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                var line=PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i));
                if(!line.isEmpty()&&!line.equals(" ")) {
                    for(var en: database){
                        line=line.toLowerCase();
                        if(line.contains(en.toLowerCase())){
                            list.add(en);
                        }
                    }
                }
            }
            pdfDoc.close();
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc tệp PDF: " + e.getMessage());
        }Test.writeTopic(file.getName(),list,"topics/"+file.getName());
    }
    public static void main(String[] args) {
        String directoryPath = "academics";
        Path path = Paths.get(directoryPath);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.pdf")) {
            int count=0;
            System.out.println("[");
            for (Path filePath : stream) {
                readPDFContent(filePath.toFile());
            }
        } catch (IOException | DirectoryIteratorException e) {
            System.err.println("Lỗi khi đọc thư mục: " + e.getMessage());
        }
    }
    public static void main1(String[] args) {
        String pdfPath = "academics/Cambridge IELTS 17 - Academic (clean).pdf";  // Thay thế bằng đường dẫn thực tế đến tệp PDF của bạn
        try {
            // Mở tệp PDF
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(pdfPath));

            // Kiểm tra tổng số trang
            int numberOfPages = pdfDoc.getNumberOfPages();
            System.out.println("Total number of pages: " + numberOfPages);

            System.out.println("[");
            int[] list = new int[]{10, 31, 53, 75,96};
            for (int id=0;id<list.length-1;id++) {
                int s=list[id];
                int e=list[id+1];
                String test = "";
                for (int i = s; i <e && i <= numberOfPages; i++) {
                    String pageContent = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i));
                    test += pageContent;
                }
                var database = Test.databaseEnglish();
                System.out.print("['name'=>'"+"Test "+(id+1)+"','vs'=>[");
                for (var i : database) {
                    if (test.contains(i)) {
                        System.out.print("\""+i+"\",");
                    }
                }
                System.out.println("]],");

            }
            System.out.println("]");
            // Đóng tài liệu PDF
            pdfDoc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
