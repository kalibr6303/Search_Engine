package searchengine.services.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.developer.IndexPage;
import searchengine.developer.IndexSite;
import searchengine.developer.Lemma;
import searchengine.model.StatusType;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.IndexService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Component
@RequiredArgsConstructor
@Slf4j

public class IndexServiceImpl implements IndexService {

    public final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private ExecutorService executorService;
    private final Lemma lemma;


    public boolean indexAll() {

        if (isIndexingActive()) {
            return false;
        } else {
            List<Site> urlList = sitesList.getSites();

            executorService = Executors.newCachedThreadPool();
            for (Site s : urlList) {
                String url = s.getUrl();
                executorService.submit(new IndexSite(siteRepository,
                        pageRepository,
                        url,
<<<<<<< HEAD
                        sitesList, lemma
                        ));
=======
                        sitesList, lemma));
>>>>>>> 025241e972bcbb4cf9ecfb32bdd6ce75d407bb47
            }
            executorService.shutdown();
            return true;
        }
    }


    public boolean indexPage(String link) {
        String url = containSiteOfBaseByLink(link);
        if (url == null) return false;
        executorService = Executors.newCachedThreadPool();
        executorService.submit(new IndexPage(siteRepository,
                pageRepository,
                url,
<<<<<<< HEAD
                sitesList, link,lemma));
=======
                sitesList, link, lemma));
>>>>>>> 025241e972bcbb4cf9ecfb32bdd6ce75d407bb47
        executorService.shutdown();
        return true;
    }


    @Override
    public boolean stopIndexing() {
        if (isIndexingActive()) {
            executorService.shutdownNow();
            return true;
        } else {
            return false;
        }
    }

    private String containSiteOfBaseByLink(String link) {
        List<Site> urList = sitesList.getSites();
        for (Site s : urList) {
            String url = s.getUrl();
            if (link.matches(url + "[^:#]+")) return url;
        }
        return null;
    }


    private boolean isIndexingActive() {
        List<searchengine.model.Site> siteList = siteRepository.findAll();
        for (searchengine.model.Site site : siteList) {
            if (site.getStatus() == StatusType.INDEXING) {
                return true;
            }
        }
        return false;
    }
}
