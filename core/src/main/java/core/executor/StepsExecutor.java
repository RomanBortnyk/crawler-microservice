package core.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StepsExecutor extends ThreadPoolExecutor {

    private static final Logger log = LoggerFactory.getLogger(StepsExecutor.class);

    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit TIME_UNIT = TimeUnit.MINUTES;
    private static final double BLOCKING_COEFFICIENT = 0.89;
    private static final int INITIAL_QUEUE_CAPACITY = 40;
    private static final int EFFECTIVE_NUMBER_OF_THREADS = determineNumberOfThreads();
    private AtomicInteger runningStepsCount = new AtomicInteger(0);

    public StepsExecutor() {
//        super(EFFECTIVE_NUMBER_OF_THREADS, EFFECTIVE_NUMBER_OF_THREADS, KEEP_ALIVE_TIME, TIME_UNIT,
//                new PriorityBlockingQueue<>(INITIAL_QUEUE_CAPACITY, new PriorityStepComparator())
//        );

        super(EFFECTIVE_NUMBER_OF_THREADS, EFFECTIVE_NUMBER_OF_THREADS, KEEP_ALIVE_TIME, TIME_UNIT,
                new LinkedBlockingQueue<>());

    }

    private static int determineNumberOfThreads() {
        int n = (int) (Runtime.getRuntime().availableProcessors() / (1 - BLOCKING_COEFFICIENT));
        log.info("Calculated number of threads: " + n);
        return n;
    }


    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        runningStepsCount.incrementAndGet();
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        runningStepsCount.decrementAndGet();

        if (t != null) {
            log.error("Step " + r + " was executed with exception " + t.getMessage());
        }
    }

    public int getRunningStepsCount() {
        return runningStepsCount.intValue();
    }
}
