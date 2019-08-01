package org.jungrapht.visualization.util;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.renderers.Renderer;

public class LightweightRenderingVisitor implements ChangeListener {

  private VisualizationServer visualizationServer;
  private Timer timer;

  public static void visit(VisualizationServer visualizationServer) {
    visit(visualizationServer, 0.5);
  }

  public static void visit(VisualizationServer visualizationServer, double scaleLimit) {
    visualizationServer.addChangeListener(
        new LightweightRenderingVisitor(visualizationServer, scaleLimit));
  }

  public static <V, E> void visit(
      VisualizationServer visualizationServer,
      Renderer<V, E> lightweightRenderer,
      double scaleLimit) {
    visualizationServer.addChangeListener(
        new LightweightRenderingVisitor(visualizationServer, scaleLimit));
    visualizationServer.setLightweightRenderer(lightweightRenderer);
  }

  private LightweightRenderingVisitor(VisualizationServer visualizationServer) {
    this(visualizationServer, 0.5);
  }

  private LightweightRenderingVisitor(VisualizationServer visualizationServer, double scaleLimit) {
    this.visualizationServer = visualizationServer;
    visualizationServer.setSmallScaleOverridePredicate(scale -> (double) scale < scaleLimit);
  }

  @Override
  public void stateChanged(ChangeEvent e) {
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
