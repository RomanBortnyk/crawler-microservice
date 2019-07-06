package core.service;

import core.Query;
import core.QueryProvider;
import core.annotation.processor.StartupParserClassesContainer;
import core.model.BaseEntry;
import core.step.Step;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class QueryService {

    private final QueryProvider queryProvider;
    private final StartupParserClassesContainer startupParserClassesContainer;
    private final EntriesPersistenceService persistenceService;
    private final StepsProvider stepsProvider;

    private Map<String, Query> runningQueries = new ConcurrentHashMap<>();
    private Map<String, List<BaseEntry>> queriesResults = new ConcurrentHashMap<>();
    private Map<String, Set<String>> successfulRequestedUrls = new ConcurrentHashMap<>();

    /**
     * Starts passed query immediately, otherwise adds to queue
     * Rejects query if it already running or present in queue
     */
    public void startQuery() {

        boolean atLeastOneQueryIsRunning = !runningQueries.isEmpty();

        if (atLeastOneQueryIsRunning) {
            return;
        }

        Optional<Query> queryOpt = queryProvider.getNextQuery();
        if (!queryOpt.isPresent()) return;

        Query query = queryOpt.get();

        Optional<Class<?>> startupClassOptional = startupParserClassesContainer.getStartupClass(query.getParserName());

        if (!startupClassOptional.isPresent()) {
            log.info("Could not find parser with name: {}", query.getParserName());
            return;
        }

        try {
            Constructor<?> constructor = startupClassOptional.get().getConstructor(Query.class);
            Object startupStep = constructor.newInstance(query);

            if (startupStep instanceof Step) {
                Step casted = (Step) startupStep;
                prepareQuerySupportCollections(query);
                stepsProvider.addNewStep(casted, casted.getExecutionResult());
                log.info(query.toString() + " was started");

            } else {
                log.info("Created instance is not of type Step");
            }

        } catch (Exception e) {
            log.info("Could not create instance of startup class. Reason: " + e.getCause());
        }
    }

    private void prepareQuerySupportCollections(Query query) {
        runningQueries.put(query.getId(), query);
        queriesResults.put(query.getId(), new ArrayList<>());
        successfulRequestedUrls.put(query.getId(), new HashSet<>());
    }

    private void cleanQuerySupportCollections(Query query) {
        runningQueries.remove(query.getId());
        queriesResults.remove(query.getId());
        successfulRequestedUrls.remove(query.getId());
    }

    void finishQuery(Query query) {

        log.info("Finishing query {}", query);

        List<BaseEntry> results = queriesResults.get(query.getId());
        log.info("Sent {} results to persistence service", results.size());
        persistenceService.persistResults(results);

        queryProvider.removeQueryFromQueue(query.getId());

        cleanQuerySupportCollections(query);
    }

    public Map<String, Query> getRunningQueries() {
        return runningQueries;
    }

    public List<BaseEntry> getQueryResults(String queryId) {
        return queriesResults.get(queryId);
    }

    public Set<String> getSuccessfulRequestedUrls(String queryId) {
        return successfulRequestedUrls.get(queryId);
    }
}
