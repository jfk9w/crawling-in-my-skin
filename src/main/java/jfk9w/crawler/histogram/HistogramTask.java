package jfk9w.crawler.histogram;

import jfk9w.crawler.Histogram;
import jfk9w.crawler.executor.JsoupService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public final class HistogramTask extends RecursiveTask<Histogram> {

  private static final Logger logger = LoggerFactory.getLogger(HistogramTask.class);

  private final Context ctx;
  private final int depth;
  private final Document document;
  private final Executor io;

  public static HistogramTask initial(String url, int maxDepth, Executor io) throws IOException {
    Context ctx = Context.create(url, maxDepth);
    return new HistogramTask(ctx, 1, Jsoup.connect(url).get(), io);
  }

  private HistogramTask(Context ctx, int depth, Document document, Executor io) {
    this.ctx = ctx;
    this.depth = depth;
    this.document = document;
    this.io = io;
  }

  @Override
  protected Histogram compute() {
    JsoupService jsoup = new JsoupService(io);
    if (depth < ctx.maxDepth) {
      Elements elements = document.select("a[href]");
      for (Element element : elements) {
        String url = element.attr("abs:href");
        if (ctx.mustContinue(url)) {
          logger.debug("processing {}", url);
          jsoup.submit(element.attr("abs:href"));
        }
      }
    }

    Histogram hg = Arrays
        .stream(document.body().text().split("[\\s+\\.,\\/#!$%\\^&\\*;:{}=—_`~()|]"))
        .filter(s -> !s.isEmpty())
        .collect(new HistogramCollector());

    List<ForkJoinTask<Histogram>> forks = new LinkedList<>();
    for (Document ref : jsoup) {
      if (ref != null) {
        forks.add(new HistogramTask(ctx, depth + 1, ref, io).fork());
      }
    }

    return hg.merge(
        forks.stream()
            .map(ForkJoinTask::join)
            .reduce(Histogram::merge)
            .orElse(Histogram.EMPTY));
  }
}
