package searchengine.developer;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import searchengine.config.SitesList;
import searchengine.dto.PageDto;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.StatusType;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

@RequiredArgsConstructor

public class IndexSite implements Runnable {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final String url;
    private final  SitesList sitesList;

    @SneakyThrows
    @Override
    public void run() {

    if (siteRepository.findByUrl(url) != null) {
        Site site = siteRepository.findByUrl(url);
        site.setStatus(StatusType.INDEXING);
        site.setStatusTime(new Date());
        siteRepository.save(site);
        siteRepository.delete(site);
    }

    Site site = new Site();
    site.setUrl(url);
    site.setName(getName());
    site.setStatus(StatusType.INDEXING);
    site.setStatusTime(new Date());
    siteRepository.save(site);
    try {
        List<PageDto> pageDtoList = getPageDtoList();
        saveToBase(pageDtoList);
        site.setStatus(StatusType.INDEXED);
        site.setStatusTime(new Date());
        siteRepository.save(site);

    } catch (Exception e) {
        e.printStackTrace();
        site.setLastError("Индексация остановлена");
        site.setStatus(StatusType.FAILED);
        site.setStatusTime(new Date());
        siteRepository.save(site);
    }

    }

    private List<PageDto> getPageDtoList() throws InterruptedException {
        if (!Thread.interrupted()) {
            String urlFormat = url + "/";
            List<PageDto> pageDtoVector = new ArrayList<>();
            List<String> urlList = new ArrayList<>();
            ForkJoinPool forkJoinPool = new ForkJoinPool();
            List<PageDto> pages = forkJoinPool.invoke(new PageUrlFound(urlFormat, pageDtoVector, urlList));
            return pages;
        } else throw new InterruptedException();
    }

    public void saveToBase(List<PageDto> pages) throws InterruptedException, SQLException, IOException {
        if (!Thread.interrupted()) {
            Site site = siteRepository.findByUrl(url);
            ConnectionSql connectionSql = new ConnectionSql();
            for (PageDto s : pages) {
                Page page = new Page();
                String content = s.getContent();
                page.setContent(content);
                String path = s.getUrl().replaceAll(url, "");
                page.setPath(path);
                page.setCode(s.getStatus());
                page.setSite(site);
                pageRepository.save(page);
                if (s.getStatus() == 200) {
                    LemmaParser lemmaParser = new LemmaParser(page);
                    lemmaParser.writeLemmaToBase(content, site, connectionSql.getConnection());
                }
            }
            connectionSql.getConnection().close();

        } else {
            throw new InterruptedException();
        }
    }


    private String getName() {
List<searchengine.config.Site> list = sitesList.getSites();
        for (searchengine.config.Site s : list) {
            if (s.getUrl().equals(url)) {
                return s.getName();
            }
        }
        return "";
    }

}