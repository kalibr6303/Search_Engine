package searchengine.services;

import searchengine.dto.SearchDto;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface SearchService {
    List<SearchDto> allSiteSearch(String text, int offset, int limit) throws IOException;
    List<SearchDto> siteSearch(String request, String url, int offset, int limit) throws IOException, SQLException;
}
