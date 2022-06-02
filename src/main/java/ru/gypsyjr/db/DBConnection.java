package ru.gypsyjr.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;
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
import java.util.List;

public class DBConnection {
    @PersistenceContext
    EntityManager entityManager;
    EntityManagerFactory emf;
    private static volatile DBConnection instance;

    private DBConnection() {
        emf = Persistence.createEntityManagerFactory("SearchEngine");
        entityManager = emf.createEntityManager();
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

    public <T> void addClass(T type) {
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }

        entityManager.persist(type);
        entityManager.getTransaction().commit();
    }

    public <T> List<T> getAllData(Class<T> type) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(type);
        criteria.from(type);
        return entityManager.createQuery(criteria).getResultList();
    }

    public <T> void updateData(T type) {
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }

        entityManager.merge(type);
    }

    public void closeConnection() {
        entityManager.close();
    }


    public void test() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SearchEngine");
        entityManager = emf.createEntityManager();
        List<?> rty = entityManager.createQuery("SELECT id from Page where path = 'http://www.playback.ru/catalog/1300.html'")
                .getResultList();

        rty.forEach(System.out::println);
    }
}
