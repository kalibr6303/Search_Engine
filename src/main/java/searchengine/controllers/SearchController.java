package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.Response.ResponseResult;
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
    private final ResponseResult responseResult = new ResponseResult();


    @GetMapping("/search")
    public ResponseEntity<Object> searchWords(
            String query,
            @RequestParam(name = "site", required = false, defaultValue = "") String site,
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit)
            throws  IOException, SQLException {

        if (query.isEmpty()) {
            responseResult.setResult(false);
            responseResult.setError("Пустой запрос");
            return new ResponseEntity<>(responseResult,
                    HttpStatus.OK);
        } else {
            List<SearchDto> searchData;
            if (!site.isEmpty()) {
                searchData = searchService.siteSearch(query, site, offset, limit);

            } else {
                searchData = searchService.allSiteSearch(query, offset, limit);
            }

            if (searchData == null) {
                responseResult.setResult(false);
                responseResult.setError("Ничего не найдено");
                return new ResponseEntity<>(responseResult, HttpStatus.OK);
            }
            else {

                return new ResponseEntity<>(new SearchResponse(true, searchData.size(), searchData),
                        HttpStatus.OK);
            }
        }
    }
}
