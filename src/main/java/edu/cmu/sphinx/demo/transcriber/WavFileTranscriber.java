package edu.cmu.sphinx.demo.transcriber;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.result.WordResult;

import java.io.FileInputStream;
import java.io.InputStream;

public class WavFileTranscriber {

    public static void main(String[] args) {

        try {
            String audioFilePath = "output.wav";
            Configuration configuration = new Configuration();

            // Đặt mô hình âm thanh
            configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
            configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
            configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

            StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
            InputStream stream = new FileInputStream(audioFilePath);

            recognizer.startRecognition(stream);
            SpeechResult result;
            StringBuilder resultText = new StringBuilder();

            while ((result = recognizer.getResult()) != null) {
                for (WordResult r : result.getWords()) {
                    resultText.append(r.getWord().getSpelling()).append(' ');
                    System.out.println(r.getWord().getSpelling());
                }
            }
            recognizer.stopRecognition();

            System.out.println("Transcription: " + resultText);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
