package searchengine.developer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import searchengine.config.SitesList;
import searchengine.dto.PageDto;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.sql.Lemma;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class IndexPage implements Runnable {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final String url;
    private final SitesList sitesList;
    private final String link;
    private final Lemma lemma;

    @SneakyThrows
    @Override
    public void run() {
        Site site = siteRepository.findByUrl(url);
        String path = link.replaceAll(url, "");
        Page page = pageRepository.findByPath(path);
        if (page != null) pageRepository.delete(page);
        PageUrlFound pageUrlFound = new PageUrlFound();
        List<PageDto> pageDtoList = pageUrlFound.getOnePageUrlFound(link);
        if (pageDtoList.isEmpty()) {
            site.setLastError("Страница недоступна");
            site.setStatusTime(new Date());
            siteRepository.save(site);
        }

        else {
            IndexSite indexSite = new IndexSite(siteRepository, pageRepository, url, sitesList, lemma);
            indexSite.saveToBase(pageDtoList, site);
        }

    }

}
