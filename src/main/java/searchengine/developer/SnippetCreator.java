package searchengine.developer;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import searchengine.morphology.Morphology;
import java.util.*;

@Component
@RequiredArgsConstructor
public class SnippetCreator implements Snippet {

    private final Morphology morphology;


    private List<String> getTegFromContent(String content) {
        List<String> listContent = new ArrayList<>();
        Document document = Jsoup.parse(content);
        List<String> nameTag = Arrays.asList("p", "h1", "h2", "h3", "title", "h4", "h5",
                "cite", "hr", "code", "var", "pre", "a", "br");
        nameTag.forEach(s -> {
            Elements elements = document.select(s);
            elements.forEach(e -> {
                listContent.add(String.valueOf(e));
            });
        });

        return listContent;
    }


    private HashMap<String, Integer> addBoltFontInTag(String teg, String word) {
        HashMap<String, String> lemmasOfTeg = morphology.getLemmasByTeg(teg);
        List<String> lemmasByQuery = morphology.getLemmasByQuery(word);
        HashMap<String, Integer> snippetList = new HashMap<>();

        HashSet<String> lemmasBolt = new HashSet<>();
        lemmasOfTeg.entrySet().forEach(s -> {
            lemmasByQuery.forEach(k -> {
                if (s.getValue().equals(k)) lemmasBolt.add(s.getKey());
            });
        });

        String tegBolt = null;
        String tegElementary = teg;
        int count = 0;
        for (String s : lemmasBolt) {
            tegBolt = teg.replaceAll(s, "<b>" + s + "</b>");
            teg = tegBolt;
            count++;
        }
        if (tegElementary.equals(teg)) return null;
        String tegBoltLast;
        if (tegBolt != null) {
            tegBoltLast = tegBolt.replaceAll("</b> <b>", " ");
            snippetList.put(tegBoltLast, count);
            return snippetList;
        }
        return null;
    }


    @Override
    public String getSnippet(String content, String word) {

        List<String> listContent = getTegFromContent(content);
        HashMap<String, Integer> snippets = new HashMap<>();
        List<String> snippetList = new ArrayList<>();
        for (String s : listContent) {
            HashMap<String, Integer> snipList = addBoltFontInTag(s, word);

            if (snipList != null) {
                snipList.entrySet().forEach(d -> snippets.put(d.getKey(), d.getValue()));

            }
        }
        snippets.entrySet().stream() // сортируем для выбора наиболее информативного сниппета
                .sorted(Map.Entry.<String, Integer>comparingByValue()
                        .reversed())
                .forEach(s -> {
                    snippetList.add(s.getKey());

                });
        if (snippetList.isEmpty()) return null;
        return snippetList.get(0);
    }
}
