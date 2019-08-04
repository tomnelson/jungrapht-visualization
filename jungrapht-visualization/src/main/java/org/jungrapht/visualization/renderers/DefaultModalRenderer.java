package org.jungrapht.visualization.renderers;

import static org.jungrapht.visualization.VisualizationServer.PREFIX;
import static org.jungrapht.visualization.renderers.ModalRenderer.Mode.DEFAULT;
import static org.jungrapht.visualization.renderers.ModalRenderer.Mode.LIGHTWEIGHT;

import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.spatial.Spatial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Renderer} that delegates to either a {@link DefaultRenderer} or a {@link
 * LightweightRenderer} depending on the results of a count predicate and a scale predicate
 *
 * <p>The count predicate defaults to a comparison of the vertex count with the
 * lightweightCountThreshold
 *
 * <p>The scale predicate defauls to a comparison of the VIEW transform scale with the
 * lightweightScaleThreshold
 *
 * <p>if the scale threshold is less than 0.5, then the graph is always drawn with the lightweight
 * renderer
 *
 * <p>if the vertex count is less than (for example) 20 the the graph is always drawn with the
 * default renderer
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class DefaultModalRenderer<V, E> implements ModalRenderer<V, E>, ChangeListener {

  private static final Logger log = LoggerFactory.getLogger(DefaultModalRenderer.class);

  private static final String LIGHTWEIGHT_COUNT_THRESHOLD = PREFIX + "lightweightCountThreshold";

  private static final String LIGHTWEIGHT_SCALE_THRESHOLD = PREFIX + "lightweightScaleThreshold";

  protected int lightweightRenderingCountThreshold =
      Integer.parseInt(System.getProperty(LIGHTWEIGHT_COUNT_THRESHOLD, "20"));

  protected double lightweightRenderingScaleThreshold =
      Double.parseDouble(System.getProperty(LIGHTWEIGHT_SCALE_THRESHOLD, "0.5"));

  protected Supplier<Double> scaleSupplier;
  protected Predicate<Supplier<Double>> scalePredicate =
      t -> scaleSupplier.get() < lightweightRenderingScaleThreshold;

  protected Supplier<Integer> countSupplier;
  protected Predicate<Supplier<Integer>> countPredicate =
      t -> countSupplier.get() > lightweightRenderingCountThreshold;

  private Renderer<V, E> defaultRenderer = new DefaultRenderer<>();
  private Renderer<V, E> lightweightRenderer = new LightweightRenderer<>();
  private Renderer<V, E> currentRenderer;

  Timer timer;
  JComponent component;

  public DefaultModalRenderer(JComponent component) {
    this.component = component;
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
  }

  @Override
  public void setMode(Mode mode) {
    log.trace("setMode({})", mode);
    switch (mode) {
      case LIGHTWEIGHT:
        currentRenderer = lightweightRenderer;
        log.trace("setMode:{}", mode);
        break;
      case DEFAULT:
      default:
        currentRenderer = defaultRenderer;
        log.trace("setMode:{}", mode);
        break;
    }
  }

  /**
   * if the graph has few vertices, always use the default renderer if the graph is at small scale,
   * always use the lightweight renderer if the graph is being manipulated, use the lightweight then
   * default
   */
  private void confirmInitialRenderer() {
    if (currentRenderer == null) {
      if (!this.countPredicate.test(countSupplier)) {
        // small graph, initial state is default
        setMode(DEFAULT);
      } else if (this.scalePredicate.test(scaleSupplier)) {
        // so its a big graph, test the scale
        // not a small graph and the scale is small. use lightweight
        setMode(LIGHTWEIGHT);
      } else {
        // its a big graph, but the scale is big, use default
        setMode(DEFAULT);
      }
    }
  }

  @Override
  public void render(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E> visualizationModel,
      Spatial<V> vertexSpatial,
      Spatial<E> edgeSpatial) {
    confirmInitialRenderer();
    currentRenderer.render(renderContext, visualizationModel, vertexSpatial, edgeSpatial);
  }

  @Override
  public void render(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel) {
    confirmInitialRenderer();
    currentRenderer.render(renderContext, visualizationModel);
  }

  @Override
  public void renderVertex(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v) {
    currentRenderer.renderVertex(renderContext, visualizationModel, v);
  }

  @Override
  public void renderVertexLabel(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, V v) {
    currentRenderer.renderVertexLabel(renderContext, visualizationModel, v);
  }

  @Override
  public void renderEdge(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, E e) {
    currentRenderer.renderEdge(renderContext, visualizationModel, e);
  }

  @Override
  public void renderEdgeLabel(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel, E e) {
    currentRenderer.renderEdgeLabel(renderContext, visualizationModel, e);
  }

  @Override
  public void setVertexRenderer(Vertex<V, E> r) {
    currentRenderer.setVertexRenderer(r);
  }

  @Override
  public void setEdgeRenderer(Edge<V, E> r) {
    currentRenderer.setEdgeRenderer(r);
  }

  @Override
  public void setVertexLabelRenderer(VertexLabel<V, E> r) {
    currentRenderer.setVertexLabelRenderer(r);
  }

  @Override
  public void setEdgeLabelRenderer(EdgeLabel<V, E> r) {
    currentRenderer.setEdgeLabelRenderer(r);
  }

  @Override
  public VertexLabel<V, E> getVertexLabelRenderer() {
    return currentRenderer.getVertexLabelRenderer();
  }

  @Override
  public Vertex<V, E> getVertexRenderer() {
    return currentRenderer.getVertexRenderer();
  }

  @Override
  public Edge<V, E> getEdgeRenderer() {
    return currentRenderer.getEdgeRenderer();
  }

  @Override
  public EdgeLabel<V, E> getEdgeLabelRenderer() {
    return currentRenderer.getEdgeLabelRenderer();
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    log.trace("count supplier got {}", countSupplier.get());
    log.trace("scale supplier got {}", scaleSupplier.get());
    if (!this.countPredicate.test(this.countSupplier)) {
      // its a small graph, always use default renderer
      setMode(DEFAULT);
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

  class Timer extends Thread {
    long value = 10;
    boolean done;

    Timer() {
      setMode(LIGHTWEIGHT);
      component.repaint();
    }

    void incrementValue() {
      value = 10;
    }

    @Override
    public void run() {
      done = false;
      while (value > 0) {
        value--;
        try {
          Thread.sleep(10);
        } catch (InterruptedException ex) {
          // ignore
        }
      }
      setMode(Mode.DEFAULT);
      done = true;
      component.repaint();
    }
  }
}
