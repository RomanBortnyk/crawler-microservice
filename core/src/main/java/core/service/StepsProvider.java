package core.service;

import core.step.Step;
import core.step.result.ExecutionResult;
import javafx.util.Pair;

import java.util.List;

public interface StepsProvider {

    void addNewStep(Step step, ExecutionResult executionResult);

    void completeStep(Step step);

    List<Pair<Step, ExecutionResult>> getSteps();

    boolean anyStepsLeft();

    void resetStepsContainers();

}
