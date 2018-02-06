package jfk9w.crawler.histogram;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import jfk9w.crawler.executor.Document;
import jfk9w.crawler.executor.JsoupService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class HistogramTaskTest {

  private ForkJoinPool pure;

  @Before
  public void setUp() {
    pure = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
  }

  @After
  public void tearDown() {
    pure.shutdown();
  }

  @Test
  public void basic() throws Exception {
    Context ctx = new TestContext("https://anotherdomain.com");
    HistogramTask task = HistogramTask.withContext("https://anotherdomain.com", 0, ctx);

    Histogram sample = new Histogram(
        ImmutableMap.of(
            "another", 1,
            "domain", 2
        ));

    assertEquals(sample, pure.submit(task).join());
  }

  @Test
  public void one() throws Exception {
    Context ctx = new TestContext("https://example.com");
    HistogramTask task = HistogramTask.withContext("https://example.com", 1, ctx);

    Histogram sample = new Histogram(
        ImmutableMap.<String, Integer>builder()
            .put("human", 2)
            .put("hello", 2)
            .put("goodbye", 1)
            .put("world", 1)
            .build()
    );

    assertEquals(sample, pure.submit(task).join());
  }

  @Test
  public void two() throws Exception {
    Context ctx = new TestContext("https://example.com");
    HistogramTask task = HistogramTask.withContext("https://example.com", 2, ctx);

    Histogram sample = new Histogram(
        ImmutableMap.<String, Integer>builder()
            .put("hello", 3)
            .put("human", 2)
            .put("world", 2)
            .put("goodbye", 1)
            .build()
    );

    assertEquals(sample, pure.submit(task).join());
  }

  private static final class TestContext extends Context {

    public TestContext(String domain) {
      super(domain);
    }

    @Override
    public JsoupService io() {
      class Impl implements JsoupService {
        private final Queue<Optional<Document>> queue = new ArrayBlockingQueue<>(10);

        @Override
        public Future<Document> submit(String url) {
          queue.add(Optional.ofNullable(pages.get(url)));
          return CompletableFuture.completedFuture(pages.get(url));
        }

        @Override
        public Iterator<Optional<Document>> iterator() {
          return queue.iterator();
        }
      }

      return new Impl();
    }
  }

  private static final Map<String, Document> pages =
      ImmutableMap.<String, Document>builder()
          .put("https://example.com", doc("Hello, world", "https://example.com/A", "https://example.com/B"))
          .put("https://example.com/A", doc("Hello, human", "https://example.com/AA", "https://anotherdomain.com"))
          .put("https://example.com/AA", doc("Hello, world", "https://example.com/AAA"))
          .put("https://anotherdomain.com", doc("Another domain DOMAIN"))
          .put("https://example.com/B", doc("Goodbye, human"))
          .build();

  private static Document doc(String text, String... links) {
    return new BasicDocument(ImmutableList.copyOf(links), text);
  }

  private static final class BasicDocument implements Document {
    private final List<String> links;
    private final String text;

    BasicDocument(List<String> links, String text) {
      this.links = links;
      this.text = text;
    }

    @Override
    public Stream<String> links() {
      return links.stream();
    }

    @Override
    public Optional<String> text() {
      return Optional.ofNullable(text);
    }
  }
}
