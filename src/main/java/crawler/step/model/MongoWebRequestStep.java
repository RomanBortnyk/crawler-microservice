package crawler.step.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

@RequiredArgsConstructor
@Getter
public class MongoWebRequestStep {

    @Id
    private final String id;
    private final MongoWebRequestSettings webRequestSettings;
    private final MongoProxyAddress mongoProxyAddress;
    private final String queryId;

}
