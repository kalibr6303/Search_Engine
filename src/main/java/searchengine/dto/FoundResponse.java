package searchengine.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
@Data
public class FoundResponse {

    Boolean result;
    List<DataResponse> data;
    int count;
    String error;

}
