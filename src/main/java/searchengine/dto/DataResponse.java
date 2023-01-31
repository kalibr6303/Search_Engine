package searchengine.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DataResponse {
    String site;
    String siteName;
    String uri;
    String title;
    String snippet;
    Float relevance;
}