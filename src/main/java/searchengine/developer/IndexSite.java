package searchengine.developer;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import searchengine.config.SitesList;
import searchengine.dto.PageDto;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import java.util.*;
import java.util.concurrent.ForkJoinPool;


@RequiredArgsConstructor
public class IndexSite implements Runnable {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final String url;
    private final SitesList sitesList;
    private final Lemma lemma;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;


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



    private void saveToBase(List<PageDto> pages) throws InterruptedException {
        if (!Thread.interrupted()) {
            Site site = siteRepository.findByUrl(url);
            HashMap<String, Integer> lemmasOfWriteForBase = new HashMap<>();
            HashMap<String, Integer> lemmasOfSite = lemma.getLemmasOfSite(pages);
            for (PageDto s : pages) {
                Page page = writePageInBase(s, site);
                if (s.getStatus() == 200) {
                    writeLemmaForPageInBase(lemmasOfSite, s, site, lemmasOfWriteForBase);
                    writeIndexForPageInBase(s, page, lemmasOfWriteForBase);
                } else site.setLastError("Страница недоступна");
                siteRepository.save(site);

            }

        } else {
            throw new InterruptedException();
        }
    }

    public Page writePageInBase(PageDto pageDto, Site site) throws InterruptedException {
        if (!Thread.interrupted()) {
            Page page = new Page();
            String content = pageDto.getContent();
            page.setContent(content);
            String path = pageDto.getUrl().replaceAll(url, "");
            page.setPath(path);
            page.setCode(pageDto.getStatus());
            page.setSite(site);
            pageRepository.save(page);
            return page;
        } else {
            throw new InterruptedException();
        }
    }


    private void writeLemmaForPageInBase(HashMap<String, Integer> lemmasOfSite,
                                         PageDto pageDto, Site site, HashMap<String,
            Integer> lemmasOfWriteForBase) throws InterruptedException {
        if (!Thread.interrupted()) {
            HashMap<String, Integer> lemmasOfPageForWrite = lemma.getLemmaOfPageForWrite(lemmasOfSite, pageDto);
            lemmasOfPageForWrite.entrySet().forEach(l -> {
                searchengine.model.Lemma lemma = new searchengine.model.Lemma();
                if (!lemmasOfWriteForBase.containsKey(l.getKey())) {
                    lemma.setLemma(l.getKey());
                    lemma.setFrequency(l.getValue());
                    lemma.setSite(site);
                    lemmaRepository.save(lemma);
                    lemmasOfWriteForBase.put(l.getKey(), lemma.getId());

                }
            });
        } else {
            throw new InterruptedException();
        }
    }


    public void writeIndexForPageInBase(PageDto pageDto, Page page,
                                        HashMap<String, Integer> lemmasOfWriteForBase) throws InterruptedException {
        HashMap<String, Integer> lemmasInIndex = lemma.getLemmasInIndex(pageDto);
        if (!Thread.interrupted()) {
            lemmasInIndex.entrySet().forEach(i -> {
                Index index = new Index();
                if (lemmasOfWriteForBase.containsKey(i.getKey())) {
                    index.setPage(page);
                    index.setLemma(lemmaRepository.findLemmaById(lemmasOfWriteForBase.get(i.getKey())));
                    index.setRanks(i.getValue());
                    indexRepository.save(index);
                }
            });
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
