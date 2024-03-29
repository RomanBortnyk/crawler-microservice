package core.step;

import core.WebRequestSettings;
import core.proxy.ProxyAddress;
import core.step.result.ExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

@Slf4j
public class WebRequestStepProxy extends AbstractParsingStep {

    private static final int DEFAULT_RETRY_STEP_COUNT = 10;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 20000;
    private final WebRequestStep webRequestStep;

    public WebRequestStepProxy(WebRequestStep webRequestStep) {
        super(webRequestStep.getId(), webRequestStep.getQuery());
        this.webRequestStep = webRequestStep;
    }

    @Override
    public ExecutionResult getExecutionResult() {
        return webRequestStep.getExecutionResult();
    }

    @Override
    public void run() {

        // provides html document to web request step
        Connection.Response response = doWebRequest();

        if (response == null) {
            retry();
            return;
        }

        webRequestStep.setDocument(createDocument(response));
        webRequestStep.setSuccessfulRequestUrl(response.url().toString());
        webRequestStep.setResponseCode(response.statusCode());

        if (!webRequestStep.isValidResponse()) {
            retry();
            return;
        }

        // step logic
        webRequestStep.run();
    }

    private void retry() {
        if (webRequestStep.getTries() <= DEFAULT_RETRY_STEP_COUNT) {
            WebRequestStep copy = webRequestStep.copy();
            copy.setTries(webRequestStep.getTries() + 1);
            addNextStep(copy);
        }
    }

    private Document createDocument(Connection.Response response) {
        try {
            return response.parse();
        } catch (IOException e) {
            log.error("Exception during parsing html for url: " + webRequestStep.getWebRequestSettings().getUrl());
        }
        return new Document(StringUtils.EMPTY);
    }

    private Connection.Response doWebRequest() {

        ProxyAddress proxyAddress = webRequestStep.getProxyAddress();

        WebRequestSettings webRequestSettings = webRequestStep.getWebRequestSettings();
        final String requestUrl = webRequestSettings.getUrl();

        Connection.Response response = null;

        log.info("Request to url: {}", requestUrl);

        try {

            Connection connection = Jsoup.connect(webRequestSettings.getUrl())
                    .proxy(proxyAddress.getIpAddress(), proxyAddress.getPort());

            webRequestSettings.getRequestHeader()
                    .forEach(connection::header);

            int connectionTimeout = webRequestSettings.getConnectionTimeout();
            connection.timeout(connectionTimeout == 0 ? DEFAULT_CONNECTION_TIMEOUT : connectionTimeout);

            connection.followRedirects(true);
            response = connection.execute();

        } catch (Exception e) {
            log.warn("Failed request to: {}. Reason: {}", requestUrl, e.getMessage());
        }

        return response;
    }
}
