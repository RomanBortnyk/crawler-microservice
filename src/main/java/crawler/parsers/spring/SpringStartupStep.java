package crawler.parsers.spring;

import core.Query;
import core.WebRequestSettings;
import core.annotation.Parser;
import core.step.BaseStep;

@Parser(name = "spring")
public class SpringStartupStep extends BaseStep {

    public SpringStartupStep(Query query) {
        super(query);
    }

    @Override
    public void run() {
        System.out.println("executing step");

        String url = getQuery().getParameters().get("url");

        if (url == null){
            return;
        }

        Query query = getQuery();

        WebRequestSettings webRequestSettings = new WebRequestSettings(url);

        SpringRouterStep springRouterStep = new SpringRouterStep(query, webRequestSettings);

        addNextSteps(springRouterStep);
    }

}
