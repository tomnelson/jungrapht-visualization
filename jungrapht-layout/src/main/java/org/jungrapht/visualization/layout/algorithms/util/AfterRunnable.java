package org.jungrapht.visualization.layout.algorithms.util;

/** Used to cause a LayoutAlgorithm to call a Runnable once it has completed */
public interface AfterRunnable {

  default void setAfter(Runnable after) {}

  default void runAfter() {}
}
