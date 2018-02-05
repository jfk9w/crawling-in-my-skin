package jfk9w.crawler.histogram;

import com.google.common.base.Joiner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Histogram {

  public static final Histogram EMPTY = new Histogram(Collections.emptyMap());

  private final Map<String, Integer> words;

  Histogram(Map<String, Integer> words) {
    this.words = Collections.unmodifiableMap(words);
  }

  Histogram merge(Histogram that) {
    Map<String, Integer> result = new HashMap<>(words);
    that.words.forEach((k, v) -> result.merge(k, v, (x, y) -> x + y));
    return new Histogram(result);
  }

  private static final Joiner.MapJoiner joiner = Joiner.on("\n").withKeyValueSeparator(": ");

  @Override
  public String toString() {
    return joiner.join(words);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Histogram histogram = (Histogram) o;
    return Objects.equals(words, histogram.words);
  }

  @Override
  public int hashCode() {
    return Objects.hash(words);
  }
}
