package ru.gypsyjr.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gypsyjr.lemmatizer.Lemmatizer;
import ru.gypsyjr.main.models.*;
import ru.gypsyjr.main.repository.*;
import ru.gypsyjr.parse.WebMapParse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final List<Thread> threads = new ArrayList<>();
    private final List<ForkJoinPool> forkJoinPools = new ArrayList<>();
    private boolean indexing = false;

    private void clearData() {
        indexRepository.deleteAll();
        lemmaRepository.deleteAll();
        pageRepository.deleteAll();
    }

    private WebMapParse newParse(Site site) {
        return new WebMapParse(site.getUrl(), site, config, fieldRepository, indexRepository, pageRepository);
    }

    public ApiStatistics getStatistic() {
        Statistic statistic = new Statistic();

        AtomicInteger allLemmas = new AtomicInteger();
        AtomicInteger allPages = new AtomicInteger();
        AtomicInteger allSites = new AtomicInteger();

        List<Site> siteList = siteRepository.findAll();

        if (siteList.size() == 0) {
            return new ApiStatistics();
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

    public boolean indexing() {
        indexing = true;

        if (threads.size() > 0) {
            return false;
        }

        Lemmatizer.setLemmaRepository(lemmaRepository);

        List<Site> sites = siteRepository.findAll();
        List<WebMapParse> parses = new ArrayList<>();
        List<String> urls = config.getSitesUrl();
        List<String> namesUrls = config.getSitesName();

        if (sites.size() != urls.size()) {
            for (int i = 0; i < urls.size(); ++i) {
                String mainPage = urls.get(i);

                Site site = siteRepository.findSiteByUrl(mainPage);

                if (site == null) {
                    site = new Site();
                }

                site.setUrl(mainPage);
                site.setStatusTime(new Date());
                site.setStatus(Status.INDEXING);
                site.setName(namesUrls.get(i));

                parses.add(newParse(site));
                siteRepository.save(site);
            }
        } else {
            clearData();
            sites.forEach(site -> parses.add(newParse(site)));
        }

        parses.forEach(parse -> threads.add(new Thread(() -> {
            Site site = parse.getSite();

            try {

                site.setStatus(Status.INDEXING);
                siteRepository.save(site);

                ForkJoinPool forkJoinPool = new ForkJoinPool(NUMBER_OF_THREADS);

                forkJoinPools.add(forkJoinPool);

                forkJoinPool.execute(parse);
                int count = parse.join();

                site.setStatus(Status.INDEXED);
                siteRepository.save(site);

                System.out.println("Сайт " + site.getName() + " проиндексирован,кол-во ссылок - " + count);
            } catch (CancellationException ex) {
                ex.printStackTrace();
                site.setLastError("Ошибка индексации: " + ex.getMessage());
                site.setStatus(Status.FAILED);
                siteRepository.save(site);
            }
        })));

        threads.forEach(Thread::start);
        forkJoinPools.forEach(ForkJoinPool::shutdown);

        return indexing;
    }

    public boolean stopIndexing() {
        if (threads.size() == 0) {
            return true;
        }

        indexing = false;

        forkJoinPools.forEach(ForkJoinPool::shutdownNow);
        threads.forEach(Thread::interrupt);

        siteRepository.findAll().forEach(site -> {
            site.setLastError("Остановка индексации");
            site.setStatus(Status.FAILED);
            siteRepository.save(site);
        });

        threads.clear();
        forkJoinPools.clear();

        return indexing;
    }

    public boolean indexPage(String url) {
        Lemmatizer.setLemmaRepository(lemmaRepository);
        List<Site> siteList = siteRepository.findAll();

        if (siteList.size() == 0) {
            List<String> urls = config.getSitesUrl();
            List<String> namesUrls = config.getSitesName();

            for (int i = 0; i < urls.size(); ++i) {
                if (url.contains(urls.get(i))) {
                    String mainPage = urls.get(i);
                    Site site = new Site();

                    site.setUrl(mainPage);
                    site.setStatusTime(new Date());
                    site.setStatus(Status.INDEXING);
                    site.setName(namesUrls.get(i));
                    siteRepository.save(site);

                    WebMapParse parse = new WebMapParse(mainPage, site, config, fieldRepository, indexRepository,
                            pageRepository);
                    try {
                        parse.addPage();
                    } catch (IOException e) {
                        return false;
                    }

                    site.setStatus(Status.INDEXED);
                    siteRepository.save(site);

                    return true;
                }
            }
        } else {
            for (Site site : siteList) {
                if (url.contains(site.getUrl())) {
                    site.setStatus(Status.INDEXING);
                    siteRepository.save(site);

                    WebMapParse parse = new WebMapParse(site.getUrl(), site, config, fieldRepository, indexRepository,
                            pageRepository);
                    try {
                        parse.addPage();
                    } catch (IOException e) {
                        return false;
                    }

                    site.setStatus(Status.INDEXED);
                    siteRepository.save(site);

                    return true;
                }
            }
        }


        return false;
    }
}
