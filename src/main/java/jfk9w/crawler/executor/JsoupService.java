package jfk9w.crawler.executor;

import java.util.Optional;
import java.util.concurrent.Future;

public interface JsoupService extends Iterable<Optional<Document>> {
  Future<Document> submit(String url);
}
