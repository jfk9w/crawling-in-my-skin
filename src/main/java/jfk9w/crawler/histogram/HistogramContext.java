package jfk9w.crawler.histogram;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class HistogramContext {

  final int maxDepth;
  private final ConcurrentMap<String, Boolean> done = new ConcurrentHashMap<>();

  public HistogramContext(int maxDepth) {
    this.maxDepth = maxDepth;
  }

  boolean isParsed(String url) {
    return done.put(url, true) != null;
  }
}
