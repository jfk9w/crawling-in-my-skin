package jfk9w.crawler.histogram;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

final class Context {

  final int maxDepth;
  private final String domain;
  private final ConcurrentMap<String, Boolean> done = new ConcurrentHashMap<>();

  static Context create(String url, int maxDepth) {
    checkArgument(maxDepth >= 0);
    return new Context(maxDepth, extract(url).orElseThrow(IllegalArgumentException::new));
  }

  private Context(int maxDepth, String domain) {
    this.maxDepth = maxDepth;
    this.domain = domain;
  }

  boolean proceed(String url) {
    return extract(url).map(d -> d.equals(domain)).orElse(false)
        && done.put(url, true) == null;
  }

  private static final Pattern DOMAIN_REGEXP =
      Pattern.compile("(?:https?:\\/\\/)?(?:[^@\\/\\n]+@)?(?:www\\.)?([^:\\/\\n]+)");

  private static Optional<String> extract(String url) {
    checkNotNull(url);
    Matcher m = DOMAIN_REGEXP.matcher(url.toLowerCase());
    if (m.find()) {
      return Optional.of(m.group());
    } else {
      return Optional.empty();
    }
  }
}
