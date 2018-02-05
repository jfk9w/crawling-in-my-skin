package jfk9w.crawler.util;

public final class Timer {

  private final long start;

  private Timer(long start) {
    this.start = start;
  }

  public static Timer start() {
    return new Timer(System.currentTimeMillis());
  }

  public long stop() {
    return System.currentTimeMillis() - start;
  }

}
