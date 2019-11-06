package org.jungrapht.visualization.layout.algorithms.util;

/** Used to cause a LayoutAlgorithm to call vv::scaleToLayout once it has completed */
public interface AfterRunnable extends Runnable {

  void setAfter(Runnable after);
}
