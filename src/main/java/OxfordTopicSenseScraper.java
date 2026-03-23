import org.apache.poi.ss.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tối ưu so với bản gốc:
 *  1. Pre-fetch song song (FETCH_THREADS=4): thu thập toàn bộ URL trước,
 *     fetch đồng thời nhiều thread → phase xử lý row chỉ đọc cache, không chờ HTTP.
 *  2. Pattern cache: Pattern.compile() chỉ chạy 1 lần cho mỗi word.
 *  3. Normalize cache: normalize() trên cùng string không tính lại.
 *  4. ExpandedKeywords cache: set keyword cho mỗi topic chỉ build 1 lần.
 *  5. KEYWORD_MAP static final: không tạo HashMap mới mỗi lần gọi hàm.
 *  6. ConcurrentHashMap cho tất cả cache: thread-safe khi fetch song song.
 */
public class OxfordTopicSenseScraper {

    private static final Path ROOT_DIR   = Paths.get("C:/Users/Nguyen Tan Dat/Documents/Topics of Oxford");
    private static final Path OUTPUT_DIR = Paths.get("C:/Users/Nguyen Tan Dat/Documents/Topics of Oxford meaning data");

    private static final long MIN_DELAY_MS  = 1000;
    private static final long MAX_DELAY_MS  = 1800;
    private static final int  TIMEOUT_MS    = 25000;
    /**
     * Số thread HTTP song song. Tăng lên 6-8 nếu Oxford chịu được; mặc định 4 là an toàn.
     */
    private static final int  FETCH_THREADS = 4;

    // input columns
    private static final int COL_WORD  = 0;
    private static final int COL_POS   = 1;
    private static final int COL_LEVEL = 2;
    private static final int COL_LINK  = 3;

    // output columns
    private static final int COL_MATCHED_TOPIC = 4;
    private static final int COL_DEF_EN        = 5;
    private static final int COL_EXAMPLE_EN    = 6;
    private static final int COL_SENSE_ID      = 7;
    private static final int COL_SCORE         = 8;
    private static final int COL_STATUS        = 9;

    // ── caches (thread-safe) ──────────────────────────────────────────────────
    private static final ConcurrentHashMap<String, Document>    DOC_CACHE     = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Pattern>     PATTERN_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String>      NORM_CACHE    = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Set<String>> KW_CACHE      = new ConcurrentHashMap<>();

    // ── keyword map: static final, xây dựng 1 lần ───────────────────────────
    private static final Map<String, List<String>> KEYWORD_MAP;
    static {
        Map<String, List<String>> m = new LinkedHashMap<>();
        m.put("animals",          List.of("animal","animals","pet","pets","wildlife","horse","bird","fish","dog","cat"));
        m.put("animal behaviour", List.of("behaviour","behavior","hunt","feed","mate","breed","move","sleep"));
        m.put("sports",           List.of("sport","sports","ball","racket","baseball","cricket","tennis"));
        m.put("food",             List.of("food","drink","cooking","meal","restaurant"));
        m.put("health",           List.of("health","body","illness","disease","medical","medicine"));
        m.put("appearance",       List.of("appearance","clothes","fashion","hair","face","body"));
        m.put("culture",          List.of("culture","art","music","literature","media"));
        m.put("communication",    List.of("communication","language","speaking","writing","discussion"));
        m.put("nature",           List.of("nature","environment","weather","plant","animal"));
        KEYWORD_MAP = Collections.unmodifiableMap(m);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MAIN — 3 phase rõ ràng
    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            Files.createDirectories(OUTPUT_DIR);

            List<Path> excelFiles = findExcelFiles(ROOT_DIR);
            System.out.println("Found excel files: " + excelFiles.size());

            // Phase 1 ── scan tất cả file, thu thập URL duy nhất (không fetch)
            Set<String> allUrls = collectAllUrls(excelFiles);
            System.out.println("Unique URLs to fetch: " + allUrls.size());

            // Phase 2 ── fetch song song, lấp đầy DOC_CACHE
            preFetchUrls(allUrls);
            System.out.println("Pre-fetch done. Cached: " + DOC_CACHE.size());

            // Phase 3 ── xử lý row (toàn bộ hit cache, không chờ HTTP nữa)
            for (Path excel : excelFiles) {
                try {
                    processExcelFile(excel);
                } catch (Exception e) {
                    System.err.println("ERROR processing file: " + excel);
                    e.printStackTrace();
                }
            }

            System.out.println("DONE.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Chỉ tìm file all.xlsx (không phân biệt hoa/thường), bỏ qua OUTPUT_DIR
    // ─────────────────────────────────────────────────────────────────────────
    private static List<Path> findExcelFiles(Path root) throws IOException {
        Path outputAbs = OUTPUT_DIR.toAbsolutePath().normalize();
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> !p.toAbsolutePath().normalize().startsWith(outputAbs))
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase("all.xlsx"))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Phase 1: scan nhanh workbook, thu thập tập URL duy nhất
    // ─────────────────────────────────────────────────────────────────────────
    private static Set<String> collectAllUrls(List<Path> excelFiles) {
        Set<String> urls = new LinkedHashSet<>();
        DataFormatter fmt = new DataFormatter();
        for (Path excel : excelFiles) {
            try (InputStream in = Files.newInputStream(excel);
                 Workbook wb = WorkbookFactory.create(in)) {
                Sheet sheet = wb.getSheetAt(0);
                for (int r = 0; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    String link = getCellString(row, COL_LINK, fmt);
                    if (!isBlank(link)) urls.add(link.trim());
                }
            } catch (Exception e) {
                System.err.println("collectAllUrls error: " + excel + " — " + e.getMessage());
            }
        }
        return urls;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Phase 2: pre-fetch song song với FETCH_THREADS thread
    // Mỗi thread tự sleep ngẫu nhiên → rate-limit phân tán tự nhiên
    // ─────────────────────────────────────────────────────────────────────────
    private static void preFetchUrls(Set<String> urls) throws InterruptedException {
        if (urls.isEmpty()) return;

        ExecutorService pool  = Executors.newFixedThreadPool(FETCH_THREADS);
        AtomicInteger   done  = new AtomicInteger(0);
        int             total = urls.size();

        for (String url : urls) {
            pool.submit(() -> {
                if (DOC_CACHE.containsKey(url)) {
                    System.out.printf("[%d/%d] already cached: %s%n",
                            done.incrementAndGet(), total, url);
                    return;
                }
                sleepRandom();  // sleep trước khi fetch, độc lập mỗi thread
                try {
                    fetchAndCache(url);
                    System.out.printf("[%d/%d] OK: %s%n", done.incrementAndGet(), total, url);
                } catch (Exception e) {
                    System.err.printf("[%d/%d] FAIL: %s — %s%n",
                            done.incrementAndGet(), total, url, e.getMessage());
                }
            });
        }

        pool.shutdown();
        if (!pool.awaitTermination(2, TimeUnit.HOURS)) {
            System.err.println("WARNING: pre-fetch timeout sau 2h");
        }
    }

    /**
     * Gọi HTTP và lưu cache. putIfAbsent đảm bảo không ghi đè
     * khi 2 thread hiếm khi cùng fetch 1 URL.
     */
    private static void fetchAndCache(String url) throws IOException {
        if (DOC_CACHE.containsKey(url)) return; // double-check trước khi gọi HTTP
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0 Safari/537.36")
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Referer", "https://www.google.com/")
                .get();
        DOC_CACHE.putIfAbsent(url, doc);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Phase 3: xử lý từng file — sequential (Apache POI không thread-safe)
    // ─────────────────────────────────────────────────────────────────────────
    private static void processExcelFile(Path excelFile) throws Exception {
        System.out.println("\nProcessing: " + excelFile);

        Path relative = ROOT_DIR.relativize(excelFile);
        Path outPath  = OUTPUT_DIR.resolve(relative);
        Files.createDirectories(outPath.getParent());

        String topicLevel2 = getTopicLevel2(ROOT_DIR, excelFile);
        String fileTopic   = removeExtension(excelFile.getFileName().toString());

        System.out.println("topicLevel2 = " + topicLevel2);
        System.out.println("fileTopic   = " + fileTopic);

        try (InputStream in = Files.newInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(in)) {

            Sheet         sheet        = workbook.getSheetAt(0);
            DataFormatter formatter    = new DataFormatter();
            Set<String>   fileSeenKeys = new HashSet<>();

            int lastRow = sheet.getLastRowNum();
            for (int r = 0; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String word  = getCellString(row, COL_WORD,  formatter);
                String pos   = getCellString(row, COL_POS,   formatter);
                String level = getCellString(row, COL_LEVEL, formatter);
                String link  = getCellString(row, COL_LINK,  formatter);

                if (isBlank(word) || isBlank(link)) {
                    setCell(row, COL_STATUS, "SKIP_EMPTY");
                    continue;
                }

                String dedupeKey = normalize(word) + "||" + normalize(pos) + "||" + link.trim();
                if (fileSeenKeys.contains(dedupeKey)) {
                    setCell(row, COL_STATUS, "SKIP_DUPLICATE_ROW");
                    continue;
                }
                fileSeenKeys.add(dedupeKey);

                try {
                    boolean inCache = DOC_CACHE.containsKey(link.trim());
                    SenseResult best = fetchBestSense(link, word, pos, level, topicLevel2, fileTopic);

                    if (best == null) {
                        setCell(row, COL_MATCHED_TOPIC, "");
                        setCell(row, COL_DEF_EN,        "");
                        setCell(row, COL_EXAMPLE_EN,    "");
                        setCell(row, COL_SENSE_ID,      "");
                        setCell(row, COL_SCORE,         "0");
                        setCell(row, COL_STATUS,        "NO_MATCH");
                    } else {
                        setCell(row, COL_MATCHED_TOPIC, best.topicName);
                        setCell(row, COL_DEF_EN,        best.definition);
                        setCell(row, COL_EXAMPLE_EN,    best.example);
                        setCell(row, COL_SENSE_ID,      best.senseId);
                        setCell(row, COL_SCORE,         String.format(Locale.US, "%.2f", best.score));
                        setCell(row, COL_STATUS,        inCache ? "OK_CACHE" : "OK_FETCH");

                        System.out.printf(Locale.US, "Row %d | %s | %s | score=%.2f | topic=%s%n",
                                r, word, pos, best.score, best.topicName);
                    }

                } catch (Exception ex) {
                    setCell(row, COL_STATUS,     "ERROR");
                    setCell(row, COL_EXAMPLE_EN, safe(ex.getMessage()));
                    System.err.printf("Row %d failed | word=%s | link=%s%n", r, word, link);
                }
            }

            try (OutputStream out = Files.newOutputStream(outPath)) {
                workbook.write(out);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Scoring
    // ─────────────────────────────────────────────────────────────────────────
    private static SenseResult fetchBestSense(String url,
                                              String word,
                                              String pos,
                                              String level,
                                              String topicLevel2,
                                              String fileTopic) throws IOException {
        Document doc = getDocument(url);
        List<SenseResult> senses = extractSenses(doc, word, pos, level);
        if (senses.isEmpty()) return null;

        // Normalize 1 lần, tái dùng cho toàn bộ senses trong row này
        String normTopicLevel2 = normalize(topicLevel2);
        String normFileTopic   = normalize(fileTopic);
        String normLevel       = normalize(level);

        for (SenseResult s : senses) {
            s.score = scoreSense(s, normTopicLevel2, normFileTopic, normLevel);
        }

        senses.sort(
                Comparator.comparingDouble((SenseResult s) -> s.score).reversed()
                        .thenComparing(s -> safe(s.topicName))
                        .thenComparing(s -> safe(s.definition))
        );

        return senses.get(0);
    }

    private static Document getDocument(String url) throws IOException {
        String key    = url.trim();
        Document cached = DOC_CACHE.get(key);
        if (cached != null) return cached;

        // Fallback nếu URL không có trong cache (rất hiếm)
        sleepRandom();
        fetchAndCache(key);
        return DOC_CACHE.get(key);
    }

    private static List<SenseResult> extractSenses(Document doc, String word, String pos, String level) {
        List<SenseResult> results = new ArrayList<>();
        Elements senseEls = doc.select("li.sense");

        for (Element senseEl : senseEls) {
            String def = cleanText(senseEl.selectFirst("span.def"));
            if (def.isBlank()) continue;

            SenseResult s = new SenseResult();
            s.word       = word;
            s.pos        = pos;
            s.level      = level;
            s.senseId    = safe(senseEl.id());
            s.definition = def;
            s.example    = cleanText(senseEl.selectFirst("ul.examples li span.x"));
            s.cefr       = cleanText(senseEl.attr("cefr"));

            List<String> topics = senseEl.select("span.topic-g span.topic_name")
                    .stream()
                    .map(Element::text)
                    .map(OxfordTopicSenseScraper::cleanText)
                    .filter(t -> !t.isBlank())
                    .collect(Collectors.toList());

            s.allTopics = topics;
            s.topicName = topics.isEmpty() ? "" : topics.get(0);
            results.add(s);
        }

        return results;
    }

    private static double scoreSense(SenseResult s,
                                     String topicLevel2,
                                     String fileTopic,
                                     String level) {
        double score = 0.0;

        String topic     = normalize(s.topicName);
        String allTopics = normalize(String.join(" ", s.allTopics));
        String def       = normalize(s.definition);
        String ex        = normalize(s.example);
        String cefr      = normalize(s.cefr);

        if (!topicLevel2.isBlank()) {
            if (containsPhrase(topic,     topicLevel2)) score += 12;
            if (containsPhrase(allTopics, topicLevel2)) score += 8;
        }

        if (!fileTopic.isBlank()) {
            if (containsPhrase(topic,     fileTopic)) score += 8;
            if (containsPhrase(allTopics, fileTopic)) score += 6;
            if (containsKeyword(def,      fileTopic)) score += 3;
            if (containsKeyword(ex,       fileTopic)) score += 2;
        }

        if (!topic.isBlank()) score += 1.2;

        if (!level.isBlank() && !cefr.isBlank() && level.equals(cefr)) score += 1.5;

        score += keywordBoost(topicLevel2, def, ex, topic, allTopics);
        score += keywordBoost(fileTopic,   def, ex, topic, allTopics);

        return score;
    }

    private static double keywordBoost(String source, String def, String ex,
                                       String topic, String allTopics) {
        if (source.isBlank()) return 0.0;
        double score = 0.0;
        for (String kw : expandedKeywords(source)) {
            if (kw.isBlank()) continue;
            if (containsWholeWord(topic,     kw)) score += 2.0;
            if (containsWholeWord(allTopics, kw)) score += 1.5;
            if (containsWholeWord(def,       kw)) score += 0.8;
            if (containsWholeWord(ex,        kw)) score += 0.5;
        }
        return score;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // expandedKeywords: cached — không build lại set keyword cho cùng 1 string
    // ─────────────────────────────────────────────────────────────────────────
    private static Set<String> expandedKeywords(String raw) {
        return KW_CACHE.computeIfAbsent(raw, OxfordTopicSenseScraper::buildKeywords);
    }

    private static Set<String> buildKeywords(String raw) {
        Set<String> out = new LinkedHashSet<>();
        String norm = normalize(raw);
        if (norm.isBlank()) return out;

        out.add(norm);
        for (String p : norm.split("\\s+")) {
            if (p.length() >= 3) out.add(p);
        }
        for (Map.Entry<String, List<String>> e : KEYWORD_MAP.entrySet()) {
            if (norm.contains(e.getKey())) out.addAll(e.getValue());
        }
        return Collections.unmodifiableSet(out);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static String getTopicLevel2(Path rootDir, Path filePath) {
        Path rel = rootDir.relativize(filePath);
        if (rel.getNameCount() >= 3) return rel.getName(1).toString();
        if (rel.getNameCount() >= 2) return rel.getName(0).toString();
        return "";
    }

    private static String getCellString(Row row, int col, DataFormatter formatter) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }

    private static void setCell(Row row, int col, String value) {
        Cell cell = row.getCell(col);
        if (cell == null) cell = row.createCell(col);
        cell.setCellValue(value == null ? "" : value);
    }

    private static String cleanText(Element el) {
        if (el == null) return "";
        return cleanText(el.text());
    }

    private static String cleanText(String s) {
        if (s == null) return "";
        return s.replace('\u00A0', ' ')
                .replace("\u200B", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * normalize: kết quả được cache — topicLevel2/fileTopic/level
     * lặp lại hàng nghìn lần nên cache cực kỳ hiệu quả.
     */
    private static String normalize(String s) {
        if (s == null) return "";
        return NORM_CACHE.computeIfAbsent(s, raw ->
                Normalizer.normalize(raw, Normalizer.Form.NFD)
                        .replaceAll("\\p{M}+", "")
                        .toLowerCase(Locale.ROOT)
                        .replace("&", " and ")
                        .replaceAll("[^a-z0-9 ]+", " ")
                        .replaceAll("\\s+", " ")
                        .trim()
        );
    }

    private static boolean containsPhrase(String text, String phrase) {
        if (isBlank(text) || isBlank(phrase)) return false;
        return text.contains(phrase);
    }

    private static boolean containsKeyword(String text, String phrase) {
        if (isBlank(text) || isBlank(phrase)) return false;
        for (String p : phrase.split("\\s+")) {
            if (p.length() >= 3 && containsWholeWord(text, p)) return true;
        }
        return false;
    }

    /**
     * containsWholeWord: Pattern compile 1 lần per word, cache lại.
     * Không compile lại hàng triệu lần như bản gốc.
     */
    private static boolean containsWholeWord(String text, String word) {
        if (isBlank(text) || isBlank(word)) return false;
        Pattern p = PATTERN_CACHE.computeIfAbsent(
                word, w -> Pattern.compile("\\b" + Pattern.quote(w) + "\\b"));
        return p.matcher(text).find();
    }

    private static void sleepRandom() {
        long delay = ThreadLocalRandom.current().nextLong(MIN_DELAY_MS, MAX_DELAY_MS + 1);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String removeExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx >= 0 ? fileName.substring(0, idx) : fileName;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static class SenseResult {
        String       word;
        String       pos;
        String       level;
        String       senseId;
        String       definition;
        String       example;
        String       topicName;
        List<String> allTopics = new ArrayList<>();
        String       cefr;
        double       score;
    }
}