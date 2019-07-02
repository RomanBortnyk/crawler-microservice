package core.step;

import core.Query;
import lombok.Getter;

import java.util.Arrays;

@Getter
abstract class AbstractParsingStep extends PriorityStepImpl {

    private final Query query;

    public AbstractParsingStep(Query query) {
        this.query = query;
    }

    public void addNextSteps(Step... steps) {
        getExecutionResult().getNextSteps().addAll(Arrays.asList(steps));
    }
}

