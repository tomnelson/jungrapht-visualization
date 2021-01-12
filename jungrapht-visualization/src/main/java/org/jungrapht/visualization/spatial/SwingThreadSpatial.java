package org.jungrapht.visualization.spatial;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import org.jungrapht.visualization.layout.event.LayoutStateChange;
import org.jungrapht.visualization.layout.event.LayoutVertexPositionChange;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.spatial.rtree.TreeNode;

public class SwingThreadSpatial<T, NT> implements Spatial<T, NT> {

  protected Spatial<T, NT> spatial;

  public static <T, NT> Spatial<T, NT> of(Spatial<T, NT> delegate) {
    return new SwingThreadSpatial(delegate);
  }

  protected SwingThreadSpatial(Spatial<T, NT> delegate) {
    this.spatial = delegate;
  }

  public Spatial<T, NT> getSpatial() {
    return this.spatial;
  }

  @Override
  public void layoutStateChanged(LayoutStateChange.Event evt) {
    // if the layoutmodel is not active, then it is safe to activate this
    setActive(!evt.active);
    // if the layout model is finished, then rebuild the spatial data structure
    if (!evt.active) {
      recalculate();
      getLayoutModel().getModelChangeSupport().fireModelChanged(); // this will cause a repaint
    }
  }

  @Override
  public void setActive(boolean active) {
    spatial.setActive(active);
  }

  @Override
  public boolean isActive() {
    return spatial.isActive();
  }

  @Override
  public List<Shape> getGrid() {
    return spatial.getGrid();
  }

  @Override
  public Collection<Shape> getPickShapes() {
    return spatial.getPickShapes();
  }

  @Override
  public void clear() {
    SwingUtilities.invokeLater(() -> spatial.clear());
  }

  @Override
  public void recalculate() {
    SwingUtilities.invokeLater(() -> spatial.recalculate());
  }

  @Override
  public Rectangle2D getLayoutArea() {
    return spatial.getLayoutArea();
  }

  @Override
  public void setBounds(Rectangle2D bounds) {
    spatial.setBounds(bounds);
  }

  @Override
  public Rectangle2D getUnion(Rectangle2D rect, Point2D p) {
    return spatial.getUnion(rect, p);
  }

  @Override
  public Rectangle2D getUnion(Rectangle2D rect, double x, double y) {
    return spatial.getUnion(rect, x, y);
  }

  @Override
  public void update(T element, Point location) {
    spatial.update(element, location);
  }

  @Override
  public Set<? extends TreeNode> getContainingLeafs(Point2D p) {
    return spatial.getContainingLeafs(p);
  }

  @Override
  public Set<? extends TreeNode> getContainingLeafs(double x, double y) {
    return spatial.getContainingLeafs(x, y);
  }

  @Override
  public TreeNode getContainingLeaf(Object element) {
    return spatial.getContainingLeaf(element);
  }

  @Override
  public Set<T> getVisibleElements(Shape shape) {
    return spatial.getVisibleElements(shape);
  }

  @Override
  public T getClosestElement(Point2D p) {
    return spatial.getClosestElement(p);
  }

  @Override
  public T getClosestElement(double x, double y) {
    return spatial.getClosestElement(x, y);
  }

  @Override
  public LayoutModel getLayoutModel() {
    return spatial.getLayoutModel();
  }

  @Override
  public void layoutVertexPositionChanged(LayoutVertexPositionChange.Event<NT> evt) {
    spatial.layoutVertexPositionChanged(evt);
  }

  @Override
  public void layoutVertexPositionChanged(LayoutVertexPositionChange.GraphEvent<NT> evt) {
    spatial.layoutVertexPositionChanged(evt);
  }
}
