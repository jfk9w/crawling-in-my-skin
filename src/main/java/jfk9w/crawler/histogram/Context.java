package jfk9w.crawler.histogram;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

final class Context {

  private static final Pattern DOMAIN_REGEXP =
      Pattern.compile("(?:https?:\\/\\/)?(?:[^@\\/\\n]+@)?(?:www\\.)?([^:\\/\\n]+)");

  final Executor io;

  private final String domain;
  private final ConcurrentMap<String, Boolean> done = new ConcurrentHashMap<>();

  static Context create(String url, Executor io) {
    String domain = extract(url)
        .orElseThrow(() ->
            new IllegalArgumentException("Invalid URL: " + url));

    Context ctx = new Context(domain, io);
    ctx.done.put(strip(url), true);
    return ctx;
  }

  private Context(String domain, Executor io) {
    this.domain = domain;
    this.io = io;
  }

  boolean proceed(String url) {
    return extract(url).map(d -> d.equals(domain)).orElse(false)
        && done.put(strip(url), true) == null;
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
