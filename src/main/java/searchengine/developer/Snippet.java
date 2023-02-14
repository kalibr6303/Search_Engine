package searchengine.developer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.morphology.LuceneMorphology;
import searchengine.morphology.Morphology;
import java.io.IOException;
import java.util.*;


public class Snippet implements SnippetParser {

    private Morphology morphology = new LuceneMorphology();

    public Snippet() throws IOException {
    }


    public List<String> getTegFromContent(String content) {
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


    public String addBoltFontInTag(String teg, String word) {

        HashMap<String, String> lemmasOfTeg = morphology.getLemmasByTeg(teg);
        List<String> lemmasByQuery = morphology.getLemmasByQuery(word);
        HashSet<String> lemmasBolt = new HashSet<>();
        lemmasOfTeg.entrySet().forEach(s -> {
            lemmasByQuery.forEach(k -> {
                if (s.getKey().equals(k)) lemmasBolt.add(s.getValue());
            });
        });
        String tegBolt = null;
        String tegElementary = teg;
        for (String s : lemmasBolt) {
            tegBolt = teg.replaceAll(s, "<b>" + s + "</b>");
            teg = tegBolt;
        }
        if (tegElementary.equals(teg)) return null;
        String tegBoltLast;
        if (tegBolt != null) {
            tegBoltLast = tegBolt.replaceAll("</b> <b>", " ");
            return tegBoltLast;
        }
        return null;
    }


    @Override
    public List<String> getSnippet(String content, String word) {

        List<String> listContent = getTegFromContent(content);
        List<String> snippetList = new ArrayList<>();
        for (String s : listContent) {
            String snip = addBoltFontInTag(s, word);
            if (snip != null) {
                snippetList.add(snip);
            }
        }
        return snippetList;
    }

}
