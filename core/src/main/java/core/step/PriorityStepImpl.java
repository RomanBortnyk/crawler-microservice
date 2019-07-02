package core.step;

import lombok.Getter;

@Getter
public abstract class PriorityStepImpl implements Step {

    public static final int DEFAULT_PRIORITY = 5;

    private int priority = DEFAULT_PRIORITY;

    public void incrementPriority() {
        priority++;
    }
}
