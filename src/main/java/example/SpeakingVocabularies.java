package example;

public class SpeakingVocabularies {
    public static void main(String[] args) {
        var text=Test.readPdf("IELTS Books/Speaking_for_IELTS_Collins.pdf",9,10);
        System.out.println(text);
    }
}
