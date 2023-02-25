package searchengine.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import searchengine.model.Site;

import java.util.Map;

@Setter
@Getter
@RequiredArgsConstructor
public class LemmaDto {
    private String lemma;
   // private Site site;

    public LemmaDto(String lemma) {
        this.lemma = lemma;

      //  this.site = site;
    }
}
