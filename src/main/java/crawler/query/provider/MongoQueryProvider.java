package crawler.query.provider;

import core.Query;
import core.QueryProvider;
import crawler.exception.RunningQueryException;
import crawler.query.QueryStatus;
import crawler.query.mapper.MongoQueryMapper;
import crawler.query.model.MongoQuery;
import crawler.repository.QueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class MongoQueryProvider implements QueryProvider {

    private final QueryRepository queryRepository;
    private final MongoQueryMapper mongoQueryMapper;

    private boolean firstStartup = true;

    @Override
    @Transactional
    public Optional<Query> getNextQuery() {

        // check do we have interrupted queries like after application crash
        if (firstStartup){
            Optional<MongoQuery> runningQueryOpt = queryRepository.findByQueryStatus(QueryStatus.RUNNING)
                    .findFirst();
            if (runningQueryOpt.isPresent()){
                return runningQueryOpt.map(mongoQueryMapper::map);
            }
            firstStartup = false;
        }

        Optional<MongoQuery> oldestPendingQueryOptional = queryRepository.findByQueryStatus(QueryStatus.PENDING)
                .min(Comparator.comparing(MongoQuery::getCreatedAt));

        if (oldestPendingQueryOptional.isPresent()){
            MongoQuery mongoQuery = oldestPendingQueryOptional.get();
            mongoQuery.setQueryStatus(QueryStatus.RUNNING);
            queryRepository.save(mongoQuery);
        }

        return oldestPendingQueryOptional
                .map(mongoQueryMapper::map);
    }

    @Override
    @Transactional
    public boolean containsQuery(Query query) {
        Optional<MongoQuery> mongoQueryOpt = queryRepository.findById(query.getId());
        return mongoQueryOpt.filter(mongoQuery -> mongoQuery.getQueryStatus() == QueryStatus.PENDING).isPresent();
    }

    @Override
    @Transactional
    public boolean addToQueue(Query query) {

        checkQueryAlreadyRunning(query);

        MongoQuery mongoQuery = new MongoQuery(query, QueryStatus.PENDING, LocalDateTime.now());

        queryRepository.save(mongoQuery);

        return true;
    }

    @Override
    @Transactional
    public void removeQueryFromQueue(String queryId) {

        Optional<MongoQuery> queryOptional = queryRepository.findById(queryId);

        if (queryOptional.isPresent()){
            MongoQuery mongoQuery = queryOptional.get();
            mongoQuery.setQueryStatus(QueryStatus.FINISHED);

            queryRepository.save(mongoQuery);
        }
    }

    private void checkQueryAlreadyRunning(Query query){

        if (containsQuery(query)){
            throw new RunningQueryException("Query "+ query +" is already running");
        }

    }
}
