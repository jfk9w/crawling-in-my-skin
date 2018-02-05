package jfk9w.crawler.executor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

import static java.util.Objects.requireNonNull;

public final class JsoupService implements Iterable<Document> {

  private static final Logger logger = LoggerFactory.getLogger(JsoupService.class);

  private final CompletionService<Document> executor;
  private long jobs = 0L;

  public JsoupService(Executor executor) {
    this.executor = new ExecutorCompletionService<>(executor);
  }

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
          Document doc = executor.take().get();
          idx++;
          return doc;
        } catch (Exception e) {
          logger.error("Error getting document", e);
          return null;
        }
      }
    };
  }
}
