package searchengine.developer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import searchengine.config.SitesList;
import searchengine.dto.PageDto;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.StatusType;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


@RequiredArgsConstructor
public class IndexPage implements Runnable {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final String url;
    private final SitesList sitesList;
    private final String link;
    private final Lemma lemma;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;


    @SneakyThrows
    @Override
    public void run() {

        String path = link.replaceAll(url, "");
        Page page = pageRepository.findByPath(path);
        if (page != null) pageRepository.delete(page);
        PageUrlFound pageUrlFound = new PageUrlFound();
        List<PageDto> pageDtoList = pageUrlFound.getOnePageUrlFound(link);
        saveToBaseOnePage(pageDtoList);

    }


    public void saveToBaseOnePage(List<PageDto> pages) throws InterruptedException {

        IndexSite indexSite = new IndexSite(siteRepository, pageRepository,
                url, sitesList, lemma, lemmaRepository, indexRepository);
        Site site = siteRepository.findByUrl(url);
        site.setStatus(StatusType.INDEXING);
        site.setStatusTime(new Date());
        siteRepository.save(site);
        HashMap<String, Integer> lemmasOfWriteForBase = new HashMap<>();
        for (PageDto s : pages) {
            Page page = indexSite.writePageInBase(s, site);
            if (s.getStatus() == 200) {
                writeLemmaForPageInBase(s, site, lemmasOfWriteForBase);
                indexSite.writeIndexForPageInBase(s, page, lemmasOfWriteForBase);
                site.setStatus(StatusType.INDEXED);
                site.setStatusTime(new Date());
                siteRepository.save(site);
            } else site.setLastError("Страница недоступна");
            siteRepository.save(site);

        }

    }


    private void writeLemmaForPageInBase(PageDto pageDto, Site site, HashMap<String,
            Integer> lemmasOfWriteForBase) {

        HashMap<String, Integer> lemmasOfPageForWrite = lemma.getLemmasInIndex(pageDto);
        lemmasOfPageForWrite.entrySet().forEach(d -> System.out.println(d.getKey() + " " + d.getValue()));
        lemmasOfPageForWrite.entrySet().forEach(l -> {
            if (isLemmaInBase(l.getKey(), site) == 0) {
                searchengine.model.Lemma lemma = new searchengine.model.Lemma();
                lemma.setLemma(l.getKey());
                lemma.setFrequency(l.getValue());
                lemma.setSite(site);
                lemmaRepository.save(lemma);
                lemmasOfWriteForBase.put(l.getKey(), lemma.getId());

            } else {
                searchengine.model.Lemma lemma = lemmaRepository.findLemmaById(isLemmaInBase(l.getKey(), site));
                lemma.setSite(site);
                lemma.setLemma(l.getKey());
                lemma.setFrequency(lemma.getFrequency() + 1);
                lemmaRepository.save(lemma);
                lemmasOfWriteForBase.put(l.getKey(), lemma.getId());
            }
        });
    }


    private int isLemmaInBase(String lemma, Site site) {
        List<searchengine.model.Lemma> lemmas = lemmaRepository.findAll();
        for (searchengine.model.Lemma l : lemmas) {

            if (l.getLemma().equals(lemma) &&
                    l.getSite().getName().equals(site.getName())) return l.getId();
        }
        return 0;
    }

}
