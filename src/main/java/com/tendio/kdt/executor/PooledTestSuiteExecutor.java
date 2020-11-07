package com.tendio.kdt.executor;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tendio.kdt.TestProperties;
import com.tendio.kdt.configurator.model.TestCase;
import com.tendio.kdt.configurator.model.TestSuite;
import com.tendio.kdt.executor.browser.Browser;
import com.tendio.kdt.reporting.Report;
import com.tendio.kdt.reporting.html.AggregatedHtmlReport;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class PooledTestSuiteExecutor {
    private static final int THREADS_COUNT = Integer.parseInt(TestProperties.getProperty("test.threads.count"));
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
            .setNameFormat("TestThread-%d")
            .setUncaughtExceptionHandler(new UncaughtExceptionHandlerImpl())
            .build();

    private static final ExecutorService executor = Executors.newFixedThreadPool(THREADS_COUNT, THREAD_FACTORY);

    private PooledTestSuiteExecutor() {
    }

    public static void execute(TestSuite testSuite) {
        List<TestCase> executables = testSuite.getTestCases();
        Instant start = Instant.now();
        CompletableFuture<?>[] futures = executables.stream()
                .map(task -> CompletableFuture.runAsync(task, executor))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).join();
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        new AggregatedHtmlReport().withId(testSuite.getId()).withExecutionTime(duration).build().write();
        executor.shutdown();
    }

    private static class UncaughtExceptionHandlerImpl implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if (Browser.getBrowser() != null) {
                File screenshot = Browser.getScreenshot();
                Report.getReport().error(e.getMessage(), screenshot);
            }
            LogManager.getLogger().error("Uncaught exception occurred: {} in thread: {}. Cause: {}. Stacktrace: {}",
                    e.getMessage(), Thread.currentThread().getName(), e.getCause(), Throwables.getStackTraceAsString(e));
        }
    }
}
