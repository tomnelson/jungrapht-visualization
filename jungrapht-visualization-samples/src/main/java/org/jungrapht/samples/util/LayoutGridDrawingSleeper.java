package org.jungrapht.samples.util;

import java.awt.*;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.event.ModelChange;
import org.jungrapht.visualization.layout.model.LayoutModel;

public class LayoutGridDrawingSleeper<V, E> implements ModelChange.Listener {

  protected LayoutModel<V> layoutModel;
  protected VisualizationServer.Paintable gridPaintable;

  protected long sleepDuration = 1000L;

  public LayoutGridDrawingSleeper(
      LayoutModel<V> layoutModel, VisualizationServer.Paintable gridPaintable) {
    this.layoutModel = layoutModel;
    this.gridPaintable = gridPaintable;
  }

  public static class LayoutGridPaintable<V> implements VisualizationServer.Paintable {

    protected LayoutModel<V> layoutModel;

    @Override
    public void paint(Graphics g) {}

    @Override
    public boolean useTransform() {
      return false;
    }
  }

  /** on model change, resize the layout for best fit */
  @Override
  public void modelChanged() {}
}
