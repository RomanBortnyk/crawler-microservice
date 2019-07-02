package core;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(of = "id")
@Getter
public class Query {

    private final String parserName;
    private final String id;
    private final List<String> modes;
    private final Map<String, String> parameters;

    public Query(String parserName, List<String> modes, Map<String, String> params) {
        this.id = createQueryId(parserName, modes, params);
        this.modes = modes;
        this.parameters = params;
        this.parserName = parserName;
    }

    public boolean containsMode(String mode) {
        return modes.contains(mode);
    }

    private String createQueryId(String parserName,
                                 List<String> modes,
                                 Map<String, String> params) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(parserName);

        modes.forEach(stringBuilder::append);

        params.forEach((key, value) -> {
            stringBuilder.append(key);
            stringBuilder.append(value);
        });

        return UUID.nameUUIDFromBytes(stringBuilder.toString().getBytes()).toString();
    }

    @Override
    public String toString() {
        return "Query{" +
                "modes=" + modes +
                ", parameters=" + parameters +
                ", id='" + id + '\'' +
                ", parserName='" + parserName + '\'' +
                '}';
    }
}
