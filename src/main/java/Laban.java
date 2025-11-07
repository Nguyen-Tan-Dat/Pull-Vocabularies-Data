import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.nio.charset.StandardCharsets;

public record Laban(String word) {


    public String pronunciationsDictionary() {
        String pronunciation = "";
        String apiUrl = "https://dict.laban.vn/ajax/getsound?accent=us&word=" + word;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            URI uri = new URI(apiUrl);
            HttpGet httpGet = new HttpGet(uri);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    pronunciation = new JSONObject(responseBody).getString("data");
                }
            }
        } catch (Exception _) {
        }
        return pronunciation;
    }

    public String phonetic() {
        String phonetic = "";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            URI uri = new URI("https://dict.laban.vn/ajax/widget-search?type=1&query=" + word + "&vi=0");
            HttpGet httpGet = new HttpGet(uri);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    String data = new JSONObject(responseBody).getJSONObject("enEnData").getJSONObject("best").getString("details");
                    Document document = Jsoup.parse(data);

                    // Lấy phần tử span có class là color-black để lấy phát âm
                    Elements spanElements = document.select("span.color-black");
                    for (Element span : spanElements) {
                        phonetic = span.text();
                        break; // Chỉ lấy phát âm đầu tiên
                    }
                }
            }
        } catch (Exception _) {
        }
        return phonetic;
    }

    public String partOfSpeech() {
        String apiKey = "pvi0hwxptqvz5t4fbyz51dcz8l8ivfj2jalnhjj8b2gt1k13w";
        String partOfSpeech = "";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Xây dựng URL cho yêu cầu API
            URI uri = new URI("https", "api.wordnik.com", "/v4/word.json/" + word + "/definitions", "api_key=" + apiKey, null);
            HttpGet httpGet = new HttpGet(uri);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            // Xử lý phản hồi
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                // Lấy nội dung phản hồi
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONArray jsonObject = new JSONArray(responseBody);
                if (!jsonObject.isEmpty()) {
                    JSONObject firstPhoto = jsonObject.getJSONObject(0);
                    partOfSpeech = firstPhoto.getString("partOfSpeech");

                }
            }
        } catch (Exception _) {
        }
        return partOfSpeech;
    }

    public String image() {
        String apiKey = "3cXnXWgieuriCD5oOmEM6SSepoFTrUs6A7OqK2IG7y1dkg96QFGcLenH";

        // URL của API
        String apiUrl = "https://api.pexels.com/v1/search?query=" + word;

        // Tạo HttpClient
        HttpClient httpClient = HttpClients.createDefault();

        // Tạo HttpGet request
        HttpGet httpGet = new HttpGet(apiUrl);

        // Thiết lập Authorization header
        httpGet.setHeader("Authorization", apiKey);
        String image = "";
        try {
            // Gửi request và nhận phản hồi
            HttpResponse response = httpClient.execute(httpGet);

            // Đọc nội dung phản hồi
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray photosArray = jsonObject.getJSONArray("photos");

            // Check if there are any photos
            if (!photosArray.isEmpty()) {
                JSONObject firstPhoto = photosArray.getJSONObject(0);
                image = firstPhoto.getJSONObject("src").getString("original");

            }
        } catch (Exception _) {
        }
        return image;
    }

    static void main() {
        var word = "lotus";
        var laban = new Laban(word);
        System.out.println(laban.image());
        System.out.println(laban.pronunciationsDictionary());
        System.out.println(laban.phonetic());
        System.out.println(laban.partOfSpeech());
    }
}
