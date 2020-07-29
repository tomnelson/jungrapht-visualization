package org.jungrapht.visualization.layout.algorithms;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFA2Repulsion;
import org.jungrapht.visualization.layout.algorithms.repulsion.StandardFA2Repulsion;
import org.jungrapht.visualization.layout.algorithms.util.IterativeContext;
import org.jungrapht.visualization.layout.algorithms.util.VertexBoundsFunctionConsumer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of ForceAtlas2 algorithm.
 *
 * @see "ForceAtlas2, a Continuous Graph Layout Algorithm for Handy Network Visualization Designed
 *     for the Gephi Software"
 * @see "https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0098679"
 * @param <V>
 */
public class ForceAtlas2LayoutAlgorithm<V> extends AbstractIterativeLayoutAlgorithm<V>
    implements VertexBoundsFunctionConsumer<V>, IterativeContext {
  private static final Logger log = LoggerFactory.getLogger(ForceAtlas2LayoutAlgorithm.class);

  // Initializer
  private Function<V, Point> initializer;

  // Constants
  private static final double ks = 0.1; // Global speed coefficient
  private static final double ksMax = 10.0; // Global speed maximum coefficient
  private static final double epsilon = 1e-16; // Math stability

  // Attraction
  private boolean useLinLog = false; // LinLog model
  private boolean attractionByWeights = false; // Weighted attraction
  private double weightsDelta = 1.0; // Weights power
  private boolean dissuadeHubs = false; // Dissuade Hubs mode

  // Repulsion
  private StandardFA2Repulsion.Builder repulsionContractBuilder;
  private StandardFA2Repulsion repulsionContract;

  // Meta parameters
  private int maxIterations;
  private int currentIteration;

  // Gravity
  private double kg = 5.0;

  // Sizes and masses
  private Function<V, Double> nodeSizes;
  private Map<V, Double> nodeMasses;

  // Swinging
  private double globalSwg; // Global swinging
  private double globalTra; // Global traces
  private Map<V, Double> swg; //Local swinging
  private Map<V, Double> trace; // Local traces
  private double speed = 1.0; // Initial speed
  private double tolerance = 1.0; // Tolerance to swinging

  // Forces
  private Map<V, Point> frVertexData;
  private Map<V, Point> prevStepFrVertexData;

  private boolean tuneToGraphSize;

  public static class Builder<
          V, T extends ForceAtlas2LayoutAlgorithm<V>, B extends Builder<V, T, B>>
      extends AbstractIterativeLayoutAlgorithm.Builder<V, T, B>
      implements LayoutAlgorithm.Builder<V, T, B> {
    private StandardFA2Repulsion.Builder repulsionContractBuilder =
        new BarnesHutFA2Repulsion.Builder();

    private boolean useLinLog = false;
    private boolean attractionByWeights = false;
    private double weightsDelta = 1.0;
    private boolean dissuadeHubs = false;
    private int maxIterations = 1000;
    private double kg = 5.0;
    private double tolerance = 1.0;
    private Map<V, Double> nodeSizes = null;
    private Map<V, Double> nodeMasses = null;
    private Function<V, Point> initializer =
        v -> Point.of(random.nextDouble(), random.nextDouble());
    //    private boolean tuneToGraphSize = true;

    public B repulsionContractBuilder(StandardFA2Repulsion.Builder repulsionContractBuilder) {
      this.repulsionContractBuilder = repulsionContractBuilder;
      return self();
    }

    /**
     * Set this function if you want to use pre-computed or old layout.
     *
     * @param initializer
     * @return
     */
    public B initializer(Function<V, Point> initializer) {
      this.initializer = initializer;
      return (B) this;
    }

    /**
     * Set usage of LinLog model. Andreas Noack produced an excellent work on placement quality
     * measures. His LinLog energy model arguably provides the most readable placements, since it
     * results in a placement that corresponds to Newman’s modularity, a widely used measure of
     * community structure. The LinLog mode just uses a logarithmic attraction force.
     *
     * <p>Default: false
     *
     * @see "https://opus4.kobv.de/opus4-btu/files/377/ThesisNoack.pdf"
     * @see "https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0098679"
     * @param useLinLog
     * @return
     */
    public B linLog(boolean useLinLog) {
      this.useLinLog = useLinLog;
      return (B) this;
    }

    /**
     * Set usage of Attraction by Weights concept.
     *
     * <p>Default: false
     *
     * @see "https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0098679"
     * @param attractionByWeights
     * @return
     */
    public B attractionByWeights(boolean attractionByWeights) {
      this.attractionByWeights = attractionByWeights;
      return (B) this;
    }

    /**
     * Set weights power. Only for AttractionByWeights mode. If the setting “Edge Weight Influence”
     * is set to 0, the weights are ignored. If it is set to 1, then the attraction is proportional
     * to the weight. Values above 1 emphasize the weight effects.
     *
     * <p>Default: 1.0
     *
     * @see "https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0098679"
     * @param weightsDelta
     * @return
     */
    public B delta(double weightsDelta) {
      this.weightsDelta = weightsDelta;
      return (B) this;
    }

    /**
     * Set usage of "Dissuade Hubs" model. "Dissuade Hubs" tends to push hubs to the periphery while
     * keeping authorities in the center.
     *
     * <p>Default: false
     *
     * @see "https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0098679"
     * @see "https://dl.acm.org/doi/abs/10.1145/324133.324140"
     * @param dissuadeHubs
     * @return
     */
    public B dissuadeHubs(boolean dissuadeHubs) {
      this.dissuadeHubs = dissuadeHubs;
      return (B) this;
    }

    /**
     * Set number of iterations.
     *
     * <p>Default: 400
     *
     * @param maxIterations
     * @return
     */
    public B maxIterations(int maxIterations) {
      this.maxIterations = maxIterations;
      return (B) this;
    }

    /**
     * Set gravity K. Gravity is a common improvement of force-directed layouts. This force prevents
     * disconnected components (islands) from drifting away. It attracts nodes to the center of the
     * spatialization space. Recommended values 1.0-5.0
     *
     * <p>Default: 5.0
     *
     * @see "https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0098679"
     * @param kg
     * @return
     */
    public B gravityK(double kg) {
      this.kg = kg;
      return (B) this;
    }

    /**
     * Tolerance to swinging. Higher value of tolerance allow higher value of speed. For details see
     * original paper about ForceAtlas2.
     *
     * <p>Default: 1.0
     *
     * @see "https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0098679"
     * @param tolerance
     * @return
     */
    public B tolerance(double tolerance) {
      this.tolerance = tolerance;
      return (B) this;
    }

    /**
     * Set node sizes. They may have fixed size or size based on centrality measure or anything
     * else.
     *
     * <p>Default: 1.0
     *
     * @see "https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0098679"
     * @param nodeSizes
     * @return
     */
    public B nodeSizes(Map<V, Double> nodeSizes) {
      this.nodeSizes = nodeSizes;
      return (B) this;
    }

    /**
     * Set node masses. This may have fixed masses or masses based on degrees or anything else.
     *
     * <p>By default node degrees plus one
     *
     * @see "https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0098679"
     * @param nodeMasses
     * @return
     */
    public B nodeMasses(Map<V, Double> nodeMasses) {
      this.nodeMasses = nodeMasses;
      return (B) this;
    }

    //    public B tuneToGraphSize(boolean tuneToGraphSize) {
    //      this.tuneToGraphSize = tuneToGraphSize;
    //      return (B)this;
    //    }

    @Override
    public T build() {
      return (T) new ForceAtlas2LayoutAlgorithm<V>(this);
    }
  }

  public static <V> ForceAtlas2LayoutAlgorithm.Builder<V, ?, ?> builder() {
    return new ForceAtlas2LayoutAlgorithm.Builder<>();
  }

  public ForceAtlas2LayoutAlgorithm() {
    this(ForceAtlas2LayoutAlgorithm.builder());
  }

  /**
   * Create instance with the configured builder. Note that layoutModel is null until the visit
   * method is called.
   *
   * @param builder
   */
  protected ForceAtlas2LayoutAlgorithm(ForceAtlas2LayoutAlgorithm.Builder<V, ?, ?> builder) {
    super(builder);
    this.useLinLog = builder.useLinLog;
    this.attractionByWeights = builder.attractionByWeights;
    this.weightsDelta = builder.weightsDelta;
    this.dissuadeHubs = builder.dissuadeHubs;
    this.maxIterations = builder.maxIterations;
    this.kg = builder.kg;
    this.tolerance = builder.tolerance;
    this.initializer = builder.initializer;
    this.repulsionContractBuilder = builder.repulsionContractBuilder;
  }

  @Override
  public void setVertexBoundsFunction(Function<V, Rectangle> vertexBoundsFunction) {
    nodeSizes =
        v -> {
          Rectangle r = vertexBoundsFunction.apply(v);
          return Math.max(r.width, r.height);
        };
  }

  @Override
  public void visit(LayoutModel<V> layoutModel) {
    super.visit(layoutModel);
    Graph<V, ?> graph = layoutModel.getGraph();
    if (graph == null || graph.vertexSet().isEmpty()) {
      return;
    }
    if (nodeSizes == null) {
      this.nodeSizes = v -> 1.0;
    }

    this.nodeMasses =
        layoutModel
            .getGraph()
            .vertexSet()
            .stream()
            .collect(Collectors.toMap(v -> v, v -> layoutModel.getGraph().degreeOf(v) + 1.0));

    if (log.isTraceEnabled()) {
      log.trace("visiting " + layoutModel);
    }

    frVertexData = new ConcurrentHashMap<>(layoutModel.getGraph().vertexSet().size());
    for (V vertex : layoutModel.getGraph().vertexSet()) {
      frVertexData.put(vertex, Point.ORIGIN);
    }
    swg = new HashMap<>(layoutModel.getGraph().vertexSet().size());
    trace = new HashMap<>(layoutModel.getGraph().vertexSet().size());

    //    if (tuneToGraphSize) {
    //      Graph<V,?> graph = layoutModel.getGraph();
    //      int vertexCount = graph.vertexSet().size();
    //      kg = 10000.0 / vertexCount;
    ////      if (graph.vertexSet().size() < 100) {
    ////        kg = 100.0;
    ////      } else if (graph.vertexSet().size() < 1000) {
    ////        kg = 1.0;
    ////      } else {
    ////        kg = 0.1;
    ////      }
    //    }

    repulsionContract =
        repulsionContractBuilder
            .layoutModel(layoutModel)
            .nodeData(frVertexData)
            .nodeMasses(nodeMasses)
            .nodeSizes(nodeSizes)
            .initializer(initializer)
            .random(random)
            .build();

    currentIteration = 0;
  }

  @Override
  public boolean done() {
    if (cancelled) return true;
    boolean done = currentIteration >= maxIterations;
    if (done) {
      runAfter();
    }
    return done;
  }

  private Point getFRData(V vertex) {
    return frVertexData.computeIfAbsent(vertex, initializer);
  }

  /**
   * Compute gravity force for given vertex.
   *
   * @param vertex
   */
  private void calcGravity(V vertex) {
    Point xy = layoutModel.apply(vertex);
    Point center = layoutModel.getCenter();
    double r = xy.distanceSquared(center);
    double gravity = -kg * nodeMasses.get(vertex) / r;

    boolean locked = layoutModel.isLocked(vertex);

    if (!locked) {
      Point fvd = getFRData(vertex);
      frVertexData.put(vertex, fvd.add(gravity * (xy.x - center.x), gravity * (xy.y - center.y)));
    }
  }

  /**
   * Compute attraction forces for given edge.
   *
   * @param edge
   */
  private void calcAttraction(Object edge) {
    Graph<V, Object> graph = layoutModel.getGraph();
    V vertex1 = graph.getEdgeSource(edge);
    V vertex2 = graph.getEdgeTarget(edge);

    boolean v1_locked = layoutModel.isLocked(vertex1);
    boolean v2_locked = layoutModel.isLocked(vertex2);

    if (v1_locked && v2_locked) {
      return;
    }
    Point p1 = layoutModel.apply(vertex1);
    Point p2 = layoutModel.apply(vertex2);
    if (p1 == null || p2 == null) {
      return;
    }
    double xDelta = p1.x - p2.x;
    double yDelta = p1.y - p2.y;

    double dist = Math.max(epsilon, Math.sqrt(xDelta * xDelta + yDelta * yDelta));
    dist -= nodeSizes.apply(vertex1) + nodeSizes.apply(vertex2);

    if (dist > 0) {
      double force1;
      double force2;

      if (useLinLog) {
        force1 = force2 = Math.log(1 + dist);
      } else if (dissuadeHubs) {
        force1 = dist / nodeMasses.getOrDefault(vertex1, 1.0);
        force2 = dist / nodeMasses.getOrDefault(vertex2, 1.0);
      } else if (attractionByWeights) {
        force1 = force2 = Math.pow(graph.getEdgeWeight(edge), weightsDelta) * dist;
      } else {
        force1 = force2 = dist;
      }

      if (Double.isNaN(force1) || Double.isNaN(force2))
        throw new IllegalArgumentException(
            "Unexpected mathematical result in FRLayout:calcPositions");

      force1 /= dist;
      force2 /= dist;

      if (!v1_locked) {
        Point fvd1 = getFRData(vertex1);
        frVertexData.put(vertex1, fvd1.add(-force1 * xDelta, -force1 * yDelta));
      }
      if (!v2_locked) {
        Point fvd2 = getFRData(vertex2);
        frVertexData.put(vertex2, fvd2.add(force2 * xDelta, force2 * yDelta));
      }
    }
  }

  /** Calculate swinging and traces. */
  private void calcSwinging() {
    globalSwg = 0.0;
    globalTra = 0.0;

    for (V vertex : layoutModel.getGraph().vertexSet()) {
      double dFx = frVertexData.get(vertex).x - prevStepFrVertexData.get(vertex).x;
      double dFy = frVertexData.get(vertex).y - prevStepFrVertexData.get(vertex).y;

      double dFxPlus = frVertexData.get(vertex).x + prevStepFrVertexData.get(vertex).x;
      double dFyPlus = frVertexData.get(vertex).y + prevStepFrVertexData.get(vertex).y;

      swg.put(vertex, Math.sqrt(dFx * dFx + dFy * dFy));
      trace.put(vertex, Math.sqrt(dFxPlus * dFxPlus + dFyPlus * dFyPlus) / 2.0);

      //      double deg = layoutModel.getGraph().degreeOf(vertex);

      globalSwg += nodeMasses.get(vertex) * swg.get(vertex);
      globalTra += nodeMasses.get(vertex) * trace.get(vertex);
    }
  }

  private synchronized void calcPositions(V vertex) {
    Point fvd = getFRData(vertex);
    if (fvd == null) {
      return;
    }
    Point xyd = layoutModel.apply(vertex);
    double deltaLength = Math.max(epsilon, fvd.length());

    double positionX = xyd.x;
    double positionY = xyd.y;

    // Speed estimation. For details see original paper.
    double sn = ks * speed / (1 + speed * Math.sqrt(swg.get(vertex)));
    sn = Math.max(sn, ksMax / deltaLength);

    positionX += fvd.x * sn;
    positionY += fvd.y * sn;

    double borderWidth = layoutModel.getWidth() / 50.0;
    if (positionX < borderWidth) {
      positionX = borderWidth + random.nextDouble() * borderWidth * 2.0;
    } else if (positionX > layoutModel.getWidth() - borderWidth * 2) {
      positionX = layoutModel.getWidth() - borderWidth - random.nextDouble() * borderWidth * 2.0;
    }

    if (positionY < borderWidth) {
      positionY = borderWidth + random.nextDouble() * borderWidth * 2.0;
    } else if (positionY > layoutModel.getWidth() - borderWidth * 2) {
      positionY = layoutModel.getWidth() - borderWidth - random.nextDouble() * borderWidth * 2.0;
    }

    layoutModel.set(vertex, positionX, positionY);
  }

  @Override
  public synchronized void step() {
    if (repulsionContract == null) return;
    repulsionContract.step();
    Graph<V, ?> graph = layoutModel.getGraph();
    currentIteration++;

    // Forces from previous step.
    prevStepFrVertexData = new HashMap<>(frVertexData.size());
    frVertexData.forEach((key, value) -> prevStepFrVertexData.put(key, Point.of(value.x, value.y)));

    while (true) {
      try {
        repulsionContract.calculateRepulsion();
        break;
      } catch (ConcurrentModificationException ignored) {
      }
    }

    while (true) {
      try {
        for (Object edge : graph.edgeSet()) {
          calcAttraction(edge);
        }
        break;
      } catch (ConcurrentModificationException ignored) {
      }
    }

    while (true) {
      try {
        for (V vertex : graph.vertexSet()) {
          calcGravity(vertex);
        }
        break;
      } catch (ConcurrentModificationException ignored) {
      }
    }

    while (true) {
      try {
        calcSwinging();
        break;
      } catch (ConcurrentModificationException ignored) {
      }
    }

    double newSpeed = tolerance * globalTra / (globalSwg + epsilon);
    /*
    From the original paper:
    During our tests we observed that an excessive rise of the global speed could have a negative impact.
    That is why we limited the increase of global speed to 1.5x of the previous step.
     */
    speed = Math.min(newSpeed, (speed * 1.5));

    while (true) {
      try {
        for (V vertex : graph.vertexSet()) {
          if (layoutModel.isLocked(vertex)) {
            continue;
          }
          if (cancelled) {
            return;
          }
          calcPositions(vertex);
        }
        break;
      } catch (ConcurrentModificationException ignored) {
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("ForceAtlas2 layout algorithm.\n");
    builder.append(String.format("Gravity constant: %.2f\n", kg));
    builder.append(String.format("Swinging tolerance: %.2f\n", tolerance));

    return builder.toString();
  }
}
