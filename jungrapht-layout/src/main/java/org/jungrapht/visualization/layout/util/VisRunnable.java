package org.jungrapht.visualization.layout.util;

import org.jungrapht.visualization.layout.algorithms.util.IterativeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a {@code Runnable} object to pass to the {@code Thread} that will perform the relax function on a
 * graph layout
 *
 * @author Tom Nelson
 */
public class VisRunnable implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(VisRunnable.class);
  private final IterativeContext iterativeContext;
  private long sleepTime = 10;
  private boolean stop = false;

  public static VisRunnable noop() {
    return new VisRunnable(null) {
      public void run() {}
    };
  }

  public VisRunnable(IterativeContext iterativeContext) {
    log.trace("created a VisRunnable {} for {}", hashCode(), iterativeContext);
    this.iterativeContext = iterativeContext;
  }

  public void stop() {
    log.trace("told {} to stop", this);
    stop = true;
  }

  @Override
  public void run() {
    while (!iterativeContext.done() && !stop) {
      try {
        iterativeContext.step();
        try {
          Thread.sleep(sleepTime);
        } catch (InterruptedException ex) {
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    if (stop) {
      log.trace("done here because {} stop = {}", hashCode(), stop);
    }
  }
}
