package core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueryScheduler {

    private final QueryService queryService;

    @Scheduled(cron = "${scheduling.service.query.check}")
    public void scheduleNewQueryCheck() {
        queryService.startQuery();
    }

}
