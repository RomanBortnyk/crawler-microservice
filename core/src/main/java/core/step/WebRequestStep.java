package core.step;

import core.Query;
import core.WebRequestSettings;
import core.proxy.ProxyAddress;
import core.step.result.WebRequestStepExecutionResult;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;

@Slf4j
public abstract class WebRequestStep extends AbstractParsingStep {

    private static final int OK_RESPONSE_CODE = 200;

    @Getter
    private final WebRequestStepExecutionResult executionResult;
    @Getter
    private Document document;
    @Getter
    protected ProxyAddress proxyAddress;
    @Getter
    @Setter
    protected int responseCode;
    private WebRequestSettings webRequestSettings;
    @Getter
    @Setter
    private int tries;

    public WebRequestStep(Query query, WebRequestSettings webRequestSettings) {
        super(query);
        this.webRequestSettings = webRequestSettings;
        executionResult = new WebRequestStepExecutionResult(this);
    }

    public void setSuccessfulRequestUrl(String url) {
        executionResult.setSuccessfulRequestUrl(url);
    }

    public boolean isValidResponse() {
        return responseCode == OK_RESPONSE_CODE;
    }

    void setDocument(Document document) {
        this.document = document;
    }

    public WebRequestSettings getWebRequestSettings() {
        return webRequestSettings;
    }

    public void setProxyAddress(ProxyAddress proxyAddress) {
        this.proxyAddress = proxyAddress;
    }

    public abstract WebRequestStep copy();
}
