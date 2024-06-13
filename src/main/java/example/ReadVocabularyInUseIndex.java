package example;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.IOException;

public class ReadVocabularyInUseIndex {
    private String pdfPath;

    // Constructor
    public ReadVocabularyInUseIndex(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    // Method to read text from startPage to endPage
    public void readTextFromPages(int startPage, int endPage) {
        try {
            // Open the PDF document
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(pdfPath));

            // Get the total number of pages
            int numberOfPages = pdfDoc.getNumberOfPages();

            // Validate page numbers
            if (startPage < 1 || endPage > numberOfPages || startPage > endPage) {
                System.out.println("Invalid page range");
                pdfDoc.close();
                return;
            }

            // Read text from the specified page range
            for (int i = startPage; i <= endPage; i++) {
                String pageContent = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i));
//                System.out.println("Content of page " + i + ":");
                System.out.println(pageContent);
            }

            // Close the PDF document
            pdfDoc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Path to the PDF file
        String pdfFilePath = "English Vocabulary in Use book Series/Michael McCarthy_ Felicity O’Dell - English Vocabulary in Use Elementary Book with Answers and Enhanced eBook_ Vocabulary Reference and Practice-Cambridge University Press (2017).pdf"; // Thay thế bằng đường dẫn thực tế đến tệp PDF của bạn

        // Create an instance of ReadVocabularyInUseIndex
        ReadVocabularyInUseIndex reader = new ReadVocabularyInUseIndex(pdfFilePath);

        // Read text from page 10 to page 30
        reader.readTextFromPages(160, 170);
    }
}
