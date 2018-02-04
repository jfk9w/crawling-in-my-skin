package jfk9w.crawler.histogram;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

final class HistogramContext {

  final int maxDepth;
  private final String domain;
  private final ConcurrentMap<String, Boolean> done = new ConcurrentHashMap<>();

  private HistogramContext(String domain, int maxDepth) {
    this.domain = domain;
    this.maxDepth = maxDepth;
  }

  static HistogramContext create(String url, int maxDepth) {
    checkArgument(maxDepth >= 0);
    return new HistogramContext(
        extractDomain(url)
            .orElseThrow(() -> new IllegalArgumentException("invalid url " + url)),
        maxDepth);
  }

  boolean shouldParse(String url) {
    return extractDomain(url).map(d -> d.equals(domain)).orElse(false) && done.put(url, true) == null;
  }

  private static final Pattern extractor = Pattern.compile("(?:https?:\\/\\/)?(?:[^@\\/\\n]+@)?(?:www\\.)?([^:\\/\\n]+)");

  private static Optional<String> extractDomain(String url) {
    checkNotNull(url);
    Matcher m = extractor.matcher(url.toLowerCase());
    if (m.find()) {
      return Optional.of(m.group());
    } else {
      return Optional.empty();
    }
  }
}
