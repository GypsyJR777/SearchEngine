import models.Page;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.io.File;
import java.util.Set;

public class DBConnection {
    private final StandardServiceRegistry registry;
    private final Metadata metadata;
    private final SessionFactory sessionFactory;
    private final Session session;
    private static volatile DBConnection instance;

    private DBConnection() {
        registry = new StandardServiceRegistryBuilder()
                .configure(new File(System.getProperty("user.dir") + "/config/hibernate.cfg.xml"))
                .build();
        metadata = new MetadataSources(registry).getMetadataBuilder().build();
        sessionFactory = metadata.getSessionFactoryBuilder().build();
        session = sessionFactory.openSession();
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }

        return instance;
    }

    public void addToDB(Page page) {
        if (session.getTransaction().getStatus() != TransactionStatus.ACTIVE){
            session.beginTransaction();
        }
        if (!session.getTransaction().isActive()) {
            session.beginTransaction();
        }

        session.persist(page);
        session.getTransaction().commit();
    }

    public void closeConnection() {
        session.close();
    }
}
