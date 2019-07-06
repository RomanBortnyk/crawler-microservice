package crawler.parsers.amazon;

import core.Query;
import core.WebRequestSettings;
import core.annotation.Parser;
import core.step.BaseStep;
import org.apache.commons.lang3.StringUtils;

@Parser(name = "amazon")
public class AmazonStartupStep extends BaseStep {

    public AmazonStartupStep(Query query) {
        super(query);
    }

    @Override
    public void run() {

        AmazonHelper.clearExtractedAsinsMap();

        String url = getQuery().getParam("url");

        if (StringUtils.isBlank(url)) return;

        AmazonHelper amazonHelper = new AmazonHelper();

        WebRequestSettings settings = amazonHelper.createWebRequestSettings(url, "");

        addNextSteps(new AmazonRouterStep(settings, getQuery(), amazonHelper));
    }
}
