package org.jungrapht.visualization;

import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jgrapht.Graph;
import org.jungrapht.visualization.control.GraphMouseListener;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.control.TransformSupport;
import org.jungrapht.visualization.layout.GraphElementAccessor;
import org.jungrapht.visualization.layout.event.LayoutStateChange;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.spatial.Spatial;

public class DelegateVisualizationViewer<V, E> extends JPanel
    implements VisualizationViewer<V, E>, VisualizationComponent {

  public static <V1, E1> Builder<V1, E1, ?, ?> builder(
      VisualizationModel<V1, E1> visualizationModel) {
    return VisualizationViewer.builder(visualizationModel);
  }

  public JComponent getComponent() {
    return this;
  }

  @Override
  public void setGraphMouse(GraphMouse graphMouse) {
    delegate.setGraphMouse(graphMouse);
  }

  @Override
  public GraphMouse getGraphMouse() {
    return delegate.getGraphMouse();
  }

  @Override
  public void addGraphMouseListener(GraphMouseListener<V> graphMouseListener) {
    delegate.addGraphMouseListener(graphMouseListener);
  }

  @Override
  public void addKeyListener(KeyListener l) {
    delegate.addKeyListener(l);
  }

  @Override
  public void setEdgeToolTipFunction(Function<E, String> edgeToolTipFunction) {
    delegate.setEdgeToolTipFunction(edgeToolTipFunction);
  }

  @Override
  public void setMouseEventToolTipFunction(Function<MouseEvent, String> mouseEventToolTipFunction) {
    delegate.setMouseEventToolTipFunction(mouseEventToolTipFunction);
  }

  @Override
  public void setVertexToolTipFunction(Function<V, String> vertexToolTipFunction) {
    delegate.setVertexToolTipFunction(vertexToolTipFunction);
  }

  @Override
  public String getToolTipText(MouseEvent event) {
    return delegate.getToolTipText(event);
  }

  @Override
  public void setToolTipText(String toolTipText) {
    delegate.setToolTipText(toolTipText);
  }

  @Override
  public void setDoubleBuffered(boolean doubleBuffered) {
    delegate.setDoubleBuffered(doubleBuffered);
  }

  @Override
  public boolean isDoubleBuffered() {
    return delegate.isDoubleBuffered();
  }

  @Override
  public Shape viewOnLayout() {
    return delegate.viewOnLayout();
  }

  @Override
  public Spatial<V> getVertexSpatial() {
    return delegate.getVertexSpatial();
  }

  @Override
  public void setVertexSpatial(Spatial<V> spatial) {
    delegate.setVertexSpatial(spatial);
  }

  @Override
  public Spatial<E> getEdgeSpatial() {
    return delegate.getEdgeSpatial();
  }

  @Override
  public void setEdgeSpatial(Spatial<E> spatial) {
    delegate.setEdgeSpatial(spatial);
  }

  @Override
  public TransformSupport<V, E> getTransformSupport() {
    return delegate.getTransformSupport();
  }

  @Override
  public void setTransformSupport(TransformSupport<V, E> transformSupport) {
    delegate.setTransformSupport(transformSupport);
  }

  @Override
  public VisualizationModel<V, E> getVisualizationModel() {
    return delegate.getVisualizationModel();
  }

  @Override
  public void setVisualizationModel(VisualizationModel<V, E> visualizationModel) {
    delegate.setVisualizationModel(visualizationModel);
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    delegate.stateChanged(e);
  }

  @Override
  public Renderer<V, E> getRenderer() {
    return delegate.getRenderer();
  }

  @Override
  public void setVisible(boolean aFlag) {
    delegate.setVisible(aFlag);
  }

  @Override
  public Map<RenderingHints.Key, Object> getRenderingHints() {
    return delegate.getRenderingHints();
  }

  @Override
  public void setRenderingHints(Map<RenderingHints.Key, Object> renderingHints) {
    delegate.setRenderingHints(renderingHints);
  }

  @Override
  public void prependPreRenderPaintable(Paintable paintable) {
    delegate.prependPreRenderPaintable(paintable);
  }

  @Override
  public void addPreRenderPaintable(Paintable paintable) {
    delegate.addPreRenderPaintable(paintable);
  }

  @Override
  public void removePreRenderPaintable(Paintable paintable) {
    delegate.removePreRenderPaintable(paintable);
  }

  @Override
  public void addPostRenderPaintable(Paintable paintable) {
    delegate.addPostRenderPaintable(paintable);
  }

  @Override
  public void removePostRenderPaintable(Paintable paintable) {
    delegate.removePostRenderPaintable(paintable);
  }

  @Override
  public void addChangeListener(ChangeListener l) {
    delegate.addChangeListener(l);
  }

  @Override
  public void removeChangeListener(ChangeListener l) {
    delegate.removeChangeListener(l);
  }

  @Override
  public ChangeListener[] getChangeListeners() {
    return delegate.getChangeListeners();
  }

  @Override
  public void fireStateChanged() {
    delegate.fireStateChanged();
  }

  @Override
  public MutableSelectedState<V> getSelectedVertexState() {
    return delegate.getSelectedVertexState();
  }

  @Override
  public MutableSelectedState<E> getSelectedEdgeState() {
    return delegate.getSelectedEdgeState();
  }

  @Override
  public void setSelectedVertexState(MutableSelectedState<V> selectedVertexState) {
    delegate.setSelectedVertexState(selectedVertexState);
  }

  @Override
  public void setSelectedEdgeState(MutableSelectedState<E> selectedEdgeState) {
    delegate.setSelectedEdgeState(selectedEdgeState);
  }

  @Override
  public GraphElementAccessor<V, E> getPickSupport() {
    return delegate.getPickSupport();
  }

  @Override
  public void setPickSupport(GraphElementAccessor<V, E> pickSupport) {
    delegate.setPickSupport(pickSupport);
  }

  @Override
  public Point2D getCenter() {
    return delegate.getCenter();
  }

  @Override
  public RenderContext<V, E> getRenderContext() {
    return delegate.getRenderContext();
  }

  @Override
  public void setRenderContext(RenderContext<V, E> renderContext) {
    delegate.setRenderContext(renderContext);
  }

  @Override
  public void repaint() {
    delegate.repaint();
  }

  @Override
  public void scaleToLayout(ScalingControl scaler) {
    delegate.scaleToLayout(scaler);
  }

  @Override
  public void scaleToLayout() {
    delegate.scaleToLayout();
  }

  public static <V1, E1> Builder<V1, E1, ?, ?> builder(Graph<V1, E1> graph) {
    return VisualizationViewer.builder(graph);
  }

  protected VisualizationViewer<V, E> delegate;

  public DelegateVisualizationViewer(VisualizationViewer<V, E> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void modelChanged() {
    this.delegate.modelChanged();
  }

  @Override
  public void viewChanged() {
    this.delegate.viewChanged();
  }

  @Override
  public void layoutStateChanged(LayoutStateChange.Event evt) {
    this.delegate.layoutStateChanged(evt);
  }

  //  @Override
  //  public void layoutVertexPositionChanged(LayoutVertexPositionChange.Event evt) {
  //    delegate.repaint();
  //  }
  //
  //  @Override
  //  public void layoutVertexPositionChanged(LayoutVertexPositionChange.GraphEvent evt) {
  //    delegate.repaint();
  //  }
}
