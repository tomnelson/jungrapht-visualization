package org.jungrapht.visualization.layout.model;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.IterativeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.jungrapht.visualization.layout.algorithms.util.Threaded;
import org.jungrapht.visualization.layout.event.LayoutSizeChange;
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
    protected Function<Graph<V, ?>, Pair<Integer>> initialDimensionFunction =
        g -> Pair.of(width, height);

    // the model will create a VisRunnable and start it in a new Thread
    protected boolean createVisRunnable = true;

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

    public B initialDimensionFunction(
        Function<Graph<V, ?>, Pair<Integer>> initialDimensionFunction) {
      // do not set to null. Default implementation returns the
      // preferred width and height
      if (initialDimensionFunction != null) {
        this.initialDimensionFunction = initialDimensionFunction;
      }
      return (B) this;
    }

    public B createVisRunnable(boolean createVisRunnable) {
      this.createVisRunnable = createVisRunnable;
      return (B) this;
    }
  }

  protected Set<V> lockedVertices = new HashSet<>();
  protected boolean locked;
  protected int width;
  protected int height;
  protected int preferredWidth;
  protected int preferredHeight;
  protected boolean createVisRunnable;

  protected Graph<V, ?> graph;
  protected VisRunnable visRunnable = VisRunnable.noop();
  /** @value relaxing true is this layout model is being accessed by a running relaxer */
  protected boolean relaxing;

  /** Handles events fired when vertex locations are changed */
  protected LayoutVertexPositionChange.Support layoutVertexPositionSupport =
      LayoutVertexPositionChange.Support.create();
  /** Handles events fired when the LayoutModel is actively being modified by a LayoutAlgorithm */
  protected LayoutStateChange.Support layoutStateChangeSupport = LayoutStateChange.Support.create();

  /** Handles events fired when the Graph is changed */
  protected ModelChange.Support modelChangeSupport = ModelChange.Support.create();

  protected ViewChange.Support viewChangeSupport = ViewChange.Support.create();

  protected LayoutSizeChange.Support layoutSizeChangeSupport = LayoutSizeChange.Support.create();

  protected int appendageCount;

  protected Function<Graph<V, ?>, Pair<Integer>> initialDimensionFunction;

  protected AbstractLayoutModel(Builder builder) {
    this.graph = Objects.requireNonNull(builder.graph);
    this.initialDimensionFunction = builder.initialDimensionFunction; // check for null before use
    setSize(builder.width, builder.height);
    setPreferredSize(builder.width, builder.height);
    this.createVisRunnable = builder.createVisRunnable;
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

  public void setInitialDimensionFunction(
      Function<Graph<V, ?>, Pair<Integer>> initialDimensionFunction) {
    this.initialDimensionFunction = initialDimensionFunction;
  }

  /** stop any running Relaxer */
  public void stop() {
    if (this.visRunnable != null) {
      this.visRunnable.stop();
    }
    setRelaxing(false);
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
    if (graph.vertexSet().isEmpty()) {
      return;
    }
    log.debug("accept {}", layoutAlgorithm);
    this.appendageCount = 0;
    // stop any running layout algorithm
    if (this.visRunnable != null) {
      if (log.isTraceEnabled()) {
        log.trace("stopping {}", visRunnable);
      }
      this.visRunnable.stop();
    }
    // if there is an initialDimensionFunction, and if the LayoutAlgorithm
    // is not an Unconstrained type (not a Tree or Circle) then apply the function to
    // set the layout area constraints
    log.debug("{} is constrained: {}", layoutAlgorithm, layoutAlgorithm.constrained());

    if (layoutAlgorithm.constrained()) {
      log.trace("{} constrained: {}", layoutAlgorithm, true);
      Pair<Integer> dimension = initialDimensionFunction.apply(graph);
      // setSize will fire an event with the new size
      setSize(dimension.first, dimension.second);
    } else {
      //      log.trace("{} constrained: {}", layoutAlgorithm, false);
      //       setSize will fire an event if the size has changed
      //            setSize(preferredWidth, preferredHeight);
      // leave the size the same but fire an event for the transforms
      //      getLayoutSizeChangeSupport().fireLayoutSizeChanged(this, width, height);
    }

    log.trace("reset the model size to {} x {}", preferredWidth, preferredHeight);
    // the layoutMode is active with a new LayoutAlgorithm
    layoutStateChangeSupport.fireLayoutStateChanged(this, true);
    layoutVertexPositionSupport.setFireEvents(true);
    modelChangeSupport.fireModelChanged();
    if (layoutAlgorithm != null) {
      layoutAlgorithm.visit(this);

      if (graph.vertexSet().size() > 0
          && // don't create thread for empty graph
          createVisRunnable
          && layoutAlgorithm instanceof IterativeLayoutAlgorithm) {
        setRelaxing(true);
        // don't start a visRunner if the called has set threaded to false
        setupVisRunner((IterativeLayoutAlgorithm) layoutAlgorithm);
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
      log.debug("start visRunner thread");
      CompletableFuture.runAsync(visRunnable, executor)
          .thenRun(
              () -> {
                log.debug("We're done");
                setRelaxing(false);
                this.viewChangeSupport.fireViewChanged();
                // fire an event to say that the layout relax is done
                this.layoutStateChangeSupport.fireLayoutStateChanged(this, false);
              });
    } else {
      log.debug("start visRunner thread");
      CompletableFuture.runAsync(visRunnable)
          .thenRun(
              () -> {
                log.debug("We're done");
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
    this.appendageCount = 0;
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
    log.trace("setSize({} x {}), old values: {} x {}", width, height, this.width, this.height);
    if (width <= 0) {
      width = preferredWidth;
    }
    if (height <= 0) {
      height = preferredHeight;
    }
    this.width = width;
    this.height = height;
    layoutSizeChangeSupport.fireLayoutSizeChanged(this, width, height);
    log.trace("setSize to {} by {}", this.width, this.height);
  }

  public void setPreferredSize(int preferredWidth, int preferredHeight) {
    log.trace("setPreferredSize({},{})", preferredWidth, preferredHeight);
    if (preferredWidth <= 0 || preferredHeight <= 0) {
      throw new IllegalArgumentException(
          "Can't be <= zeros " + preferredWidth + "/" + preferredHeight);
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

  @Override
  public LayoutSizeChange.Support getLayoutSizeChangeSupport() {
    return this.layoutSizeChangeSupport;
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
    //    layoutStateChangeSupport.fireLayoutStateChanged(this, true);
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
    //    layoutStateChangeSupport.fireLayoutStateChanged(this, false);
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

  /**
   * Append the supplied layoutModel to the right of this LayoutModel by increasing the width of
   * this layoutModel by the width of the supplied LayoutModel, and by offsetting all locations in
   * the new layoutModel by the previous width of this layoutModel
   *
   * @param layoutModel contains width and vertex locations to place
   */
  @Override
  public void appendLayoutModel(LayoutModel<V> layoutModel) {
    if (log.isTraceEnabled()) {
      log.trace(
          "appending layoutModel with width {} to this layoutModel width:{}",
          layoutModel.getWidth(),
          this.width);
    }
    if (appendageCount++ == 0) {
      // first one in
      int newWidth = layoutModel.getWidth();
      int newHeight = Math.max(this.height, layoutModel.getHeight());
      setSize(newWidth, newHeight); // so it will fire the event
      layoutModel.getLocations().keySet().stream().forEach(v -> this.set(v, layoutModel.get(v)));

    } else {
      // how much to move over incoming vertex locations
      int widthDelta = this.width;
      // make this wider
      int newWidth = this.width + layoutModel.getWidth();
      int newHeight = Math.max(this.height, layoutModel.getHeight());
      setSize(newWidth, newHeight); // so it will fire the event
      // offset the incoming vertex locations and place them in this layoutModel
      layoutModel
          .getLocations()
          .keySet()
          .stream()
          .forEach(v -> this.set(v, layoutModel.get(v).add(widthDelta, 0)));
    }
  }
}
