package ru.gypsyjr.search;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import ru.gypsyjr.db.DBConnection;
import ru.gypsyjr.lemmatizer.Lemmatizer;
import ru.gypsyjr.models.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SearchEngine {
    private final DBConnection dbConnection;
    private final Lemmatizer lemmatizer;
    private List<String> words;

    public SearchEngine() {
        words = new ArrayList<>();
        lemmatizer = Lemmatizer.getInstance();
        dbConnection = DBConnection.getInstance();
    }

    public void addSearchQuery(String query) {
        SortedSet<Lemma> lemmas = new TreeSet<>();

        for (String word : query.split(" ")) {
            Lemma lemma = lemmatizer.getLemma(word.toLowerCase());
            if (lemma != null) {
                lemmas.add(lemma);
            }
        }

        List<Page> pages = getPages(lemmas);
        List<IndexRanks> indexRanks = getIndexRanks(lemmas, pages);
        SortedSet<SearchResult> searchResults = getSearchResults(indexRanks, lemmas);

//        searchResults.forEach(System.out::println);
    }

    private List<Page> getPages(SortedSet<Lemma> lemmas) {
        List<Page> pages = new ArrayList<>();

        List<?> pagesLemmas = dbConnection.getSearchIndexesByLemma(lemmas.first());
        pagesLemmas.forEach(it -> {
            if (it.getClass() == Page.class) {
                Page page = (Page) it;
                pages.add(page);
            }
        });

        return pages;
    }

    private List<IndexRanks> getIndexRanks(SortedSet<Lemma> lemmas, List<Page> pages) {
        List<IndexRanks> indexRanks = new ArrayList<>();

        lemmas.forEach(lemma -> {
            int count = 0;
            while (pages.size() > count) {
                IndexTable indexTable = dbConnection.getSearchIndexesByLemma(lemma, pages.get(count));
                if (indexTable == null) {
                    pages.remove(count);
                } else {
                    IndexRanks indexRank = new IndexRanks();

                    indexRank.setPage(pages.get(count));
                    indexRank.setRanks(lemma.getLemma(), indexTable.getLemmaRank());
                    indexRank.setRAbs();

                    indexRanks.add(indexRank);
                    count++;
                }
            }
        });

        indexRanks.forEach(IndexRanks::setRRel);
        return indexRanks;
    }

    private SortedSet<SearchResult> getSearchResults(List<IndexRanks> indexRanks, SortedSet<Lemma> lemmas) {
        SortedSet<SearchResult> searchResults = new TreeSet<>();

        indexRanks.forEach(it -> {
            SearchResult sResult = new SearchResult();
            Document document = Jsoup.parse(it.getPage().getContent());

            AtomicReference<String> snippet = new AtomicReference<>("");
            AtomicInteger maxSnippet = new AtomicInteger();

            document.select("div").forEach(i -> {
                String str = i.text().toLowerCase();
                int count = 0;
                for(Lemma lem: lemmas.stream().toList()){
                    String l = lem.getLemma();
                    if (str.contains(l)){
                        count++;
                        str = str.replaceAll("(?i)" + l,
                                "<b>" + l + "</b>");
                    }
                }

                if (count > maxSnippet.get()) {
                    snippet.set(str);
                    maxSnippet.set(count);
                }
            });
            sResult.setTitle(document.title());
            sResult.setRelevance(it.getRRel());
            sResult.setSnippet(snippet.get());
            sResult.setUri(it.getPage().getPath());

            searchResults.add(sResult);
        });

        return searchResults;
    }
}