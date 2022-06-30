package ru.gypsyjr.parse;


import ru.gypsyjr.lemmatizer.Lemmatizer;
import ru.gypsyjr.main.Config;
import ru.gypsyjr.main.models.IndexTable;
import ru.gypsyjr.main.models.Page;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import ru.gypsyjr.main.models.Site;
import ru.gypsyjr.main.repository.FieldRepository;
import ru.gypsyjr.main.repository.PageRepository;
import ru.gypsyjr.main.repository.SearchIndexRepository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebMapParse extends RecursiveTask<Integer> {

    static {
        websites = new CopyOnWriteArraySet<>();
        fields = new HashMap<>();
    }

    private static final Set<String> websites;
    private String mainPage = "";
    private final static Map<String, Float> fields;
    private static SearchIndexRepository searchIndexRepository;
    private static PageRepository pageRepository;
    private static Config config;

    private Integer pageCount;
    private final List<WebMapParse> children;
    private final String startPage;
    private final Site site;
    private final Lemmatizer lemmatizer;

    public WebMapParse(String startPage, Site site, Lemmatizer lemmatizer, String mainPage) {
        children = new ArrayList<>();

        this.startPage = startPage;
        websites.add(startPage);
        pageCount = 0;

        if (this.mainPage.equals("")) {
            this.mainPage = mainPage;
        }

        this.site = site;
        this.lemmatizer = lemmatizer;
    }

    public WebMapParse(String startPage, Site site, Config config, FieldRepository fieldRepository,
                       SearchIndexRepository searchIndexRepository, PageRepository pageRepository) {
        children = new ArrayList<>();

        this.startPage = startPage;
        websites.add(startPage);
        websites.add(startPage + "/");
        pageCount = 0;

        if (mainPage.equals("")) {
            mainPage = startPage;
        }

        fieldRepository.findAll().forEach(it ->
                WebMapParse.fields.put(it.getName(), it.getWeight())
        );

        if (WebMapParse.searchIndexRepository == null) {
            WebMapParse.searchIndexRepository = searchIndexRepository;
        }

        if (WebMapParse.pageRepository == null) {
            WebMapParse.pageRepository = pageRepository;
        }

        this.site = site;
        lemmatizer = new Lemmatizer();

        WebMapParse.config = config;
    }

    @Override
    protected Integer compute() {
        try {
            Connection.Response response = Jsoup.connect(startPage)
                    .userAgent(config.getUserAgent())
                    .referrer(config.getReferrer())
                    .ignoreHttpErrors(true)
                    .execute();

            Document document = response.parse();

            addPage(response, document);

            Elements elements = document.select("a");
            elements.forEach(element -> {
                String attr = element.attr("href");
                if (!attr.contains("http")) {
                    if (!attr.startsWith("/") && attr.length() > 1) {
                        attr = "/" + attr;
                    }

                    attr = mainPage + attr;
                }
                if (attr.contains(mainPage) && !websites.contains(attr) && !attr.contains("#")) {
                    newChild(attr);
                }
            });

            Thread.sleep(1000);

        } catch (IOException | InterruptedException exception) {
            exception.printStackTrace();
        }

        children.forEach(it -> pageCount += it.join());

        return pageCount;
    }

    public void addPage() throws IOException {
        Connection.Response response = Jsoup.connect(startPage)
                .userAgent(config.getUserAgent())
                .referrer(config.getReferrer())
                .ignoreHttpErrors(true)
                .execute();

        addPage(response, response.parse());
    }

    private void newChild(String attr) {
        websites.add(attr);

        WebMapParse newChild = new WebMapParse(attr, site, lemmatizer, mainPage);
        newChild.fork();
        children.add(newChild);
    }

    private void addPage(Connection.Response response, Document document) {
        Page page = pageRepository.findByPath(startPage);
        if (page == null) {
            page = new Page();
        }

        page.setCode(response.statusCode());
        page.setPath(startPage);
        page.setContent(document.html());
        page.setSite(site);

        pageRepository.save(page);

        if (response.statusCode() < 400) {
            addLemmas(document, page);
        }
    }

    private void addLemmas(Document document, Page page) {
        AtomicBoolean newPage = new AtomicBoolean(true);
        fields.forEach((key, value) -> {
            Elements el = document.select(key);
            lemmatizer.addString(el.text(), newPage.get(), value, site);
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
            searchIndexRepository.save(indexTable);
        });
    }

    public Site getSite() {
        return site;
    }
}
