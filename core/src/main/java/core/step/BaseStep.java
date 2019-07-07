package core.step;

import core.Query;
import core.step.result.BaseStepExecutionResult;
import lombok.Getter;

@Getter
public abstract class BaseStep extends AbstractParsingStep {

    private final BaseStepExecutionResult executionResult;

    public BaseStep(Query query) {
        super(query);
        this.executionResult = new BaseStepExecutionResult(this);
    }

}
