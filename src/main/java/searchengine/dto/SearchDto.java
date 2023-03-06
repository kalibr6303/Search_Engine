package searchengine.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchDto {
    String site;
    String siteName;
    String uri;
    String title;
    String snippet;
    Float relevance;

   public SearchDto(){}
}
