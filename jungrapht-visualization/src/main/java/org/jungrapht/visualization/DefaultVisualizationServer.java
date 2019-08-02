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

import com.google.common.base.Preconditions;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jgrapht.Graph;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.ScalingControl;
import org.jungrapht.visualization.control.TransformSupport;
import org.jungrapht.visualization.layout.BoundingRectangleCollector;
import org.jungrapht.visualization.layout.GraphElementAccessor;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.event.LayoutStateChange;
import org.jungrapht.visualization.layout.event.LayoutVertexPositionChange;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.util.Caching;
import org.jungrapht.visualization.renderers.BasicRenderer;
import org.jungrapht.visualization.renderers.LightweightRenderer;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.selection.MultiMutableSelectedState;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.selection.ShapePickSupport;
import org.jungrapht.visualization.spatial.Spatial;
import org.jungrapht.visualization.spatial.SpatialGrid;
import org.jungrapht.visualization.spatial.SpatialQuadTree;
import org.jungrapht.visualization.spatial.SpatialRTree;
import org.jungrapht.visualization.spatial.rtree.QuadraticLeafSplitter;
import org.jungrapht.visualization.spatial.rtree.QuadraticSplitter;
import org.jungrapht.visualization.spatial.rtree.RStarLeafSplitter;
import org.jungrapht.visualization.spatial.rtree.RStarSplitter;
import org.jungrapht.visualization.spatial.rtree.SplitterContext;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
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
public class DefaultVisualizationServer<V, E> extends JPanel implements VisualizationServer<V, E> {

  static Logger log = LoggerFactory.getLogger(DefaultVisualizationServer.class);

  public static class Builder<
      V, E, T extends DefaultVisualizationServer<V, E>, B extends Builder<V, E, T, B>> {
    protected Graph<V, E> graph;
    protected Dimension layoutSize;
    protected Dimension viewSize;
    protected LayoutAlgorithm<V> layoutAlgorithm;
    protected VisualizationModel<V, E> visualizationModel;

    protected Builder(Graph<V, E> graph) {
      this.graph = graph;
    }

    protected Builder(VisualizationModel<V, E> visualizationModel) {
      this.visualizationModel = visualizationModel;
    }

    protected B self() {
      return (B) this;
    }

    public B layoutSize(Dimension layoutSize) {
      this.layoutSize = layoutSize;
      return self();
    }

    public B viewSize(Dimension viewSize) {
      this.viewSize = viewSize;
      return self();
    }

    public B layoutAlgorithm(LayoutAlgorithm<V> layoutAlgorithm) {
      this.layoutAlgorithm = layoutAlgorithm;
      return self();
    }

    public T build() {
      return (T) new DefaultVisualizationServer(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder(Graph<V, E> graph) {
    return new Builder(graph);
  }

  public static <V, E> Builder<V, E, ?, ?> builder(VisualizationModel<V, E> visualizationModel) {
    return new Builder(visualizationModel);
  }

  private static final String PREFIX = "jungrapht.";
  private static final String VERTEX_SPATIAL_SUPPORT = PREFIX + "vertexSpatialSupport";
  private static final String EDGE_SPATIAL_SUPPORT = PREFIX + "edgeSpatialSupport";
  private static final String PROPERTIES_FILE_NAME =
      System.getProperty("graph.visualization.properties.file.name", PREFIX + "properties");

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
  protected VisualizationModel<V, E> model;

  /** handles the actual drawing of graph elements */
  protected Renderer<V, E> renderer;

  protected Renderer<V, E> lightweightRenderer = new LightweightRenderer<>();
  protected Renderer<V, E> complexRenderer;

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

  protected Predicate<Double> smallScaleOverridePredicate = e -> false;

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
      Preconditions.checkNotNull(graph);
      Preconditions.checkNotNull(viewSize);
      if (viewSize == null) {
        viewSize = getPreferredSize();
      }
      if (layoutSize == null) {
        layoutSize = viewSize;
      }
      Preconditions.checkArgument(layoutSize.width > 0, "width must be > 0");
      Preconditions.checkArgument(layoutSize.height > 0, "height must be > 0");
      this.model =
          DefaultVisualizationModel.builder(graph)
              .layoutAlgorithm(layoutAlgorithm)
              .layoutSize(layoutSize)
              .build();
    } else {
      this.model = visualizationModel;
    }
    renderContext = new DefaultRenderContext<>(model.getGraph());
    renderContext.setScreenDevice(this);
    renderer = complexRenderer = new BasicRenderer<>();
    createSpatialStuctures(model, renderContext);
    model
        .getLayoutModel()
        .getLayoutChangeSupport()
        .addLayoutChangeListener(this); // will cause a repaint
    model.getLayoutModel().getLayoutStateChangeSupport().addLayoutStateChangeListener(this);
    setDoubleBuffered(false);
    this.addComponentListener(new VisualizationListener(this));

    setPickSupport(new ShapePickSupport<>(this));
    setSelectedVertexState(new MultiMutableSelectedState<>());
    setSelectedEdgeState(new MultiMutableSelectedState<>());

    setPreferredSize(viewSize);
    renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    renderContext.getMultiLayerTransformer().addChangeListener(this);

    Spatial<V> vertexSpatial = createVertexSpatial(this);
    if (vertexSpatial != null) {
      setVertexSpatial(vertexSpatial);
    }

    Spatial<E> edgeSpatial = createEdgeSpatial(this);
    if (edgeSpatial != null) {
      setEdgeSpatial(edgeSpatial);
    }
  }

  private void createSpatialStuctures(VisualizationModel model, RenderContext renderContext) {
    setVertexSpatial(
        SpatialRTree.Vertices.builder()
            .visualizationModel(model)
            .boundingRectangleCollector(
                new BoundingRectangleCollector.Vertices<>(renderContext, model))
            .splitterContext(SplitterContext.of(new RStarLeafSplitter<>(), new RStarSplitter<>()))
            .reinsert(true)
            .build());

    setEdgeSpatial(
        SpatialRTree.Edges.builder()
            .visualizationModel(model)
            .boundingRectangleCollector(
                new BoundingRectangleCollector.Edges<>(renderContext, model))
            .splitterContext(
                SplitterContext.of(new QuadraticLeafSplitter(), new QuadraticSplitter()))
            .reinsert(false)
            .build());
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

    boolean layoutModelRelaxing = model.getLayoutModel().isRelaxing();
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

    boolean layoutModelRelaxing = model.getLayoutModel().isRelaxing();
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
    LayoutModel<V> layoutModel = model.getLayoutModel();
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

    LayoutModel<V> layoutModel = model.getLayoutModel();
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
  public VisualizationModel<V, E> getModel() {
    return model;
  }

  @Override
  public void setModel(VisualizationModel<V, E> model) {
    this.model = model;
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    repaint();
    fireStateChanged();
  }

  @Override
  public void setRenderer(Renderer<V, E> r) {
    this.renderer = r;
    this.complexRenderer = renderer;
    repaint();
  }

  @Override
  public void setLightweightRenderer(Renderer<V, E> r) {
    this.lightweightRenderer = r;
    repaint();
  }

  @Override
  public Renderer<V, E> getRenderer() {
    return renderer;
  }

  public void scaleToLayout() {
    SwingUtilities.invokeLater(() -> scaleToLayout(new CrossoverScalingControl()));
  }

  public void scaleToLayout(ScalingControl scaler) {
    Dimension vd = getPreferredSize();
    log.trace("pref vd {}", vd);
    if (this.isShowing()) {
      vd = getSize();
      log.trace("actual vd {}", vd);
    }
    Dimension ld = model.getLayoutSize();
    if (!vd.equals(ld)) {
      log.trace("vd.getWidth() {} ld.getWidth() {} ", vd.getWidth(), ld.getWidth());
      getRenderContext().getMultiLayerTransformer().setToIdentity();
      scaler.scale(this, (float) (vd.getWidth() / ld.getWidth()), new Point2D.Double());
      log.trace("scaled by {}", vd.getWidth() / ld.getWidth());
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

    if (model instanceof Caching) {
      ((Caching) model).clear();
    }

    renderer.render(renderContext, model, vertexSpatial, edgeSpatial);

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

  public void setSmallScaleOverridePredicate(Predicate<Double> smallScaleOverridePredicate) {
    this.smallScaleOverridePredicate = smallScaleOverridePredicate;
    // ensures that the first rendering of the visualization checks to see if it should be simplified
    // based on the current view scale
    simplifyRenderer(smallScale());
  }

  private boolean smallScale() {
    return smallScaleOverridePredicate.test(
        getRenderContext()
            .getMultiLayerTransformer()
            .getTransformer(MultiLayerTransformer.Layer.VIEW)
            .getScale());
  }

  /** a LayoutChange.Event from the LayoutModel will trigger a repaint of the visualization */
  @Override
  public void layoutChanged() {
    if (renderContext instanceof DefaultRenderContext) {
      ((DefaultRenderContext) renderContext).setupArrows(model.getGraph().getType().isDirected());
    }
    repaint();
  }

  public void layoutStateChanged(LayoutStateChange.Event evt) {
    //    no op
  }

  @Override
  public void simplifyRenderer(boolean simplify) {
    if (smallScale() || simplify) {
      this.renderer = lightweightRenderer;
      this.getRenderingHints().remove(RenderingHints.KEY_ANTIALIASING);
    } else {
      this.renderer = complexRenderer;
      this.getRenderingHints()
          .put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
    repaint();
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
    Preconditions.checkNotNull(selectedVertexState);
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
    Preconditions.checkNotNull(selectedEdgeState);
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

  private Spatial<V> createVertexSpatial(VisualizationServer<V, E> visualizationServer) {
    switch (getVertexSpatialSupportPreference()) {
      case RTREE:
        return SpatialRTree.Vertices.builder()
            .visualizationModel(visualizationServer.getModel())
            .boundingRectangleCollector(
                new BoundingRectangleCollector.Vertices<>(
                    visualizationServer.getRenderContext(), visualizationServer.getModel()))
            .splitterContext(SplitterContext.of(new RStarLeafSplitter<>(), new RStarSplitter<>()))
            .reinsert(true)
            .build();
      case GRID:
        return new SpatialGrid<>(visualizationServer.getModel().getLayoutModel());
      case QUADTREE:
        return new SpatialQuadTree<>(visualizationServer.getModel().getLayoutModel());
      case NONE:
      default:
        return new Spatial.NoOp.Vertex<>(visualizationServer.getModel().getLayoutModel());
    }
  }

  private Spatial<E> createEdgeSpatial(VisualizationServer<V, E> visualizationServer) {
    switch (getEdgeSpatialSupportPreference()) {
      case RTREE:
        return SpatialRTree.Edges.builder()
            .visualizationModel(visualizationServer.getModel())
            .boundingRectangleCollector(
                new BoundingRectangleCollector.Edges<>(
                    visualizationServer.getRenderContext(), visualizationServer.getModel()))
            .splitterContext(
                SplitterContext.of(new QuadraticLeafSplitter(), new QuadraticSplitter()))
            .reinsert(false)
            .build();
      case NONE:
      default:
        return new Spatial.NoOp.Edge<>(visualizationServer.getModel());
    }
  }
}
