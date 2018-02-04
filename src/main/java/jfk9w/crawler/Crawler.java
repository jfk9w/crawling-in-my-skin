package jfk9w.crawler;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Ordering;
import jfk9w.crawler.histogram.HistogramContext;
import jfk9w.crawler.histogram.HistogramTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public final class Crawler {

  private static final Logger logger = LoggerFactory.getLogger(Crawler.class);

  public static void main(String[] args) {
    String url = args[0];
    int maxDepth = Integer.parseInt(args[1]);

    logger.debug("URL: {}, maximum depth: {}", url, maxDepth);

    ForkJoinPool pool = new ForkJoinPool(32);
    List<HistogramItem> histogram = pool
        .submit(new HistogramTask(new HistogramContext(maxDepth), url, 0)).join()
        .entrySet().stream()
        .map(e -> new HistogramItem(e.getKey(), e.getValue()))
        .collect(Collectors.toList());

    Collection<HistogramItem> top = Ordering.natural()
        .<HistogramItem>onResultOf(item -> item.frequency)
        .greatestOf(histogram, 100);

    logger.info("Histogram: {}", top);
  }

  private static final class HistogramItem {
    final String word;
    final int frequency;
    public HistogramItem(String word, int frequency) {
      this.word = word;
      this.frequency = frequency;
    }
    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("word", word)
          .add("frequency", frequency)
          .toString();
    }
  }
}
