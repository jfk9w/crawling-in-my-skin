package jfk9w.crawler;

import jfk9w.crawler.histogram.Histogram;
import jfk9w.crawler.histogram.HistogramTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public final class Crawler {

  private static final Logger logger = LoggerFactory.getLogger(Crawler.class);

  public static void main(String[] args) throws Exception {
    int cpus = Runtime.getRuntime().availableProcessors();
    ForkJoinPool pure = new ForkJoinPool(cpus);
    Executor io = Executors.newFixedThreadPool(cpus);

    String url = args[0];
    int maxDepth = Integer.parseInt(args[1]);

    long start = System.currentTimeMillis();
    Histogram histogram = pure.submit(HistogramTask.initial(url, maxDepth, io)).join();

    logger.info("{}\0n=======\ntime: {} ms.", histogram, System.currentTimeMillis() - start);
  }

}
