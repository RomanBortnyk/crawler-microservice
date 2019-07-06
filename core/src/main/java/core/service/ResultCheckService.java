package core.service;

import core.executor.StepsExecutor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResultCheckService {

    private static final Logger log = LoggerFactory.getLogger(ResultCheckService.class);
    private final CrawlerService crawlerService;
    private final QueryService queryService;

    @Scheduled(initialDelay = 5000, fixedRate = 15000)
    public void resultCheck() {

        StepsExecutor executorService = crawlerService.getStepsExecutor();

        final int stepsQueueSize = executorService.getQueue().size();
        final int runningStepsCount = executorService.getRunningStepsCount();

        boolean anyStepsLeft = runningStepsCount != 0 || stepsQueueSize != 0;

        if (anyStepsLeft) {
            log.info(String.format("Result check: %d (steps in queue)/%d (running steps)", stepsQueueSize, runningStepsCount));
        }

        boolean runningQueriesPresent = !queryService.getRunningQueries().isEmpty();

        if (runningQueriesPresent && !anyStepsLeft) {

            // currently service can execute only one query,
            // running queries map is for feature when multiple queries can be executed
            queryService.getRunningQueries().values().forEach(queryService::finishQuery);
        }
    }
}
