package parse;

import db.DBConnection;
import lemmatizer.Lemmatizer;
import models.Field;
import models.IndexTable;
import models.Lemma;
import models.Page;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WebMapParse extends RecursiveTask<Integer> {
    static {
        websites = new CopyOnWriteArraySet<>();
        pageId = new AtomicInteger(0);
        dbConnection = DBConnection.getInstance();
        lemmatizer = Lemmatizer.getInstance();
        fields = new HashMap<>();
        WebMapParse.dbConnection.getAllData(Field.class).forEach(it -> {
            WebMapParse.fields.put(it.getName(), it.getWeight());
        });
    }

    private static final Set<String> websites;
    private static final DBConnection dbConnection;
    private final static AtomicInteger pageId;
    private static String mainPage = "";
    private final static Lemmatizer lemmatizer;
//    private final static List<Field> fields;
    private final static Map<String, Float> fields;

    private Integer pageCount;
    private final List<WebMapParse> children;
    private final String startPage;

    public WebMapParse() {
        startPage = "http://www.playback.ru";
        children = new ArrayList<>();
        websites.add(startPage);
        pageCount = 0;

        if (mainPage.equals("")) {
            mainPage = startPage;
        }
    }

    public WebMapParse(String startPage) {
        children = new ArrayList<>();

        this.startPage = startPage;
        websites.add(startPage);
        pageCount = 0;

        if (mainPage.equals("")) {
            mainPage = startPage;
        }
    }

    @Override
    protected Integer compute() {
        try {
            Connection.Response response = Jsoup.connect(startPage)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; rv:98.0) Gecko/20100101 Firefox/98.0")
                    .referrer("http://google.com")
                    .ignoreHttpErrors(true)
                    .execute();

            Document document = response.parse();

            synchronized (pageId) {
                pageId.getAndIncrement();

                addPage(response, document);

                Elements elements = document.select("a");
                elements.forEach(element -> {
                    String attr = element.attr("href");
                    if (!attr.contains(mainPage)) {
                        if (!attr.startsWith("/")) {
                            attr = "/" + attr;
                        }

                        attr = mainPage + attr;
                    }
                    if (attr.contains(mainPage) && !websites.contains(attr) && !attr.contains("#")) {
                        newChild(attr);
                    }
                });

                Thread.sleep(1000);
            }

        } catch (IOException | InterruptedException exception) {
            exception.printStackTrace();
        }

        children.forEach(it -> pageCount += it.join());

        return pageCount;
    }

    private void newChild(String attr) {
        websites.add(attr);

        WebMapParse newChild = new WebMapParse(attr);
        newChild.fork();
        children.add(newChild);
    }

    private void addPage(Connection.Response response, Document document) {
        Page page = new Page();

        page.setId(pageId.get());
        page.setCode(response.statusCode());
        page.setPath(startPage);
        page.setContent(document.html());

        dbConnection.addClass(page);

        pageCount++;

        if (response.statusCode() < 400) {
            addLemmas(document, page);
        }
    }

    private void addLemmas(Document document, Page page) {
        AtomicBoolean newPage = new AtomicBoolean(true);
        fields.forEach((key, value) -> {
            Elements el = document.select(key);
            lemmatizer.addString(el.text(), newPage.get(), value);
            newPage.set(false);
        });

        addIndexTable(page);
    }

    private void addIndexTable(Page page) {
        lemmatizer.getLemmasWithRanks().forEach((lemma, rank) -> {
            IndexTable indexTable = new IndexTable();
            indexTable.setLemma(lemma);
            indexTable.setPage(page);
            indexTable.setLemmaRank(rank);

            dbConnection.addClass(indexTable);
        });
    }
}
