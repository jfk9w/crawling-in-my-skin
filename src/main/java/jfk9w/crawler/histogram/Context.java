package jfk9w.crawler.histogram;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

final class Context {

  private static final Pattern DOMAIN_REGEXP =
      Pattern.compile("(?:https?:\\/\\/)?(?:[^@\\/\\n]+@)?(?:www\\.)?([^:\\/\\n]+)");

  private final String domain;
  private final ConcurrentMap<String, Boolean> done = new ConcurrentHashMap<>();

  static Context create(String url) {
    Context ctx = new Context(extract(url)
        .orElseThrow(() -> new IllegalArgumentException("Invalid URL: " + url)));

    ctx.done.put(strip(url), true);
    return ctx;
  }

  private Context(String domain) {
    this.domain = domain;
  }

  boolean proceed(String url) {
    requireNonNull(url);
    url = strip(url);
    return extract(url).map(d -> d.equals(domain)).orElse(false)
        && done.put(url, true) == null;
  }

  private static String strip(String url) {
    url = url.trim();
    if (url.endsWith("/")) {
      return strip(url.substring(0, url.length() - 1));
    }

    return url;
  }

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
