package org.jungrapht.visualization.layout.algorithms.util;

public interface Threaded {

  boolean isThreaded();

  void setThreaded(boolean threaded);

  void cancel();

  static Threaded noop() {
    if (NoOp.INSTANCE == null) {
      NoOp.INSTANCE = new NoOp();
    }
    return NoOp.INSTANCE;
  }

  class NoOp implements Threaded {

    static Threaded INSTANCE;

    @Override
    public boolean isThreaded() {
      return false;
    }

    @Override
    public void setThreaded(boolean threaded) {}

    @Override
    public void cancel() {}
  }
}
