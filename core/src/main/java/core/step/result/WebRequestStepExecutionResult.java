package core.step.result;

import core.step.Step;
import lombok.Getter;

@Getter
public class WebRequestStepExecutionResult extends ExecutionResultImpl {

    private String successfulRequestUrl;

    public WebRequestStepExecutionResult(Step step) {
        super(step);
    }

    public void setSuccessfulRequestUrl(String successfulRequestUrl) {
        this.successfulRequestUrl = successfulRequestUrl;
    }
}
