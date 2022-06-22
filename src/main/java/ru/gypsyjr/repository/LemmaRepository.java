package ru.gypsyjr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import ru.gypsyjr.models.Lemma;
import ru.gypsyjr.models.Site;

import java.util.List;

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    public List<Lemma> findAllByLemma(String lemma);
    public List<Lemma> findAllBySite(Site site);
}
