package crawler.parsers.amazon;

import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.internal.JsonContext;
import common.util.JsonPathWrapper;
import common.util.ParserUtil;
import core.WebRequestSettings;
import core.model.BaseEntry;
import core.step.BaseStep;
import crawler.parsers.amazon.dto.Product;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static common.util.HtmlConstants.Attributes;
import static common.util.ParserUtil.extractFirstMatchElementAttribute;
import static common.util.ParserUtil.extractFirstMatchElementOwnText;
import static common.util.ParserUtil.extractFirstMatchElementText;
import static common.util.ParserUtil.extractNumber;
import static common.util.ParserUtil.getUrlParam;
import static common.util.ParserUtil.normalizeText;
import static common.util.ParserUtil.removeNonDigits;
import static common.util.ParserUtil.substringBeforeRefPath;

public class AmazonProductStep extends BaseStep {

    private static final Logger log = LoggerFactory.getLogger(AmazonProductStep.class);
    private static final Pattern asinPattern = Pattern.compile("[A-Z0-9]{10}");

    private final Document document;
    private final String pageUrl;
    private final AmazonHelper helper;

    AmazonProductStep(AmazonRouterStep parent) {
        super(parent.getQuery());
        this.document = parent.getDocument();
        this.pageUrl = parent.getPageUrl();
        this.helper = parent.getHelper();
    }

    static boolean isResponsible(Document document) {
        return !document.select("div#dp").isEmpty();
    }

    static String extractAsinFromPageUriStr(String pageUriStr) {
        String preparedString = StringUtils.substringAfter(pageUriStr, "/dp/");

        if (pageUriStr.contains("/gp/")) {
            preparedString = StringUtils.substringAfter(pageUriStr, "/gp/");
        }

        if (StringUtils.isNotBlank(preparedString)) {

            Matcher m = asinPattern.matcher(preparedString);

            if (m.find()) {
                return m.group(0);
            }
        }
        return StringUtils.EMPTY;
    }

    public static Element extractSimilarFbtsBlock(Document document) {

        for (Element element : document.select("div[data-a-carousel-options]")) {
            String heading = normalizeText(extractFirstMatchElementText(element, "h2.a-carousel-heading"));

            if (element.select("div#purchase-sims-feature").size() > 0
                    || heading.contains("Customers who bought this item also bought")
                    || heading.contains("Customers also shopped for")) {
                return element;
            }
        }

        return null;
    }

    public static List<String> extractFbtLinksFromFbtBlock(Document document) {

        return document.select("div#sims-fbt-content li[class*='sims-fbt-image'] a.a-link-normal").stream()
                .map(e -> e.attr(Attributes.HREF))
                .collect(Collectors.toList());
    }

    public static boolean isUnavailable(Document document) {
        return extractFirstMatchElementText(document, "div#availability").contains("Currently unavailable");
    }

    @Override
    public void run() {

        final String currentAsin = extractAsinFromPageUriStr(pageUrl);

        if (StringUtils.isBlank(currentAsin)) {
            log.warn("Product was not extracted. Asin is empty");
            return;
        }

        Product product = new Product();

        if (isParentProduct()) {
            determineCategoryInfoAndRankForParentProduct(product);
            createNextStepsForFbtProducts(currentAsin, product);
        } else {
            determineCategoryInfoAndRankForFbtProduct(product);
            product.setFbtNumber(helper.getFbtNumber());
            product.setParentAsin(helper.getParentAsin());
        }

        final String mainCategoryStr = extractMainCategoryStr();

        product.setProductName(extractProductName());
        product.setBrand(extractBrand());
        product.setPrice(extractPrice());
        product.setShopUrl(substringBeforeRefPath(pageUrl));
        product.setRating(extractRating());
        product.setRatingCount(extractRatingCount());
        product.setAsin(currentAsin);
        product.setMainCategory(extractMainCategory(mainCategoryStr));
        product.setMainCategoryRank(extractMainCategoryRank(mainCategoryStr));
        product.setIsFbt(!isParentProduct());
        product.setFulfillment(extractFulfillment());
        product.setImageUrl(extractImageUrl());

        getExecutionResult().getEntries().add(new BaseEntry<>(getQuery(), product));

        AmazonHelper.addExtractedAsin(product);
    }

    private Integer extractMainCategoryRank(String mainCategoryStr) {
        String rankStr = StringUtils.substringBefore(mainCategoryStr, "in");
        rankStr = ParserUtil.removeNonDigits(rankStr);

        return StringUtils.isNumeric(rankStr) ? Integer.parseInt(rankStr) : null;
    }

    private String extractImageUrl() {
        String jsonStr = extractFirstMatchElementAttribute(document, "div#imgTagWrapperId img#landingImage", "data-a-dynamic-image");

        if (StringUtils.isBlank(jsonStr)) {
            jsonStr = extractFirstMatchElementAttribute(document, "div#img-canvas img[data-a-dynamic-image]", "data-a-dynamic-image");
        }

        try {
            JsonContext context = JsonPathWrapper.parse(jsonStr);
            Map<String, ?> map = context.read("$", new TypeRef<Map<String, ?>>() {
            });
            return map.keySet().stream().findFirst().orElse(StringUtils.EMPTY);
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    private String extractFulfillment() {
        final Element dpContainer = document.getElementById("dp-container");
        Supplier<String> merchantStrSplr1 = () -> extractFirstMatchElementText(dpContainer, "div#merchant-info");
        Supplier<String> merchantStrSplr2 = () -> extractFirstMatchElementText(dpContainer, "span#merchant-info");
        Supplier<String> merchantStrSplr3 = () -> extractFirstMatchElementText(dpContainer, "div[id*=pantry-availability]");
        Supplier<String> availabilityStrSplr1 = () -> extractFirstMatchElementText(dpContainer, "span#availability");
        Supplier<String> availabilityStrSplr2 = () -> extractFirstMatchElementText(dpContainer, "div#availability");

        Optional<String> fulfillmentStrOptional = chooseFirstByPredicate(
                Stream.of(merchantStrSplr1, merchantStrSplr2, merchantStrSplr3, availabilityStrSplr1, availabilityStrSplr2), StringUtils::isNotBlank);

        if (fulfillmentStrOptional.isPresent()) {
            String fulfillmentStr = fulfillmentStrOptional.get();

            if (fulfillmentStr.contains("fulfilled by Amazon")) return "FBA";
            if (fulfillmentStr.contains("sold by Amazon")) return "Amazon";

            boolean isFBM = fulfillmentStr.contains("sold by")
                    || (fulfillmentStr.contains("Available from") && fulfillmentStr.contains("these sellers"));

            if (isFBM) return "FBM";
        }

        return StringUtils.EMPTY;
    }

    private List<String> extractFbtAsinsFromAlsoBoughtBlock(Element simsFbtBlock) {

        if (simsFbtBlock == null) return Collections.emptyList();

        String jsonStr = extractFirstMatchElementAttribute(simsFbtBlock, "div[data-a-carousel-options]", "data-a-carousel-options");

        try {
            JsonContext jsonContext = JsonPathWrapper.parse(jsonStr);
            return jsonContext.read("$.ajax.id_list[*]", new TypeRef<List<String>>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String extractMainCategoryStr() {

        Supplier<String> mainCatSupplier1 = () -> extractFirstMatchElementOwnText(document, "li#SalesRank");
        Supplier<String> mainCatSupplier2 = () -> extractFirstMatchElementOwnText(document, "div#prodDetails tr#SalesRank td.value");
        Supplier<String> mainCatSupplier3 = () -> {
            for (Element spanElement : document.select("table[id*='productDetails'] td span span")) {
                if (spanElement.text().toLowerCase().contains("see top 100")) {
                    return spanElement.ownText();
                }
            }
            return StringUtils.EMPTY;
        };

        Stream<Supplier<String>> supplierStream = Stream.of(mainCatSupplier1, mainCatSupplier2, mainCatSupplier3);
        return chooseFirstByPredicate(supplierStream, StringUtils::isNotBlank).orElse(StringUtils.EMPTY);
    }

    private String extractMainCategory(String mainCategoryStr) {
        String mainCategory = StringUtils.substringAfter(mainCategoryStr, "in");
        mainCategory = mainCategory.replaceAll("\\)", "");
        mainCategory = mainCategory.replaceAll("\\(", "");
        return normalizeText(mainCategory);
    }

    private Integer extractRatingCount() {

        String ratingCountStr = removeNonDigits(extractFirstMatchElementText(document, "span#acrCustomerReviewText"));

        return StringUtils.isNumeric(ratingCountStr) ? Integer.parseInt(ratingCountStr) : null;
    }

    private Double extractRating() {

        String ratingStr = extractFirstMatchElementAttribute(document, "span[class^='reviewCountTextLinkedHistogram']", Attributes.TITLE);

        ratingStr = StringUtils.substringBefore(ratingStr, "out")
                .trim();

        return StringUtils.isBlank(ratingStr) ? null : Double.valueOf(ratingStr);
    }

    private Double extractPrice() {
        final Element dpDiv = document.getElementById("dp");
        if (dpDiv == null) return null;

        Supplier<String> priceSupplier1 = () -> extractFirstMatchElementText(dpDiv, "span#priceblock_ourprice");
        Supplier<String> priceSupplier2 = () -> extractFirstMatchElementText(dpDiv, "span#priceblock_saleprice");
        Supplier<String> priceSupplier3 = () -> extractFirstMatchElementText(dpDiv, "span#priceblock_dealprice");
        Supplier<String> priceSupplier4 = () -> extractFirstMatchElementText(dpDiv, "div#toggleBuyBox span.a-color-price");
        Supplier<String> priceSupplier5 = () -> {
            // products available from "these sellers"
            String priceStr = extractFirstMatchElementText(dpDiv, "div#olp_feature_div a[href*='offer-listing']");
            return StringUtils.substringAfter(priceStr, "from");
        };
        Supplier<String> priceSupplier6 = () -> extractFirstMatchElementText(dpDiv, "div.sims-fbt-rows li[data-p13n-asin-metadata] div.sims-fbt-checkbox-label span.p13n-sc-price");
        Supplier<String> priceSupplier7 = () -> extractFirstMatchElementAttribute(dpDiv, "div#cerberus-data-metrics", "data-asin-price");
        Supplier<String> priceSupplier8 = () -> extractFirstMatchElementText(dpDiv, "div#guild-buybox-container span[class*='guild_priceblock_ourprice']");

        Stream<Supplier<String>> priceSuppliers = Stream.of(priceSupplier1, priceSupplier2, priceSupplier3,
                priceSupplier4, priceSupplier5, priceSupplier6, priceSupplier7, priceSupplier8);

        String priceStr = chooseFirstByPredicate(priceSuppliers, StringUtils::isNotBlank).orElse(StringUtils.EMPTY);

        BigDecimal price = extractNumber(priceStr, ',', '.');

        return price == null ? null : price.doubleValue();
    }

    private String extractBrand() {

        Supplier<String> brandSupplier1 = () -> extractFirstMatchElementText(document, "a#brand");
        Supplier<String> brandSupplier2 = () -> extractFirstMatchElementAttribute(document, "div#mbc", "data-brand");
        Supplier<String> brandSupplier3 = () -> extractFirstMatchElementText(document, "div#dp a#bylineInfo");
        Supplier<String> brandSupplier4 = () -> {

            String brandUrl = extractFirstMatchElementAttribute(document, "a#brand", Attributes.HREF);
            Optional<String> brandStrOptional = getUrlParam(brandUrl, "field-lbr_brands_browse-bin");
            try {
                if (brandStrOptional.isPresent()) return URLDecoder.decode(brandStrOptional.get(), "UTF-8");
            } catch (Exception e) {
                log.warn("Error during decoding url while extracting brand");
            }
            return StringUtils.EMPTY;
        };

        Stream<Supplier<String>> suppliersStream = Stream.of(brandSupplier1, brandSupplier2, brandSupplier3, brandSupplier4);

        return chooseFirstByPredicate(suppliersStream, StringUtils::isNotBlank)
                .orElse(StringUtils.EMPTY);
    }

    private String extractProductName() {
        Supplier<String> prodNameSupplier1 = () -> extractFirstMatchElementText(document, "span#productTitle");
        Supplier<String> prodNameSupplier2 = () -> extractFirstMatchElementAttribute(document, "div#dp-container div#imgTagWrapperId img", Attributes.ALT);
        Supplier<String> prodNameSupplier3 = () -> extractFirstMatchElementText(document, "div#giveaway div[class*='giveaway-product-title'] span");

        Stream<Supplier<String>> suppliersStream = Stream.of(prodNameSupplier1, prodNameSupplier2, prodNameSupplier3);

        String productName = chooseFirstByPredicate(suppliersStream, StringUtils::isNotBlank)
                .orElse(StringUtils.EMPTY);

        return normalizeText(productName);
    }

    private <T> Optional<T> chooseFirstByPredicate(Stream<Supplier<T>> suppliers, Predicate<T> predicate) {

        return suppliers
                .map(Supplier::get)
                .filter(predicate)
                .findFirst();
    }

    private void determineCategoryInfoAndRankForFbtProduct(Product product) {

        String category = StringUtils.EMPTY;
        String categoryLink = StringUtils.EMPTY;
        String rank = StringUtils.EMPTY;

        List<Element> categoriesPathsElements = document.select("li#SalesRank li.zg_hrsr_item");

        if (categoriesPathsElements.isEmpty()) {
            categoriesPathsElements = document.select("tr#SalesRank li.zg_hrsr_item");
        }

        if (!categoriesPathsElements.isEmpty()) {
            Element firstLiElement = categoriesPathsElements.get(0);
            rank = removeNonDigits(extractFirstMatchElementText(firstLiElement, "span.zg_hrsr_rank"));
            List<Element> categories = firstLiElement.select("span.zg_hrsr_ladder a");

            if (!categories.isEmpty()) {
                Element lastCategoryATagElement = categories.get(categories.size() - 1);
                category = normalizeText(lastCategoryATagElement.text());
                categoryLink = lastCategoryATagElement.attr(Attributes.HREF);
            }

            product.setCategory(category);
            product.setCategoryRank(StringUtils.isNumeric(rank) ? Integer.parseInt(rank) : null);
            product.setCategoryLink(createCategoryLink(categoryLink));

            return;
        }

        for (Element tr : document.select("table[id^='productDetails'] tr")) {

            if (!tr.text().contains("Best Sellers Rank")) continue;

            for (Element span : tr.select("td span span")) {

                if (span.text().contains("See Top 100")) continue;

                List<Element> categoriesHrefs = span.select("a");

                if (categoriesHrefs.isEmpty()) continue;

                Element lastCategoryATagElement = categoriesHrefs.get(categoriesHrefs.size() - 1);

                category = normalizeText(lastCategoryATagElement.text());

                rank = ParserUtil.removeNonDigits(span.ownText());

                categoryLink = lastCategoryATagElement.attr(Attributes.HREF);

                product.setCategory(category);
                product.setCategoryRank(StringUtils.isNumeric(rank) ? Integer.parseInt(rank) : null);
                product.setCategoryLink(createCategoryLink(categoryLink));

                return;
            }
        }
    }

    private String createCategoryLink(String relativeUrl) {
        String categoryLink = substringBeforeRefPath(relativeUrl);

        if (categoryLink.contains(AmazonHelper.BASE_URL)) {
            return categoryLink;
        }

        return AmazonHelper.BASE_URL + categoryLink;
    }

    private List<String> extractElementsStartFromIndex(List<String> list, int fromIndex, int numberOfElements) {

        int toIndex = fromIndex + numberOfElements;

        if (list == null
                || fromIndex < 0
                || fromIndex >= list.size()
                || numberOfElements < 1
                || toIndex >= list.size()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(list.subList(fromIndex, toIndex));
    }

    private String buildProductUrlFromAsin(String asin) {

        if (StringUtils.isNotBlank(asin)) {

            Matcher m = asinPattern.matcher(asin);

            if (m.find()) {
                return String.format("%s/dp/%s", AmazonHelper.BASE_URL, m.group(0));
            }
        }

        return StringUtils.EMPTY;
    }

    /**
     * Extracts links for fbt product for current parent. Creates next steps for this fbts.
     * Put rest fbt asin values to parent product
     *
     * @param parentAsin current parent asin
     * @param product    parent product object
     */
    private void createNextStepsForFbtProducts(String parentAsin, Product product) {

        List<String> fbtBlockLinks = extractFbtLinksFromFbtBlock(document);

        List<String> asinsFromAlsoBoughtBlock = extractFbtAsinsFromAlsoBoughtBlock(extractSimilarFbtsBlock(document));

        // remove fbt block asins from alsoBoughtBlock to avoid duplication
        for (String link : fbtBlockLinks) {
            String asin = extractAsinFromPageUriStr(link);
            asinsFromAlsoBoughtBlock.remove(asin);
        }

        List<String> resultFbtLinks = new ArrayList<>(fbtBlockLinks);

        // TODO ajax request to similar fbt block if absent

        if (resultFbtLinks.size() == 1) {

            // add first asin from also bought block if exists
            if (asinsFromAlsoBoughtBlock.size() > 0) {
                resultFbtLinks.add(buildProductUrlFromAsin(asinsFromAlsoBoughtBlock.get(0)));
            }

            List<String> restFbts = extractElementsStartFromIndex(asinsFromAlsoBoughtBlock, 1, 3);
            product.setRestFbt(StringUtils.join(restFbts, ", "));

        } else if (resultFbtLinks.size() == 0) {

            // add first two asins from also bought block
            Iterator<String> asinsIter = asinsFromAlsoBoughtBlock.iterator();

            while (resultFbtLinks.size() != 2 && asinsIter.hasNext()) {
                resultFbtLinks.add(buildProductUrlFromAsin(asinsIter.next()));
            }

            List<String> restFbts = extractElementsStartFromIndex(asinsFromAlsoBoughtBlock, 2, 3);
            product.setRestFbt(StringUtils.join(restFbts, ", "));
        }

        for (int i = 1; i <= resultFbtLinks.size(); i++) {

            String fbtLink = resultFbtLinks.get(i);
            String fbtAsin = extractAsinFromPageUriStr(fbtLink);

            if (AmazonHelper.isExtractedAsParent(fbtAsin) || AmazonHelper.isExtractedAsFbt(fbtAsin)) {

                Product partialProduct = new Product();

                partialProduct.setAsin(fbtAsin);
                partialProduct.setFbtNumber(i);
                partialProduct.setParentAsin(parentAsin);
                partialProduct.setIsFbt(true);

                getExecutionResult().getEntries().add(new BaseEntry<>(getQuery(), partialProduct));

            } else {

                AmazonHelper amazonHelper = new AmazonHelper(helper);
                amazonHelper.setParentAsin(parentAsin);
                amazonHelper.setFbtNumber(i);

                if (StringUtils.isNotBlank(fbtLink)) {
                    WebRequestSettings settings = amazonHelper.createWebRequestSettings(fbtLink, pageUrl);
                    addNextSteps(new AmazonRouterStep(settings, getQuery(), amazonHelper));
                } else {
                    log.warn("Fbt link is blank. Parent product: " + pageUrl);
                }
            }
        }
    }

    private boolean isParentProduct() {
        return StringUtils.isBlank(helper.getParentAsin());
    }

    private void determineCategoryInfoAndRankForParentProduct(Product product) {
        product.setCategory(helper.getCategory());
        product.setCategoryRank(helper.getProductRankInCategory());
        product.setCategoryLink(helper.getCategoryLink());
    }
}
