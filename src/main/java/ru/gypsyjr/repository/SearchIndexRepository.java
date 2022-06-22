package ru.gypsyjr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import ru.gypsyjr.models.IndexTable;
import ru.gypsyjr.models.Lemma;
import ru.gypsyjr.models.Page;

import java.util.List;

public interface SearchIndexRepository extends JpaRepository<IndexTable, Integer> {
    List<IndexTable> findByLemma(Lemma lemma);
    List<IndexTable> findByLemmaAndPage(Lemma lemma, Page page);
}
