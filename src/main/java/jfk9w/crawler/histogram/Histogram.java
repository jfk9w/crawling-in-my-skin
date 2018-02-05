package jfk9w.crawler.histogram;

import com.google.common.base.Joiner;
import com.google.common.collect.Ordering;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

public final class Histogram {

  static final Histogram EMPTY = new Histogram(Collections.emptyMap());

  private static final Pattern PROBABLY_WORD_PATTERN = Pattern.compile("^.*\\D+.*$");

  private final Map<String, Integer> words;

  Histogram(Map<String, Integer> words) {
    this.words = Collections.unmodifiableMap(words);
  }

  public View topWords(int n) {
    checkArgument(n >= 0);
    return new View(
        Ordering.natural()
            .greatestOf(words.entrySet().stream()
                .parallel()
                .filter(e -> PROBABLY_WORD_PATTERN.matcher(e.getKey()).matches())
                .map(e -> new Item(e.getKey(), e.getValue()))
                .collect(Collectors.toList()), n)
    );
  }

  public static final class View {
    private final List<Item> rows;
    private View(List<Item> rows) {
      this.rows = Collections.unmodifiableList(rows);
    }
    @Override
    public String toString() {
      return Joiner.on("\n").join(rows);
    }
  }

  private final static class Item implements Comparable<Item> {
    private final String word;
    private final int frequency;
    private Item(String word, int frequency) {
      this.word = word;
      this.frequency = frequency;
    }
    @Override
    public int compareTo(Item o) {
      return Integer.compare(frequency, o.frequency);
    }
    @Override
    public String toString() {
      return Joiner.on(": ").join(word, frequency);
    }
  }

  Histogram merge(Histogram that) {
    if (this.words.isEmpty()) { return that; }
    if (that.words.isEmpty()) { return this; }
    Map<String, Integer> result = new HashMap<>(words);
    that.words.forEach((k, v) -> result.merge(k, v, (x, y) -> x + y));
    return new Histogram(result);
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
