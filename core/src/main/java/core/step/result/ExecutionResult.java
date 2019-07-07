package core.step.result;

import core.step.Step;

import java.util.List;

public interface ExecutionResult {

    Step getStep();
    List<Step> getNextSteps();
}
