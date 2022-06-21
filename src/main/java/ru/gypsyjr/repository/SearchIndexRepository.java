package ru.gypsyjr.repository;

import org.springframework.data.repository.CrudRepository;

import ru.gypsyjr.models.IndexTable;

public interface SearchIndexRepository extends CrudRepository<IndexTable, Integer> {
}
