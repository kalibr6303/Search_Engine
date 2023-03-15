package searchengine.developer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.dto.SnippetDto;
import searchengine.morphology.Morphology;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
@RequiredArgsConstructor
public class SnippetCreator implements Snippet {

    private final Morphology morphology;


    private List<SnippetDto> addBoldFontInWordOfText(String content, String word) {
        List<SnippetDto> snippetsList = new ArrayList<>();
        HashMap<String, String> lemmasOfTeg = morphology.getLemmasByTeg(content);
        List<String> lemmasByQuery = morphology.getLemmasByQuery(word);
        HashSet<String> lemmasBolt = new HashSet<>();
        lemmasOfTeg.entrySet().forEach(s -> {
            lemmasByQuery.forEach(k -> {
                if (s.getValue().equals(k)) lemmasBolt.add(s.getKey());
            });
        });

        String tegBolt = null;
        String tegElementary = content;
        for (String s : lemmasBolt) {
            tegBolt = content.replaceAll(s, "<b>" + s + "</b>");
            content = tegBolt;
        }
        if (tegElementary.equals(content)) return null;
        String tegBoltLast;
        if (tegBolt != null) {
            tegBoltLast = tegBolt.replaceAll("</b> <b>", " ");
            List<String> snippetsAll = getWordsFromText(tegBoltLast);
            HashMap<String, Integer> snippetsFilter = getTextForSnippetFromWords(snippetsAll, lemmasBolt);
            snippetsFilter.entrySet().forEach(s -> {
                SnippetDto snippetDto = new SnippetDto();
                snippetDto.setField(s.getKey());
                snippetDto.setCount(s.getValue());
                snippetDto.setLength(getLengthFromRussianString(s.getKey()));
                snippetsList.add(snippetDto);
            });

            return snippetsList;
        }
        return null;
    }


    @Override
    public String getSnippet(String content, String word) {

        List<SnippetDto> snippetList = addBoldFontInWordOfText(content, word);
        if (snippetList == null) return null;

            snippetList.sort((o1, o2) -> {
                if (o1.getCount().compareTo(o2.getCount()) == 0) {
                    return o2.getLength().compareTo(o1.getLength());
                }
                else {
                    return o2.getCount().compareTo(o1.getCount());
                }
            });
        return snippetList.get(0).getField();
    }


    private   List<String> getWordsFromText(String content) {
        List<String> requestBolt = new ArrayList<>();
        List<String> resultBolt = new ArrayList<>();
        int prevPoint;
        int lastPoint;

        String regex = "<b>[а-яА-Я\\s]+</b>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if (content.lastIndexOf(" ", end + 50) != -1 ){
                lastPoint = content.lastIndexOf(" ", end + 50);
            }
            else lastPoint = end;
            requestBolt.add(content.substring(start, end));
            if (content.lastIndexOf(" ", start - 20) != -1) {
                prevPoint = content.lastIndexOf(" ", start - 20);
            }
            else prevPoint = start;
            String snippet = content.substring(prevPoint, lastPoint);
            if (prevPoint != start) snippet = "... " + snippet;
            if (lastPoint != end) snippet = snippet + " ...";
            resultBolt.add(snippet);
        }
        return resultBolt;

    }


    private HashMap<String, Integer> getTextForSnippetFromWords(List<String> snippetList, HashSet<String> lemmasBolt){
        HashMap<String, Integer> snippetsList = new HashMap<>();
        for (String s : snippetList) {
            int count = 0;
            String wordResult = s.replaceAll("[^а-яА-Я\\s]+", " ");
            String[] lemmasWordResult = wordResult.split("\\s+");
            List<String> linkString = Arrays.asList(lemmasWordResult);
            for (String d : linkString) {
                for (String l : lemmasBolt) {
                    if (d.equals(l)) count++;
                }
                }

            if (count != 0) snippetsList.put(s, count);



            }
       return snippetsList;
    }


    private int getLengthFromRussianString(String snippet){
            String russianSymbol = snippet.replaceAll("[^а-яА-Я\\s]+", "");
        return russianSymbol.length();
    }
}
