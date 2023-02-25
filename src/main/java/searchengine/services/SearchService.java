package searchengine.services;

import searchengine.dto.Response.SearchResponse;

import java.io.IOException;
import java.sql.SQLException;

public interface SearchService {
    SearchResponse allSiteSearch(String text, int offset, int limit) throws IOException, SQLException;
    SearchResponse siteSearch(String request, String url, int offset, int limit) throws IOException, SQLException;
}
