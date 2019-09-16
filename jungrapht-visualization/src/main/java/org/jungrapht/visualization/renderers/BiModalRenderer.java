package org.jungrapht.visualization.renderers;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_OFF;
import static org.jungrapht.visualization.VisualizationServer.PREFIX;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jgrapht.Graph;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.spatial.Spatial;
import org.jungrapht.visualization.transform.BidirectionalTransformer;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.MagnifyTransformer;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.transform.shape.MagnifyIconGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Renderer} that delegates to either a {@link HeavyweightRenderer} or a {@link
 * LightweightRenderer} depending on the results of a count Predicate and a scale Predicate
 *
 * <p>The count predicate defaults to a comparison of the vertex count with the
 * lightweightCountThreshold.
 *
 * <p>The scale predicate defaults to a comparison of the VIEW transform scale with the
 * lightweightScaleThreshold. Note that the VIEW transform scale range is <b>0 &lt; VIEW transform
 * scale &lt;= 1.0</b>
 *
 * <p>The following conditions apply:
 *
 * <ol>
 *   <li><b>vertex count &lt; {@code lightweightCountThreshold}:</b> Always draw with {@link
 *       HeavyweightRenderer}
 *   <li><b>{@code lightweightScaleThreshold} &gt; 1.0:</b> Always draw with {@link
 *       LightweightRenderer} (unless 1 is true)
 *   <li>If neither 1 nor 2 are true:
 *       <ul>
 *         <li>the static graph will be rendered with the {@link HeavyweightRenderer} when <b>VIEW
 *             transform scale &gt; {@code lightweightScaleThreshold}</b>. Otherwise, the static
 *             graph will be rendered with the {@link LightweightRenderer}
 *         <li>While the graph is being manipulated with mouse gestures (zoom/pan, etc) the graph
 *             will be rendered with the {@link LightweightRenderer}
 *       </ul>
 * </ol>
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class BiModalRenderer<V, E> implements ModalRenderer<V, E>, ChangeListener {

  public static final Mode LIGHTWEIGHT = new Mode();
  public static final Mode HEAVYWEIGHT = new Mode();
  /**
   * Builder to create a configured instance of a BiModalRenderer
   *
   * @param <V> vertex type
   * @param <E> edge type
   * @param <T> BiModalRenderer type
   * @param <B> Builder type
   */
  public static class Builder<
      V, E, T extends BiModalRenderer<V, E>, B extends Builder<V, E, T, B>> {

    /** Map of Renderers keyed on Mode */
    protected Map<Mode, Renderer<V, E>> rendererMap = new HashMap<>();
    /** a Component to render on */
    protected JComponent component;

    /** @return an instance of this Builder cast to B */
    protected B self() {
      return (B) this;
    }

    /**
     * @param component to render on
     * @return this Builder
     */
    public B component(JComponent component) {
      this.component = component;
      return self();
    }

    /**
     * @param lightweightRenderer the value for the LIGHTWEIGHT key
     * @return this Builder
     */
    public B lightweightRenderer(Renderer<V, E> lightweightRenderer) {
      rendererMap.put(LIGHTWEIGHT, lightweightRenderer);
      return self();
    }

    /**
     * @param heavyweightRenderer the value for the HEAVYWEIGHT key
     * @return this Builder
     */
    public B heavyweightRenderer(Renderer<V, E> heavyweightRenderer) {
      rendererMap.put(HEAVYWEIGHT, heavyweightRenderer);
      return self();
    }

    /** @return a configured instance of a BiModalRenderer */
    public T build() {
      return (T) new BiModalRenderer<>(this);
    }
  }

  /**
   * @param <V> vertex type
   * @param <E> edge type
   * @return a Builder to configure
   */
  public static <V, E> Builder<V, E, ?, ?> builder() {
    return new Builder();
  }

  private static final Logger log = LoggerFactory.getLogger(BiModalRenderer.class);

  /** Property key for the vertex count threshold that affects the rendering mode */
  private static final String LIGHTWEIGHT_COUNT_THRESHOLD = PREFIX + "lightweightCountThreshold";

  /** Property key for the scale threshold that affects the rendering mode */
  private static final String LIGHTWEIGHT_SCALE_THRESHOLD = PREFIX + "lightweightScaleThreshold";

  /** threshold for vertex count that affects rendering mode */
  protected int lightweightRenderingCountThreshold =
      Integer.parseInt(System.getProperty(LIGHTWEIGHT_COUNT_THRESHOLD, "20"));

  /** scale that affects rendering mode */
  protected double lightweightRenderingScaleThreshold =
      Double.parseDouble(System.getProperty(LIGHTWEIGHT_SCALE_THRESHOLD, "0.5"));

  /** */
  protected Supplier<Double> scaleSupplier = () -> 0.5;

  protected Predicate<Supplier<Double>> scalePredicate =
      t -> t.get() < lightweightRenderingScaleThreshold;

  protected Supplier<Integer> countSupplier = () -> 0;
  protected Predicate<Supplier<Integer>> countPredicate =
      t -> t.get() > lightweightRenderingCountThreshold;

  protected Mode mode;

  protected Map<Mode, Renderer<V, E>> rendererMap;

  Timer timer;
  JComponent component;

  protected BiModalRenderer(Builder<V, E, ?, ?> builder) {
    this(builder.component, builder.rendererMap);
  }

  protected BiModalRenderer(JComponent component, Map<Mode, Renderer<V, E>> rendererMap) {
    this.component = component;
    this.rendererMap = rendererMap;
    if (rendererMap.get(LIGHTWEIGHT) == null) {
      rendererMap.put(LIGHTWEIGHT, new LightweightRenderer<>());
    }
    if (rendererMap.get(HEAVYWEIGHT) == null) {
      rendererMap.put(HEAVYWEIGHT, new HeavyweightRenderer<>());
    }
  }

  public Supplier<Double> getScaleSupplier() {
    return scaleSupplier;
  }

  public void setScaleSupplier(Supplier<Double> scaleSupplier) {
    this.scaleSupplier = scaleSupplier;
  }

  public Supplier<Integer> getCountSupplier() {
    return countSupplier;
  }

  public void setCountSupplier(Supplier<Integer> countSupplier) {
    this.countSupplier = countSupplier;
    stateChanged(null);
  }

  @Override
  public void setMode(Mode mode) {
    log.trace("setMode({})", mode);
    this.mode = mode;
  }

  public Mode getMode() {
    if (mode == null) {
      return HEAVYWEIGHT;
    }
    return mode;
  }

  /**
   * if the graph has few vertices, always use the default renderer if the graph is at small scale,
   * always use the lightweight renderer if the graph is being manipulated, use the lightweight then
   * default
   */
  private Renderer<V, E> getInitialRenderer() {
    log.trace("initialMode...");
    if (!this.countPredicate.test(countSupplier)) {
      // small graph, initial state is Heavyweight
      return rendererMap.get(HEAVYWEIGHT);
    } else if (this.scalePredicate.test(scaleSupplier)) {
      // bigger graph, test the scale
      // not a small graph and the scale is small. use lightweight
      return rendererMap.get(LIGHTWEIGHT);
    } else {
      // bigger graph, but the scale is big, use Heavyweight
      return rendererMap.get(HEAVYWEIGHT);
    }
  }

  @Override
  public void render(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E> visualizationModel,
      Spatial<V> vertexSpatial,
      Spatial<E> edgeSpatial) {

    if (mode == null) {
      setAntialias(renderContext, HEAVYWEIGHT);
      getInitialRenderer().render(renderContext, visualizationModel, vertexSpatial, edgeSpatial);
    } else {
      setAntialias(renderContext, mode);
      doRender(renderContext, visualizationModel, vertexSpatial, edgeSpatial);
    }
  }

  private void setAntialias(RenderContext<V, E> renderContext, Mode mode) {
    if (mode == HEAVYWEIGHT) {
      renderContext
          .getGraphicsContext()
          .getRenderingHints()
          .put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    } else {
      renderContext.getGraphicsContext().setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);
    }
  }

  @Override
  public void render(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel) {
    setAntialias(renderContext, mode);
    doRender(renderContext, visualizationModel);
  }

  private void doRender(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E> visualizationModel,
      Spatial<V> vertexSpatial,
      Spatial<E> edgeSpatial) {
    if (vertexSpatial == null) {
      render(renderContext, visualizationModel);
      return;
    }
    Iterable<V> visibleVertices = null;
    Iterable<E> visibleEdges = null;

    try {
      visibleVertices =
          vertexSpatial.getVisibleElements(
              ((VisualizationServer) renderContext.getScreenDevice()).viewOnLayout());

      if (edgeSpatial != null) {
        visibleEdges =
            edgeSpatial.getVisibleElements(
                ((VisualizationServer) renderContext.getScreenDevice()).viewOnLayout());
      } else {
        visibleEdges = visualizationModel.getGraph().edgeSet();
      }
    } catch (ConcurrentModificationException ex) {
      // skip rendering until graph vertex index is stable,
      // this can happen if the layout relax thread is changing locations while the
      // visualization is rendering
      log.trace("got {} so returning", ex.toString());
      log.trace(
          "layoutMode active: {}, edgeSpatial active {}, vertexSpatial active: {}",
          visualizationModel.getLayoutModel().isRelaxing(),
          edgeSpatial != null && edgeSpatial.isActive(),
          vertexSpatial.isActive());
      return;
    }

    try {
      Graph<V, E> graph = visualizationModel.getGraph();
      // paint all the edges
      log.trace("the visibleEdges are {}", visibleEdges);
      for (E e : visibleEdges) {
        if (graph.edgeSet().contains(e)) {
          renderEdge(renderContext, visualizationModel, e);
          renderEdgeLabel(renderContext, visualizationModel, e);
        }
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the vertices
    try {
      log.trace("the visibleVertices are {}", visibleVertices);

      for (V v : visibleVertices) {
        renderVertex(renderContext, visualizationModel, v);
        renderVertexLabel(renderContext, visualizationModel, v);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  private void doRender(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel) {
    Graph<V, E> graph = visualizationModel.getGraph();
    // paint all the edges
    try {
      for (E e : graph.edgeSet()) {
        renderEdge(renderContext, visualizationModel, e);
        renderEdgeLabel(renderContext, visualizationModel, e);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the vertices
    try {
      for (V v : graph.vertexSet()) {
        renderVertex(renderContext, visualizationModel, v);
        renderVertexLabel(renderContext, visualizationModel, v);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  @Override
  public void renderVertex(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v) {

    GraphicsDecorator graphicsDecorator = renderContext.getGraphicsContext();
    if (graphicsDecorator instanceof MagnifyIconGraphics) {
      MagnifyIconGraphics magnifyIconGraphics = (MagnifyIconGraphics) graphicsDecorator;
      BidirectionalTransformer bidirectionalTransformer = magnifyIconGraphics.getTransformer();
      if (bidirectionalTransformer instanceof MagnifyTransformer) {
        MagnifyTransformer magnifyTransformer = (MagnifyTransformer) bidirectionalTransformer;
        Lens lens = magnifyTransformer.getLens();
        // layoutLocation
        Point p = visualizationModel.getLayoutModel().apply(v);
        Point2D layoutPoint = new Point2D.Double(p.x, p.y);
        // transform to view
        Point2D viewPoint =
            renderContext
                .getMultiLayerTransformer()
                .transform(MultiLayerTransformer.Layer.LAYOUT, layoutPoint);
        Shape lensShape = lens.getLensShape();
        if (lensShape.contains(viewPoint)) {
          double magnification = magnifyTransformer.getLens().getMagnification();
          double product = magnification * magnifyTransformer.getScale();
          // override for the magnifier scale
          Mode mode = getModeFor(() -> product);
          rendererMap.get(mode).renderVertex(renderContext, visualizationModel, v);
        } else {
          rendererMap.get(mode).renderVertex(renderContext, visualizationModel, v);
        }
      }
    } else {
      rendererMap.get(mode).renderVertex(renderContext, visualizationModel, v);
    }
  }

  @Override
  public void renderVertexLabel(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v) {
    rendererMap.get(mode).renderVertexLabel(renderContext, visualizationModel, v);
  }

  @Override
  public void renderEdge(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, E e) {
    rendererMap.get(mode).renderEdge(renderContext, visualizationModel, e);
  }

  @Override
  public void renderEdgeLabel(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, E e) {
    rendererMap.get(mode).renderEdgeLabel(renderContext, visualizationModel, e);
  }

  @Override
  public void setVertexRenderer(Vertex<V, E> r) {
    rendererMap.get(mode).setVertexRenderer(r);
  }

  @Override
  public void setEdgeRenderer(Edge<V, E> r) {
    rendererMap.get(mode).setEdgeRenderer(r);
  }

  @Override
  public void setVertexLabelRenderer(VertexLabel<V, E> r) {
    rendererMap.get(mode).setVertexLabelRenderer(r);
  }

  @Override
  public void setEdgeLabelRenderer(EdgeLabel<V, E> r) {
    rendererMap.get(mode).setEdgeLabelRenderer(r);
  }

  @Override
  public VertexLabel<V, E> getVertexLabelRenderer() {
    return rendererMap.get(mode).getVertexLabelRenderer();
  }

  @Override
  public Vertex<V, E> getVertexRenderer() {
    return rendererMap.get(mode).getVertexRenderer();
  }

  @Override
  public Edge<V, E> getEdgeRenderer() {
    return rendererMap.get(mode).getEdgeRenderer();
  }

  @Override
  public EdgeLabel<V, E> getEdgeLabelRenderer() {
    return rendererMap.get(mode).getEdgeLabelRenderer();
  }

  @Override
  public void setVertexRenderer(Mode mode, Vertex<V, E> r) {
    rendererMap.get(mode).setVertexRenderer(r);
  }

  @Override
  public void setEdgeRenderer(Mode mode, Edge<V, E> r) {
    rendererMap.get(mode).setEdgeRenderer(r);
  }

  @Override
  public void setVertexLabelRenderer(Mode mode, VertexLabel<V, E> r) {
    rendererMap.get(mode).setVertexLabelRenderer(r);
  }

  @Override
  public void setEdgeLabelRenderer(Mode mode, EdgeLabel<V, E> r) {
    rendererMap.get(mode).setEdgeLabelRenderer(r);
  }

  @Override
  public VertexLabel<V, E> getVertexLabelRenderer(Mode mode) {
    return rendererMap.get(mode).getVertexLabelRenderer();
  }

  @Override
  public Vertex<V, E> getVertexRenderer(Mode mode) {
    return rendererMap.get(mode).getVertexRenderer();
  }

  @Override
  public Edge<V, E> getEdgeRenderer(Mode mode) {
    return rendererMap.get(mode).getEdgeRenderer();
  }

  @Override
  public EdgeLabel<V, E> getEdgeLabelRenderer(Mode mode) {
    return rendererMap.get(mode).getEdgeLabelRenderer();
  }

  protected Mode getModeFor(Supplier<Double> scaleSupplier) {
    if (!this.countPredicate.test(this.countSupplier)) {
      // its a small graph, always use default renderer
      return HEAVYWEIGHT;
    }
    // its a big graph, check the scale
    double scale = scaleSupplier.get();
    if (this.scalePredicate.test(scaleSupplier)) {
      return LIGHTWEIGHT;
    } else {
      return HEAVYWEIGHT;
    }
  }

  protected void manageMode() {
    if (!this.countPredicate.test(this.countSupplier)) {
      // its a small graph, always use default renderer
      setMode(HEAVYWEIGHT);
      component.repaint();
      return;
    }

    // its a big graph, check the scale
    if (this.scalePredicate.test(this.scaleSupplier)) {
      // the scale is small, use lightweight
      setMode(LIGHTWEIGHT);
      component.repaint();
      return;
    }
    // its a big graph, but the scale is not small, start a timer
    if (timer == null || timer.done) {
      timer = new Timer();
      timer.start();
    } else {
      timer.incrementValue();
    }
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    log.trace("count supplier got {}", countSupplier.get());
    log.trace("count threshold is {}", lightweightRenderingCountThreshold);
    log.trace("count predicate {}", countPredicate);
    log.trace("scale supplier got {}", scaleSupplier.get());
    log.trace("scale threshold is {}", lightweightRenderingScaleThreshold);
    manageMode();
  }

  static final int TIMER_MAX = Integer.getInteger(PREFIX + "modalRendererTimerMax", 10);
  static final int TIMER_INCREMENT = Integer.getInteger(PREFIX + "modalRendererTimerIncrement", 10);
  static final int TIMER_SLEEP = Integer.getInteger(PREFIX + "modalRendererTimerSleep", 30);

  class Timer extends Thread {
    long value = TIMER_MAX;
    boolean done;

    Timer() {
      log.trace("timer start mode");
      setMode(LIGHTWEIGHT);
      component.repaint();
    }

    void incrementValue() {
      value = TIMER_INCREMENT;
    }

    @Override
    public void run() {
      done = false;
      while (value > 0) {
        value--;
        try {
          Thread.sleep(TIMER_SLEEP);
        } catch (InterruptedException ex) {
          // ignore
        }
      }
      log.trace("timer end mode");

      if (scalePredicate.test(scaleSupplier)) {
        // the scale is small, use lightweight
        setMode(LIGHTWEIGHT);
      } else {
        setMode(HEAVYWEIGHT);
      }
      done = true;
      component.repaint();
    }
  }
}
