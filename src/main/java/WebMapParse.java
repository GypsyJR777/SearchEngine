import models.Page;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveTask;

public class WebMapParse extends RecursiveTask<Integer> {
    static {
        registry = new StandardServiceRegistryBuilder()
                .configure(new File(System.getProperty("user.dir") + "/config/hibernate.cfg.xml"))
                .build();

        metadata = new MetadataSources(WebMapParse.registry).getMetadataBuilder().build();

        sessionFactory = WebMapParse.metadata.getSessionFactoryBuilder().build();

        session = WebMapParse.sessionFactory.openSession();
        WebMapParse.session.beginTransaction();

        websites = new CopyOnWriteArraySet<>();
    }

    private static final StandardServiceRegistry registry;
    private static final Metadata metadata;
    private static final SessionFactory sessionFactory;
    private static final Session session;
    private static final Set<String> websites;

    private Integer pageCount;
    private final List<WebMapParse> children;
    private final String startPage;

    public WebMapParse() {
        startPage = "https://www.playback.ru/";
        children = new ArrayList<>();
        pageCount = 0;
    }

    public WebMapParse(String startPage) {
        children = new ArrayList<>();

        this.startPage = startPage;
        websites.add(startPage);
        pageCount = 0;
    }

    @Override
    protected Integer compute() {
        while (websites.size() < 15) {
            try {
                Connection connection = Jsoup.connect(startPage)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; rv:98.0) Gecko/20100101 Firefox/98.0")
                        .referrer("http://google.com");
                Document document = connection.get();

                Connection.Response response = connection.execute();

                if (websites.size() == 1) {
                    document = Jsoup.connect(startPage).get();
                }
                Elements elements = document.select("a");
                Document finalDocument = document;
                elements.forEach(element -> {
                    String attr = element.attr("href");
                    if (attr.contains(startPage) && !websites.contains(attr) && !attr.contains("#")) {
                        websites.add(attr);

                        WebMapParse newChild = new WebMapParse(attr);
                        newChild.fork();
                        children.add(newChild);
                        Page page = new Page();

                        page.setCode(response.statusCode());
                        page.setPath(attr);
                        page.setContent(finalDocument.html());

                        if (!session.getTransaction().isActive()) {
                            session.beginTransaction();
                        }
//                        session.evict(page);
//                        session.persist(page);
                        page = session.merge(page);
                        session.persist(page);
                        session.getTransaction().commit();
                        pageCount++;
                    }
                });

                Thread.sleep(1000);

            } catch (IOException | InterruptedException exception) {
                exception.printStackTrace();
            }
        }

        children.forEach(it -> pageCount += it.join());

        return pageCount;
    }

    public static void closeSession() {
        session.close();
    }
}
