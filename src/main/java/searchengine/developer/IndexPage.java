package searchengine.developer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.PageDto;
import searchengine.model.Page;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class IndexPage implements Runnable {


    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final String url;
    private final SitesList sitesList;
    private final String link;

    @SneakyThrows
    @Override
    public void run() {

        String path = link.replaceAll(url, "");
        Page page = pageRepository.findByPath(path);
        if (page != null) pageRepository.delete(page);
        PageUrlFound pageUrlFound = new PageUrlFound();
        List<PageDto> pageDtoList = pageUrlFound.getOnePageUrlFound(link);
        IndexSite indexSite = new IndexSite(siteRepository, pageRepository, url, sitesList);
        indexSite.saveToBase(pageDtoList);

    }


}
