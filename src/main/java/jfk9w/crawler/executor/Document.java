package jfk9w.crawler.executor;

import java.util.Optional;
import java.util.stream.Stream;

public interface Document {
  Stream<String> links();
  Optional<String> text();
}
