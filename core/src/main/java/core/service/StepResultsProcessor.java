package core.service;

import core.model.BaseEntry;
import core.proxy.ProxyProvider;
import core.step.BaseStep;
import core.step.Step;
import core.step.WebRequestStep;
import core.step.WebRequestStepProxy;
import core.step.result.BaseStepExecutionResult;
import core.step.result.ExecutionResult;
import core.step.result.WebRequestStepExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepResultsProcessor {

    private final ExecutorService stepsProcessorPool = Executors.newFixedThreadPool(1);

    private final CrawlerService crawlerService;
    private final ProxyProvider proxyProvider;
    private final QueryService queryService;
    private final StepsProvider stepsProvider;

    @PostConstruct
    public void initialize() {
        start();
    }

    private void start() {

        stepsProcessorPool.execute(() -> {

            while (!crawlerService.getStepsExecutor().isShutdown()) {

                try {
                    Future<ExecutionResult> stepResultFuture = crawlerService.takeStep();

                    if (crawlerService.ignoreStepsResults()) {
                        log.info("Step result was ignored due to query cancellation");
                    } else {
                        processExecutionResult(stepResultFuture.get());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void processExecutionResult(ExecutionResult execResult) {

        final String queryId = execResult.getQuery().getId();

        for (Step nextStep : execResult.getNextSteps()) {

            if (nextStep instanceof WebRequestStep) {
                handleWebRequestStep((WebRequestStep) nextStep, queryId);
            }

            if (nextStep instanceof BaseStep) {
                ((BaseStep) nextStep).incrementPriority();
                stepsProvider.addNewStep(nextStep, nextStep.getExecutionResult());
            }

        }

        if (execResult instanceof WebRequestStepExecutionResult) {
            String successfulRequestedUrl = ((WebRequestStepExecutionResult) execResult).getSuccessfulRequestUrl();
            Optional.ofNullable(successfulRequestedUrl)
                    .ifPresent(u -> queryService.addUrlAsSuccessfullyRequested(queryId, u));
        }

        if (execResult instanceof BaseStepExecutionResult) {
            List<BaseEntry> entries = ((BaseStepExecutionResult) execResult).getEntries();
            queryService.addQueryResults(queryId, entries);
        }
    }

    /**
     * Process web request step. Assigns proxy address to step.
     * Ignores step if url was successfully requested before, otherwise
     * wrap step in proxy and pass to executor
     */
    private void handleWebRequestStep(WebRequestStep webRequestStep, String queryId) {

        String url = webRequestStep.getWebRequestSettings().getUrl();

        if (queryService.containsUrlAsSuccessfullyRequested(queryId, url)) {
            log.info("Duplicated request was ignored: " + url);
            return;
        }

        webRequestStep.setProxyAddress(proxyProvider.getNextProxy());

        WebRequestStepProxy webRequestStepProxy = new WebRequestStepProxy(webRequestStep);
        stepsProvider.addNewStep(webRequestStepProxy, webRequestStepProxy.getExecutionResult());
    }
}
