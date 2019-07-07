package common.util;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.internal.JsonContext;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class JsonPathWrapper {
    private static Configuration CONFIGURATION = config();

    private JsonPathWrapper() {
        // hide utility class constructor
    }

    public static DocumentContext parse(Object obj) {
        return new JsonContext(CONFIGURATION).parse(obj);
    }

    public static JsonContext parse(String input) {
        return (JsonContext) new JsonContext(CONFIGURATION).read(input);
    }

    public static DocumentContext read(DocumentContext context, String jsonPath) {
        return parse(context.read(jsonPath));
    }

    private static Configuration config() {
        return Configuration.defaultConfiguration().jsonProvider(jsonProvider()).mappingProvider(new JacksonMappingProvider()).addOptions(Option.SUPPRESS_EXCEPTIONS);
    }

    private static JsonProvider jsonProvider() {
        return new JacksonJsonProvider();
    }
}
