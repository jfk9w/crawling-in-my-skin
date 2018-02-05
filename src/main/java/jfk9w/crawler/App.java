package jfk9w.crawler;

import jfk9w.crawler.executor.JsoupService;
import jfk9w.crawler.histogram.Histogram;
import jfk9w.crawler.histogram.HistogramTask;
import jfk9w.crawler.util.Timer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public final class App {

  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

  public static void main(String[] args) throws Exception {
    Arguments arguments = parse(args);
    ExecutorService io = Executors.newFixedThreadPool(4 * CPU_COUNT);
    ForkJoinPool pure = new ForkJoinPool(CPU_COUNT);
    Timer timer = Timer.start();
    try {
      HistogramTask task = HistogramTask.initial(arguments.url, arguments.depth, io);
      Histogram.View histogram = pure.submit(task).join().topWords(100);
      System.out.printf("---\n%s\n", histogram.toString());
    } finally {
      System.out.printf("---\nTime elapsed: %d ms.\nTime spent in IO: %d ms.\n", timer.stop(), JsoupService.time());
      pure.shutdown();
      io.shutdown();
    }
  }

  private static Arguments parse(String[] args) {
    if (args.length != 2) {
      return help();
    }

    String url = args[0];
    try {
      int depth = Integer.parseInt(args[1]);
      if (depth < 0) {
        return error("Error: DEPTH must be non-negative");
      }

      return new Arguments(url, depth);
    } catch (NumberFormatException e) {
      return help();
    }
  }

  private static <T> T help() {
    return error("Usage: java -jar crawler.jar URL DEPTH");
  }

  private static <T> T error(String msg) {
    System.err.println(msg);
    return error();
  }

  private static <T> T error() {
    System.exit(-1);
    return null;
  }

  private static final class Arguments {
    final String url;
    final int depth;
    Arguments(String url, int depth) {
      this.url = url;
      this.depth = depth;
    }
  }
}
