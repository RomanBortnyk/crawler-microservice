package crawler.query.provider;

import core.Query;
import core.QueryProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


@Slf4j
//@Component
public class InMemoryQueryProvider implements QueryProvider {

    private Queue<Query> queriesQueue = new ConcurrentLinkedQueue<>();

    @Override
    public Optional<Query> getNextQuery() {
        return Optional.ofNullable(queriesQueue.poll());
    }

    @Override
    public boolean containsQuery(Query query) {
        return queriesQueue.contains(query);
    }

    @Override
    public boolean addToQueue(Query query) {

        if (containsQuery(query)) {
            log.info("Queries queue already has query: " + query);
            return false;
        }

        queriesQueue.add(query);
        log.info("Query was added to execution queue: " + query);

        return true;
    }

    @Override
    public void removeQueryFromQueue(String queryId) {

        queriesQueue.stream()
                .filter(q -> q.getId().equals(queryId))
                .findFirst()
                .ifPresent(q -> {
                    queriesQueue.remove(q);
                    log.info("Query with id " + queryId + " was removed from queue");
                });
    }
}
