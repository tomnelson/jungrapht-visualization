/*
\* Copyright (c) 2003, The JUNG Authors
*
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
*/
package org.jungrapht.visualization;

import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jgrapht.Graph;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.control.TransformSupport;
import org.jungrapht.visualization.layout.GraphElementAccessor;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.util.EdgeShapeFunctionSupplier;
import org.jungrapht.visualization.layout.algorithms.util.VertexShapeAware;
import org.jungrapht.visualization.layout.event.LayoutStateChange;
import org.jungrapht.visualization.layout.event.LayoutVertexPositionChange;
import org.jungrapht.visualization.layout.event.RenderContextStateChange;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.renderers.DefaultModalRenderer;
import org.jungrapht.visualization.renderers.ModalRenderer;
import org.jungrapht.visualization.selection.MultiMutableSelectedState;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.selection.ShapePickSupport;
import org.jungrapht.visualization.spatial.Spatial;
import org.jungrapht.visualization.spatial.SpatialGrid;
import org.jungrapht.visualization.spatial.SpatialQuadTree;
import org.jungrapht.visualization.spatial.SpatialRTree;
import org.jungrapht.visualization.spatial.rtree.*;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.util.BoundingRectangleCollector;
import org.jungrapht.visualization.util.ChangeEventSupport;
import org.jungrapht.visualization.util.DefaultChangeEventSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that maintains many of the details necessary for creating visualizations of graphs. This
 * is the old VisualizationViewer without tooltips and mouse behaviors. Its purpose is to be a base
 * class that can also be used on the server side of a multi-tiered application.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
class DefaultVisualizationServer<V, E> extends JPanel
    implements VisualizationServer<V, E>, VisualizationComponent {

  static Logger log = LoggerFactory.getLogger(DefaultVisualizationServer.class);

  private static final String VERTEX_SPATIAL_SUPPORT = PREFIX + "vertexSpatialSupport";
  private static final String EDGE_SPATIAL_SUPPORT = PREFIX + "edgeSpatialSupport";
  private static final String PROPERTIES_FILE_NAME =
      System.getProperty("jungrapht.properties.file.name", PREFIX + "properties");
  private static final String LIGHTWEIGHT_VERTEX_COUNT_THRESHOLD =
      PREFIX + "lightweightVertexCountThreshold";
  private static final String LIGHTWEIGHT_SCALE_THRESHOLD = PREFIX + "lightweightScaleThreshold";

  private static final String DOUBLE_BUFFERED = PREFIX + "doubleBuffered";

  private static boolean loadFromAppName() {
    try {
      String launchProgram = System.getProperty("sun.java.command");
      if (launchProgram != null && !launchProgram.isEmpty()) {
        launchProgram = launchProgram.substring(launchProgram.lastIndexOf('.') + 1) + ".properties";
        InputStream stream = DefaultRenderContext.class.getResourceAsStream("/" + launchProgram);
        System.getProperties().load(stream);
        return true;
      }
    } catch (Exception ex) {
    }
    return false;
  }

  private static boolean loadFromDefault() {
    try {
      InputStream stream =
          DefaultRenderContext.class.getResourceAsStream("/" + PROPERTIES_FILE_NAME);
      System.getProperties().load(stream);
      return true;
    } catch (Exception ex) {
    }
    return false;
  }

  static {
    loadFromDefault();
    loadFromAppName();
  }

  protected ChangeEventSupport changeSupport = new DefaultChangeEventSupport(this);

  /** holds the state of this View */
  protected VisualizationModel<V, E> visualizationModel;

  /** handles the actual drawing of graph elements */
  protected DefaultModalRenderer<V, E> renderer;

  /** rendering hints used in drawing. Anti-aliasing is on by default */
  protected Map<Key, Object> renderingHints = new HashMap<>();

  /** holds the state of which vertices of the graph are currently 'selected' */
  protected MutableSelectedState<V> selectedVertexState;

  /** holds the state of which edges of the graph are currently 'selected' */
  protected MutableSelectedState<E> selectedEdgeState;

  /**
   * a listener used to cause pick events to result in repaints, even if they come from another view
   */
  protected ItemListener pickEventListener;

  /** an offscreen image to render the graph Used if doubleBuffered is set to true */
  protected BufferedImage offscreen;

  /** graphics context for the offscreen image Used if doubleBuffered is set to true */
  protected Graphics2D offscreenG2d;

  /** user-settable choice to use the offscreen image or not. 'false' by default */
  protected boolean doubleBuffered;

  /**
   * a collection of user-implementable functions to render under the topology (before the graph is
   * rendered)
   */
  protected List<Paintable> preRenderers = new ArrayList<>();

  /**
   * a collection of user-implementable functions to render over the topology (after the graph is
   * rendered)
   */
  protected List<Paintable> postRenderers = new ArrayList<>();

  protected RenderContext<V, E> renderContext;

  protected TransformSupport<V, E> transformSupport = new TransformSupport();

  protected Spatial<V> vertexSpatial;

  protected Spatial<E> edgeSpatial;

  protected int lightweightRenderingVertexCountThreshold =
      Integer.parseInt(System.getProperty(LIGHTWEIGHT_VERTEX_COUNT_THRESHOLD, "20"));

  protected double lightweightRenderingScaleThreshold =
      Double.parseDouble(System.getProperty(LIGHTWEIGHT_SCALE_THRESHOLD, "0.5"));

  protected Predicate<Double> smallScaleOverridePredicate =
      e -> e < lightweightRenderingScaleThreshold;

  protected DefaultVisualizationServer(Builder<V, E, ?, ?> builder) {
    this(
        builder.graph,
        builder.visualizationModel,
        builder.layoutAlgorithm,
        builder.layoutSize,
        builder.viewSize);
  }

  protected DefaultVisualizationServer(
      Graph<V, E> graph,
      VisualizationModel<V, E> visualizationModel,
      LayoutAlgorithm<V> layoutAlgorithm,
      Dimension layoutSize,
      Dimension viewSize) {
    if (visualizationModel == null) {
      Objects.requireNonNull(graph);
      Objects.requireNonNull(viewSize);
      if (layoutSize == null) {
        layoutSize = viewSize;
      }
      if (layoutSize.width <= 0 || layoutSize.height <= 0)
        throw new IllegalArgumentException("width and height must be > 0");
      visualizationModel =
          VisualizationModel.builder(graph)
              .layoutAlgorithm(layoutAlgorithm)
              .layoutSize(layoutSize)
              .build();
    }
    this.doubleBuffered = Boolean.parseBoolean(System.getProperty(DOUBLE_BUFFERED, "true"));
    setBackground(Color.white);
    setLayout(null); // don't want a default FlowLayout
    setVisualizationModel(visualizationModel);
    renderContext = new DefaultRenderContext();
    renderContext.getRenderContextStateChangeSupport().addRenderContextStateChangeListener(this);
    renderContext.setScreenDevice(this);
    renderer = DefaultModalRenderer.<V, E>builder().component(this).build();
    renderer.setCountSupplier(getVisualizationModel().getGraph().vertexSet()::size);
    renderer.setScaleSupplier(
        getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.VIEW)
            ::scale);
    createSpatialStuctures(this.visualizationModel, renderContext);
    this.addComponentListener(new VisualizationListener(this));

    setPickSupport(new ShapePickSupport<>(this));
    setSelectedVertexState(new MultiMutableSelectedState<>());
    setSelectedEdgeState(new MultiMutableSelectedState<>());

    setPreferredSize(viewSize);
    renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    renderContext.getMultiLayerTransformer().addChangeListener(this);

    Spatial<V> vertexSpatial = createVertexSpatial(visualizationModel, renderContext);
    if (vertexSpatial != null) {
      setVertexSpatial(vertexSpatial);
    }

    Spatial<E> edgeSpatial = createEdgeSpatial(visualizationModel, renderContext);
    if (edgeSpatial != null) {
      setEdgeSpatial(edgeSpatial);
    }
    addChangeListener(renderer);
  }

  @Override
  public void reset() {
    getRenderContext().getMultiLayerTransformer().setToIdentity();
  }

  @Override
  public JComponent getComponent() {
    return this;
  }

  private void createSpatialStuctures(
      VisualizationModel<V, E> visualizationModel, RenderContext<V, E> renderContext) {
    setVertexSpatial(createVertexSpatial(visualizationModel, renderContext));
    setEdgeSpatial(createEdgeSpatial(visualizationModel, renderContext));
  }

  @Override
  public Spatial<V> getVertexSpatial() {
    return vertexSpatial;
  }

  @Override
  public void setVertexSpatial(Spatial<V> spatial) {

    if (this.vertexSpatial != null) {
      disconnectListeners(this.vertexSpatial);
    }
    this.vertexSpatial = spatial;

    boolean layoutModelRelaxing = visualizationModel.getLayoutModel().isRelaxing();
    vertexSpatial.setActive(!layoutModelRelaxing);
    if (!layoutModelRelaxing) {
      vertexSpatial.recalculate();
    }
    connectListeners(spatial);
  }

  @Override
  public Spatial<E> getEdgeSpatial() {
    return edgeSpatial;
  }

  @Override
  public void setEdgeSpatial(Spatial<E> spatial) {

    if (this.edgeSpatial != null) {
      disconnectListeners(this.edgeSpatial);
    }
    this.edgeSpatial = spatial;

    boolean layoutModelRelaxing = visualizationModel.getLayoutModel().isRelaxing();
    edgeSpatial.setActive(!layoutModelRelaxing);
    if (!layoutModelRelaxing) {
      edgeSpatial.recalculate();
    }
    connectListeners(edgeSpatial);
  }

  /**
   * hook up events so that when the VisualizationModel gets an event from the LayoutModel and fires
   * it, the Spatial will get the same event and know to update or recalculate its space
   *
   * @param spatial will listen for events
   */
  private void connectListeners(Spatial spatial) {
    LayoutModel<V> layoutModel = visualizationModel.getLayoutModel();
    layoutModel.getLayoutStateChangeSupport().addLayoutStateChangeListener(spatial);
    if (spatial instanceof LayoutVertexPositionChange.Listener) {
      layoutModel
          .getLayoutVertexPositionSupport()
          .addLayoutVertexPositionChangeListener((LayoutVertexPositionChange.Listener) spatial);
    }
  }

  /**
   * disconnect listeners that will no longer be used
   *
   * @param spatial will no longer receive events
   */
  private void disconnectListeners(Spatial spatial) {

    LayoutModel<V> layoutModel = visualizationModel.getLayoutModel();
    layoutModel.getLayoutStateChangeSupport().removeLayoutStateChangeListener(spatial);
    if (spatial instanceof LayoutVertexPositionChange.Listener) {
      layoutModel
          .getLayoutVertexPositionSupport()
          .removeLayoutVertexPositionChangeListener((LayoutVertexPositionChange.Listener) spatial);
    }
  }

  @Override
  public void setDoubleBuffered(boolean doubleBuffered) {
    this.doubleBuffered = doubleBuffered;
  }

  @Override
  public boolean isDoubleBuffered() {
    return doubleBuffered;
  }

  /**
   * Always sanity-check getLayoutSize so that we don't use a value that is improbable
   *
   * @see java.awt.Component#getSize()
   */
  @Override
  public Dimension getSize() {
    Dimension d = super.getSize();
    if (d.width <= 0 || d.height <= 0) {
      d = getPreferredSize();
    }
    return d;
  }

  /**
   * Ensure that, if doubleBuffering is enabled, the offscreen image buffer exists and is the
   * correct layoutSize.
   *
   * @param d the expected Dimension of the offscreen buffer
   */
  protected void checkOffscreenImage(Dimension d) {
    if (doubleBuffered) {
      if (offscreen == null
          || offscreen.getWidth() != d.width
          || offscreen.getHeight() != d.height) {
        offscreen = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        offscreenG2d = offscreen.createGraphics();
      }
    }
  }

  @Override
  public VisualizationModel<V, E> getVisualizationModel() {
    return visualizationModel;
  }

  @Override
  public void setVisualizationModel(VisualizationModel<V, E> visualizationModel) {
    if (this.visualizationModel != null) {
      this.visualizationModel.getModelChangeSupport().removeModelChangeListener(this);
      this.visualizationModel.getViewChangeSupport().removeViewChangeListener(this);
      this.visualizationModel
          .getLayoutModel()
          .getLayoutStateChangeSupport()
          .removeLayoutStateChangeListener(this);
    }
    this.visualizationModel = visualizationModel;
    this.visualizationModel.getModelChangeSupport().addModelChangeListener(this);
    this.visualizationModel.getViewChangeSupport().addViewChangeListener(this);
    this.visualizationModel
        .getLayoutModel()
        .getLayoutStateChangeSupport()
        .addLayoutStateChangeListener(this);
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    repaint();
    fireStateChanged();
  }

  @Override
  public ModalRenderer<V, E> getRenderer() {
    return renderer;
  }

  @Override
  public void scaleToLayout() {
    this.scaleToLayout(false);
  }

  @Override
  public void scaleToLayout(boolean resizeToPoints) {
    SwingUtilities.invokeLater(() -> scaleToLayout(new CrossoverScalingControl(), resizeToPoints));
  }

  @Override
  public void scaleToLayout(ScalingControl scaler) {
    this.scaleToLayout(scaler, false);
  }

  @Override
  public void scaleToLayout(ScalingControl scaler, boolean resizeToPoints) {
    log.trace("scaleToLayout({})", resizeToPoints);
    getRenderContext().getMultiLayerTransformer().setToIdentity();
    Dimension vd = getPreferredSize();
    log.trace("pref vd {}", vd);
    if (this.isShowing()) {
      vd = getSize();
      log.trace("actual vd {}", vd);
    }
    if (resizeToPoints) {
      LayoutModel<V> layoutModel = visualizationModel.getLayoutModel();
      // switch off spatial structures
      vertexSpatial.setActive(false);
      edgeSpatial.setActive(false);
      layoutModel.resizeToSurroundingRectangle();
    }
    Dimension ld = visualizationModel.getLayoutSize();
    if (!vd.equals(ld)) {
      double widthRatio = vd.getWidth() / ld.getWidth();
      double heightRatio = vd.getHeight() / ld.getHeight();
      double ratio = Math.min(widthRatio, heightRatio);
      log.debug(
          "scaling with {} {}", (widthRatio < heightRatio ? "widthRatio" : "heightRatio"), ratio);
      log.debug("vd.getWidth() {} ld.getWidth() {} ", vd.getWidth(), ld.getWidth());
      log.debug("vd.getHeight() {} ld.getHeight() {} ", vd.getHeight(), ld.getHeight());
      scaler.scale(this, (float) (ratio), new Point2D.Double());
      log.trace("center of view is " + this.getCenter());
      log.trace(
          "center of layout is "
              + visualizationModel.getLayoutModel().getWidth() / 2
              + ", "
              + visualizationModel.getLayoutModel().getHeight() / 2);
      Point2D centerOfView = this.getCenter();
      // transform to layout coords
      Point2D viewCenterOnLayout =
          getRenderContext().getMultiLayerTransformer().inverseTransform(centerOfView);
      org.jungrapht.visualization.layout.model.Point layoutCenter =
          visualizationModel.getLayoutModel().getCenter();
      double deltaX = viewCenterOnLayout.getX() - layoutCenter.x;
      double deltaY = viewCenterOnLayout.getY() - layoutCenter.y;
      getRenderContext()
          .getMultiLayerTransformer()
          .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
          .translate(deltaX, deltaY);
    }
  }

  @Override
  public Map<Key, Object> getRenderingHints() {
    return renderingHints;
  }

  @Override
  public void setRenderingHints(Map<Key, Object> renderingHints) {
    this.renderingHints = renderingHints;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2d = (Graphics2D) g;
    if (doubleBuffered) {
      checkOffscreenImage(getSize());
      renderGraph(offscreenG2d);
      g2d.drawImage(offscreen, null, 0, 0);
    } else {
      renderGraph(g2d);
    }
  }

  @Override
  public Shape viewOnLayout() {
    Dimension d = this.getSize();
    MultiLayerTransformer vt = renderContext.getMultiLayerTransformer();
    Shape s = new Rectangle2D.Double(0, 0, d.width, d.height);
    return vt.inverseTransform(s);
  }

  protected void renderGraph(Graphics2D g2d) {
    renderContext.setupArrows(visualizationModel.getGraph().getType().isDirected());
    if (renderContext.getGraphicsContext() == null) {
      renderContext.setGraphicsContext(new GraphicsDecorator(g2d));
    } else {
      renderContext.getGraphicsContext().setDelegate(g2d);
    }
    renderContext.setScreenDevice(this);

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

    if (log.isTraceEnabled()) {
      // when logging is set to trace, the grid will be drawn on the graph visualization
      addSpatialAnnotations(this.vertexSpatial, Color.blue);
      addSpatialAnnotations(this.edgeSpatial, Color.green);
    } else {
      removeSpatialAnnotations();
    }

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

    renderer.render(renderContext, visualizationModel, vertexSpatial, edgeSpatial);

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

  /** a ModelChange.Event from the LayoutModel will trigger a repaint of the visualization */
  @Override
  public void modelChanged() {
    log.trace("modelChanged");
    if (renderContext instanceof DefaultRenderContext) {
      ((DefaultRenderContext) renderContext)
          .setupArrows(visualizationModel.getGraph().getType().isDirected());
    }
    applyLayoutAlgorithmConnections();
    renderer.setCountSupplier(visualizationModel.getGraph().vertexSet()::size);
    repaint();
  }

  /**
   * The LayoutAlgorithms that use Articulated edges (with bends) need to set the edgeShapeFunction
   * to a class that will provide the bend points for each edge. The LayoutAlgorithms that comsider
   * vertex Shape during layout need to access the vertexShapeFunction
   */
  protected void applyLayoutAlgorithmConnections() {
    LayoutAlgorithm<V> layoutAlgorithm = visualizationModel.getLayoutAlgorithm();
    if (layoutAlgorithm instanceof EdgeShapeFunctionSupplier) {
      ((EdgeShapeFunctionSupplier<V, E>) layoutAlgorithm)
          .setEdgeShapeFunctionConsumer(renderContext::setEdgeShapeFunction);
    }
    if (layoutAlgorithm instanceof VertexShapeAware) {
      ((VertexShapeAware) layoutAlgorithm)
          .setVertexShapeFunction(renderContext.getVertexShapeFunction());
    }
  }

  @Override
  public void viewChanged() {
    repaint();
  }

  @Override
  public void layoutStateChanged(LayoutStateChange.Event evt) {
    //    repaint();
    //    no op
  }

  @Override
  public void renderContextStateChanged(RenderContextStateChange.Event evt) {
    this.createSpatialStuctures(visualizationModel, renderContext);
  }

  /**
   * VisualizationListener reacts to changes in the layoutSize of the VisualizationViewer. When the
   * layoutSize changes, it ensures that the offscreen image is sized properly. If the layout is
   * locked to this view layoutSize, then the layout is also resized to be the same as the view
   * layoutSize.
   */
  protected class VisualizationListener extends ComponentAdapter {
    protected DefaultVisualizationServer<V, E> vv;

    public VisualizationListener(DefaultVisualizationServer<V, E> vv) {
      this.vv = vv;
    }

    /** create a new offscreen image for the graph whenever the window is resied */
    @Override
    public void componentResized(ComponentEvent e) {
      Dimension d = vv.getSize();
      if (d.width <= 0 || d.height <= 0) {
        return;
      }
      checkOffscreenImage(d);
      repaint();
    }
  }

  @Override
  public void addPreRenderPaintable(Paintable paintable) {
    if (preRenderers == null) {
      preRenderers = new ArrayList<>();
    }
    preRenderers.add(paintable);
  }

  @Override
  public void prependPreRenderPaintable(Paintable paintable) {
    if (preRenderers == null) {
      preRenderers = new ArrayList<>();
    }
    preRenderers.add(0, paintable);
  }

  @Override
  public void removePreRenderPaintable(Paintable paintable) {
    if (preRenderers != null) {
      preRenderers.remove(paintable);
    }
  }

  @Override
  public void addPostRenderPaintable(Paintable paintable) {
    if (postRenderers == null) {
      postRenderers = new ArrayList<>();
    }
    postRenderers.add(paintable);
  }

  public void prependPostRenderPaintable(Paintable paintable) {
    if (postRenderers == null) {
      postRenderers = new ArrayList<>();
    }
    postRenderers.add(0, paintable);
  }

  @Override
  public void removePostRenderPaintable(Paintable paintable) {
    if (postRenderers != null) {
      postRenderers.remove(paintable);
    }
  }

  @Override
  public void addChangeListener(ChangeListener l) {
    changeSupport.addChangeListener(l);
  }

  @Override
  public void removeChangeListener(ChangeListener l) {
    changeSupport.removeChangeListener(l);
  }

  @Override
  public ChangeListener[] getChangeListeners() {
    return changeSupport.getChangeListeners();
  }

  @Override
  public void fireStateChanged() {
    changeSupport.fireStateChanged();
  }

  @Override
  public MutableSelectedState<V> getSelectedVertexState() {
    return this.selectedVertexState;
  }

  @Override
  public MutableSelectedState<E> getSelectedEdgeState() {
    return selectedEdgeState;
  }

  @Override
  public void setSelectedVertexState(MutableSelectedState<V> selectedVertexState) {
    Objects.requireNonNull(selectedVertexState);
    if (pickEventListener != null && this.selectedVertexState != null) {
      this.selectedVertexState.removeItemListener(pickEventListener);
    }
    this.selectedVertexState = selectedVertexState;
    this.renderContext.setSelectedVertexState(selectedVertexState);
    if (pickEventListener == null) {
      pickEventListener = e -> repaint();
    }
    selectedVertexState.addItemListener(pickEventListener);
  }

  @Override
  public void setSelectedEdgeState(MutableSelectedState<E> selectedEdgeState) {
    Objects.requireNonNull(selectedEdgeState);
    if (pickEventListener != null && this.selectedEdgeState != null) {
      this.selectedEdgeState.removeItemListener(pickEventListener);
    }
    this.selectedEdgeState = selectedEdgeState;
    this.renderContext.setSelectedEdgeState(selectedEdgeState);
    if (pickEventListener == null) {
      pickEventListener = e -> repaint();
    }
    selectedEdgeState.addItemListener(pickEventListener);
  }

  @Override
  public GraphElementAccessor<V, E> getPickSupport() {
    return renderContext.getPickSupport();
  }

  @Override
  public void setPickSupport(GraphElementAccessor<V, E> pickSupport) {
    renderContext.setPickSupport(pickSupport);
  }

  @Override
  public Point2D getCenter() {
    Dimension d = getSize();
    return new Point2D.Double(d.width / 2, d.height / 2);
  }

  @Override
  public RenderContext<V, E> getRenderContext() {
    return renderContext;
  }

  @Override
  public void setRenderContext(RenderContext<V, E> renderContext) {
    this.renderContext = renderContext;
  }

  private void addSpatialAnnotations(Spatial spatial, Color color) {
    if (spatial != null) {
      addPreRenderPaintable(new SpatialPaintable(spatial, color));
    }
  }

  private void removeSpatialAnnotations() {
    preRenderers.removeIf(
        paintable -> paintable instanceof DefaultVisualizationServer.SpatialPaintable);
  }

  @Override
  public TransformSupport<V, E> getTransformSupport() {
    return transformSupport;
  }

  @Override
  public void setTransformSupport(TransformSupport<V, E> transformSupport) {
    this.transformSupport = transformSupport;
  }

  class SpatialPaintable<T> implements VisualizationServer.Paintable {

    Spatial<T> quadTree;
    Color color;

    public SpatialPaintable(Spatial<T> quadTree, Color color) {
      this.quadTree = quadTree;
      this.color = color;
    }

    @Override
    public boolean useTransform() {
      return false;
    }

    @Override
    public void paint(Graphics g) {
      Graphics2D g2d = (Graphics2D) g;
      Color oldColor = g2d.getColor();
      //gather all the grid shapes
      List<Shape> grid = quadTree.getGrid();

      g2d.setColor(color);
      for (Shape r : grid) {
        Shape shape = transformSupport.transform(DefaultVisualizationServer.this, r);
        g2d.draw(shape);
      }
      g2d.setColor(Color.red);

      for (Shape pickShape : quadTree.getPickShapes()) {
        if (pickShape != null) {
          Shape shape = transformSupport.transform(DefaultVisualizationServer.this, pickShape);

          g2d.draw(shape);
        }
      }
      g2d.setColor(oldColor);
    }
  }

  private VisualizationModel.SpatialSupport getVertexSpatialSupportPreference() {
    String spatialSupportProperty = System.getProperty(VERTEX_SPATIAL_SUPPORT, "RTREE");
    try {
      return VisualizationModel.SpatialSupport.valueOf(spatialSupportProperty);
    } catch (IllegalArgumentException ex) {
      // the user set an unknown name
      // issue a warning because unlike colors and shapes, it is not immediately obvious what spatial
      // support is being used
      log.warn("Unknown ModelStructure type {} ignored.", spatialSupportProperty);
    }
    return VisualizationModel.SpatialSupport.QUADTREE;
  }

  private VisualizationModel.SpatialSupport getEdgeSpatialSupportPreference() {
    String spatialSupportProperty = System.getProperty(EDGE_SPATIAL_SUPPORT, "RTREE");
    try {
      return VisualizationModel.SpatialSupport.valueOf(spatialSupportProperty);
    } catch (IllegalArgumentException ex) {
      // the user set an unknown name
      // issue a warning because unlike colors and shapes, it is not immediately obvious what spatial
      // support is being used
      log.warn("Unknown ModelStructure type {} ignored.", spatialSupportProperty);
    }
    return VisualizationModel.SpatialSupport.NONE;
  }

  private Spatial<V> createVertexSpatial(
      VisualizationModel<V, E> visualizationModel, RenderContext<V, E> renderContext) {
    switch (getVertexSpatialSupportPreference()) {
      case RTREE:
        return SpatialRTree.Vertices.builder()
            .visualizationModel(visualizationModel)
            .boundingRectangleCollector(
                new BoundingRectangleCollector.Vertices<>(
                    renderContext.getVertexShapeFunction(), visualizationModel.getLayoutModel()))
            .splitterContext(SplitterContext.of(new RStarLeafSplitter<>(), new RStarSplitter<>()))
            .reinsert(true)
            .build();
      case GRID:
        return new SpatialGrid<>(visualizationModel.getLayoutModel());
      case QUADTREE:
        return new SpatialQuadTree<>(visualizationModel.getLayoutModel());
      case NONE:
      default:
        return new Spatial.NoOp.Vertex<>(visualizationModel.getLayoutModel());
    }
  }

  private Spatial<E> createEdgeSpatial(
      VisualizationModel<V, E> visualizationModel, RenderContext<V, E> renderContext) {
    switch (getEdgeSpatialSupportPreference()) {
      case RTREE:
        return SpatialRTree.Edges.builder()
            .visualizationModel(visualizationModel)
            .boundingRectangleCollector(
                new BoundingRectangleCollector.Edges<>(
                    renderContext.getVertexShapeFunction(),
                    renderContext.getEdgeShapeFunction(),
                    visualizationModel.getLayoutModel()))
            .splitterContext(
                SplitterContext.of(new QuadraticLeafSplitter(), new QuadraticSplitter()))
            .reinsert(false)
            .build();
      case NONE:
      default:
        return new Spatial.NoOp.Edge<>(visualizationModel);
    }
  }
}
