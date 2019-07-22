package org.jungrapht.visualization.layout.model;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Sets;
import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.IterativeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.event.LayoutChange;
import org.jungrapht.visualization.layout.event.LayoutNodePositionChange;
import org.jungrapht.visualization.layout.event.LayoutStateChange;
import org.jungrapht.visualization.layout.util.VisRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * superclass for LayoutModels. Holds the required attributes for placing graph nodes in a
 * visualization
 *
 * @author Tom Nelson
 * @param <N> the node type
 */
public abstract class AbstractLayoutModel<N> implements LayoutModel<N> {

  private static final Logger log = LoggerFactory.getLogger(AbstractLayoutModel.class);

  public abstract static class Builder<
      N, T extends AbstractLayoutModel<N>, B extends Builder<N, T, B>> {
    protected Graph<N, ?> graph;
    protected int width;
    protected int height;

    public B graph(Graph<N, ?> graph) {
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

  protected Set<N> lockedNodes = Sets.newHashSet();
  protected boolean locked;
  protected int width;
  protected int height;
  protected Graph<N, ?> graph;
  protected VisRunnable visRunnable;
  /** @value relaxing true is this layout model is being accessed by a running relaxer */
  protected boolean relaxing;

  protected CompletableFuture theFuture;
  protected LayoutNodePositionChange.Support layoutNodePositionSupport =
      LayoutNodePositionChange.Support.create();
  protected LayoutStateChange.Support layoutStateChangeSupport = LayoutStateChange.Support.create();
  protected LayoutChange.Support layoutChangeSupport = LayoutChange.Support.create();

  protected AbstractLayoutModel(Builder builder) {
    this.graph = checkNotNull(builder.graph);
    setSize(builder.width, builder.height);
  }

  protected AbstractLayoutModel(Graph<N, ?> graph, int width, int height) {
    this.graph = checkNotNull(graph);
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

  public CompletableFuture getTheFuture() {
    return theFuture;
  }

  /**
   * accept the visit of a LayoutAlgorithm. If it is an IterativeContext, create a VisRunner to run
   * its relaxer in a new Thread. If there is a current VisRunner, stop it first.
   *
   * @param layoutAlgorithm the algorithm to apply to the model node locations
   */
  @Override
  public void accept(LayoutAlgorithm<N> layoutAlgorithm) {
    // the layoutMode is active with a new LayoutAlgorithm
    layoutStateChangeSupport.fireLayoutStateChanged(this, true);
    log.trace("accepting {}", layoutAlgorithm);
    layoutNodePositionSupport.setFireEvents(true);
    layoutChangeSupport.fireLayoutChanged();
    if (this.visRunnable != null) {
      log.trace("stopping {}", visRunnable);
      this.visRunnable.stop();
    }
    if (theFuture != null) {
      theFuture.cancel(true);
    }
    if (log.isTraceEnabled()) {
      log.trace("{} will visit {}", layoutAlgorithm, this);
    }
    if (layoutAlgorithm != null) {
      layoutAlgorithm.visit(this);

      if (layoutAlgorithm instanceof IterativeLayoutAlgorithm) {
        setRelaxing(true);
        setupVisRunner((IterativeLayoutAlgorithm) layoutAlgorithm);

        // need to have the visRunner fire the layoutStateChanged event when it finishes
      } else {
        if (log.isTraceEnabled()) {
          log.trace("no visRunner for this {}", this);
        }
        // the layout model has finished with the layout algorithm
        log.trace("will fire layoutStateCHanged with false");
        layoutStateChangeSupport.fireLayoutStateChanged(this, false);
      }
    }
  }

  @Override
  public LayoutStateChange.Support getLayoutStateChangeSupport() {
    return layoutStateChangeSupport;
  }

  @Override
  public LayoutChange.Support getLayoutChangeSupport() {
    return this.layoutChangeSupport;
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
    layoutNodePositionSupport.setFireEvents(false);
    layoutChangeSupport.setFireEvents(false);
    iterativeContext.preRelax();
    layoutChangeSupport.setFireEvents(true);
    layoutNodePositionSupport.setFireEvents(true);
    log.trace("prerelax is done");

    visRunnable = new VisRunnable(iterativeContext);
    theFuture =
        CompletableFuture.runAsync(visRunnable)
            .thenRun(
                () -> {
                  log.trace("We're done");
                  setRelaxing(false);
                  this.layoutChangeSupport.fireLayoutChanged();
                  // fire an event to say that the layout relax is done
                  this.layoutStateChangeSupport.fireLayoutStateChanged(this, false);
                });
  }

  /** @return the graph */
  @Override
  public Graph<N, ?> getGraph() {
    return graph;
  }

  public void setGraph(Graph<N, ?> graph) {
    this.graph = graph;
    this.layoutChangeSupport.fireLayoutChanged();
    if (log.isTraceEnabled()) {
      log.trace("setGraph to n:{} e:{}", graph.vertexSet(), graph.edgeSet());
    }
  }

  /**
   * set locked state for the provided node
   *
   * @param node the node to affect
   * @param locked to lock or not
   */
  @Override
  public void lock(N node, boolean locked) {
    if (locked) {
      this.lockedNodes.add(node);
    } else {
      this.lockedNodes.remove(node);
    }
  }

  /**
   * @param node the node whose locked state is being queried
   * @return whether the passed node is locked
   */
  @Override
  public boolean isLocked(N node) {
    return this.lockedNodes.contains(node);
  }

  /**
   * lock the entire model (all nodes)
   *
   * @param locked will prevent the nodes from being moved
   */
  @Override
  public void lock(boolean locked) {
    log.trace("lock:{}", locked);
    this.locked = locked;
  }

  /** @return whether this LayoutModel is locked for all nodes */
  @Override
  public boolean isLocked() {
    return this.locked;
  }

  /** */
  public void setSize(int width, int height) {
    if (width == 0 || height == 0) {
      throw new IllegalArgumentException("Can't be zeros " + width + "/" + height);
    }
    this.width = width;
    this.height = height;
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
   * mode all the nodes to the new center of the layout domain
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

    // now, move each node to be at the new screen center
    while (true) {
      try {
        for (N node : this.graph.vertexSet()) {
          offsetnode(node, xOffset, yOffset);
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

  @Override
  public void set(N node, double x, double y) {
    this.set(node, Point.of(x, y));
  }

  @Override
  public void set(N node, Point location) {
    layoutNodePositionSupport.fireLayoutNodePositionChanged(node, location);
    layoutChangeSupport.fireLayoutChanged();
  }

  /**
   * @param node the node whose coordinates are to be offset
   * @param xOffset the change to apply to this node's x coordinate
   * @param yOffset the change to apply to this node's y coordinate
   */
  protected void offsetnode(N node, double xOffset, double yOffset) {
    if (!locked && !isLocked(node)) {
      Point p = get(node);
      this.set(node, p.x + xOffset, p.y + yOffset);
    }
  }

  @Override
  public LayoutNodePositionChange.Support<N> getLayoutNodePositionSupport() {
    return this.layoutNodePositionSupport;
  }

  public boolean isRelaxing() {
    return relaxing;
  }

  public void setRelaxing(boolean relaxing) {
    log.trace("setRelaxing:{}", relaxing);
    this.relaxing = relaxing;
  }

  @Override
  public void layoutNodePositionChanged(LayoutNodePositionChange.Event<N> evt) {
    visRunnable.stop();
  }

  @Override
  public void layoutNodePositionChanged(LayoutNodePositionChange.NetworkEvent<N> evt) {}

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
