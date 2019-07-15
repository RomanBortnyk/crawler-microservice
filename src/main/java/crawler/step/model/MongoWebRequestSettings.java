package crawler.step.model;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class MongoWebRequestSettings {

    private final String url;
    private final Map<String, String> requestHeader;
    private final int readWriteTimeout;
    private final int connectionTimeout;
}
