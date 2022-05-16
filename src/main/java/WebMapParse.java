import models.Page;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

public class WebMapParse extends RecursiveTask<Integer> {
    static {
        websites = new CopyOnWriteArraySet<>();
        pageId = new AtomicInteger(0);
        dbConnection = DBConnection.getInstance();
    }

    private static final Set<String> websites;
    private static final DBConnection dbConnection;
    private final static AtomicInteger pageId;
    private static String mainPage = "";

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
            Connection connection = Jsoup.connect(startPage)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; rv:98.0) Gecko/20100101 Firefox/98.0")
                    .referrer("http://google.com");
            Document document = connection.get();

            Connection.Response response = connection.execute();

            if (websites.size() == 1) {
                document = Jsoup.connect(startPage).get();
            }

            synchronized (pageId) {
                pageId.getAndIncrement();

                Page page = new Page();

                page.setId(pageId.get());
                page.setCode(response.statusCode());
                page.setPath(startPage);
                page.setContent(document.html());

                dbConnection.addToDB(page);

                pageCount++;

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
}
