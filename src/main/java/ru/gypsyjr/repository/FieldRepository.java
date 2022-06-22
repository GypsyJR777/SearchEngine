package ru.gypsyjr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import ru.gypsyjr.models.Field;

@org.springframework.stereotype.Repository
public interface FieldRepository extends JpaRepository<Field, Integer> {

}
