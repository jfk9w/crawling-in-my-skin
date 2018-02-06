package jfk9w.crawler.histogram;

import jfk9w.crawler.executor.JsoupService;
import jfk9w.crawler.executor.JsoupServiceImpl;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

final class DefaultContext implements Context {

  private static final Pattern DOMAIN_REGEXP =
      Pattern.compile("(?:https?:\\/\\/)?(?:[^@\\/\\n]+@)?(?:www\\.)?([^:\\/\\n]+)");

  private final ConcurrentMap<String, Boolean> done = new ConcurrentHashMap<>();

  private final String domain;
  private final Executor io;

  private DefaultContext(String domain, Executor io) {
    this.domain = domain;
    this.io = io;
  }

  static Context create(String url, Executor io) {
    String domain = DefaultContext.extract(url)
        .orElseThrow(() ->
            new IllegalArgumentException("Invalid URL: " + url));

    DefaultContext ctx = new DefaultContext(domain, io);
    ctx.done.put(DefaultContext.strip(url), true);
    return ctx;
  }

  @Override
  public boolean check(String url) {
    return extract(url).map(d -> d.equals(domain)).orElse(false)
        && done.put(strip(url), true) == null;
  }

  @Override
  public JsoupService io() {
    return new JsoupServiceImpl(io);
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
