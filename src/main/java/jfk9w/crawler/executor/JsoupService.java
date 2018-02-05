package jfk9w.crawler.executor;

import jfk9w.crawler.util.Timer;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;

import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Objects.requireNonNull;

public final class JsoupService implements Iterable<Document> {

  private static final AtomicLong time = new AtomicLong();

  private final CompletionService<Document> executor;
  private long jobs = 0L;

  public JsoupService(Executor executor) {
    this.executor = new ExecutorCompletionService<>(executor);
  }

  public static long time() {
    return time.get();
  }

  public Future<Document> submit(String url) {
    requireNonNull(url);
    jobs++;
    return executor.submit(() -> {
      Timer t = Timer.start();
      try {
        return Jsoup.connect(url).get();
      } finally {
        time.addAndGet(t.stop());
      }
    });
  }

  @Override
  public Iterator<Document> iterator() {
    return new Iterator<Document>() {
      private long idx = 0;
      @Override
      public boolean hasNext() {
        return idx < jobs;
      }
      @Override
      public Document next() {
        try {
          return executor.take().get();
        } catch (ExecutionException e) {
          Throwable cause = e.getCause();
          if (cause != null) {
            if (cause instanceof HttpStatusException) {
              System.err.printf("%s: %s %d\n", cause.getMessage(),
                  ((HttpStatusException) cause).getUrl(),
                  ((HttpStatusException) cause).getStatusCode());
            } else if (cause instanceof UnsupportedMimeTypeException) {
              System.err.printf("%s: %s %s\n", cause.getMessage(),
                  ((UnsupportedMimeTypeException) cause).getUrl(),
                  ((UnsupportedMimeTypeException) cause).getMimeType());
            } else {
              cause.printStackTrace();
            }
          } else {
            e.printStackTrace();
          }

          return null;
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        } finally {
          idx++;
        }
      }
    };
  }
}
