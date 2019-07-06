package core.service;

import core.Query;
import core.QueryProvider;
import core.annotation.processor.StartupParserClassesContainer;
import core.executor.StepsExecutor;
import core.model.BaseEntry;
import core.step.Step;
import core.step.result.ExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class CrawlerService {

    private StepsProvider stepsProvider;
    private final StepsExecutor stepsExecutor;
    private final CompletionService<ExecutionResult> stepsCompletionService;

    private AtomicBoolean ignoreStepsResults = new AtomicBoolean();

    public CrawlerService(StepsExecutor stepsExecutor,
                          StepsProvider stepsProvider) {
        this.stepsExecutor = stepsExecutor;
        this.stepsCompletionService = new ExecutorCompletionService<>(stepsExecutor);
        this.stepsProvider = stepsProvider;
    }

    StepsExecutor getStepsExecutor() {
        return stepsExecutor;
    }

    public Future<ExecutionResult> takeStep() throws InterruptedException{
         return stepsCompletionService.take();
    }

    @PreDestroy
    public void cleanUpResources() {
        stepsExecutor.shutdownNow();
    }

    // 10 seconds
    @Scheduled(fixedRate = 10000)
    public void pollSteps(){

        stepsProvider.getSteps()
                .forEach(pair -> stepsCompletionService.submit(pair.getKey(), pair.getValue()));

    }


    // todo implement removing of running query
    /**
     * Removes query by id from from active execution
     * Ignores steps related to removed query
     *
     * @param queryId query id to remove
     */
//    private synchronized void stopAndRemoveQuery(String queryId) {
//
//        if (!runningQueries.containsKey(queryId)) {
//            log.info("Tried to stop not running query with id {}", queryId);
//            return;
//        }
//
//        ignoreStepsResults.set(true);
//        stepsExecutor.getQueue().clear();
//
//        try {
//
//            while (stepsExecutor.getRunningStepsCount() != 0) {
//                Thread.sleep(1000);
//            }
//
//            runningQueries.remove(queryId);
//            queriesResults.remove(queryId);
//            successfulRequestedUrls.remove(queryId);
//
//            log.info("Query with id {} was removed from execution", queryId);
//
//        } catch (InterruptedException e) {
//            log.error(e.getMessage());
//        }
//
//        ignoreStepsResults.set(false);
//
//    }


    boolean ignoreStepsResults() {
        return ignoreStepsResults.get();
    }
}
