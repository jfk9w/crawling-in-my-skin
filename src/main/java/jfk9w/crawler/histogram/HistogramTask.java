package jfk9w.crawler.histogram;

import jfk9w.crawler.executor.JsoupService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public final class HistogramTask extends RecursiveTask<Histogram> {

  private final Context ctx;
  private final int depth;
  private final Document document;
  private final Executor io;

  public static HistogramTask initial(String url, int depth, Executor io)
      throws InterruptedException, ExecutionException {
    Context ctx = Context.create(url);
    return new HistogramTask(ctx, depth, new JsoupService(io).submit(url).get(), io);
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
    if (depth > 0) {
      Elements elements = document.select("a[href]");
      for (Element element : elements) {
        String url = element.attr("abs:href");
        if (ctx.proceed(url)) {
          jsoup.submit(element.attr("abs:href"));
        }
      }
    }

    final Histogram histogram;
    if (document.body() != null && document.body().text() != null) {
      String text = document.body().text();
      histogram = Arrays.stream(text.split("[\\s+-.,/#!$%^&*;:{}\\[\\]=—_`~()|'\"?°′″·–•→<>†]"))
          .filter(s -> !s.isEmpty())
          .map(String::toLowerCase)
          .collect(new HistogramCollector());
    } else {
      histogram = Histogram.EMPTY;
    }

    List<ForkJoinTask<Histogram>> forks = new LinkedList<>();
    for (Document ref : jsoup) {
      if (ref != null) {
        forks.add(new HistogramTask(ctx, depth - 1, ref, io).fork());
      }
    }

    return histogram.merge(
        forks.stream()
            .map(ForkJoinTask::join)
            .reduce(Histogram::merge)
            .orElse(Histogram.EMPTY));
  }
}
