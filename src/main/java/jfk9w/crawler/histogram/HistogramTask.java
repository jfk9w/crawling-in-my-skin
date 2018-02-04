package jfk9w.crawler.histogram;

import com.google.common.collect.ImmutableMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

public class HistogramTask extends RecursiveTask<Map<String, Integer>> {

  private static final Logger logger = LoggerFactory.getLogger(HistogramTask.class);

  private final HistogramContext ctx;
  private final String url;
  private final int depth;

  public HistogramTask(HistogramContext ctx, String url, int depth) {
    this.ctx = ctx;
    this.url = url;
    this.depth = depth;
  }

  @Override
  protected Map<String, Integer> compute() {
    if (ctx.isParsed(url)) {
      return ImmutableMap.of();
    }

    logger.debug("Processing {}", url);

    try {
      Document doc = Jsoup.connect(url).get();
      Map<String, Integer> histogram =
          Arrays.stream(doc.body().text().split("\\W+"))
              .filter(s -> !s.isEmpty())
              .map(String::toLowerCase)
              .collect(new HistogramCollector<>());

      if (depth >= ctx.maxDepth) {
        return histogram;
      }

      Map<String, Integer> sub = doc.select("a[href]").stream()
          .parallel()
          .map(e -> e.attr("abs:href"))
          .map(url -> new HistogramTask(ctx, url, depth + 1).fork().join())
          .reduce(this::merge)
          .orElseGet(ImmutableMap::of);

      return merge(histogram, sub);
    } catch (Exception e) {
      logger.error("Failed to load {}", url, e);
      return ImmutableMap.of();
    }
  }

  private Map<String, Integer> merge(Map<String, Integer> a, Map<String, Integer> b) {
    Map<String, Integer> r = new HashMap<>(a);
    b.forEach((k, v) -> r.merge(k, v, (x, y) -> x + y));
    return r;
  }
}
