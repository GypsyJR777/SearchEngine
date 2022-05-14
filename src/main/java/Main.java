import java.util.concurrent.ForkJoinPool;

public class Main {
    private static final String START_PAGE = "https://www.rbc.ru/";
    private static final int NUMBER_OF_THREADS = 5;

    public static void main(String[] args) {
        WebMapParse webMapParse = new WebMapParse(START_PAGE);

        ForkJoinPool forkJoinPool = new ForkJoinPool(NUMBER_OF_THREADS);
        Integer websCount = forkJoinPool.invoke(webMapParse);

        WebMapParse.closeSession();
    }
}
