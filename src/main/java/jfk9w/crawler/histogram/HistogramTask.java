package jfk9w.crawler.histogram;

import jfk9w.crawler.executor.Document;
import jfk9w.crawler.executor.JsoupService;

import java.util.*;
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

    Map<String, Integer> hg = new HashMap<>();
    Optional<String> text = document.text();
    if (text.isPresent()) {
      String[] words = text.get().split("[\\s+-.,/#!$%^&*;:{}\\[\\]=—_`~()|'\"?°′″·–•→<>†]");
      for (String word : words) {
        if (word != null && !word.isEmpty()) {
          hg.merge(word.toLowerCase(), 1, (x, y) -> x + y);
        }
      }
    }

    List<ForkJoinTask<Histogram>> forks = new LinkedList<>();
    for (Optional<Document> ref : jsoup) {
      // Optional has no forEach method
      ref.map(r -> forks.add(new HistogramTask(ctx, depth - 1, r).fork()));
    }

    forks.stream()
        .map(ForkJoinTask::join)
        .forEach(h -> h.dump(hg));

    return new Histogram(hg);
  }
}
