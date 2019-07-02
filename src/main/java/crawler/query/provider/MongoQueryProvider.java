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

@Slf4j
@Component
@RequiredArgsConstructor
public class MongoQueryProvider implements QueryProvider {

    private final QueryRepository queryRepository;
    private final MongoQueryMapper mongoQueryMapper;

    @Override
    public Optional<Query> getNextQuery() {

        return queryRepository.findByQueryStatus(QueryStatus.PENDING)
                .sorted(Comparator.comparing(MongoQuery::getCreatedAt))
                .map(mongoQueryMapper::map)
                .findFirst();
    }

    @Override
    @Transactional
    public boolean containsQuery(Query query) {
        Optional<MongoQuery> mongoQueryOpt = queryRepository.findById(query.getId());
        return mongoQueryOpt.filter(mongoQuery -> mongoQuery.getQueryStatus() == QueryStatus.PENDING).isPresent();
    }

    @Override
    @Transactional
    public boolean addToQueue(core.Query query) {

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
