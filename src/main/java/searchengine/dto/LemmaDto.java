package searchengine.dto;

import lombok.Setter;
import lombok.Value;
import searchengine.model.Site;


@Setter
public class LemmaDto {

    private String lemma;
    private Site site;


    public LemmaDto(String lemma, Site site) {
        this.lemma = lemma;
        this.site = site;
    }
    public LemmaDto(){}
}
