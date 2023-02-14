package searchengine.morphology;

import java.util.HashMap;
import java.util.List;

public interface Morphology {

    HashMap<String, Integer> getLemmaList(String content);
    HashMap<String, String> getLemmasByTeg(String word);
    List<String> getLemmasByQuery(String word);
}
