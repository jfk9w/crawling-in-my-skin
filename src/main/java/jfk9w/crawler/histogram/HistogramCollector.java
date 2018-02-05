package jfk9w.crawler.histogram;

import com.google.common.collect.ImmutableSet;
import jfk9w.crawler.util.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

final class HistogramCollector
    implements Collector<String, Map<String, Integer>, Histogram> {

  @Override
  public Supplier<Map<String, Integer>> supplier() {
    return HashMap::new;
  }

  @Override
  public BiConsumer<Map<String, Integer>, String> accumulator() {
    return (acc, k) -> acc.merge(k, 1, (a, b) -> a + b);
  }

  @Override
  public BinaryOperator<Map<String, Integer>> combiner() {
    return Utils::merge;
  }

  @Override
  public Function<Map<String, Integer>, Histogram> finisher() {
    return Histogram::new;
  }

  @Override
  public Set<Characteristics> characteristics() {
    return ImmutableSet.of(Characteristics.UNORDERED, Characteristics.CONCURRENT);
  }
}
