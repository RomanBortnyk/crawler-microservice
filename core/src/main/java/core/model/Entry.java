package core.model;

import core.Query;

import java.time.LocalDateTime;

public interface Entry<T> {

    Query getQuery();

    LocalDateTime getCreatedAt();

    T getContent();
}
