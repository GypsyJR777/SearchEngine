package ru.gypsyjr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import ru.gypsyjr.models.Site;

public interface SiteRepository extends JpaRepository<Site, Integer> {
}
