package org.liws.framework.util.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * 基于guava
 */
public class ExecutorServiceUtils {

    public static ExecutorService create(String name,int minPoolSize,int maxPoolSize){
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat(name+"-%d")
                .setUncaughtExceptionHandler((thread, throwable) -> {
                	// TODO logger.error(thread.getName(),throwable);
                	})
                .build();

        ExecutorService pool = new ThreadPoolExecutor(
                minPoolSize,
                maxPoolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(2048), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

        return pool;
    }

    public static ExecutorService create(String name){
        return create(name,Math.min(3,Runtime.getRuntime().availableProcessors()*2),
                Runtime.getRuntime().availableProcessors()*2);

    }
}
