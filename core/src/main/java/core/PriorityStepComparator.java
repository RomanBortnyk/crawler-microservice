package core;

import core.step.PriorityStepImpl;

import java.util.Comparator;

public class PriorityStepComparator implements Comparator<Runnable> {

    @Override
    public int compare(Runnable o1, Runnable o2) {
        if (o1 instanceof PriorityStepImpl && o2 instanceof PriorityStepImpl) {
            return Integer.compare(((PriorityStepImpl) o1).getPriority(), ((PriorityStepImpl) o2).getPriority());
        }
        return 0;
    }
}
