package ru.gypsyjr.db;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import ru.gypsyjr.models.IndexTable;
import ru.gypsyjr.models.Lemma;
import ru.gypsyjr.models.Page;

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

    public Page getPageById(Integer id) {
        List<?> pages = entityManager.createQuery("SELECT p FROM Page p WHERE id = :id")
                .setParameter("id", id)
                .getResultList();

        if (pages.size() > 0 && pages.get(0).getClass() == Page.class){
            return (Page) pages.get(0);
        }

        return null;
    }

    public Lemma getLemmaByName(String field){
        List<?> lemmas = entityManager.createQuery("SELECT l FROM Lemma l WHERE lemma = :field")
                .setParameter("field", field)
                .getResultList();

        if (lemmas.size() > 0 && lemmas.get(0).getClass() == Lemma.class){
            return (Lemma) lemmas.get(0);
        }

        return null;
    }

    public List<?> getSearchIndexesByLemma(Lemma id) {
        return entityManager.createQuery("SELECT page FROM IndexTable AS i WHERE lemma = :lemma")
                .setParameter("lemma", id)
                .getResultList();
    }

    public IndexTable getSearchIndexesByLemma(Lemma lemma, Page page) {

        List<?> indexes = entityManager
                .createQuery("SELECT i FROM IndexTable AS i WHERE lemma = :lemma AND page = :page")
                .setParameter("lemma", lemma)
                .setParameter("page", page)
                .getResultList();

        if (indexes.size() > 0 && indexes.get(0).getClass() == IndexTable.class){
            return (IndexTable) indexes.get(0);
        }

        return null;
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
