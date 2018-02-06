package jfk9w.crawler.executor;

import org.jsoup.nodes.Document;

import java.util.concurrent.Future;

public interface JsoupService extends Iterable<Document> {
  Future<Document> submit(String url);
}
