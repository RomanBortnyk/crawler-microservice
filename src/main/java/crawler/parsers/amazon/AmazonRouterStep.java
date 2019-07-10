package crawler.parsers.amazon;

import core.Query;
import core.WebRequestSettings;
import core.step.WebRequestStep;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AmazonRouterStep extends WebRequestStep {

    private static final Logger log = LoggerFactory.getLogger(AmazonRouterStep.class);
    private static final int ALSO_BOUGHT_BLOCK_TRIES_LIMIT = 5;
    private static final Set<Integer> RETRY_ERROR_CODES = new HashSet<>(
            Arrays.asList(400, 401, 403, 405, 407, 500, 501, 502, 503, 504)
    );

    private final AmazonHelper helper;
    private final String pageUrl;

    AmazonRouterStep(WebRequestSettings settings, Query query, AmazonHelper helper) {
        super(query, settings);
        this.helper = helper;
        this.pageUrl = settings.getUrl();
    }

    /**
     * Returns true if page is product page,
     * has less then 2 fbts in fbt block,
     * product is available,
     * has not "also bought" block
     *
     * @return true if request retry needed
     */
    private static boolean isAlsoBoughtBlockRequestRepeatNeeded(Document document) {
        return AmazonProductStep.isResponsible(document)
                && AmazonProductStep.extractFbtLinksFromFbtBlock(document).size() < 2
                && !AmazonProductStep.isUnavailable(document)
                && AmazonProductStep.extractSimilarFbtsBlock(document) == null;
    }

    @Override
    public void run() {

        Document document = getDocument();

        if (AmazonProductStep.isResponsible(document)) {
            addNextStep(new AmazonProductStep(this));

        } else if (AmazonNavigationStep.isResponsible(document)) {
            addNextStep(new AmazonNavigationStep(this));

        } else if (AmazonNewNavigationStep.isResponsible(document)) {
            addNextStep(new AmazonNewNavigationStep(this));
        }
    }

    public AmazonHelper getHelper() {
        return helper;
    }

    @Override
    public boolean isValidResponse() {

        Integer responseCode = getResponseCode();

        if (RETRY_ERROR_CODES.contains(responseCode)) {
            return false;
        }

        Document document = getDocument();

        boolean isCaptchaPage = !document.select("form[action='/errors/validateCaptcha']").isEmpty();
        if (responseCode == 200 && isCaptchaPage) {
            return false;
        }

        if (isAlsoBoughtBlockRequestRepeatNeeded(document) && getTries() <= ALSO_BOUGHT_BLOCK_TRIES_LIMIT) {
            return false;
        }

        return true;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    @Override
    public WebRequestStep copy() {
        return new AmazonRouterStep(getWebRequestSettings(), getQuery(), new AmazonHelper(this.helper));
    }
}
