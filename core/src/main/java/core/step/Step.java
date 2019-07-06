package core.step;

import core.Query;
import core.step.result.ExecutionResult;

public interface Step extends Runnable {

    String getId();

    Query getQuery();

    ExecutionResult getExecutionResult();
}
