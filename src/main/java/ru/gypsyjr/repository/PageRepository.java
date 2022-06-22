package ru.gypsyjr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import ru.gypsyjr.models.Page;
import ru.gypsyjr.models.Site;

import java.util.List;

public interface PageRepository extends JpaRepository<Page, Integer> {
    public List<Page> findAllBySite(Site site);
}
