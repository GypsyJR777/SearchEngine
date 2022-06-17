package ru.gypsyjr.db;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import ru.gypsyjr.models.Lemma;

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

    public Lemma getLemmaByParameter(String parameter, String field){
        List<?> lemmas = entityManager.createQuery("SELECT l FROM Lemma l WHERE lemma = :field")
//                .setParameter("parameter", parameter)
                .setParameter("field", field)
                .getResultList();

        if (lemmas.size() > 0 && lemmas.get(0).getClass() == Lemma.class){
            return (Lemma) lemmas.get(0);
        }

        return null;
    }

    public List<?> getSearchIndexesByLemma(Lemma id) {
        return entityManager.createQuery("SELECT i FROM IndexTable AS i WHERE lemma = :lemma")
                .setParameter("lemma", id)
                .getResultList();
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
