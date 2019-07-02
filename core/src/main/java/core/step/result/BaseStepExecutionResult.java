package core.step.result;

import core.Query;
import core.model.BaseEntry;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BaseStepExecutionResult extends ExecutionResultImpl {

    private final List<BaseEntry> entries;

    public BaseStepExecutionResult(Query query) {
        super(query);
        entries = new ArrayList<>(2);
    }

}
