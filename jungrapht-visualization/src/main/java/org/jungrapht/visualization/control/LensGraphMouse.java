package org.jungrapht.visualization.control;

import org.jungrapht.visualization.VisualizationViewer;

public interface LensGraphMouse extends VisualizationViewer.GraphMouse {
  void setKillSwitch(Runnable killSwitch);
}
