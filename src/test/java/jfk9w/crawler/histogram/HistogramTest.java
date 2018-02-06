package jfk9w.crawler.histogram;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static com.google.common.collect.ImmutableMap.of;
import static org.junit.Assert.assertEquals;

public class HistogramTest {

  @Test
  public void topWords() throws Exception {
    Histogram histogram = new Histogram(of(
        "aaa", 1,
        "ccc", 2,
        "bbb", 2,
        "ddd", 3
    ));

    assertEquals(new Histogram.View(ImmutableList.of()), histogram.topWords(0));
    assertEquals(new Histogram.View(ImmutableList.of(
        new Histogram.Item("ddd", 3)
    )), histogram.topWords(1));
    assertEquals(new Histogram.View(ImmutableList.of(
            new Histogram.Item("ddd", 3),
            new Histogram.Item("bbb", 2)
    )), histogram.topWords(2));
    assertEquals(new Histogram.View(ImmutableList.of(
        new Histogram.Item("ddd", 3),
        new Histogram.Item("bbb", 2),
        new Histogram.Item("ccc", 2)
    )), histogram.topWords(3));
    assertEquals(new Histogram.View(ImmutableList.of(
        new Histogram.Item("ddd", 3),
        new Histogram.Item("bbb", 2),
        new Histogram.Item("ccc", 2),
        new Histogram.Item("aaa", 1)
    )), histogram.topWords(100));
  }
}
