package core.step;

import core.Query;
import lombok.Getter;

import java.util.Arrays;
import java.util.UUID;

@Getter
abstract class AbstractParsingStep extends PriorityStepImpl {

    private final Query query;
    private final String id;

    public AbstractParsingStep(Query query) {
        this.query = query;
        this.id = UUID.randomUUID().toString();
    }

    public void addNextSteps(Step... steps) {
        getExecutionResult().getNextSteps().addAll(Arrays.asList(steps));
    }
}

