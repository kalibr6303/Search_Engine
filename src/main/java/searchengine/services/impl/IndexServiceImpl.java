package searchengine.services.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.developer.IndexPage;
import searchengine.developer.IndexSite;
import searchengine.sql.Lemma;
import searchengine.dto.Response.IndexingResponse;
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




    public IndexingResponse indexAll() {
        IndexingResponse indexingResponse = new IndexingResponse();

        if (isIndexingActive()) {
            indexingResponse.setResult(false);
            indexingResponse.setError("Индексация уже запущена");
            return indexingResponse;
        } else {
            List<Site> urlList = sitesList.getSites();

            executorService = Executors.newCachedThreadPool();
            for (Site s : urlList) {
                String url = s.getUrl();
                executorService.submit(new IndexSite(siteRepository,
                        pageRepository,
                        url,
                        sitesList, lemma
                ));
            }
            executorService.shutdown();
            indexingResponse.setResult(true);
            return indexingResponse;
        }
    }


    public IndexingResponse indexPage(String link) {
        IndexingResponse indexingResponse = new IndexingResponse();
        String url = containSiteOfBaseByLink(link);
        if (url == null) {
            indexingResponse.setResult(false);
            indexingResponse.setError("Данная страница находится за пределами сайтов, указанных " +
                    "в конфигурационном файле");
            return indexingResponse;
        }
        executorService = Executors.newCachedThreadPool();
        executorService.submit(new IndexPage(siteRepository,
                pageRepository,
                url,
                sitesList, link, lemma));
        executorService.shutdown();
        indexingResponse.setResult(true);
        return indexingResponse;
    }


    @Override
    public IndexingResponse stopIndexing() {
        IndexingResponse indexingResponse = new IndexingResponse();
        if (isIndexingActive()) {
            executorService.shutdownNow();
            indexingResponse.setResult(true);
            return indexingResponse;
        } else {
            indexingResponse.setResult(false);
            indexingResponse.setError("Индексация не запущена");
            return indexingResponse;
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