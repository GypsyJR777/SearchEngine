package ru.gypsyjr.repository;

import org.springframework.data.repository.CrudRepository;

import ru.gypsyjr.models.Lemma;

public interface LemmaRepository extends CrudRepository<Lemma, Integer>{
}
