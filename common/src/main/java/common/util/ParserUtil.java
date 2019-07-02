package common.util;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;


public class ParserUtil {

    private static final Pattern NON_DIGITS_PATTERN = Pattern.compile("\\D+");

    public static String removeNonDigits(String s) {
        return NON_DIGITS_PATTERN.matcher(s).replaceAll(StringUtils.EMPTY);
    }

    public static String extractFirstMatchElementText(Element element, String cssPath) {
        return selectFirstMatchElement(element, cssPath)
                .map(Element::text)
                .orElse(StringUtils.EMPTY);
    }

    private static Optional<Element> selectFirstMatchElement(Element element, String cssPath) {
        return selectElement(element, cssPath, 0);
    }

    private static Optional<Element> selectElement(Element element, String cssPath, int index) {
        if (element == null){
            throw new IllegalArgumentException("Element should not be null");
        }
        if (cssPath == null){
            throw new IllegalArgumentException("Css path should not be null");
        }

        Elements elements = element.select(cssPath);

        if (index >= elements.size()) {
            return Optional.empty();
        }

        return Optional.of(elements.get(index));
    }

    public static String extractFirstMatchElementOwnText(Element element, String cssPath) {
        return selectFirstMatchElement(element, cssPath)
                .map(Element::ownText)
                .orElse(StringUtils.EMPTY);
    }

    public static String extractElementText(Element element, String cssPath, int index) {
        return selectElement(element, cssPath, index)
                .map(Element::text)
                .orElse(StringUtils.EMPTY);
    }

    public static String extractFirstMatchElementAttribute(Element element, String cssPath, String attr) {
        return selectFirstMatchElement(element, cssPath)
                .map(e -> e.attr(attr))
                .orElse(StringUtils.EMPTY);
    }

    public static String normalizeText(String text) {
        if (StringUtils.isBlank(text)) {
            return StringUtils.EMPTY;
        }

        return text
                .replace("\r", " ")
                .replace("\n", " ")
                .replaceAll(" +", " ")
                .trim();
    }

    public static BigDecimal extractNumber(String inputString, Character thousandSeparator, Character decimalSeparator) {
        return extractNumber(inputString, thousandSeparator, decimalSeparator, null);
    }

    public static BigDecimal extractNumber(String inputString) {
        return extractNumber(inputString, new DecimalFormatSymbols(Locale.US));
    }

    public static BigDecimal extractNumber(String inputString, Locale locale) {
        return extractNumber(inputString, new DecimalFormatSymbols(locale));
    }

    private static BigDecimal extractNumber(String inputString, DecimalFormatSymbols symbols) {
        return extractNumber(inputString, symbols.getGroupingSeparator(), symbols.getDecimalSeparator());
    }

    private static BigDecimal extractNumber(String inputString, Character thousandSeparator, Character decimalSeparator, DecimalFormatSymbols symbols) {
        String pattern = null;
        BigDecimal bigDecimal = null;
        DecimalFormatSymbols localSymbols = null;

        if (symbols == null) {
            pattern = String.format(".*?((?:\\d{1,3}\\%1$s)*\\d+(?:\\%2$s\\d+)?).*", thousandSeparator, decimalSeparator);
            localSymbols = new DecimalFormatSymbols(new Locale("en", "US"));
            localSymbols.setGroupingSeparator(thousandSeparator);
            localSymbols.setDecimalSeparator(decimalSeparator);
        } else {
            localSymbols = symbols;
            pattern = String.format(".*?((?:\\d{1,3}\\%1$s)*\\d+(?:\\%2$s\\d+)?).*", symbols.getGroupingSeparator(), symbols.getDecimalSeparator());
        }

        if (StringUtils.isNoneEmpty(pattern, inputString)) {

            String rawNum = StringUtils.replacePattern(inputString, pattern, "$1");
            try {
                DecimalFormat f = new DecimalFormat();

                f.setDecimalFormatSymbols(localSymbols);
                f.setParseBigDecimal(Boolean.TRUE);

                BigDecimal parsed = (BigDecimal) f.parse(rawNum);
                if (parsed.scale() == 0) {
                    bigDecimal = parsed.setScale(2);
                } else {
                    return parsed;
                }

                return bigDecimal;
            } catch (ParseException exception) {
                return bigDecimal;
            }
        }

        return bigDecimal;
    }

    // TODO validate is string url or not
    public static Map<String, String> getUrlParams(String url) {
        Map<String, String> params = new HashMap<>();

        if (StringUtils.isBlank(url)) {
            return params;
        }

        String paramsString = StringUtils.substringAfter(url, "?");
        String[] paramsArr = paramsString.split("&");

        for (String param : paramsArr) {

            int paramSeparatorIndex = param.indexOf("=");

            if (paramSeparatorIndex != -1) {
                String paramName = param.substring(0, paramSeparatorIndex);
                String paramValue = param.substring(paramSeparatorIndex + 1, param.length());

                params.put(paramName, paramValue);
            }
        }
        return params;
    }

    public static Optional<String> getUrlParam(String url, String paramName) {
        String paramValue = getUrlParams(url).get(paramName);
        return Optional.ofNullable(paramValue);
    }

    public static String substringBeforeRefPath(String s) {
        return StringUtils.substringBefore(s, "ref=");
    }
}
