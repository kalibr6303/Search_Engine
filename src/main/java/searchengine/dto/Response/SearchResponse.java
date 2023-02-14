package searchengine.dto.Response;

import lombok.Value;
import searchengine.dto.SearchDto;

import java.util.List;

@Value
public class SearchResponse {
    boolean result;
    int count;
    List<SearchDto> data;
}