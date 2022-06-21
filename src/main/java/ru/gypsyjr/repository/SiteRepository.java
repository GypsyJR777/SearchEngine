package ru.gypsyjr.repository;

import org.springframework.data.repository.CrudRepository;
import ru.gypsyjr.models.Site;

public interface SiteRepository extends CrudRepository<Site, Integer> {
}
