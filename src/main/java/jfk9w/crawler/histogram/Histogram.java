package jfk9w.crawler.histogram;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Ordering;
import jfk9w.crawler.util.Utils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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
    View(List<Item> rows) {
      this.rows = Collections.unmodifiableList(rows);
    }
    @Override
    public String toString() {
      return Joiner.on("\n").join(rows);
    }
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      View view = (View) o;
      return Objects.equals(rows, view.rows);
    }
    @Override
    public int hashCode() {
      return Objects.hash(rows);
    }
  }

  final static class Item implements Comparable<Item> {
    private final String word;
    private final int frequency;
    Item(String word, int frequency) {
      this.word = checkNotNull(word);
      this.frequency = frequency;
    }
    @Override
    public int compareTo(Item o) {
      int r = Integer.compare(frequency, o.frequency);
      if (r != 0) {
        return r;
      }
      return o.word.compareTo(word);
    }
    @Override
    public String toString() {
      return Joiner.on(": ").join(word, frequency);
    }
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Item item = (Item) o;
      return frequency == item.frequency &&
          Objects.equals(word, item.word);
    }
    @Override
    public int hashCode() {
      return Objects.hash(word, frequency);
    }
  }

  // Requires mutable target.
  void dump(Map<String, Integer> target) {
    words.forEach((k, v) -> target.merge(k, v, (x, y) -> x + y));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("words", words)
        .toString();
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
