package jfk9w.crawler.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Utils {

  public static Map<String, Integer> merge(Map<String, Integer> one, Map<String, Integer> another) {
    if (one.isEmpty()) { return another; }
    if (another.isEmpty()) { return one; }
    Map<String, Integer> result = new HashMap<>(one);
    another.forEach((k, v) -> result.merge(k, v, (x, y) -> x + y));
    return Collections.unmodifiableMap(result);
  }

  private Utils() { }
}
