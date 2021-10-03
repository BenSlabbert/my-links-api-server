package com.github.benslabbert.mylinks.thread;

import java.util.concurrent.ThreadFactory;

public record MyThreadFactory(String prefix) implements ThreadFactory {

  @Override
  public Thread newThread(Runnable r) {
    var thread = new Thread(r);
    thread.setName(prefix + "-" + thread.getId());
    return thread;
  }
}
