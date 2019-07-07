package core.service;

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
    private final StepsProvider stepsProvider;

    @Scheduled(cron = "${scheduling.service.result.check}")
    public void resultCheck() {

        final int receivedNextSteps = crawlerService.pollSteps();

        boolean anyStepsLeft = stepsProvider.anyStepsLeft();

        if (anyStepsLeft) {
            log.info(String.format("Received %d steps from steps provider", receivedNextSteps));
            return;
        }

        boolean runningQueriesPresent = !queryService.getRunningQueries().isEmpty();

        if (runningQueriesPresent) {

            // currently service can execute only one query,
            // running queries map is for feature when multiple queries can be executed
            queryService.getRunningQueries().values().forEach(queryService::finishQuery);
        }
    }
}
