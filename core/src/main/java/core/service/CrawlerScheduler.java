package core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrawlerScheduler {

    private final CrawlerService crawlerService;

    @Scheduled(cron = "0/10 * * * * *")
    public void scheduleFixedDelayTask() {

        crawlerService.startQuery();
    }

}
