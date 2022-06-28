package ru.gypsyjr.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import ru.gypsyjr.main.models.Lemma;
import ru.gypsyjr.main.models.Site;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Lemma findLemmaByLemma(String lemma);
    List<Lemma> findAllBySite(Site site);


}
