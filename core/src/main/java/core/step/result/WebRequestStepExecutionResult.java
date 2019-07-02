package core.step.result;

import core.Query;
import lombok.Getter;

@Getter
public class WebRequestStepExecutionResult extends ExecutionResultImpl {

    private String successfulRequestUrl;

    public WebRequestStepExecutionResult(Query query) {
        super(query);
    }

    public void setSuccessfulRequestUrl(String successfulRequestUrl) {
        this.successfulRequestUrl = successfulRequestUrl;
    }
}
