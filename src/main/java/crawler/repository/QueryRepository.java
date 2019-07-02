package crawler.repository;

import crawler.query.model.MongoQuery;
import crawler.query.QueryStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.stream.Stream;

public interface QueryRepository extends MongoRepository<MongoQuery, String> {

//    List<MongoQuery> findByQueryStatus(QueryStatus queryStatus);

    Stream<MongoQuery> findByQueryStatus(QueryStatus queryStatus);

}
