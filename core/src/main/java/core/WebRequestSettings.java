package core;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class WebRequestSettings {

    private final String url;
    private Map<String, String> requestHeader = Collections.emptyMap();
    private int readWriteTimeout;
    private int connectionTimeout;

    public WebRequestSettings(String url) {
        this.url = url;
    }
}
