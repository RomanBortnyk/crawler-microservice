package crawler.query;

import core.Query;
import crawler.exception.RunningQueryException;
import crawler.query.mapper.MongoQueryMapper;
import crawler.query.model.MongoQuery;
import crawler.query.provider.MongoQueryProvider;
import crawler.repository.QueryRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MongoQueryProviderTest {

    @Mock
    private QueryRepository queryRepository;
    @Mock
    private MongoQueryMapper mongoQueryMapper;

    @InjectMocks
    private MongoQueryProvider queryProvider;

    private Query query;

    @Before
    public void init(){
        query = new Query("test", new ArrayList<>(), new HashMap<>());

    }

    @Test
    public void shouldNotThrowException__WhenQueryPresentAsRunning() {
        Optional<MongoQuery> mongoQueryOptional = Optional.of(new MongoQuery(query, QueryStatus.RUNNING, LocalDateTime.now()));
        when(queryRepository.findById(Mockito.anyString())).thenReturn(mongoQueryOptional);

        queryProvider.addToQueue(query);
    }

    @Test(expected = RunningQueryException.class)
    public void shouldThrowException__WhenQueryPresentAsPending() {
        Optional<MongoQuery> mongoQueryOptional = Optional.of(new MongoQuery(query, QueryStatus.PENDING, LocalDateTime.now()));
        when(queryRepository.findById(Mockito.anyString())).thenReturn(mongoQueryOptional);

        queryProvider.addToQueue(query);
    }

    @Test
    public void shouldNotThrowException__WhenQueryIsAbsent() {
        Optional<MongoQuery> mongoQueryOptional = Optional.of(new MongoQuery(query, QueryStatus.RUNNING, LocalDateTime.now()));
        mongoQueryOptional.get().setId("123");

        when(queryRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
        queryProvider.addToQueue(query);
    }


    @Test
    public void shouldSelectOldestQuery(){

        Query expectedQuery = new Query("expected", new ArrayList<>(), new HashMap<>());

        MongoQuery mongoQueryPending1 = new MongoQuery(expectedQuery, QueryStatus.PENDING, LocalDateTime.now());
        MongoQuery mongoQueryPending2 = new MongoQuery(query, QueryStatus.PENDING, LocalDateTime.now().plusDays(1));

        Stream<MongoQuery> stream = Stream.of(mongoQueryPending2, mongoQueryPending1);

        when(queryRepository.findByQueryStatus(any(QueryStatus.class))).thenReturn(stream);
        when(mongoQueryMapper.map(any(MongoQuery.class))).thenCallRealMethod();

        Optional<Query> oldestQuery = queryProvider.getNextQuery();

        Assert.assertTrue(oldestQuery.isPresent());
        Assert.assertEquals(expectedQuery, oldestQuery.get());
    }


}