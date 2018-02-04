package jfk9w.crawler.histogram;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class HistogramTask extends RecursiveTask<Map<String, Integer>> {

  private static final Logger logger = LoggerFactory.getLogger(HistogramTask.class);

  private final HistogramContext ctx;
  private final String url;
  private final int depth;

  private HistogramTask(HistogramContext ctx, String url, int depth) {
    this.ctx = ctx;
    this.url = url;
    this.depth = depth;
  }

  public static HistogramTask initial(String url, int maxDepth) {
    HistogramContext ctx = HistogramContext.create(url, maxDepth);
    return new HistogramTask(ctx, url, 0);
  }

  @Override
  protected Map<String, Integer> compute() {
    if (!ctx.shouldParse(url)) {
      return new HashMap<>();
    }

    logger.debug("Processing {}", url);

    try {
      Document doc = Jsoup.connect(url).get();
      Map<String, Integer> histogram =
          Arrays.stream(doc.body().text().split("[\\s+\\.,\\/#!$%\\^&\\*;:{}=—_`~()|]"))
              .filter(s -> !s.isEmpty())
              .map(String::toLowerCase)
              .collect(new HistogramCollector<>());

      if (depth >= ctx.maxDepth) {
        return histogram;
      }

      Collection<ForkJoinTask<Map<String, Integer>>> forks =
          invokeAll(doc.select("a[href]").stream()
              .map(e -> e.attr("abs:href"))
              .map(url -> new HistogramTask(ctx, url, depth + 1).fork())
              .collect(Collectors.toList()));

      Map<String, Integer> sub = forks.stream()
          .map(ForkJoinTask::join)
          .reduce(HistogramTask::merge)
          .orElseGet(HashMap::new);

      return merge(histogram, sub);
    } catch (Exception e) {
      logger.error("Failed to load {}", url, e);
      return new HashMap<>();
    }
  }

  private static Map<String, Integer> merge(Map<String, Integer> a, Map<String, Integer> b) {
    Map<String, Integer> r = new HashMap<>(a);
    b.forEach((k, v) -> r.merge(k, v, (x, y) -> x + y));
    return r;
  }
}
