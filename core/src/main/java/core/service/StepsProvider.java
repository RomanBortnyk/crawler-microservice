package core.service;

import core.step.result.ExecutionResult;
import javafx.util.Pair;

import java.util.List;

public interface StepsProvider {

    void addNewStep(Runnable step, ExecutionResult executionResult);

    void completeStep(String stepId);

    List<Pair<Runnable, ExecutionResult>> getSteps();

//    boolean isEmpty();
}
