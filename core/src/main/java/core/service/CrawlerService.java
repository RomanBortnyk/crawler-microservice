package core.service;

import core.Query;
import core.QueryProvider;
import core.annotation.processor.StartupParserClassesContainer;
import core.executor.StepsExecutor;
import core.model.BaseEntry;
import core.step.Step;
import core.step.result.ExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class CrawlerService {

    private final StepsExecutor stepsExecutor;
    private final CompletionService<ExecutionResult> stepsCompletionService;
    private final EntriesPersistenceService persistenceService;
    private final StartupParserClassesContainer startupParserClassesContainer;

    private Map<String, Query> runningQueries = new ConcurrentHashMap<>();
    private Map<String, List<BaseEntry>> queriesResults = new ConcurrentHashMap<>();
    private Map<String, Set<String>> successfulRequestedUrls = new ConcurrentHashMap<>();
    private AtomicBoolean ignoreStepsResults = new AtomicBoolean();

    private QueryProvider queryProvider;

    public CrawlerService(EntriesPersistenceService persistenceService,
                          StepsExecutor stepsExecutor,
                          StartupParserClassesContainer startupParserClassesContainer,
                          QueryProvider queryProvider) {
        this.persistenceService = persistenceService;
        this.stepsExecutor = stepsExecutor;
        this.stepsCompletionService = new ExecutorCompletionService<>(stepsExecutor);
        this.startupParserClassesContainer = startupParserClassesContainer;
        this.queryProvider = queryProvider;
    }

    /**
     * Starts passed query if no queries are running
     *
     * @param query query to start
     * @return true if query was started immediately, otherwise query was added to queue
     */
    public synchronized void startQuery() {

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
                stepsCompletionService.submit(casted, casted.getExecutionResult());
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

    void finishQuery(Query query) {

        log.info("Finishing query {}", query);

        List<BaseEntry> results = queriesResults.get(query.getId());
        log.info("Sent {} results to persistence service", results.size());
        persistenceService.persistResults(results);

        queryProvider.removeQueryFromQueue(query.getId());

        cleanQuerySupportCollections(query);
    }

    private void cleanQuerySupportCollections(Query query) {
        runningQueries.remove(query.getId());
        queriesResults.remove(query.getId());
        successfulRequestedUrls.remove(query.getId());
    }

    public List<BaseEntry> getQueryResults(String queryId) {
        return queriesResults.get(queryId);
    }

    public Map<String, Query> getRunningQueries() {
        return runningQueries;
    }

    StepsExecutor getStepsExecutor() {
        return stepsExecutor;
    }

    public Map<String, Set<String>> getSuccessfulRequestedUrls() {
        return successfulRequestedUrls;
    }

    public CompletionService<ExecutionResult> getStepsCompletionService() {
        return stepsCompletionService;
    }

    @PreDestroy
    public void cleanUpResources() {
        stepsExecutor.shutdownNow();
    }


    /**
     * Removes query by id from from active execution
     * Ignores steps related to removed query
     *
     * @param queryId query id to remove
     */
    private synchronized void stopAndRemoveQuery(String queryId) {

        if (!runningQueries.containsKey(queryId)) {
            log.info("Tried to stop not running query with id {}", queryId);
            return;
        }

        ignoreStepsResults.set(true);
        stepsExecutor.getQueue().clear();

        try {

            while (stepsExecutor.getRunningStepsCount() != 0) {
                Thread.sleep(1000);
            }

            runningQueries.remove(queryId);
            queriesResults.remove(queryId);
            successfulRequestedUrls.remove(queryId);

            log.info("Query with id {} was removed from execution", queryId);

        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        ignoreStepsResults.set(false);

    }


    boolean ignoreStepsResults() {
        return ignoreStepsResults.get();
    }
}
