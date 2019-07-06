package crawler.parsers.spring;

import core.Query;
import core.model.BaseEntry;
import core.step.BaseStep;
import crawler.parsers.spring.dto.Article;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.stream.Collectors;


public class SpringProjectsPage extends BaseStep {

    Document document;

    public SpringProjectsPage(Query query, Document document) {
        super(query);
        this.document = document;
    }

    @Override
    public void run() {

        List<BaseEntry<Article>> collect = document.select("div.project--title").stream()
                .map(Element::text)
                .map(Article::new)
                .map(a -> new BaseEntry<>(getQuery(), a))
                .collect(Collectors.toList());

        getExecutionResult().getEntries().addAll(collect);

    }
}
