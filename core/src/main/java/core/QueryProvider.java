package core;

import java.util.Optional;

public interface QueryProvider {

    Optional<Query> getNextQuery();

    /**
     * Checks whether query in execution queue
     * @param query
     * @return
     */
    boolean containsQuery(Query query);

    /**
     * Adds query to queries queue if same query is not already present
     *
     * @param query query to add
     */
    boolean addToQueue(Query query);

    void removeQueryFromQueue(String queryId);
}
