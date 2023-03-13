package searchengine.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SnippetDto {
    String field;
    Integer count;
    Integer length;
}
