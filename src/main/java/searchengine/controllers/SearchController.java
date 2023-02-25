package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.Response.IndexingResponse;
import searchengine.dto.Response.SearchResponse;
import searchengine.dto.SearchDto;
import searchengine.services.SearchService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SearchController {

    private final SearchService searchService;



    @SneakyThrows
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> searchWords(
            String query,
            @RequestParam(name = "site", required = false, defaultValue = "") String site,
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit)
             {

        if (site.isEmpty()) return  ResponseEntity.ok(searchService.allSiteSearch(query, offset, limit));
        else return ResponseEntity.ok(searchService.siteSearch(query, site, offset, limit));
    }
}
