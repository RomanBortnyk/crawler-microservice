package core.annotation.processor;

import core.annotation.Parser;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class StartupParserClassesContainer {

    private final Map<String, Class<?>> startupClasses;

    public StartupParserClassesContainer(String parsersPackagePath) {
        startupClasses = new HashMap<>();

        Reflections reflections = new Reflections(parsersPackagePath);

        Set<Class<?>> parserStartupClasses =
                reflections.getTypesAnnotatedWith(core.annotation.Parser.class);

        parserStartupClasses.forEach(c -> {
            String name = c.getAnnotation(Parser.class).name();
            startupClasses.put(name, c);
        });

        if (parsersPackagePath.isEmpty()){
            log.warn("No parsers where found by path: " + parsersPackagePath);
        }
    }

    public Optional<Class<?>> getStartupClass(String parserName) {
        return Optional.ofNullable(startupClasses.get(parserName));
    }

}
