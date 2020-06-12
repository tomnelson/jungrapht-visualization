package org.jungrapht.visualization.layout.algorithms.util;

import java.util.concurrent.Executor;

public interface Threaded {

  boolean isThreaded();

  void setThreaded(boolean threaded);

  void setExecutor(Executor executor);

  Executor getExecutor();
}
