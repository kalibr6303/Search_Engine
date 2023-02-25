package searchengine.developer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.dto.PageDto;
import searchengine.morphology.Morphology;
import java.util.HashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LemmaParser implements Lemma {

    private final Morphology morphology;


    public HashMap<String, Integer> getLemmasOfSite(List<PageDto> pagesDtoList) {
        HashMap<String, Integer> lemmasInSite = new HashMap<>();
        pagesDtoList.forEach(p -> {
            HashMap<String, Integer> lemmasOfPageList = morphology.getLemmaList(p.getContent());
            lemmasOfPageList.entrySet().forEach(l -> {
                if (!lemmasInSite.containsKey(l.getKey())) lemmasInSite.put(l.getKey(), 1);
                else lemmasInSite.put(l.getKey(), lemmasInSite.get(l.getKey()) + 1);
            });
        });
        return lemmasInSite;
    }


    public HashMap<String, Integer> getLemmaOfPageForWrite(HashMap<String, Integer> lemmasOfSite, PageDto pageDto) {
        HashMap<String, Integer> lemmasOfPageForWrite = new HashMap<>();
        HashMap<String, Integer> lemmasOfPage = morphology.getLemmaList(pageDto.getContent());
        lemmasOfPage.entrySet().forEach(p -> {
            if (lemmasOfSite.containsKey(p.getKey()))
                lemmasOfPageForWrite.put(p.getKey(), lemmasOfSite.get(p.getKey()));
        });
        return lemmasOfPageForWrite;
    }


    public HashMap<String, Integer> getLemmasInIndex(PageDto pageDto) {
        HashMap<String, Integer> lemmasForIndex = morphology.getLemmaList(pageDto.getContent());
        return lemmasForIndex;
    }

}
