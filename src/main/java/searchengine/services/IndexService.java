package searchengine.services;

import searchengine.dto.Response.IndexingResponse;

public interface IndexService {
    IndexingResponse indexPage(String url);
    IndexingResponse indexAll();
    IndexingResponse stopIndexing();
}
