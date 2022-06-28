package ru.gypsyjr.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gypsyjr.lemmatizer.Lemmatizer;
import ru.gypsyjr.main.models.*;
import ru.gypsyjr.main.repository.*;
import ru.gypsyjr.parse.WebMapParse;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class Storage {
    private static final int NUMBER_OF_THREADS = 5;

    @Autowired
    private FieldRepository fieldRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private SearchIndexRepository indexRepository;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private Config config;


    public ApiStatistics getStatistic() {
        Statistic statistic = new Statistic();

        AtomicInteger allLemmas = new AtomicInteger();
        AtomicInteger allPages = new AtomicInteger();
        AtomicInteger allSites = new AtomicInteger();

        List<Site> siteList = siteRepository.findAll();

        if (siteList.size() == 0) {
            return null;
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

        ApiStatistics statistics = new ApiStatistics();

        statistics.setResult(true);
        statistics.setStatistics(statistic);

        return statistics;
    }

    public int updateDB() {
        Lemmatizer.setLemmaRepository(lemmaRepository);

        String url = "http://www.playback.ru";

        Site site = siteRepository.findSiteByUrl(url);

        if (site == null) {
            site = new Site();
            site.setName("Playback.ru");
            site.setUrl(url);
            site.setStatusTime(new Date());
        }

        site.setStatus(Status.INDEXING);
        siteRepository.save(site);

        WebMapParse webMapParse = new WebMapParse(url, site, fieldRepository, indexRepository, pageRepository);
        ForkJoinPool forkJoinPool = new ForkJoinPool(NUMBER_OF_THREADS);
        int count = forkJoinPool.invoke(webMapParse);

        site.setStatus(Status.INDEXED);
        siteRepository.save(site);

        return count;
    }
}
