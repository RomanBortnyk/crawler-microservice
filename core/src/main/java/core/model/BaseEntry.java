package core.model;

import core.Query;
import lombok.Getter;
import lombok.ToString;

import java.time.Clock;
import java.time.LocalDateTime;

@Getter
@ToString
public class BaseEntry<T> implements Entry<T> {

    private final Query query;
    private final LocalDateTime createdAt;
    private final T content;

    public BaseEntry(Query query, T content) {
        this.query = query;
        this.createdAt = LocalDateTime.now(Clock.systemUTC());
        this.content = content;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
