package crawler.parsers.amazon;

import common.util.HtmlConstants.Attributes;
import core.WebRequestSettings;
import core.model.BaseEntry;
import core.step.BaseStep;
import crawler.parsers.amazon.dto.Product;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static common.util.ParserUtil.extractFirstMatchElementAttribute;
import static common.util.ParserUtil.extractFirstMatchElementText;
import static common.util.ParserUtil.normalizeText;
import static common.util.ParserUtil.removeNonDigits;
import static common.util.ParserUtil.substringBeforeRefPath;

public class AmazonNavigationStep extends BaseStep {

    private static final Logger log = LoggerFactory.getLogger(AmazonNavigationStep.class);
    private static final int PRODUCT_RANK_MAX_VALUE = 20;
    private final Document document;
    private final AmazonHelper helper;
    private final String pageUrl;

    AmazonNavigationStep(AmazonRouterStep parent) {
        super(parent.getQuery());
        this.document = parent.getDocument();
        this.pageUrl = parent.getPageUrl();
        this.helper = parent.getHelper();
    }

    static boolean isResponsible(Document document) {
        return !document.select("div#zg div.zg_itemWrapper").isEmpty();
    }

    static List<String> extractNextCategoriesLinks(Document document, String currentPageUrl, String currentCategory) {

        List<String> links = new ArrayList<>();

        boolean includeCategory = false;

        for (Element liElem : document.select("ul#zg_browseRoot li")) {

            if (includeCategory) {
                String href = substringBeforeRefPath(extractFirstMatchElementAttribute(liElem, "a", Attributes.HREF));
                if (StringUtils.isBlank(href)) {
                    log.warn("Category link is blank. Link : " + currentPageUrl);
                    continue;
                }
                links.add(href);
            } else {
                String categoryName = normalizeText(liElem.text());

                if (categoryName.equalsIgnoreCase(currentCategory)) {
                    includeCategory = true;
                }
            }
        }

        return links;
    }

    @Override
    public void run() {

        String currentCategory = helper.getCategory();
        String categoryLink = helper.getCategoryLink();

        if (StringUtils.isBlank(currentCategory)) {
            currentCategory = extractCurrentCategory();
            categoryLink = substringBeforeRefPath(pageUrl);
        }

        for (Element productDivElement : document.select("div.zg_itemImmersion")) {

            String productLink = extractProductLink(productDivElement);
            String productAsin = AmazonProductStep.extractAsinFromPageUriStr(productLink);
            Integer productRank = extractProductRank(productDivElement);

            if (StringUtils.isNotBlank(productLink) && productRank != 0 && productRank <= PRODUCT_RANK_MAX_VALUE) {

                AmazonHelper amazonHelper = new AmazonHelper(helper);

                amazonHelper.setCategory(currentCategory);
                amazonHelper.setCategoryLink(categoryLink);
                amazonHelper.setProductRankInCategory(productRank);

                if (AmazonHelper.isExtractedAsParent(productAsin)) {

                    Product partialProduct = new Product();

                    partialProduct.setAsin(productAsin);
                    partialProduct.setCategory(currentCategory);
                    partialProduct.setCategoryRank(productRank);
                    partialProduct.setCategoryLink(categoryLink);
                    partialProduct.setIsFbt(false);

                    BaseEntry<Product> productBaseEntry = new BaseEntry<>(getQuery(), partialProduct);

                    getExecutionResult().getEntries().add(productBaseEntry);

                } else {
                    WebRequestSettings settings = amazonHelper.createWebRequestSettings(productLink, pageUrl);
                    addNextSteps(new AmazonRouterStep(settings, getQuery(), amazonHelper));
                }
            }
        }

        if (getQuery().containsMode(AmazonConstants.QueryModes.CATEGORY_SUBTREES)) {

            for (String nextCategoryLink : extractNextCategoriesLinks(document, pageUrl, currentCategory)) {
                WebRequestSettings settings = helper.createWebRequestSettings(nextCategoryLink, pageUrl);
                addNextSteps(new AmazonRouterStep(settings, getQuery(), new AmazonHelper()));
            }
        }
    }

    private String extractProductLink(Element productDivElement) {
        return extractFirstMatchElementAttribute(productDivElement, "div.zg_itemWrapper div[data-p13n-asin-metadata] a[href]", Attributes.HREF);
    }

    private String extractCurrentCategory() {
        return extractFirstMatchElementText(document, "ul#zg_browseRoot span.zg_selected").trim();
    }

    private int extractProductRank(Element productDivElement) {

        String productRankStr = removeNonDigits(extractFirstMatchElementText(productDivElement, "div.zg_rankDiv"));
        return StringUtils.isNotBlank(productRankStr) ? Integer.parseInt(productRankStr) : 0;
    }
}
