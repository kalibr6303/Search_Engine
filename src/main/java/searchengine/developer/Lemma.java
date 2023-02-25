package searchengine.developer;

import searchengine.dto.LemmaDto;
import searchengine.dto.PageDto;
import searchengine.model.Site;

import java.util.HashMap;
import java.util.List;

public interface Lemma {
    public HashMap<String, Integer> getLemmasOfSite(List<PageDto> pagesDtoList);
    public HashMap<String, Integer> getLemmaOfPageForWrite(HashMap<String, Integer> lemmasOfSite, PageDto pageDto);
  public HashMap<String, Integer> getLemmasInIndex(PageDto pageDto);

}
