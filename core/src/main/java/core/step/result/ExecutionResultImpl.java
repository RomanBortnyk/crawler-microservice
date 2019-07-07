package core.step.result;

import core.step.Step;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ExecutionResultImpl implements ExecutionResult {

    private final List<Step> nextSteps;
    private final Step step;

    public ExecutionResultImpl(Step step) {
        this.step = step;
        this.nextSteps = new ArrayList<>();
    }
}
