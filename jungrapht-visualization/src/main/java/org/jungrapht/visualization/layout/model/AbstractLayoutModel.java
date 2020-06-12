package org.jungrapht.visualization.layout.model;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.IterativeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.util.Threaded;
import org.jungrapht.visualization.layout.event.LayoutStateChange;
import org.jungrapht.visualization.layout.event.LayoutVertexPositionChange;
import org.jungrapht.visualization.layout.event.ModelChange;
import org.jungrapht.visualization.layout.event.ViewChange;
import org.jungrapht.visualization.layout.util.VisRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * superclass for LayoutModels. Holds the required attributes for placing graph vertices in a
 * visualization
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 */
public abstract class AbstractLayoutModel<V> implements LayoutModel<V> {

  private static final Logger log = LoggerFactory.getLogger(AbstractLayoutModel.class);

  public abstract static class Builder<
      V, T extends AbstractLayoutModel<V>, B extends Builder<V, T, B>> {
    protected Graph<V, ?> graph;
    protected int width;
    protected int height;

    public B graph(Graph<V, ?> graph) {
      this.graph = graph;
      return (B) this;
    }

    public B size(int width, int height) {
      this.width = width;
      this.height = height;
      return (B) this;
    }

    public B width(int width) {
      this.width = width;
      return (B) this;
    }

    public B height(int height) {
      this.height = height;
      return (B) this;
    }
  }

  protected Set<V> lockedVertices = new HashSet<>();
  protected boolean locked;
  protected int width;
  protected int height;
  protected int preferredWidth;
  protected int preferredHeight;

  protected Graph<V, ?> graph;
  protected VisRunnable visRunnable;
  /** @value relaxing true is this layout model is being accessed by a running relaxer */
  protected boolean relaxing;

  protected Future theFuture;
  protected LayoutVertexPositionChange.Support layoutVertexPositionSupport =
      LayoutVertexPositionChange.Support.create();
  protected LayoutStateChange.Support layoutStateChangeSupport = LayoutStateChange.Support.create();
  protected ModelChange.Support modelChangeSupport = ModelChange.Support.create();
  protected ViewChange.Support viewChangeSupport = ViewChange.Support.create();

  protected AbstractLayoutModel(Builder builder) {
    this.graph = Objects.requireNonNull(builder.graph);
    setSize(builder.width, builder.height);
    setPreferredSize(builder.width, builder.height);
  }

  protected AbstractLayoutModel(LayoutModel<V> other) {
    this.graph = other.getGraph();
    setSize(other.getWidth(), other.getHeight());
    setPreferredSize(other.getWidth(), other.getHeight());
  }

  protected AbstractLayoutModel(Graph<V, ?> graph, int width, int height) {
    this.graph = Objects.requireNonNull(graph);
    setSize(width, height);
  }

  /** stop any running Relaxer */
  public void stopRelaxer() {
    if (this.visRunnable != null) {
      this.visRunnable.stop();
    }
    if (theFuture != null) {
      theFuture.cancel(true);
    }
    setRelaxing(false);
  }

  @Override
  public Future getTheFuture() {
    return theFuture;
  }

  /**
   * accept the visit of a LayoutAlgorithm. If it is an IterativeContext, create a VisRunner to run
   * its relaxer in a new Thread. If there is a current VisRunner, stop it first.
   *
   * <ul>
   *   <li>set size to preferred width/height
   *   <li>fire LayoutStateChanged event
   *   <li>enable LayoutVertexPosition event firing
   *   <li>fire ModelChanged event
   *   <li>call the LayoutAlgorithm visit method to initiate the algorithm
   * </ul>
   *
   * @param layoutAlgorithm the algorithm to apply to the model vertex locations
   */
  @Override
  public void accept(LayoutAlgorithm<V> layoutAlgorithm) {
    if (this.visRunnable != null) {
      if (log.isTraceEnabled()) {
        log.trace("stopping {}", visRunnable);
      }
      this.visRunnable.stop();
    }
    if (theFuture != null) {
      theFuture.cancel(true);
    }
    setSize(preferredWidth, preferredHeight);
    log.trace("reset the model size to {},{}", preferredWidth, preferredHeight);
    // the layoutMode is active with a new LayoutAlgorithm
    layoutStateChangeSupport.fireLayoutStateChanged(this, true);
    layoutVertexPositionSupport.setFireEvents(true);
    modelChangeSupport.fireModelChanged();
    if (layoutAlgorithm != null) {
      layoutAlgorithm.visit(this);

      // e.g. the SugiyamaLayoutAlgorithm
      if (layoutAlgorithm instanceof Future) {
        this.theFuture = (Future) layoutAlgorithm;
      }
      if (layoutAlgorithm instanceof IterativeLayoutAlgorithm) {
        Threaded threadedLayoutAlgorithm = (Threaded) layoutAlgorithm;
        if (threadedLayoutAlgorithm.isThreaded()) {
          setRelaxing(true);
          // don't start a visRunner if the called has set threaded tp false
          setupVisRunner((IterativeLayoutAlgorithm) layoutAlgorithm);
        }
        // ...the visRunner will fire the layoutStateChanged event when it finishes

      } else if (!(layoutAlgorithm instanceof Threaded)
          || !((Threaded) layoutAlgorithm).isThreaded()) {
        layoutStateChangeSupport.fireLayoutStateChanged(this, false);
      }
    }
  }

  @Override
  public LayoutStateChange.Support getLayoutStateChangeSupport() {
    return layoutStateChangeSupport;
  }

  @Override
  public ModelChange.Support getModelChangeSupport() {
    return this.modelChangeSupport;
  }

  @Override
  public ViewChange.Support getViewChangeSupport() {
    return this.viewChangeSupport;
  }

  /**
   * create and start a new VisRunner for the passed IterativeContext
   *
   * @param iterativeContext the algorithm to run in a thread
   */
  protected void setupVisRunner(IterativeLayoutAlgorithm iterativeContext) {
    log.trace("this {} is setting up a visRunnable with {}", this, iterativeContext);
    if (visRunnable != null) {
      visRunnable.stop();
    }
    if (theFuture != null) {
      theFuture.cancel(true);
    }

    // layout becomes active
    layoutStateChangeSupport.fireLayoutStateChanged(this, true);
    // pre-relax phase. turn off event dispatch to the visualization system
    layoutVertexPositionSupport.setFireEvents(false);
    modelChangeSupport.setFireEvents(false);
    iterativeContext.preRelax();
    modelChangeSupport.setFireEvents(true);
    layoutVertexPositionSupport.setFireEvents(true);
    log.trace("prerelax is done");

    Executor executor = iterativeContext.getExecutor();

    visRunnable = new VisRunnable(iterativeContext);

    if (executor != null) {
      // use the Executor provided with the LayoutAlgorithm
      CompletableFuture.runAsync(visRunnable, executor)
          .thenRun(
              () -> {
                log.trace("We're done");
                setRelaxing(false);
                this.viewChangeSupport.fireViewChanged();
                // fire an event to say that the layout relax is done
                this.layoutStateChangeSupport.fireLayoutStateChanged(this, false);
              });
    } else {
      theFuture =
          CompletableFuture.runAsync(visRunnable)
              .thenRun(
                  () -> {
                    log.trace("We're done");
                    setRelaxing(false);
                    this.viewChangeSupport.fireViewChanged();
                    // fire an event to say that the layout relax is done
                    this.layoutStateChangeSupport.fireLayoutStateChanged(this, false);
                  });
    }
  }

  /** @return the graph */
  @Override
  public <E> Graph<V, E> getGraph() {
    return (Graph<V, E>) graph;
  }

  @Override
  public void setGraph(Graph<V, ?> graph) {
    this.graph = graph;
    this.modelChangeSupport.fireModelChanged();
    if (log.isTraceEnabled()) {
      log.trace("setGraph to n:{} e:{}", graph.vertexSet(), graph.edgeSet());
    }
  }

  /**
   * set locked state for the provided vertex
   *
   * @param vertex the vertex to affect
   * @param locked to lock or not
   */
  @Override
  public void lock(V vertex, boolean locked) {
    if (locked) {
      this.lockedVertices.add(vertex);
    } else {
      this.lockedVertices.remove(vertex);
    }
  }

  /**
   * @param vertex the vertex whose locked state is being queried
   * @return whether the passed vertex is locked
   */
  @Override
  public boolean isLocked(V vertex) {
    return this.lockedVertices.contains(vertex);
  }

  /**
   * lock the entire model (all vertices)
   *
   * @param locked will prevent the vertices from being moved
   */
  @Override
  public void lock(boolean locked) {
    log.trace("lock:{}", locked);
    this.locked = locked;
  }

  /** @return whether this LayoutModel is locked for all vertices */
  @Override
  public boolean isLocked() {
    return this.locked;
  }

  /** */
  public void setSize(int width, int height) {
    if (width <= 0) {
      width = preferredWidth;
    }
    if (height <= 0) {
      height = preferredHeight;
    }
    this.width = width;
    this.height = height;
    log.trace("setSize to {} by {}", this.width, this.height);
  }

  public void setPreferredSize(int preferredWidth, int preferredHeight) {
    if (preferredWidth == 0 || preferredHeight == 0) {
      throw new IllegalArgumentException(
          "Can't be zeros " + preferredWidth + "/" + preferredHeight);
    }
    this.preferredWidth = preferredWidth;
    this.preferredHeight = preferredHeight;
  }

  /** */
  public void setSize(int width, int height, boolean adjustLocations) {
    if (width == 0 || height == 0) {
      throw new IllegalArgumentException("Can't be zeros " + width + "/" + height);
    }
    int oldWidth = this.width;
    int oldHeight = this.height;

    if (oldWidth == width && oldHeight == height) {
      return;
    }

    if (oldWidth != 0 || oldHeight != 0 && adjustLocations) {
      adjustLocations(oldWidth, oldHeight, width, height); //, size);
    }
    this.width = width;
    this.height = height;
  }

  /**
   * mode all the vertices to the new center of the layout domain
   *
   * @param oldWidth previous width
   * @param oldHeight revious height
   * @param width new width
   * @param height new height
   */
  private void adjustLocations(int oldWidth, int oldHeight, int width, int height) {
    if (oldWidth == width && oldHeight == height) {
      return;
    }

    int xOffset = (width - oldWidth) / 2;
    int yOffset = (height - oldHeight) / 2;

    // now, move each vertex to be at the new screen center
    while (true) {
      try {
        for (V vertex : this.graph.vertexSet()) {
          offsetvertex(vertex, xOffset, yOffset);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
  }

  /** @return the width of the layout domain */
  @Override
  public int getWidth() {
    return width;
  }

  /** @return the height of the layout domain */
  @Override
  public int getHeight() {
    return height;
  }

  /** @return the width of the layout domain */
  @Override
  public int getPreferredWidth() {
    return preferredWidth;
  }

  /** @return the height of the layout domain */
  @Override
  public int getPreferredHeight() {
    return preferredHeight;
  }

  @Override
  public void set(V vertex, double x, double y) {
    this.set(vertex, Point.of(x, y));
  }

  @Override
  public void set(V vertex, Point location) {
    layoutVertexPositionSupport.fireLayoutVertexPositionChanged(vertex, location);
    viewChangeSupport.fireViewChanged();
  }

  /**
   * @param vertex the vertex whose coordinates are to be offset
   * @param xOffset the change to apply to this vertex's x coordinate
   * @param yOffset the change to apply to this vertex's y coordinate
   */
  protected void offsetvertex(V vertex, double xOffset, double yOffset) {
    if (!locked && !isLocked(vertex)) {
      Point p = get(vertex);
      this.set(vertex, p.x + xOffset, p.y + yOffset);
    }
  }

  @Override
  public LayoutVertexPositionChange.Support<V> getLayoutVertexPositionSupport() {
    return this.layoutVertexPositionSupport;
  }

  public boolean isRelaxing() {
    return relaxing;
  }

  public void setRelaxing(boolean relaxing) {
    log.trace("setRelaxing:{}", relaxing);
    this.relaxing = relaxing;
  }

  @Override
  public void layoutVertexPositionChanged(LayoutVertexPositionChange.Event<V> evt) {
    visRunnable.stop();
  }

  @Override
  public void layoutVertexPositionChanged(LayoutVertexPositionChange.GraphEvent<V> evt) {}

  @Override
  public void resizeToSurroundingRectangle() {
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    for (Point p : getLocations().values()) {
      if (p.x > maxX) maxX = (int) p.x;
      if (p.x < minX) minX = (int) p.x;
      if (p.y > maxY) maxY = (int) p.y;
      if (p.y < minY) minY = (int) p.y;
    }
    int vertexSpaceWidth = maxX - minX;
    if (vertexSpaceWidth <= 0) {
      vertexSpaceWidth = 100;
    }
    int vertexSpaceHeight = maxY - minY;
    if (vertexSpaceHeight <= 0) {
      vertexSpaceHeight = 100;
    }
    int paddingX = vertexSpaceWidth / 10;
    int paddingY = vertexSpaceHeight / 10;
    int dx = 0;
    int dy = 0;
    minX -= paddingX;
    minY -= paddingY;
    if (minX != 0) {
      dx = -minX;
    }
    if (minY != 0) {
      dy = -minY;
    }
    maxX += paddingX;
    maxY += paddingY;
    if (dx != 0 || dy != 0) {
      for (V v : getGraph().vertexSet()) {
        Point vp = apply(v).add(dx, dy);
        set(v, vp);
      }
    }
    int newWidth = maxX + dx;
    int newHeight = maxY + dy;
    setSize(newWidth, newHeight);
  }

  @Override
  public String toString() {
    return "AbstractLayoutModel{"
        + "hashCode="
        + hashCode()
        + ", width="
        + width
        + ", height="
        + height
        + ", graph of size ="
        + graph.vertexSet().size()
        + '}';
  }
}
