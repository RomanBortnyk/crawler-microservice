package crawler.step.provider;

import core.service.StepsProvider;
import core.step.BaseStep;
import core.step.Step;
import core.step.WebRequestStep;
import core.step.result.ExecutionResult;
import crawler.exception.QueryNotFoundException;
import crawler.query.model.MongoQuery;
import crawler.repository.QueryRepository;
import crawler.repository.WebRequestStepRepository;
import crawler.step.mapper.MongoWebRequestStepMapper;
import crawler.step.status.StepStatus;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MongoStepsProvider implements StepsProvider {

    private final Map<StepStatus, List<Pair<BaseStep, ExecutionResult>>> baseStepsContainer =
            new EnumMap<>(StepStatus.class);

    private final List<Pair<WebRequestStep, ExecutionResult>> webRequestStepsBuffer = new ArrayList();
    private final List<Step> completedWebRequestStepsBuffer = new ArrayList();

    private final Object mutex = new Object();

    private final WebRequestStepRepository webRequestStepRepository;
    private final QueryRepository queryRepository;
    private final MongoWebRequestStepMapper mongoWebRequestMapper;

    private boolean firstStartup = true;

    public MongoStepsProvider(WebRequestStepRepository webRequestStepRepository,
                              QueryRepository queryRepository,
                              MongoWebRequestStepMapper mongoWebRequestStepMapper) {
        initCollections();
        this.webRequestStepRepository = webRequestStepRepository;
        this.queryRepository = queryRepository;
        this.mongoWebRequestMapper = mongoWebRequestStepMapper;
    }

    private void initCollections() {
        baseStepsContainer.put(StepStatus.PENDING, new ArrayList<>());
        baseStepsContainer.put(StepStatus.RUNNING, new ArrayList<>());
    }

    // TODO remove execution result from methods signature
    // because step already has it as a field

    @Override
    public void addNewStep(Step step, ExecutionResult executionResult) {
        synchronized (mutex) {
            if (step instanceof BaseStep) {
                baseStepsContainer.get(StepStatus.PENDING)
                        .add(new Pair<>((BaseStep) step, executionResult));
            }
            if (step instanceof WebRequestStep) {
                webRequestStepsBuffer.add(new Pair<>((WebRequestStep) step, executionResult));
            }
        }
    }

    @Override
    public void completeStep(Step step) {
        synchronized (mutex) {
            if (step instanceof BaseStep) {
                List<Pair<BaseStep, ExecutionResult>> runningSteps = baseStepsContainer.get(StepStatus.RUNNING);

                runningSteps.stream()
                        .filter(p -> p.getKey().getId().equals(step.getId()))
                        .findFirst()
                        .ifPresent(runningSteps::remove);
            }
            if (step instanceof WebRequestStep) {
                completedWebRequestStepsBuffer.add(step);
            }
        }

    }

    @Override
    @Transactional
    public List<Pair<Step, ExecutionResult>> getSteps() {

        synchronized (mutex) {

            checkQueryPresent("queryId");

            List<Pair<Step, ExecutionResult>> stepsToReturn = new ArrayList<>();

            if (firstStartup) {
                stepsToReturn.addAll(getRunningSteps());
                firstStartup = false;
            }

            removeCompletedSteps();

            // add web requests steps firstly to execute before base steps
            stepsToReturn.addAll(saveWebRequestsFromBuffer());
            stepsToReturn.addAll(getBaseSteps());

            return stepsToReturn;
        }
    }

    private void removeCompletedSteps() {
        List<String> completedStepsIds = completedWebRequestStepsBuffer.stream()
                .map(Step::getId)
                .collect(Collectors.toList());

        webRequestStepRepository.deleteByIdIn(completedStepsIds);
        completedWebRequestStepsBuffer.clear();
    }

    private List<Pair<Step, ExecutionResult>> getBaseSteps() {
        List<Pair<BaseStep, ExecutionResult>> baseSteps = baseStepsContainer.get(StepStatus.PENDING);

        List<Pair<Step, ExecutionResult>> nextBaseSteps = baseSteps.stream()
                .map(p -> new Pair<>((Step) p.getKey(), p.getValue()))
                .collect(Collectors.toList());
        baseSteps.clear();

        return nextBaseSteps;
    }

    private List<Pair<Step, ExecutionResult>> getRunningSteps() {

        return webRequestStepRepository.findAll().stream()
                .map(mongoWebRequestMapper::mapToWebRequestStep)
                .map(s -> new Pair<>((Step) s, (ExecutionResult) s.getExecutionResult()))
                .collect(Collectors.toList());
    }

    private void checkQueryPresent(String queryId) {

        // TODO implement , throw exception that no such running query

//        Optional<String> queryId = webRequestStepsBuffer.stream()
//                .mapToMongoWebRequestStep(p -> p.getKey().getQuery().getId())
//                .findFirst();
//
//        MongoQuery queryById = getQueryById(queryId.get());

    }

    private List<Pair<Step, ExecutionResult>> saveWebRequestsFromBuffer() {

        if (webRequestStepsBuffer.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<Pair<WebRequestStep, ExecutionResult>> webRequestsFromBuffer =
                new ArrayList<>(webRequestStepsBuffer);

        webRequestStepsBuffer.stream()
                .map(Pair::getKey)
                .map(mongoWebRequestMapper::mapToMongoWebRequestStep)
                .forEach(webRequestStepRepository::save);

        webRequestStepsBuffer.clear();

        return webRequestsFromBuffer.stream()
                .map(p -> new Pair<>((Step) p.getKey(), p.getValue()))
                .collect(Collectors.toList());
    }

    private MongoQuery getQueryById(String id) {
        return queryRepository.findById(id)
                .orElseThrow(() -> new QueryNotFoundException("Trying to persist steps " +
                        "but query with id " + id + " was not found"));
    }

    @Override
    public boolean anyStepsLeft() {
        return false;
    }

    @Override
    public void resetStepsContainers() {

    }
}
