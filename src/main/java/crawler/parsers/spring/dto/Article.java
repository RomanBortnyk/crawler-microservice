package crawler.parsers.spring.dto;

import lombok.Setter;
import lombok.ToString;

@Setter
@ToString
public class Article {
    private String title;
    private String text;
    private int views = 25;

    public Article(String title) {
        this.title = title;
    }
}
