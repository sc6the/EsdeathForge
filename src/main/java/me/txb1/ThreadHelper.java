package me.txb1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadHelper {
   private final ExecutorService threadpool;

   public ExecutorService getThreadpool() {
      return this.threadpool;
   }

   public ThreadHelper() {
      this.threadpool = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
   }

}
