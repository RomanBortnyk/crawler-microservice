package crawler.parsers.amazon;

import core.WebRequestSettings;
import crawler.parsers.amazon.dto.Product;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class AmazonHelper {

    static final String BASE_URL = "https://www.amazon.com";

    private static final String PARENT_KEY = "p";
    private static final String FBT_KEY = "f";
    private static final Map<String, Set<String>> extractedAsins = initMap();

    private String category;
    private String categoryLink;
    private Integer productRankInCategory;
    private Integer fbtNumber;
    private String parentAsin;

    AmazonHelper(AmazonHelper toCopy) {
        category = toCopy.getCategory();
        categoryLink = toCopy.getCategoryLink();
        productRankInCategory = toCopy.getProductRankInCategory();
        fbtNumber = toCopy.getFbtNumber();
        parentAsin = toCopy.getParentAsin();
    }

    AmazonHelper() {
    }

    private static Map<String, Set<String>> initMap() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put(PARENT_KEY, Collections.newSetFromMap(new ConcurrentHashMap<>()));
        map.put(FBT_KEY, Collections.newSetFromMap(new ConcurrentHashMap<>()));
        return map;
    }

    static boolean isExtractedAsParent(String asin) {
        return extractedAsins.get(PARENT_KEY).contains(asin);
    }

    static boolean isExtractedAsFbt(String asin) {
        return extractedAsins.get(FBT_KEY).contains(asin);
    }

    static void clearExtractedAsinsMap() {
        extractedAsins.values().forEach(Set::clear);
    }

    /**
     * Adds asin to extracted asins set to avoid request to it again
     */
    public static void addExtractedAsin(Product product) {

        boolean isParent = !product.getIsFbt();
        if (isParent) {
            extractedAsins.get(PARENT_KEY).add(product.getAsin());
        } else {
            extractedAsins.get(FBT_KEY).add(product.getAsin());
        }
    }

    private String expandPathToUrl(String url) {
        if (url.startsWith("/")) {
            return BASE_URL + url;
        }

        return url;
    }

    WebRequestSettings createWebRequestSettings(String url, String referer) {

        WebRequestSettings settings = new WebRequestSettings(expandPathToUrl(url));

        Map<String, String> header = new HashMap<>();

        header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        header.put("Accept-Encoding", "gzip, deflate, sdch, br");
        header.put("Accept-Language", "en-US,en;q=0.8,ru;q=0.6");
        header.put("Connection", "keep-alive");
        header.put("Host", "www.amazon.com");
        header.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");

        if (StringUtils.isNotBlank(referer)) {
            header.put("Referer", referer);
        }

        settings.setConnectionTimeout(10000);

        settings.setRequestHeader(header);

        return settings;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryLink() {
        return categoryLink;
    }

    public void setCategoryLink(String categoryLink) {
        this.categoryLink = categoryLink;
    }

    public Integer getProductRankInCategory() {
        return productRankInCategory;
    }

    public void setProductRankInCategory(Integer productRankInCategory) {
        this.productRankInCategory = productRankInCategory;
    }

    public Integer getFbtNumber() {
        return fbtNumber;
    }

    public void setFbtNumber(Integer fbtNumber) {
        this.fbtNumber = fbtNumber;
    }

    public String getParentAsin() {
        return parentAsin;
    }

    public void setParentAsin(String parentAsin) {
        this.parentAsin = parentAsin;
    }
}
