package jfk9w.crawler.histogram;

import jfk9w.crawler.executor.Document;
import jfk9w.crawler.executor.JsoupService;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public final class HistogramTask extends RecursiveTask<Histogram> {

  private final Context ctx;
  private final int depth;
  private final Document document;

  public static HistogramTask initial(String url, int depth, Executor io)
      throws InterruptedException, ExecutionException {
    Context ctx = Context.create(url, io);
    return withContext(url, depth, ctx);
  }

  static HistogramTask withContext(String url, int depth, Context ctx)
      throws InterruptedException, ExecutionException {
    Document doc = ctx.io().submit(url).get();
    return new HistogramTask(ctx, depth, doc);
  }

  private HistogramTask(Context ctx, int depth, Document document) {
    this.ctx = ctx;
    this.depth = depth;
    this.document = document;
  }

  @Override
  protected Histogram compute() {
    JsoupService jsoup = ctx.io();
    if (depth > 0) {
      document.links()
          .map(ctx::validate)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .forEach(jsoup::submit);
    }

    Histogram histogram = document.text()
        .map(t ->
            Arrays.stream(t.split("[\\s+-.,/#!$%^&*;:{}\\[\\]=—_`~()|'\"?°′″·–•→<>†]"))
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(new HistogramCollector()))
        .orElse(Histogram.EMPTY);

    List<ForkJoinTask<Histogram>> forks = new LinkedList<>();
    for (Optional<Document> ref : jsoup) {
      // Optional has no forEach method
      ref.map(r -> forks.add(new HistogramTask(ctx, depth - 1, r).fork()));
    }

    return histogram.merge(
        forks.stream()
            .map(ForkJoinTask::join)
            .reduce(Histogram::merge)
            .orElse(Histogram.EMPTY));
  }
}
