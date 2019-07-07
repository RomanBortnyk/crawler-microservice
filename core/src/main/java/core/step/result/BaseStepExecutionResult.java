package core.step.result;

import core.model.BaseEntry;
import core.step.Step;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BaseStepExecutionResult extends ExecutionResultImpl {

    private final List<BaseEntry> entries;

    public BaseStepExecutionResult(Step step) {
        super(step);
        entries = new ArrayList<>(2);
    }

}
