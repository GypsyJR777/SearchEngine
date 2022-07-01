package ru.gypsyjr.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import ru.gypsyjr.main.models.IndexTable;
import ru.gypsyjr.main.models.Lemma;
import ru.gypsyjr.main.models.Page;

import java.util.List;

@Repository
public interface SearchIndexRepository extends JpaRepository<IndexTable, Integer> {
    List<IndexTable> findByLemma(Lemma lemma);
    IndexTable findByLemmaAndPage(Lemma lemma, Page page);
}
