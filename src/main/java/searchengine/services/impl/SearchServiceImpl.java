package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.developer.SnippetParser;
import searchengine.dto.Response.SearchResponse;
import searchengine.dto.SearchDto;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.morphology.Morphology;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.SearchService;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor

public class SearchServiceImpl implements SearchService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final Morphology morphology;
    private final SnippetParser snippetParser;
    private final SitesList sitesList;


    public SearchResponse allSiteSearch(String word, int offset, int limit) {
        SearchResponse searchResponse = new SearchResponse();
        if (word.isEmpty() || !word.matches("[а-яА-Я\\s]+")) {
            searchResponse.setResult(false);
            searchResponse.setError("Задан пустой поисковый запрос");
            return searchResponse;
        }
        List<searchengine.config.Site> urlList = sitesList.getSites();
        List<SearchDto> allSearchDto = new ArrayList<>();
        for (searchengine.config.Site s : urlList) {
            List<Integer> lemmasOfBase = getRequestListSite(word, s.getUrl());
            List<SearchDto> searchDtoOfSite = getRequestResponse(lemmasOfBase, word, offset, limit);
            if (searchDtoOfSite != null) allSearchDto.addAll(searchDtoOfSite);
        }
         Collections.sort(allSearchDto, (o1, o2) -> {
             return o2.getRelevance().compareTo(o1.getRelevance());
        });
        searchResponse.setResult(true);
        searchResponse.setData(allSearchDto);
        searchResponse.setCount(allSearchDto.size());
        return searchResponse;
    }


    public SearchResponse siteSearch(String word, String url, int offset, int limit)  {
        SearchResponse searchResponse = new SearchResponse();
        if (word.isEmpty() || !word.matches("[а-яА-Я\\s]+")) {
            searchResponse.setResult(false);
            searchResponse.setError("Задан пустой поисковый запрос");
            return searchResponse;
        }
        List<Integer> lemmasOfBase = getRequestListSite(word, url);
        List<SearchDto> searchDtoOfSite = getRequestResponse(lemmasOfBase, word, offset, limit);
        searchResponse.setResult(true);
        searchResponse.setData(searchDtoOfSite);
        //searchResponse.setCount(searchDtoOfSite.size());
        return searchResponse;
    }

    // TODO: 11.02.2023 Получаем  сортированный список лемм по значению frequency, имеющихся в базе
    public List<Integer> getRequestListSite(String query, String url) {
        HashMap<String, Integer> store = morphology.getLemmaList(query);

        List<String> request = new ArrayList<>();
        store.entrySet().forEach(s -> request.add(s.getKey()));
        HashMap<Integer, Integer> lemmaFrequency = new HashMap<>();
        List<Integer> idLemmasSort = new ArrayList<>();
        if (url != null) {
            Site site = siteRepository.findByUrl(url);
            List<Lemma> lemmaOfBase = lemmaRepository.findAll();
            request.forEach(s -> {
                for (Lemma l : lemmaOfBase) {
                    if (l.getLemma().equals(s) && l.getSite().equals(site)) {
                        lemmaFrequency.put(l.getId(), l.getFrequency());
                    }
                }
            });

            if (lemmaFrequency != null) {  //сортируем по значению (frequency)
                lemmaFrequency.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue())
                        .forEach(s -> idLemmasSort.add(s.getKey()));
            } else return null;
        }

        return idLemmasSort;
    }


    public List<SearchDto> getRequestResponse(List<Integer> lemmasOfBase, String word, int offset, int limit) {
        HashMap<Integer, Float> listLemmasAccordingToRequest = getFilteredPage(lemmasOfBase, word);
        if (listLemmasAccordingToRequest == null) return null;
        List<SearchDto> listOfSearchDto = new ArrayList<>();
        AtomicInteger count = new AtomicInteger();
        listLemmasAccordingToRequest.entrySet().stream() // сортируем HashMap по уменьшению R(rel)
                .sorted(Map.Entry.<Integer, Float>comparingByValue()
                        .reversed())
                .forEach(s -> {
                    count.getAndIncrement();

                    SearchDto searchDto = new SearchDto();
                    Optional<Page> page = pageRepository.findById(s.getKey());
                    String content = page.get().getContent();
                    searchDto.setRelevance(s.getValue());
                    List<String> snip = snippetParser.getSnippet(content, word);
                    if (snip.size() != 0) searchDto.setSnippet(snip.get(0));
                    searchDto.setUri(page.get().getPath());
                    searchDto.setTitle(getTitleOfPage(page.get().getContent()));
                    searchDto.setSite(page.get().getSite().getUrl());
                    searchDto.setSiteName(page.get().getSite().getName());
                    listOfSearchDto.add(searchDto);
                });

        return trimListObject(listOfSearchDto, offset, limit);
    }


    // todo Получаем список страниц соответствующей последней(редкой) лемме
    public List<Integer> getListLemmasRequest(List<Integer> lemmas) {

        List<Integer> idOneOfPage = new ArrayList<>();
        int lemmaOne = 0;
        if (lemmas.size() != 0) lemmaOne = lemmas.get(0);
        List<Index> indexList = indexRepository.findAll();
        for (Index i : indexList) {
            if (i.getLemma().getId() == lemmaOne) idOneOfPage.add(i.getPage().getId());
        }
        return idOneOfPage;
    }


    // todo Фильтруем спимок страниц, ислючая страницы где не содержаться  все леммы согласно запросу
    public HashMap<Integer, Float> getFilteredPage(List<Integer> lemmasInBase, String word) {

        List<Integer> pagesForOneLemma = getListLemmasRequest(lemmasInBase);
        HashMap<String, Integer> store = morphology.getLemmaList(word);

        if (lemmasInBase == null || lemmasInBase.size() != store.size()) return null;

        HashMap<Integer, Float> pageRanks = new HashMap<>();
        lemmasInBase.forEach(l -> {
            pagesForOneLemma.forEach(p -> {
                if (pageRanks.containsKey(p) && isOnIndex(p, l) != 0)
                    pageRanks.put(p, pageRanks.get(p) + isOnIndex(p, l)); //рассчитываем абсолютную релевантность и
                if (!pageRanks.containsKey(p) && isOnIndex(p, l) != 0)// сохраняем в значении l
                    pageRanks.put(p, isOnIndex(p, l));
                if (pageRanks.containsKey(p) && isOnIndex(p, l) == 0) pageRanks.remove(p);
            });
        });

        if (pageRanks.size() == 0) return null;
        Float maxEntry = pageRanks.entrySet() // рассчитываем абсолютную и относительную релевантность
                .stream().max(Comparator.comparing(Map.Entry::getValue))
                .orElse(null).getValue();// сохраняем в значении ключа относительную релевантность
        pageRanks.entrySet().stream().forEach(s -> pageRanks.put(s.getKey(), s.getValue() / maxEntry));

        return pageRanks;
    }


    public float isOnIndex(int page, int lemma) {
        Lemma word = lemmaRepository.findLemmaById(lemma);
        Optional<Page> page1 = pageRepository.findById(page);
        List<Index> listOdIndex = indexRepository.findAll();
        for (Index i : listOdIndex) {
            if (i.getLemma().equals(word) && i.getPage().equals(page1.get())) return i.getRanks();
        }
        return 0;
    }


    public String getTitleOfPage(String text) {
        String regex = "<title>[\\W\\w\\s]+</title>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        String titleResult = null;
        if (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String title = text.substring(start, end);
            titleResult = title.replaceAll("[^а-яА-Я\\s]+", " ");
        }
        return titleResult;

    }

    public List<SearchDto> trimListObject(List<SearchDto> list, int offset, int limit) {
        int count = list.size();
        int last = count - offset;
        if (last > 0) {
            for (int i = 0; i < offset; i++) {
                list.remove(i);
            }
        }
        int number = last - limit;
        if (number > 0) {
            for (int i = count - 1; i >= limit; i--) {
                list.remove(i);
            }
        }
        return list;
    }

}
