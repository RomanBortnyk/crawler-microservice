package core.step.result;

import core.Query;
import core.step.Step;

import java.util.List;

public interface ExecutionResult {

    List<Step> getNextSteps();
    Query getQuery();
}
