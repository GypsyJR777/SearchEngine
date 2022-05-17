import db.DBConnection;
import lemmatizer.Lemmatizer;
import parse.WebMapParse;

import java.util.concurrent.ForkJoinPool;

public class Main {
    private static final String START_PAGE = "https://www.rbc.ru/";
    private static final int NUMBER_OF_THREADS = 5;

    public static void main(String[] args) {
//        WebMapParse webMapParse = new WebMapParse();
//
//        ForkJoinPool forkJoinPool = new ForkJoinPool(NUMBER_OF_THREADS);
//        Integer pages = forkJoinPool.invoke(webMapParse);
//        DBConnection dbConnection = DBConnection.getInstance();
//
//        dbConnection.closeConnection();


        Lemmatizer lemmatizer = Lemmatizer.getInstance();

        lemmatizer.addString("Повторное появление леопарда в Осетии позволяет предположить, что леопард " +
                "постоянно обитает в некоторых районах Северного Кавказа.");

        lemmatizer.printMorphInfo();
    }
}
