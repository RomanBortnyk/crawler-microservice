package crawler.parsers.spring;

import core.Query;
import core.WebRequestSettings;
import core.step.WebRequestStep;
import org.jsoup.nodes.Document;

public class SpringRouterStep extends WebRequestStep {

    public SpringRouterStep(Query query, WebRequestSettings webRequestSettings) {
        super(query, webRequestSettings);
    }

    @Override
    public WebRequestStep copy() {
        return new SpringRouterStep(getQuery(), getWebRequestSettings());
    }

    @Override
    public void run() {

        Document document = getDocument();

        if (true){

            SpringProjectsPage springProjectsPage = new SpringProjectsPage(getQuery(), document);

            addNextStep(springProjectsPage);

        }
    }


}
