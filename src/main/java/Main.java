import db.DBConnection;
import lemmatizer.Lemmatizer;
import models.Field;
import parse.WebMapParse;

import java.util.concurrent.ForkJoinPool;

public class Main {
    private static final String START_PAGE = "https://www.rbc.ru/";
    private static final int NUMBER_OF_THREADS = 5;

    public static void main(String[] args) {
        WebMapParse webMapParse = new WebMapParse();

        ForkJoinPool forkJoinPool = new ForkJoinPool(NUMBER_OF_THREADS);
        Integer pages = forkJoinPool.invoke(webMapParse);
        DBConnection dbConnection = DBConnection.getInstance();
        Lemmatizer lemmatizer = Lemmatizer.getInstance();
        lemmatizer.getLemmas().forEach(dbConnection::addClass);

        dbConnection.closeConnection();

//        Lemmatizer lemmatizer = Lemmatizer.getInstance();
//
//        lemmatizer.addString("Описание и характеристики Смартфон Xiaomi Redmi Note 10S NFC 6/64 ГБ RU, серый оникс and yeah yes no if");
//
//        lemmatizer.printMorphInfo();
    }
}
