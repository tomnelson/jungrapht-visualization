package org.jungrapht.visualization.util;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.renderers.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LightweightRenderingVisitor implements ChangeListener {

  private static Logger log = LoggerFactory.getLogger(LightweightRenderingVisitor.class);
  private VisualizationServer visualizationServer;
  private Timer timer;
  private int threshold = DEFAULT_THRESHOLD;
  private static final int DEFAULT_THRESHOLD = 19;

  public static void visit(VisualizationServer visualizationServer) {
    visit(visualizationServer, DEFAULT_THRESHOLD);
  }

  public static void visit(
      VisualizationServer visualizationServer, double scaleLimit, int threshold) {
    visualizationServer.addChangeListener(
        new LightweightRenderingVisitor(visualizationServer, scaleLimit, threshold));
  }

  public static void visit(VisualizationServer visualizationServer, double scaleLimit) {
    visit(visualizationServer, scaleLimit, DEFAULT_THRESHOLD);
  }

  public static <V, E> void visit(
      VisualizationServer visualizationServer,
      Renderer<V, E> lightweightRenderer,
      double scaleLimit) {
    visualizationServer.addChangeListener(
        new LightweightRenderingVisitor(visualizationServer, scaleLimit, DEFAULT_THRESHOLD));
    visualizationServer.setLightweightRenderer(lightweightRenderer);
  }

  private LightweightRenderingVisitor(VisualizationServer visualizationServer) {
    this(visualizationServer, 0.5, DEFAULT_THRESHOLD);
  }

  private LightweightRenderingVisitor(
      VisualizationServer visualizationServer, double scaleLimit, int threshold) {
    this.visualizationServer = visualizationServer;
    this.threshold = threshold;
    visualizationServer.setSmallScaleOverridePredicate(scale -> (double) scale < scaleLimit);
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    if (visualizationServer.getModel().getGraph().vertexSet().size() < threshold) {
      return;
    }
    if (timer == null || timer.done) {
      timer = new Timer(visualizationServer);
      timer.start();
    } else {
      timer.incrementValue();
    }
  }

  static class Timer extends Thread {
    long value = 10;
    boolean done;
    VisualizationServer visualizationServer;

    public Timer(VisualizationServer visualizationServer) {
      this.visualizationServer = visualizationServer;
      visualizationServer.simplifyRenderer(true);
      visualizationServer.repaint();
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
      visualizationServer.simplifyRenderer(false);
      done = true;
      visualizationServer.repaint();
    }
  }
}
