package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.Response.ResponseResult;
import searchengine.services.IndexService;


    @RestController
    @RequiredArgsConstructor
    @RequestMapping("/api")
    public class IndexController {

        private final IndexService indexService;
        private final ResponseResult responseResult = new ResponseResult();


        @GetMapping("/startIndexing")
        public ResponseEntity<Object> startIndexingAll() {
            if (indexService.indexAll()) {
                responseResult.setResult(true);
                responseResult.setError("");
            } else {
                responseResult.setResult(false);
                responseResult.setError("Индексация уже запущена");
            }
            return new ResponseEntity<>(responseResult, HttpStatus.OK);
        }

        @GetMapping("/stopIndexing")
        public ResponseEntity<Object> stopIndexing() {
            if (indexService.stopIndexing()) {
                responseResult.setResult(true);
                responseResult.setError("");
            } else {
                responseResult.setResult(false);
                responseResult.setError("Индексация не запущена");
            }
            return new ResponseEntity<>(responseResult, HttpStatus.OK);
        }

        @PostMapping("/indexPage")
        public ResponseEntity<Object>  indexLink(String url) {

           if (indexService.indexPage(url)) {
               responseResult.setResult(true);
               responseResult.setError("");
           } else {
              responseResult.setResult(false);
              responseResult.setError("Данная страница находится вне списка индексируемых сайтов");
           }
            return new ResponseEntity<>(responseResult, HttpStatus.OK);
        }

}
