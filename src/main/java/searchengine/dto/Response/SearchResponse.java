package searchengine.dto.Response;

import lombok.Data;
import searchengine.dto.SearchDto;
import java.util.List;

@Data
public class SearchResponse {
    boolean result;
    int count;
    List<SearchDto> data;
    String error;
}