package searchengine.services;

public interface IndexService {
    boolean indexPage(String url);
    boolean indexAll();
    boolean stopIndexing();
}
