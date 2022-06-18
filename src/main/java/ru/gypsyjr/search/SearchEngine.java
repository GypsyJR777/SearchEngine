package ru.gypsyjr.search;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.gypsyjr.db.DBConnection;
import ru.gypsyjr.lemmatizer.Lemmatizer;
import ru.gypsyjr.models.*;

import java.util.*;

public class SearchEngine {
    private final DBConnection dbConnection;
    private List<String> words;
    private Lemmatizer lemmatizer;

    public SearchEngine() {
        words = new ArrayList<>();
        lemmatizer = Lemmatizer.getInstance();
        dbConnection = DBConnection.getInstance();
    }

    public void addSearchQuery(String query) {
        //TODO заменить цикл на 2, сначала находим леммы потом достаем индексы (отдельно нужны 2 функции в Lemmatizer)

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

            sResult.setTitle(document.title());
//            System.out.println(document.body().html().replaceAll(lemmas.first().getLemma(),
//                    "<b>" + lemmas.first().getLemma() + "</b>"));
            System.out.println(document.select("body"));

            searchResults.add(sResult);

        });

        return searchResults;
    }
}
