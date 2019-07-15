package crawler.step.provider;

import core.service.StepsProvider;
import core.step.BaseStep;
import core.step.Step;
import core.step.result.ExecutionResult;
import crawler.step.status.StepStatus;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

//@Component
@Slf4j
public class InMemoryStepsProvider implements StepsProvider {

    private final Map<StepStatus, List<Pair<Step, ExecutionResult>>> stepsContainer =
            new EnumMap<>(StepStatus.class);

    private final Object mutex = new Object();

    public InMemoryStepsProvider() {
        initCollections();
    }

    private void initCollections() {
        stepsContainer.put(StepStatus.PENDING, new ArrayList<>());
        stepsContainer.put(StepStatus.RUNNING, new ArrayList<>());
    }

    @Override
    public void addNewStep(Step step, ExecutionResult executionResult) {
        synchronized (mutex) {
            stepsContainer.get(StepStatus.PENDING)
                    .add(new Pair<>(step, executionResult));
        }
    }

    @Override
    public void completeStep(Step step) {

        synchronized (mutex) {
            List<Pair<Step, ExecutionResult>> runningSteps = stepsContainer.get(StepStatus.RUNNING);

            runningSteps.stream()
                    .filter(p -> p.getKey().getId().equals(step.getId()))
                    .findFirst()
                    .ifPresent(runningSteps::remove);
        }


        log.info("Step with id: {} was completed", step.getId());
    }

    @Override
    public List<Pair<Step, ExecutionResult>> getSteps() {

        synchronized (mutex) {

            List<Pair<Step, ExecutionResult>> pendingSteps = stepsContainer.get(StepStatus.PENDING);

            if (pendingSteps.isEmpty()) {
                return Collections.emptyList();
            }

            List<Pair<Step, ExecutionResult>> stepsSendToExecutor = new ArrayList<>(pendingSteps);

            stepsContainer.get(StepStatus.RUNNING).addAll(stepsSendToExecutor);
            pendingSteps.clear();

            stepsSendToExecutor.sort((p1, p2) -> {
//                 sort steps in order that BaseStep steps are on the top of the list
                // and will be placed in execution queue firstly
                if (p1.getKey() instanceof BaseStep) {
                    return -1;
                }
                if (p2.getKey() instanceof BaseStep) {
                    return 1;
                }
                return 0;
            });

            return stepsSendToExecutor;
        }
    }


    @Override
    public boolean anyStepsLeft() {
        boolean anyPendingStepsLeft = !stepsContainer.get(StepStatus.PENDING).isEmpty();
        boolean anyRunningStepsLeft = !stepsContainer.get(StepStatus.RUNNING).isEmpty();

        return anyRunningStepsLeft || anyPendingStepsLeft;
    }

    @Override
    public void resetStepsContainers() {
        initCollections();
    }
}
