package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.FoundResponse;
import searchengine.dto.IndexingResponse;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.StatisticsServiceImpl;
import searchengine.services.ThreadLinkService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import static searchengine.services.ThreadLinkService.sitesList;

@RestController
@RequestMapping("/api")
public class LinkThreadController {

    public StatisticsServiceImpl statisticsServiceImpl;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;



    public LinkThreadController(PageRepository pageRepository,
                                SiteRepository siteRepository,

                                StatisticsServiceImpl statisticsServiceImpl) {


        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;

        this.statisticsServiceImpl = statisticsServiceImpl;


    }


    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing() throws ExecutionException, InterruptedException, SQLException {
        return ResponseEntity.ok(new ThreadLinkService(sitesList, pageRepository, siteRepository,
                statisticsServiceImpl).startIndexing2());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing() {
        return ResponseEntity.ok(new ThreadLinkService(sitesList, pageRepository, siteRepository,
                statisticsServiceImpl).stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> addPage(String url) throws IOException, SQLException {
        return ResponseEntity.ok(new ThreadLinkService(sitesList, pageRepository, siteRepository,
                statisticsServiceImpl).addUpdate(url));
    }

    @GetMapping("/search")
    public ResponseEntity<FoundResponse> searchFound(String query, @RequestParam(required = false) String site) throws SQLException, IOException {


        return ResponseEntity.ok(new ThreadLinkService(sitesList, pageRepository,
                siteRepository,
                statisticsServiceImpl).getRequestLemma(query, site));

    }

}
