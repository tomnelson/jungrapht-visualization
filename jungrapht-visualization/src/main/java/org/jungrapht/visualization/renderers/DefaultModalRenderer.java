package org.jungrapht.visualization.renderers;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
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

import static java.awt.RenderingHints.*;

public class DefaultModalRenderer<V, E> implements ModalRenderer<V, E>, ChangeListener {

    private static final Logger log = LoggerFactory.getLogger(DefaultModalRenderer.class);

    private Map<Key, Object> renderingHints = new HashMap<>();
  private static final String PREFIX = "jungrapht.";
  private static final String LIGHTWEIGHT_VERTEX_COUNT_THRESHOLD =
      PREFIX + "lightweightVertexCountThreshold";
  private static final String LIGHTWEIGHT_SCALE_THRESHOLD = PREFIX + "lightweightScaleThreshold";

  protected int lightweightRenderingVertexCountThreshold =
      Integer.parseInt(System.getProperty(LIGHTWEIGHT_VERTEX_COUNT_THRESHOLD, "20"));

  protected double lightweightRenderingScaleThreshold =
      Double.parseDouble(System.getProperty(LIGHTWEIGHT_SCALE_THRESHOLD, "0.5"));

  protected Supplier<Double> scaleSupplier;
  protected Predicate<Supplier<Double>> smallScaleOverridePredicate =
      t -> scaleSupplier.get() < lightweightRenderingScaleThreshold;

  protected Supplier<Integer> countSupplier;
  protected Predicate<Supplier<Integer>> vertexCountPredicate =
      t -> countSupplier.get() > lightweightRenderingVertexCountThreshold;

  Renderer<V, E> defaultRenderer = new BasicRenderer();
  Renderer<V, E> lightweightRenderer = new LightweightRenderer<>();
  Renderer<V, E> currentRenderer = defaultRenderer;

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

  @Override
  public void render(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E> visualizationModel,
      Spatial<V> vertexSpatial,
      Spatial<E> edgeSpatial) {
    currentRenderer.render(renderContext, visualizationModel, vertexSpatial, edgeSpatial);
//    log.trace("antialias hints:{}",renderContext.getGraphicsContext().getRenderingHints().get(KEY_ANTIALIASING));
  }

  @Override
  public void render(
      RenderContext<V, E> renderContext, VisualizationModel<V, E> visualizationModel) {
    currentRenderer.render(renderContext, visualizationModel);
      log.info("antialias hints:{}",renderContext.getGraphicsContext().getRenderingHints().get(KEY_ANTIALIASING));
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
    if (!this.vertexCountPredicate.test(this.countSupplier)) {
        log.trace(" do nothing because vertexCountPredicate {} tested false with {} when lightweightRenderingVertexCountThreshold:{}", vertexCountPredicate, countSupplier.get(), lightweightRenderingVertexCountThreshold);
      return;
    }
//      log.info("vertexCountPredicate {} tested true with {} when lightweightRenderingVertexCountThreshold:{}", vertexCountPredicate, countSupplier.get(), lightweightRenderingVertexCountThreshold, vertexCountPredicate, countSupplier.get());

    log.trace("{} < {} is {}", scaleSupplier.get(), this.lightweightRenderingScaleThreshold, this.smallScaleOverridePredicate.test(scaleSupplier));
    if (this.smallScaleOverridePredicate.test(this.scaleSupplier)) {
        log.trace("do always becaue smallScaleOverridePredicate {} tested true with {} when lightweightRenderingScaleThreshold:{}", smallScaleOverridePredicate, scaleSupplier.get(), lightweightRenderingScaleThreshold);
        setMode(Mode.LIGHTWEIGHT);
        component.repaint();
        return;
    } else {
        log.trace("run timer because smallScaleOverridePredicate {} tested false with {} when lightweightRenderingScaleThreshold:{}", smallScaleOverridePredicate, scaleSupplier.get(), lightweightRenderingScaleThreshold);
    }
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

    public Timer() {
      setMode(Mode.LIGHTWEIGHT);
      component.repaint();
    }

    public void incrementValue() {
      value = 10;
    }

    public void run() {
      done = false;
      while (value > 0) {
        value--;
        try {
          Thread.sleep(10);
        } catch (InterruptedException ex) {
          ex.printStackTrace();
        }
      }
      setMode(Mode.DEFAULT);
      done = true;
      component.repaint();
    }
  }
}
