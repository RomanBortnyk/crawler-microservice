package crawler.steps.provider;

import core.service.StepsProvider;
import core.step.result.ExecutionResult;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Slf4j
public class InMemoryStepsProvider implements StepsProvider{

    private Queue<Pair<Runnable, ExecutionResult>> stepsQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void addNewStep(Runnable step, ExecutionResult executionResult) {
        stepsQueue.add(new Pair<>(step, executionResult));
    }

    @Override
    public void completeStep(String stepId) {
        log.info("Step with id: {} was completed", stepId);
    }

    @Override
    public List<Pair<Runnable, ExecutionResult>> getSteps() {

        if (stepsQueue.isEmpty()){
            return Collections.emptyList();
        }

        List<Pair<Runnable, ExecutionResult>> steps = new ArrayList<>();

        while (!stepsQueue.isEmpty()){
            steps.add(stepsQueue.poll());
        }

        return steps;
    }



}
