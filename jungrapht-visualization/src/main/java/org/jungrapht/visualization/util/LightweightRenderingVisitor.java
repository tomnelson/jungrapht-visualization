package org.jungrapht.visualization.util;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jungrapht.visualization.VisualizationServer;

public class LightweightRenderingVisitor implements ChangeListener {

  private VisualizationServer visualizationServer;
  private Timer timer;

  public static void visit(VisualizationServer visualizationServer) {
    visualizationServer.addChangeListener(new LightweightRenderingVisitor(visualizationServer));
  }

  private LightweightRenderingVisitor(VisualizationServer visualizationServer) {
    this.visualizationServer = visualizationServer;
    visualizationServer.setSmallScaleOverridePredicate(scale -> (double) scale < 0.5);
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
