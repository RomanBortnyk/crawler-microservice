package crawler.query.model;

import core.Query;
import crawler.query.QueryStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class MongoQuery {

    @Id
    private String id;

    private QueryStatus queryStatus;

    private String parserName;

    private List<String> modes;

    private Map<String, String> parameters;

    private LocalDateTime createdAt;

    public MongoQuery(Query query, QueryStatus queryStatus, LocalDateTime createdAt) {
        this.id = query.getId();
        this.queryStatus = queryStatus;
        this.parserName = query.getParserName();
        this.parameters = query.getParameters();
        this.modes = query.getModes();
        this.createdAt = createdAt;
    }
}
