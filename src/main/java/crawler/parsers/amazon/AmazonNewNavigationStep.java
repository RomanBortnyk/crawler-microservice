package crawler.parsers.amazon;

import common.util.HtmlConstants;
import core.WebRequestSettings;
import core.model.BaseEntry;
import core.step.BaseStep;
import core.step.Step;
import crawler.parsers.amazon.dto.Product;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

import static common.util.ParserUtil.extractFirstMatchElementAttribute;
import static common.util.ParserUtil.extractFirstMatchElementText;
import static common.util.ParserUtil.removeNonDigits;
import static common.util.ParserUtil.substringBeforeRefPath;


public class AmazonNewNavigationStep extends BaseStep {

    private static final int PRODUCT_RANK_MAX_VALUE = 20;
    private final Document document;
    private final AmazonHelper helper;
    private final String pageUrl;

    public AmazonNewNavigationStep(AmazonRouterStep parent) {
        super(parent.getQuery());
        this.document = parent.getDocument();
        this.pageUrl = parent.getPageUrl();
        this.helper = parent.getHelper();
    }

    static boolean isResponsible(Document document) {
        return !document.select("div#zg li.zg-item-immersion").isEmpty();
    }

    @Override
    public void run() {

        String currentCategory = helper.getCategory();
        String categoryLink = helper.getCategoryLink();

        if (StringUtils.isBlank(currentCategory)) {
            currentCategory = extractCurrentCategory();
            categoryLink = substringBeforeRefPath(pageUrl);
        }

        List<Step> nextSteps = new ArrayList<>();

        for (Element productWrapper : document.select("li.zg-item-immersion")) {

            String productLink = extractProductLink(productWrapper);
            String productAsin = AmazonProductStep.extractAsinFromPageUriStr(productLink);
            int productRank = extractProductRank(productWrapper);

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
                    nextSteps.add(new AmazonRouterStep(settings, getQuery(), amazonHelper));
                }
            }
        }

        if (getQuery().containsMode(AmazonConstants.QueryModes.CATEGORY_SUBTREES)) {

            for (String nextCategoryLink : AmazonNavigationStep.extractNextCategoriesLinks(document, pageUrl, currentCategory)) {
                WebRequestSettings settings = helper.createWebRequestSettings(nextCategoryLink, pageUrl);
                nextSteps.add(new AmazonRouterStep(settings, getQuery(), new AmazonHelper()));
            }
        }

        addNextSteps(nextSteps);
    }

    private String extractProductLink(Element element) {
        return extractFirstMatchElementAttribute(element, "span.aok-inline-block.zg-item a.a-link-normal[href]", HtmlConstants.Attributes.HREF);
    }

    private String extractCurrentCategory() {
        return extractFirstMatchElementText(document, "ul#zg_browseRoot span.zg_selected").trim();
    }

    private int extractProductRank(Element productWrapper) {
        String prodRankStr = removeNonDigits(extractFirstMatchElementText(productWrapper, "span.zg-badge-text"));
        return StringUtils.isNotBlank(prodRankStr) ? Integer.parseInt(prodRankStr) : 0;
    }
}