package crawler.step.mapper;

import core.WebRequestSettings;
import core.step.WebRequestStep;
import crawler.step.model.MongoProxyAddress;
import crawler.step.model.MongoWebRequestSettings;
import crawler.step.model.MongoWebRequestStep;
import org.springframework.stereotype.Component;

@Component
public class MongoWebRequestStepMapper {

    public MongoWebRequestStep mapToMongoWebRequestStep(WebRequestStep webRequestStep) {

        WebRequestSettings webRequestSettings = webRequestStep.getWebRequestSettings();

        MongoWebRequestSettings mongoWebRequestSettings = new MongoWebRequestSettings(
                webRequestSettings.getUrl(),
                webRequestSettings.getRequestHeader(),
                webRequestSettings.getReadWriteTimeout(),
                webRequestSettings.getConnectionTimeout()
        );

        MongoProxyAddress mongoProxyAddress = new MongoProxyAddress(
                webRequestStep.getProxyAddress().getIpAddress(),
                webRequestStep.getProxyAddress().getPort()
        );


        return new MongoWebRequestStep(
                webRequestStep.getId(),
                mongoWebRequestSettings,
                mongoProxyAddress,
                webRequestStep.getQuery().getId());
    }

    public WebRequestStep mapToWebRequestStep(MongoWebRequestStep mongoWebRequestStep) {

        // TODO implement
        return null;
    }

}
