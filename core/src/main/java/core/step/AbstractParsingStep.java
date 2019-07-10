package core.step;

import core.Query;
import lombok.Getter;

import java.util.Collection;
import java.util.UUID;

@Getter
abstract class AbstractParsingStep extends PriorityStepImpl {

    private final String id;
    private final Query query;

    public AbstractParsingStep(Query query) {
        this.query = query;
        this.id = UUID.randomUUID().toString();
    }

    public AbstractParsingStep(String id, Query query) {
        this.id = id;
        this.query = query;
    }

    public void addNextStep(Step step) {
        getExecutionResult().getNextSteps().add(step);
    }

    public void addNextSteps(Collection<Step> steps) {
        getExecutionResult().getNextSteps().addAll(steps);
    }
}

