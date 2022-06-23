package ru.gypsyjr.service;

import org.springframework.beans.factory.annotation.Autowired;
import ru.gypsyjr.main.Config;
import ru.gypsyjr.models.Site;
import ru.gypsyjr.models.Statistic;
import ru.gypsyjr.models.Total;
import ru.gypsyjr.repository.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiService implements IService {
    @Autowired
    FieldRepository fieldRepository;
    @Autowired
    LemmaRepository lemmaRepository;
    @Autowired
    PageRepository pageRepository;
    @Autowired
    SearchIndexRepository indexRepository;
    @Autowired
    SiteRepository siteRepository;
    @Autowired
    private Config config;

    public Statistic getStatistic() {
        Statistic statistic = new Statistic();

        AtomicInteger allLemmas = new AtomicInteger();
        AtomicInteger allPages = new AtomicInteger();
        AtomicInteger allSites = new AtomicInteger();

        List<Site> siteList = siteRepository.findAll();

        if (siteList.size() == 0) {
        }

        siteList.forEach(it -> {
            int pages = pageRepository.findAllBySite(it).size();
            int lemmas = lemmaRepository.findAllBySite(it).size();

            it.setPages(pages);
            it.setLemmas(lemmas);
            statistic.addDetailed(it);

            allPages.updateAndGet(v -> v + pages);
            allLemmas.updateAndGet(v -> v + lemmas);
            allSites.getAndIncrement();
        });

        Total total = new Total();
        total.setIndexing(true);
        total.setLemmas(allLemmas.get());
        total.setPages(allPages.get());
        total.setSites(allSites.get());

        statistic.setTotal(total);

        return statistic;
    }
}