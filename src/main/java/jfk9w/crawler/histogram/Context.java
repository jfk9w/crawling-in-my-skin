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

public abstract class Context {

  private static final Pattern DOMAIN_REGEXP =
      Pattern.compile("(?:https?:\\/\\/)?(?:[^@\\/\\n]+@)?(?:www\\.)?([^:\\/\\n]+)");

  private final String domain;
  private final ConcurrentMap<String, Boolean> done = new ConcurrentHashMap<>();

  protected Context(String domain) {
    this.domain = domain;
  }

  static Context create(String url, Executor io) {
    String domain = extract(url)
        .orElseThrow(() ->
            new IllegalArgumentException("Invalid URL: " + url));

    Context ctx = new DefaultContext(domain, io);
    ctx.done.put(strip(url), true);
    return ctx;
  }

  public final Optional<String> validate(String url) {
    url = strip(url);
    if (extract(url).map(d -> d.equals(domain)).orElse(false)
        && done.putIfAbsent(url, true) == null) {
      return Optional.of(url);
    }

    return Optional.empty();
  }

  abstract JsoupService io();

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

  private static final class DefaultContext extends Context {
    private final Executor io;
    private DefaultContext(String domain, Executor io) {
      super(domain);
      this.io = io;
    }
    @Override
    JsoupService io() {
      return new JsoupServiceImpl(io);
    }
  }
}