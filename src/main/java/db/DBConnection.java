package db;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.io.File;
import java.sql.PreparedStatement;
import java.util.List;

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

    public<T> void addClass(T type) {
        if (session.getTransaction().getStatus() != TransactionStatus.ACTIVE || !session.getTransaction().isActive()) {
            session.beginTransaction();
        }

        session.persist(type);
        session.getTransaction().commit();
    }

    public <T> List<T> getAllData(Class<T> type) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(type);
        criteria.from(type);
        return session.createQuery(criteria).getResultList();
    }

    public <T> void updateData(T type) {
        if (session.getTransaction().getStatus() != TransactionStatus.ACTIVE || !session.getTransaction().isActive()) {
            session.beginTransaction();
        }

        session.merge(type);
    }

    public void closeConnection() {
        session.close();
    }
}
