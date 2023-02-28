package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.Response.IndexingResponse;
import searchengine.services.IndexService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class IndexController {

    private final IndexService indexService;



    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexingAll() {
        return  ResponseEntity.ok(indexService.indexAll());
    }


    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing() {
        return  ResponseEntity.ok(indexService.stopIndexing());
    }


    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse>  indexLink(String url) {
        return  ResponseEntity.ok(indexService.indexPage(url));
    }

}