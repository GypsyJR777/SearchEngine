import models.Page;

import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public class Main {
    private static final String START_PAGE = "https://www.rbc.ru/";
    private static final int NUMBER_OF_THREADS = 5;

    public static void main(String[] args) {
        WebMapParse webMapParse = new WebMapParse();

        ForkJoinPool forkJoinPool = new ForkJoinPool(NUMBER_OF_THREADS);
        Integer pages = forkJoinPool.invoke(webMapParse);
        DBConnection dbConnection = DBConnection.getInstance();

        dbConnection.closeConnection();
    }
}
