package jfk9w.crawler.executor;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.*;

import static java.util.Objects.requireNonNull;

public final class JsoupServiceImpl implements JsoupService {

  private static final Logger logger = LoggerFactory.getLogger(JsoupServiceImpl.class);

  private final CompletionService<Document> executor;
  private long jobs = 0L;

  public JsoupServiceImpl(Executor executor) {
    this.executor = new ExecutorCompletionService<>(executor);
  }

  @Override
  public Future<Document> submit(String url) {
    requireNonNull(url);
    jobs++;
    return executor.submit(Jsoup.connect(url)::get);
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
              logger.warn("{}: {} {}", cause.getMessage(),
                  ((HttpStatusException) cause).getUrl(),
                  ((HttpStatusException) cause).getStatusCode());
            } else if (cause instanceof UnsupportedMimeTypeException) {
              logger.warn("{}: {} {}", cause.getMessage(),
                  ((UnsupportedMimeTypeException) cause).getUrl(),
                  ((UnsupportedMimeTypeException) cause).getMimeType());
            } else {
              logger.error("Unknown error", e);
            }
          } else {
            logger.error("Unknown error", e);
          }

          return null;
        } catch (Exception e) {
          logger.error("Unknown error", e);
          return null;
        } finally {
          idx++;
        }
      }
    };
  }
}
