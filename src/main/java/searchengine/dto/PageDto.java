package searchengine.dto;

import lombok.*;


@Value
public class PageDto {
    String url;
    String content;
    int status;
}
