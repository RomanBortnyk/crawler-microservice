package crawler.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QueryDto {

    @NotEmpty
    private String parserName;

    @NotNull
    private List<String> modes;

    private Map<String, String> parameters;

}
