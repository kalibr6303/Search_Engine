package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.StatusType;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.StatisticsService;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {


    private final SitesList sites;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for(int i = 0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = getVolumePages(site.getUrl());
            int lemmas = getVolumeLemmas(site.getUrl());
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(String.valueOf(getStatusSite(site.getUrl())));
            item.setError(getError(site.getUrl()));
            item.setStatusTime(getStatusTime(site.getUrl()));
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    public int getVolumePages(String url){
        searchengine.model.Site site = siteRepository.findByUrl(url);
        int count = 0;
        if (site != null) {
            List<Page> pages = pageRepository.findAll();

            for (Page p : pages) {
                if (p.getSite().getId() == site.getId()) count++;
            }
        }
        return count;
    }

    public int getVolumeLemmas( String url){
        searchengine.model.Site site = siteRepository.findByUrl(url);
        List<Lemma> lemmas = lemmaRepository.findAll();
       int count = 0;
        if (site != null) {
            for (Lemma l : lemmas) {
                if (l.getSite().getId() == site.getId()) count++;
            }
        }
        return count;
    }

    public StatusType getStatusSite(String url){
        searchengine.model.Site site = siteRepository.findByUrl(url);
        if (site != null) return site.getStatus();
        return null;
    }

    public String getStatusTime(String url) {
        searchengine.model.Site site = siteRepository.findByUrl(url);
        if (site != null) return String.valueOf(site.getStatusTime());
        return null;
    }

    public String getError(String url) {
        searchengine.model.Site site = siteRepository.findByUrl(url);
        if (site != null)  return String.valueOf(site.getLastError());
       return null;
    }

}