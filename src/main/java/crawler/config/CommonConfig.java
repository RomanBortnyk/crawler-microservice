package crawler.config;

import core.annotation.processor.StartupParserClassesContainer;
import core.config.CoreConfiguration;
import core.executor.StepsExecutor;
import core.model.BaseEntry;
import core.model.Entry;
import core.service.EntriesPersistenceService;
import crawler.parsers.spring.model.Article;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collection;

@Configuration
@Import(CoreConfiguration.class)
public class CommonConfig {

    @Bean
    public StartupParserClassesContainer startupParserClassesContainer() {
        return new StartupParserClassesContainer("crawler.parsers");
    }

    @Bean
    public EntriesPersistenceService entriesPersistenceService() {
        return new Persistent();
    }

    @Bean
    public StepsExecutor stepsExecutor(){
        return new core.executor.StepsExecutor();
    }

    private class Persistent implements EntriesPersistenceService {

        public void persistResults(Collection<BaseEntry> entries) {

            for (Entry entry: entries){
                if (entry.getContent() instanceof Article){
                    Article article = (Article) entry.getContent();
                    System.out.println("article: " + article);
                }
            }

        }

    }
}
