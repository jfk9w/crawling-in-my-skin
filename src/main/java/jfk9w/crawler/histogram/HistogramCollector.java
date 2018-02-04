package jfk9w.crawler.histogram;

import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class HistogramCollector<T>
    implements Collector<T, Map<T, Integer>, Map<T, Integer>> {

  @Override
  public Supplier<Map<T, Integer>> supplier() {
    return HashMap::new;
  }

  @Override
  public BiConsumer<Map<T, Integer>, T> accumulator() {
    return (acc, k) -> acc.merge(k, 1, (a, b) -> a + b);
  }

  @Override
  public BinaryOperator<Map<T, Integer>> combiner() {
    return (a, b) -> {
      Map<T, Integer> r = new HashMap<>(a);
      b.forEach((k, v) -> r.merge(k, v, (x, y) -> x + y));
      return r;
    };
  }

  @Override
  public Function<Map<T, Integer>, Map<T, Integer>> finisher() {
    return Function.identity();
  }

  @Override
  public Set<Characteristics> characteristics() {
    return ImmutableSet.of(Characteristics.UNORDERED, Characteristics.IDENTITY_FINISH, Characteristics.CONCURRENT);
  }
}
