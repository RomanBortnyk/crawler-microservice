package crawler.query.mapper;

import core.Query;
import crawler.query.model.MongoQuery;
import org.springframework.stereotype.Component;

@Component
public class MongoQueryMapper {

    public Query map(MongoQuery mongoQuery){
        return new Query(mongoQuery.getParserName(), mongoQuery.getModes(), mongoQuery.getParameters());
    }

}
