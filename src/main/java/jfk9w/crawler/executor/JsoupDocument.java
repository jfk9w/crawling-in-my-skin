package jfk9w.crawler.executor;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

final class JsoupDocument implements Document {

  private final org.jsoup.nodes.Document document;

  JsoupDocument(org.jsoup.nodes.Document document) {
    this.document = document;
  }

  @Override
  public Stream<String> links() {
    return document.select("a[href]").stream()
        .map(e -> e.attr("abs:href"))
        .filter(Objects::nonNull);
  }

  @Override
  public Optional<String> text() {
    if (document.body() != null) {
      return Optional.ofNullable(document.body().text());
    }

    return Optional.empty();
  }
}
