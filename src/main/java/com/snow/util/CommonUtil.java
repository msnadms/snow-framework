package com.snow.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class CommonUtil {

    public static void gracefulShutdown(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static boolean isHeaderTerminator(byte[] bytes) {
        int len = bytes.length;
        return len >= 4 && bytes[len-4] == '\r' && bytes[len-3] == '\n' &&
                bytes[len-2] == '\r' && bytes[len-1] == '\n';
    }
}
