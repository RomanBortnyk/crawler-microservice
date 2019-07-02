package core.step.result;

import core.Query;
import core.step.Step;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ExecutionResultImpl implements ExecutionResult {

    private final List<Step> nextSteps;
    private final Query query;

    public ExecutionResultImpl(Query query) {
        this.query = query;
        this.nextSteps = new ArrayList<>();
    }
}
