package ru.gypsyjr;

import ru.gypsyjr.db.DBConnection;
import ru.gypsyjr.lemmatizer.Lemmatizer;
import ru.gypsyjr.models.Lemma;
import ru.gypsyjr.parse.WebMapParse;
import ru.gypsyjr.search.SearchEngine;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class Main {
    private static final String START_PAGE = "https://www.rbc.ru/";
    private static final int NUMBER_OF_THREADS = 5;

    public static void main(String[] args) {
        WebMapParse webMapParse = new WebMapParse();

//        ForkJoinPool forkJoinPool = new ForkJoinPool(NUMBER_OF_THREADS);
//        Integer pages = forkJoinPool.invoke(webMapParse);
        DBConnection dbConnection = DBConnection.getInstance();
        SearchEngine engine = new SearchEngine();
        engine.addSearchQuery("купить смартфон Oneplus чехол");
//        dbConnection.test();
        dbConnection.closeConnection();
    }
}
