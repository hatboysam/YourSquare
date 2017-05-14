package com.habosa.yoursquare.task;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Utility class holding threads we can run tasks on.
 */
public class TaskThreads {

    public static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(2,
            4, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());

}
