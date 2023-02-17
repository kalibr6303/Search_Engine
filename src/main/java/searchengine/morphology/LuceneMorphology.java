package searchengine.morphology;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
@Slf4j
@Component
public class LuceneMorphology implements Morphology{


    private static  org.apache.lucene.morphology.LuceneMorphology luceneMorphology;

    static {
        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    

    public Boolean isContainsServicePartSpeech(String word) throws IOException {
        String word1 = word.trim();
        if (!word1.matches("[а-яА-Я]+")) return true;
        List<String> wordBaseForms = luceneMorphology.getMorphInfo(word1);
        for (String l : wordBaseForms) {
            if (l.matches("([\\W\\w]+ПРЕДЛ)|([\\W\\w]+МЕЖД)|" +
                    "([\\w\\w]+)|([\\W\\w]+СОЮЗ)")) return true;
        }
        return false;
    }


    public static List<String> getLinkString(String word) {

        String wordResult = word.replaceAll("[^а-яА-Я\\s]+", " ");
        String wordResultNext = wordResult.toLowerCase();
        List<String> linkString;
        String[] list = wordResultNext.split("\\s+");
        linkString = Arrays.asList(list);
        return linkString;
    }

    public static List<String> getLinkStringForSearch(String word) {

        String wordResult = word.replaceAll("[^а-яА-Я\\s]+", " ");

        List<String> linkString;
        String[] list = wordResult.split("\\s+");
        linkString = Arrays.asList(list);
        return linkString;
    }

    public HashMap<String, Integer> getLemmaList(String word) {
        HashMap<String, Integer> storageString = new HashMap<>();
        Integer count = 1;
        getLinkString(word).forEach(s -> {
            try {
                if (!isContainsServicePartSpeech(s) && s.matches("[а-я]+")) {
                    List<String> forms = luceneMorphology.getNormalForms(s);
                    forms.forEach(d -> {
                        if (storageString.containsKey(d)) {
                            storageString.put(d, storageString.get(d) + 1);
                        } else storageString.put(d, count);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return storageString;

    }

    public List<String> getLemmasByQuery(String word) {
        List<String> storage = new ArrayList<>();
        HashMap<String, String> storageString = getLemmasByTeg(word);
        storageString.entrySet().forEach(s -> storage.add(s.getKey()));
        return storage;
    }

    public HashMap<String, String> getLemmasByTeg(String word) {
        HashMap<String, String> storageString = new HashMap<>();
        List<String> lemmaByTeg = getLinkStringForSearch(word);;
        lemmaByTeg.forEach(s -> {
            try {
                String wordResultNext = s.toLowerCase();
                if (!isContainsServicePartSpeech(wordResultNext) && wordResultNext.matches("[а-я]+")) {

                    List<String> forms = luceneMorphology.getNormalForms(wordResultNext);
                    forms.forEach(d -> {
                            storageString.put(d, s);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return storageString;
    }
}
