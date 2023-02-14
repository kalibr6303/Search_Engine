package searchengine.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;


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
