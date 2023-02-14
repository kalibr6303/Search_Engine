package searchengine.developer;

import java.util.List;

public interface SnippetParser {
    List<String> getSnippet(String content, String word);

}
