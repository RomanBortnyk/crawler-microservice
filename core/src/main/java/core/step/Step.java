package core.step;

import core.Query;
import core.step.result.ExecutionResult;

public interface Step extends Runnable {

    Query getQuery();

    ExecutionResult getExecutionResult();
}
