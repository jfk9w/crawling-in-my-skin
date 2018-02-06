package jfk9w.crawler.histogram;

import jfk9w.crawler.executor.JsoupService;

import java.util.concurrent.Executor;

public interface Context {
  boolean check(String url);
  JsoupService io();
}