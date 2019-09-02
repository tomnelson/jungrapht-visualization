/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Aug 15, 2005
 */

package org.jungrapht.visualization;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.ModalSatelliteGraphMouse;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.spatial.Spatial;
import org.jungrapht.visualization.transform.MutableAffineTransformer;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;

/**
 * A VisualizationViewer that can act as a satellite view for another (master) VisualizationViewer.
 * In this view, the full graph is always visible and all mouse actions affect the graph in the
 * master view.
 *
 * <p>A rectangular shape in the satellite view shows the visible bounds of the master view.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class DefaultSatelliteVisualizationViewer<V, E> extends DefaultVisualizationViewer<V, E>
    implements SatelliteVisualizationViewer<V, E> {

  /** the master VisualizationViewer that this is a satellite view for */
  VisualizationViewer<V, E> master;

  DefaultSatelliteVisualizationViewer(SatelliteVisualizationViewer.Builder<V, E, ?, ?> builder) {
    super(builder);
    this.master = builder.master;
    // create a graph mouse with custom plugins to affect the master view
    ModalGraphMouse gm = new ModalSatelliteGraphMouse();
    setGraphMouse(gm);

    // this adds the Lens to the satellite view
    addPreRenderPaintable(new ViewLens<>(this, master));

    // get a copy of the current layout transform
    // it may have been scaled to fit the graph
    AffineTransform modelLayoutTransform =
        new AffineTransform(
            master
                .getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
                .getTransform());

    // I want no layout transformations in the satellite view
    // this resets the auto-scaling that occurs in the super constructor
    getRenderContext()
        .getMultiLayerTransformer()
        .setTransformer(
            MultiLayerTransformer.Layer.LAYOUT, new MutableAffineTransformer(modelLayoutTransform));

    // make sure the satellite listens for changes in the master
    master.addChangeListener(this);

    // share the selected state of the master
    setSelectedVertexState(master.getSelectedVertexState());
    setSelectedEdgeState(master.getSelectedEdgeState());
    setVertexSpatial(new Spatial.NoOp.Vertex(visualizationModel.getLayoutModel()));
    setEdgeSpatial(new Spatial.NoOp.Edge(visualizationModel));
  }

  /**
   * override to not use the spatial data structure, as this view will always show the entire graph
   *
   * @param g2d graphics context
   */
  @Override
  protected void renderGraph(Graphics2D g2d) {
    if (renderContext.getGraphicsContext() == null) {
      renderContext.setGraphicsContext(new GraphicsDecorator(g2d));
    } else {
      renderContext.getGraphicsContext().setDelegate(g2d);
    }
    renderContext.setScreenDevice(this);
    LayoutModel<V> layoutModel = getVisualizationModel().getLayoutModel();

    g2d.setRenderingHints(renderingHints);

    // the layoutSize of the VisualizationViewer
    Dimension d = getSize();

    // clear the offscreen image
    g2d.setColor(getBackground());
    g2d.fillRect(0, 0, d.width, d.height);

    AffineTransform oldXform = g2d.getTransform();
    AffineTransform newXform = new AffineTransform(oldXform);
    newXform.concatenate(
        renderContext
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.VIEW)
            .getTransform());

    g2d.setTransform(newXform);

    // if there are  preRenderers set, paint them
    for (Paintable paintable : preRenderers) {

      if (paintable.useTransform()) {
        paintable.paint(g2d);
      } else {
        g2d.setTransform(oldXform);
        paintable.paint(g2d);
        g2d.setTransform(newXform);
      }
    }

    renderer.render(renderContext, visualizationModel);

    // if there are postRenderers set, do it
    for (Paintable paintable : postRenderers) {

      if (paintable.useTransform()) {
        paintable.paint(g2d);
      } else {
        g2d.setTransform(oldXform);
        paintable.paint(g2d);
        g2d.setTransform(newXform);
      }
    }
    g2d.setTransform(oldXform);
  }

  /** @return Returns the master. */
  public VisualizationViewer<V, E> getMaster() {
    return master;
  }
}
