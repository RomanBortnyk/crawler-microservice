package core.service;

import core.executor.StepsExecutor;
import core.step.Step;
import core.step.result.ExecutionResult;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.CompletionService;
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

    public int pollSteps() {
        List<Pair<Step, ExecutionResult>> nextSteps = stepsProvider.getSteps();
        nextSteps.forEach(pair -> stepsCompletionService.submit(pair.getKey(), pair.getValue()));
        return nextSteps.size();
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

//    public boolean requestingSteps() {
//        return requestingSteps.get();
//    }
}
