import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class DownloadTopic {
    public static void main(String[] args) {
        String html = """
                <ol>
                <li><a href="https://englishfornoobs.com/farm-animals-names-list-pdf/">Domestic animals and farm animals</a></li>
                <li><a href="https://englishfornoobs.com/list-most-popular-zoo-animals-pdf/">Zoo animals</a></li>
                <li><a href="https://englishfornoobs.com/list-of-common-birds-pdf/">The birds</a></li>
                <li><a href="https://englishfornoobs.com/list-of-insects-names-a-z/">The insects</a></li>
                <li><a href="https://englishfornoobs.com/names-of-aquatic-animals-list-a-z/">Aquatic animals</a></li>
                <li><a href="https://englishfornoobs.com/nature-related-words-list-pdf/">Nature</a></li>
                <li><a href="https://englishfornoobs.com/list-of-feelings-and-emotions-pdf/">Feelings and emotions</a></li>
                <li><a href="https://englishfornoobs.com/words-related-to-environment-list-pdf/">The environment</a></li>
                <li><a href="https://englishfornoobs.com/hospitality-vocabulary-list-pdf/">Hospitality</a></li>
                <li><a href="https://englishfornoobs.com/clothes-vocabulary-list-pdf-a-z/">Clothing</a></li>
                <li><a href="https://englishfornoobs.com/transportation-vocabulary-pdf-list-a-z/">Transportation</a></li>
                <li><a href="https://englishfornoobs.com/family-related-words-list-pdf/">The family</a></li>
                <li><a href="https://englishfornoobs.com/political-vocabulary-list-pdf/">Politics &amp; Government</a></li>
                <li><a href="https://englishfornoobs.com/words-related-to-travel-and-tourism-list-pdf/">Travel &amp; tourism</a></li>
                <li><a href="https://englishfornoobs.com/human-body-parts-list-a-z-pdf/">The human body</a></li>
                <li><a href="https://englishfornoobs.com/describing-face-vocabulary-words-list-pdf/">Physical description: the face</a></li>
                <li><a href="https://englishfornoobs.com/health-medicine-vocabulary-pdf-list/">Health and Medicine</a></li>
                <li><a href="https://englishfornoobs.com/zodiac-astrological-signs-vocabulary-words-list-pdf/">The astrological signs</a></li>
                <li><a href="https://englishfornoobs.com/halloween-vocabulary-words-list-pdf/">Halloween</a></li>
                <li><a href="https://englishfornoobs.com/business-related-words-list-pdf/">Business</a></li>
                <li><a href="https://englishfornoobs.com/list-of-all-fruits-and-vegetables-pdf-list-a-z/">Fruits and vegetables</a></li>
                <li><a href="https://englishfornoobs.com/weather-related-words-list-pdf/">The Weather</a></li>
                <li><a href="https://englishfornoobs.com/words-related-to-film-industry-list-pdf/">Movies &amp; cinema</a></li>
                <li><a href="https://englishfornoobs.com/christmas-vocabulary-words-list-pdf/">Christmas</a></li>
                <li><a href="https://englishfornoobs.com/things-in-the-house-vocabulary-list-pdf/">The House</a></li>
                <li><a href="https://englishfornoobs.com/real-estate-related-words-pdf-list/">Real estate</a></li>
                <li><a href="https://englishfornoobs.com/marketing-vocabulary-list-pdf/">Marketing</a></li>
                <li><a href="https://englishfornoobs.com/finance-vocabulary-list-pdf/">Finance</a></li>
                <li><a href="https://englishfornoobs.com/computer-related-words-list-pdf/">IT &amp; internet</a></li>
                <li><a href="https://englishfornoobs.com/job-interview-vocabulary-pdf/">Job interview</a></li>
                <li><a href="https://englishfornoobs.com/immigration-vocabulary-terms-pdf-list/">Immigration</a></li>
                <li><a href="https://englishfornoobs.com/words-related-to-science-pdf/">Sciences</a></li>
                <li><a href="https://englishfornoobs.com/journalism-vocabulary-words-pdf-list/">Journalism &amp; press</a></li>
                <li><a href="https://englishfornoobs.com/good-bad-qualities-person-vocabulary-list-pdf/">Good and bad qualities</a></li>
                <li><a href="https://englishfornoobs.com/money-banking-vocabulary-pdf-list/">Money and banking</a></li>
                <li><a href="https://englishfornoobs.com/photography-vocabulary-terms-list-pdf/">Photography</a></li>
                <li><a href="https://englishfornoobs.com/religion-vocabulary-list-pdf/">The religion&nbsp;</a></li>
                <li><a href="https://englishfornoobs.com/five-senses-words-lists-pdf/">The five senses</a></li>
                <li><a href="https://englishfornoobs.com/love-and-wedding-vocabulary-pdf-list/">Love and wedding</a></li>
                <li><a href="https://englishfornoobs.com/sea-beach-related-words-list-pdf/">Beach and seaside&nbsp;</a></li>
                <li><a href="https://englishfornoobs.com/war-peace-related-words-list-pdf/">War and peace&nbsp;</a></li>
                <li><a href="https://englishfornoobs.com/words-related-to-history-pdf/">History and past&nbsp;</a></li>
                <li><a href="https://englishfornoobs.com/sports-vocabulary-pdf-list/">Sport</a></li>
                <li><a href="https://englishfornoobs.com/common-cycling-terms-list-pdf/">Cycling</a></li>
                <li><a href="https://englishfornoobs.com/train-travel-vocabulary-list-pdf/">Trains and railways</a></li>
                <li><a href="https://englishfornoobs.com/trees-plants-gardening-vocabulary-list-pdf/">Trees, plants and gardening</a></li>
                <li><a href="https://englishfornoobs.com/mythology-heroes-word-list-pdf/">Mythology and heroes</a></li>
                <li><a href="https://englishfornoobs.com/music-vocabulary-list-pdf/">Music</a></li>
                <li><a href="https://englishfornoobs.com/hobbies-vocabulary-pdf-list/">Hobbies</a></li>
                <li><a href="https://englishfornoobs.com/places-in-the-city-vocabulary-list-pdf/">The city</a></li>
                <li><a href="https://englishfornoobs.com/basic-architecture-vocabulary-list-pdf/">Architecture and construction&nbsp;</a></li>
                <li><a href="https://englishfornoobs.com/bathroom-items-vocabulary-pdf-list/">Bathroom items</a></li>
                <li><a href="https://englishfornoobs.com/education-school-related-words-list-pdf/">Education and school</a></li>
                <li><a href="https://englishfornoobs.com/soccer-vocabulary-pdf/">Soccer</a></li>
                <li><a href="https://englishfornoobs.com/wine-vocabulary-words-pdf-list/">Wine</a></li>
                <li><a href="https://englishfornoobs.com/kitchen-utensils-vocabulary-list-pdf/">Kitchen utensils</a></li>
                <li><a href="https://englishfornoobs.com/routine-daily-life-vocabulary-list-pdf/">Routine &amp; daily life</a></li>
                <li><a href="https://englishfornoobs.com/communication-vocabulary-english-pdf/">Communication</a></li>
                <li><a href="https://englishfornoobs.com/food-nutrition-words-list-pdf/">Food &amp; nutrition</a></li>
                <li><a href="https://englishfornoobs.com/gestures-positions-vocabulary-list-pdf/">Gestures and positions</a></li>
                <li><a href="https://englishfornoobs.com/character-behaviors-vocabulary-list-pdf/">Personality: character and behavior</a></li>
                <li><a href="https://englishfornoobs.com/astronomy-space-universe-vocabulary-list-pdf/">Astronomy, space and universe</a></li>
                <li><a href="https://englishfornoobs.com/natural-disasters-vocabulary-words-list-pdf/">Natural disasters</a></li>
                <li><a href="https://englishfornoobs.com/accidents-and-injuries-vocabulary-pdf-list/">Accidents and injuries</a></li>
                <li><a href="https://englishfornoobs.com/crime-vocabulary-list-pdf/">Crimes&nbsp;</a></li>
                <li><a href="https://englishfornoobs.com/quantities-vocabulary-english/">The quantities</a></li>
                <li><a href="https://englishfornoobs.com/list-of-jobs-in-english-pdf/">List of jobs in English</a></li>
                <li><a href="https://englishfornoobs.com/expressing-opinion-in-english-pdf/">Expressing opinion</a></li>
                <li><a href="https://englishfornoobs.com/at-the-airport-vocabulary-pdf/">At the airport</a></li>
                <li><a href="https://englishfornoobs.com/english-for-logistics-vocabulary-pdf/">Logistics</a></li>
                <li><a href="https://englishfornoobs.com/food-and-drinks-vocabulary-pdf/">Food &amp; drinks</a></li>
                <li><a href="https://englishfornoobs.com/linking-words-english-pdf/">Linking words</a></li>
                <li><a href="https://englishfornoobs.com/english-accounting-vocabulary-pdf/">Accounting</a></li>
                <li><a href="https://englishfornoobs.com/materials-vocabulary-in-english-pdf/">Materials</a></li>
                <li><a href="https://englishfornoobs.com/law-and-legals-terms-vocabulary-english-pdf/">Law and Legal terms</a></li>
                <li><a href="https://englishfornoobs.com/business-and-economics-vocabulary-pdf/">Business and Economics</a></li>
                <li><a href="https://englishfornoobs.com/list-most-common-adjectives-english-pdf/">Useful adjectives (A to C)</a></li>
                <li><a href="https://englishfornoobs.com/list-of-adjectives-in-english-pdf/">Useful adjectives (D to H)</a></li>
                <li><a href="https://englishfornoobs.com/list-adjectives-in-english-pdf/">Useful adjectives (I to O)</a></li>
                <li><a href="https://englishfornoobs.com/english-adjectives-list-a-z/">Useful adjectives (P to S)</a></li>
                <li><a href="https://englishfornoobs.com/list-of-adjectives-english-pdf/">Useful adjectives (T to Z)</a></li>
                <li><a href="https://englishfornoobs.com/100-most-common-phrasal-verbs-list/">100 most common Phrasal verbs list</a></li>
                <li><a href="https://englishfornoobs.com/list-of-adverbs-in-english/">List of adverbs</a></li>
                <li><a href="https://englishfornoobs.com/most-common-verbs-in-english-pdf/">100 most used English verbs</a></li>
                <li><a href="https://englishfornoobs.com/body-movement-and-action-verbs-list-pdf/">100 English verbs of body movement and action</a></li>
                <li><a href="https://englishfornoobs.com/100-most-common-english-adverbs/">100 most common English adverbs</a></li>
                </ol>
                """;

        Document doc = Jsoup.parse(html);
        Elements links = doc.select("ol li a[href]");

        for (Element link : links) {
            String href = link.attr("href");
            String text = link.text();
            System.out.println(getLinkFromHTML(getHTML(href)));
//            break;
        }
    }
    public static String getLinkFromHTML(String html) {
        Document doc = Jsoup.parse(html);
        Elements links = doc.select("a[href][style=color: #ff0000; text-decoration: underline;]");

        for (Element link : links) {
            String href = link.attr("href");
            return href;
        }

        return null;
    }
    public static String getHTML(String url) {
        StringBuilder html = new StringBuilder();
        try {
            URL urlObj = new URL(url);
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlObj.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                html.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return html.toString();
    }
}
