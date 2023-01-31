package searchengine.dto;

import lombok.Data;
import searchengine.controllers.LinkThreadController;
import searchengine.model.Page;

import java.util.List;
@Data
public class IndexingResponse {
    private Boolean result;
    private String error;
}