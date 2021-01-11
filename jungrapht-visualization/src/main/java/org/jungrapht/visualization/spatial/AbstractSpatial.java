package org.jungrapht.visualization.spatial;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.jungrapht.visualization.layout.event.LayoutStateChange;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.RadiusVertexAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <T> the element type that is managed by the spatial data structure. Node or Edge
 * @param <NT> the node type for the LayoutModel reference. Could be the same as T.
 */
public abstract class AbstractSpatial<T, NT> implements Spatial<T, NT> {

  private static Logger log = LoggerFactory.getLogger(AbstractSpatial.class);
  /** should this model actively update itself */
  boolean active = false;

  protected Rectangle2D rectangle;

  protected Collection<Shape> pickShapes =
      new LinkedList<>() {
        @Override
        public boolean add(Shape shape) {
          if (super.size() > 4) {
            super.remove();
          }
          return super.add(shape);
        }
      };

  /** a memoization of the grid rectangles used for rendering as Paintables for debugging */
  protected List<Shape> gridCache;

  /** the layoutModel that the structure operates on */
  protected LayoutModel<NT> layoutModel;

  RadiusVertexAccessor<NT> fallback;

  protected AbstractSpatial(LayoutModel<NT> layoutModel) {
    this.layoutModel = layoutModel;
    if (layoutModel != null) {
      this.rectangle =
          new Rectangle2D.Double(0, 0, layoutModel.getWidth(), layoutModel.getHeight());
      this.fallback = new RadiusVertexAccessor();
    }
  }

  public Collection<Shape> getPickShapes() {
    return pickShapes;
  }

  @Override
  public boolean isActive() {
    return active;
  }

  @Override
  public void setActive(boolean active) {
    gridCache = null;
    this.active = active;
  }

  @Override
  public LayoutModel<NT> getLayoutModel() {
    return this.layoutModel;
  }

  protected NT getClosest(Collection<NT> nodes, double x, double y, double radius) {

    // since I am comparing with distance squared, i need to square the radius
    double radiusSq = radius * radius;
    if (nodes.size() > 0) {
      double closestSoFar = Double.MAX_VALUE;
      NT winner = null;
      double winningDistance = -1;
      for (NT node : nodes) {
        Point loc = layoutModel.apply(node);
        double dist = loc.distanceSquared(x, y);

        // consider only nodes that are inside the search radius
        // and are closer than previously found nodes
        if (dist < radiusSq && dist < closestSoFar) {
          closestSoFar = dist;
          winner = node;
          winningDistance = dist;
        }
      }
      if (log.isTraceEnabled()) {
        log.trace("closest winner is {} at distance {}", winner, winningDistance);
      }
      return winner;
    } else {
      return null;
    }
  }

  @Override
  public void layoutStateChanged(LayoutStateChange.Event evt) {
    // if the layoutmodel is not active, then it is safe to activate this
    setActive(!evt.active);
    // if the layout model is finished, then rebuild the spatial data structure
    if (!evt.active) {
      recalculate();
      layoutModel.getModelChangeSupport().fireModelChanged(); // this will cause a repaint
    }
  }
}
