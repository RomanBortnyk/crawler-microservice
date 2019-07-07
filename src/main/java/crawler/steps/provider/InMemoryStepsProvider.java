package crawler.steps.provider;

import core.service.StepsProvider;
import core.step.Step;
import core.step.result.ExecutionResult;
import crawler.steps.status.StepStatus;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
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
        stepsContainer.put(StepStatus.COMPLETED, new ArrayList<>());
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
            Optional<Pair<Step, ExecutionResult>> runningStepOptional = stepsContainer.get(StepStatus.RUNNING).stream()
                    .filter(p -> p.getKey().getId().equals(step.getId()))
                    .findFirst();

            if (runningStepOptional.isPresent()) {

                Pair<Step, ExecutionResult> runningStep = runningStepOptional.get();
                stepsContainer.get(StepStatus.COMPLETED).add(runningStep);
                stepsContainer.get(StepStatus.RUNNING).remove(runningStep);
            }

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

            List<Pair<Step, ExecutionResult>> runningSteps = stepsContainer.get(StepStatus.RUNNING);
            runningSteps.addAll(stepsSendToExecutor);

            pendingSteps.removeAll(stepsSendToExecutor);

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
