package org.jungrapht.visualization.layout.algorithms.util;

import java.util.concurrent.Executor;

public interface ExecutorConsumer {

  void setExecutor(Executor executor);

  Executor getExecutor();
}
