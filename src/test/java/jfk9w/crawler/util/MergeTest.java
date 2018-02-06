package jfk9w.crawler.util;

import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static org.junit.Assert.assertEquals;

public class MergeTest {

  @Test
  public void basic() throws Exception {
    Map<String, Integer> o1 = of(
        "o1", 1,
        "both", 3);
    Map<String, Integer> o2 = of(
        "o2", 2,
        "both", 3
    );

    Map<String, Integer> sample = of(
        "o1", 1,
        "o2", 2,
        "both", 6
    );

    assertEquals(sample, Utils.merge(o1, o2));
    assertEquals(sample, Utils.merge(o2, o1));
  }

  @Test
  public void identity() throws Exception {
    Map<String, Integer> o1 = of("test", 0);
    Map<String, Integer> id = of();

    assert o1 == Utils.merge(o1, id);
    assert o1 == Utils.merge(id, o1);
  }

}
