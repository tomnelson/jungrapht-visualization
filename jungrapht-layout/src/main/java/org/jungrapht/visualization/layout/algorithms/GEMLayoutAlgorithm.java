package org.jungrapht.visualization.layout.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.visualization.layout.algorithms.util.IterativeContext;
import org.jungrapht.visualization.layout.algorithms.util.Pair;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java implementation of the gem 2D layout. <br>
 * The algorithm needs to get various subgraphs and traversals. The recursive nature of the
 * algorithm is totally captured within those subgraphs and traversals. The main loop of the
 * algorithm is then expressed using the iterator feature, which makes it look like a simple flat
 * iteration over nodes.
 *
 * @author David Duke
 * @author Hacked by Eytan Adar for Guess
 * @author Hacked by taubertj for OVTK2
 * @author Hacked by Tom Nelson
 */
public class GEMLayoutAlgorithm<V, E> extends AbstractIterativeLayoutAlgorithm<V>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(GEMLayoutAlgorithm.class);

  public static class Builder<
          V, E, T extends GEMLayoutAlgorithm<V, E>, B extends Builder<V, E, T, B>>
      extends AbstractIterativeLayoutAlgorithm.Builder<V, T, B>
      implements LayoutAlgorithm.Builder<V, T, B> {
    private int maxIterations = 700;
    private int multi = 3;
    private int verticalSpacing = 75;
    private int horizontalSpacing = 75;
    private boolean clustered = true;
    protected boolean adjustToFit = true;

    public B multi(int multi) {
      this.multi = multi;
      return self();
    }

    public B maxIterations(int maxIterations) {
      this.maxIterations = maxIterations;
      return self();
    }

    public B verticalSpacing(int verticalSpacing) {
      this.verticalSpacing = verticalSpacing;
      return self();
    }

    public B horizontalSpacing(int horizontalSpacing) {
      this.horizontalSpacing = horizontalSpacing;
      return self();
    }

    public B clustered(boolean clustered) {
      this.clustered = clustered;
      return self();
    }

    /**
     * @param adjustToFit adjust the points to fit in the layoutModel area
     * @return the Builder
     */
    public B adjustToFit(boolean adjustToFit) {
      this.adjustToFit = adjustToFit;
      return self();
    }

    public T build() {
      return (T) new GEMLayoutAlgorithm(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> edgeAwareBuilder() {
    return new Builder<>();
  }

  public GEMLayoutAlgorithm() {
    this(GEMLayoutAlgorithm.edgeAwareBuilder());
  }

  protected GEMLayoutAlgorithm(Builder builder) {
    super(builder);
    this.maxIterations = builder.maxIterations;
    this.multi = builder.multi;
    this.horizontalSpacing = builder.horizontalSpacing;
    this.verticalSpacing = builder.verticalSpacing;
    this.clustered = builder.clustered;
    this.adjustToFit = builder.adjustToFit;
  }

  /**
   * Class containing properties per node.
   *
   * @author taubertj
   */
  private class Properties<V> {

    public int x, y; // position

    public int in;

    public int iX, iY; // impulse

    public float dir; // direction

    public float heat; // heat

    public float mass; // weight = nr edges

    public boolean mark;

    public Properties(int m) {
      x = y = 0;
      iX = iY = 0;
      dir = 0.0f;
      heat = 0;
      mass = m;
      mark = false;
    }
  }

  private boolean done;

  /** If the process gets cancelled */
  private boolean cancelled = false;

  // number of nodes in the graph
  private int nodeCount;

  // use clustered approach
  private boolean clustered;

  // number of clusters
  private int nbClusters = 1;

  // vertical cluster spacing
  public int verticalSpacing;

  // horizontal cluster spacing
  public int horizontalSpacing;

  // multiplicator of largest cluster
  public int multi;

  protected int maxIterations;

  //
  // GEM Constants
  //
  private int ELEN = 128;

  private int ELENSQR = ELEN * ELEN;

  private int MAXATTRACT = 1048576;

  //
  // GEM variables
  //
  private long iteration;

  private long temperature;

  private int centerX, centerY;

  private long maxtemp;

  private float oscillation, rotation;

  protected boolean adjustToFit;

  //
  // GEM Default Parameter Values
  //
  private float i_maxtemp = 1.0f;

  private float a_maxtemp = 1.5f;

  private float o_maxtemp = 0.25f;

  private float i_starttemp = 0.3f;

  private float a_starttemp = 1.0f;

  private float o_starttemp = 1.0f;

  private float i_finaltemp = 0.05f;

  private float a_finaltemp = 0.02f;

  private float o_finaltemp = 1.0f;

  private int i_maxiter = 10;

  private int a_maxiter = 3;

  private int o_maxiter = 3;

  private float i_gravity = 0.05f;

  private float i_oscillation = 0.4f;

  private float i_rotation = 0.5f;

  private float i_shake = 0.2f;

  private float a_gravity = 0.1f;

  private float a_oscillation = 0.4f;

  private float a_rotation = 0.9f;

  private float a_shake = 0.3f;

  private float o_gravity = 0.1f;

  private float o_oscillation = 0.4f;

  private float o_rotation = 0.9f;

  private float o_shake = 0.3f;

  long stop_temperature;
  long stop_iteration;

  // list of properties for each node
  private Properties[] gemProp;

  // inverse map from int id to V
  private V[] invmap;

  // adjacent int ids for a given V int id
  private Map<Integer, List<Integer>> adjacent;

  // map from V to int id
  private Map<V, Integer> nodeNumbers;

  // randomizer used for node selection
  private Random rand = new Random();

  // map used for current random set of nodes
  private int map[];

  // priority queue for BFS
  private Queue<Integer> q;

  private Graph<V, E> graph;

  public void visit(LayoutModel<V> layoutModel) {
    super.visit(layoutModel);
    this.graph = layoutModel.getGraph();
    if (graph == null || graph.vertexSet().isEmpty()) {
      return;
    }
    this.initialize();
    this.arrange();
    if (adjustToFit) {
      adjustToFit();
    }
    Rectangle range = computeLayoutExtent(layoutModel);
    // add the padding
    range = Rectangle.from(range.min().add(-50, -50), range.max().add(50, 50));

    int maxDimension = Math.max((int) range.width, (int) range.height);
    layoutModel.setSize(maxDimension, maxDimension);
  }

  private Rectangle getMaxBounds() {
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    for (Point p : layoutModel.getLocations().values()) {
      if (p.x < minX) minX = (int) p.x;
      if (p.y < minY) minY = (int) p.y;
      if (p.x > maxX) maxX = (int) p.x;
      if (p.y > maxY) maxY = (int) p.y;
    }
    minX -= horizontalSpacing;
    minY -= verticalSpacing;
    maxX += horizontalSpacing;
    maxY += verticalSpacing;
    return Rectangle.of(minX, minY, maxX - minX, maxY - minY);
  }

  private void adjustToFit() {
    Rectangle bounds = getMaxBounds();
    double boundsWidth = bounds.width;
    double boundsHeight = bounds.height;

    int layoutWidth = layoutModel.getWidth();
    int layoutHeight = layoutModel.getHeight();
    double scaleX = layoutWidth / boundsWidth;
    double scaleY = layoutHeight / boundsHeight;

    for (V v : graph.vertexSet()) {
      Point vp = layoutModel.apply(v);
      vp = Point.of(vp.x * scaleX, vp.y * scaleY);
      vp = vp.add(horizontalSpacing, verticalSpacing);
      layoutModel.set(v, vp);
    }
  }

  private void expandLayout() {
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    for (Point p : layoutModel.getLocations().values()) {
      if (p.x < minX) minX = (int) p.x;
      if (p.y < minY) minY = (int) p.y;
      if (p.x > maxX) maxX = (int) p.x;
      if (p.y > maxY) maxY = (int) p.y;
    }
    layoutModel.setSize(maxX - minX, maxY - minY);
  }

  private Graph<V, E> getGraph() {
    return this.graph;
  }

  public synchronized void step() {
    if (temperature > stop_temperature && iteration < stop_iteration) {
      log.trace("iteration: {}", iteration);
      a_round();
      if (cancelled) return;
    } else {
      this.done = true;
    }
  }

  @Override
  public boolean done() {
    if (done) {
      runAfter();
    }
    return done;
  }

  private void a_round() {

    Iterator<Integer> nodeSet;
    int v;

    int iX, iY, dX, dY;
    int n;
    int pX, pY;
    Properties p, q;

    for (int i = 0; i < nodeCount; i++) {
      v = select();
      p = gemProp[v];

      pX = p.x;
      pY = p.y;

      n = (int) (a_shake * ELEN);
      iX = rand() % (2 * n + 1) - n;
      iY = rand() % (2 * n + 1) - n;
      iX += (centerX / nodeCount - pX) * p.mass * a_gravity;
      iY += (centerY / nodeCount - pY) * p.mass * a_gravity;

      for (int u = 0; u < nodeCount; u++) {
        q = gemProp[u];
        dX = pX - q.x;
        dY = pY - q.y;
        n = dX * dX + dY * dY;
        if (n > 0) {
          iX += dX * ELENSQR / n;
          iY += dY * ELENSQR / n;
        }
      }
      nodeSet = adjacent.get(v).iterator();
      int u;
      while (nodeSet.hasNext()) {
        u = nodeSet.next();
        q = gemProp[u];
        dX = pX - q.x;
        dY = pY - q.y;
        n = (int) ((dX * dX + dY * dY) / p.mass);
        n = (int) Math.min(n, MAXATTRACT);
        iX -= dX * n / ELENSQR;
        iY -= dY * n / ELENSQR;
      }
      displace(v, iX, iY);
      iteration++;
    }
  }

  private void arrange() {

    vertexdata_init(a_starttemp);

    oscillation = a_oscillation;
    rotation = a_rotation;
    maxtemp = (int) (a_maxtemp * ELEN);
    stop_temperature = (int) (a_finaltemp * a_finaltemp * ELENSQR * nodeCount);
    stop_iteration = a_maxiter * nodeCount * nodeCount;
    iteration = 0;
  }

  /**
   * Performs a BFS on the graph
   *
   * @param root int
   * @return node id
   */
  private int bfs(int root) {

    Iterator<Integer> nodeSet;
    int v, ui;

    if (root >= 0) {
      q = new LinkedList<>();
      if (!gemProp[root].mark) { // root > 0
        for (int vi = 0; vi < nodeCount; vi++) {
          gemProp[vi].in = 0;
        }
      } else gemProp[root].mark = true; // root = -root;
      q.add(root);
      gemProp[root].in = 1;
    }
    if (q.size() == 0) return -1;
    v = q.poll();

    nodeSet = adjacent.get(v).iterator();
    while (nodeSet.hasNext()) {
      ui = nodeSet.next();
      if (gemProp[ui].in != 0) {
        q.add(ui);
        gemProp[ui].in = gemProp[v].in + 1;
      }
    }

    return v;
  }

  /**
   * Calculates actual bounds of a painted graph.
   *
   * @return min/max coordinates in a Point[]
   */
  private Point[] calcBounds(Graph<V, E> graph, Map<V, Point> coords) {
    Point[] result = new Point[2];
    Point min = null;
    Point max = null;
    Iterator<V> it = graph.vertexSet().iterator();
    while (it.hasNext()) {
      Point point = coords.get(it.next());
      if (min == null) {
        min = point;
      }
      if (max == null) {
        max = point;
      }
      min = Point.of(Math.min(min.x, point.x), Math.min(min.y, point.y));
      max = Point.of(Math.max(max.x, point.x), Math.max(max.y, point.y));
    }
    result[0] = min;
    result[1] = max;
    return result;
  }

  /** Clusters given graph into subgraphs. */
  public Set<Graph<V, E>> clusterGraph(Graph<V, E> original) {

    // contains all possible subgraphs
    Set<Graph<V, E>> subgraphs = new HashSet<>();

    // sort each vertex into one subgraph
    Set<V> sorted = new HashSet<V>();
    for (V n : original.vertexSet()) {

      // Orphan node
      if (!sorted.contains(n)) {

        // create new cluster starting at this node
        Graph<V, E> cluster =
            GraphTypeBuilder.<V, E>undirected()
                .allowingSelfLoops(true)
                .allowingMultipleEdges(true)
                .buildGraph();
        subgraphs.add(cluster);

        // add node to new cluster and mark as sorted
        cluster.addVertex(n);
        sorted.add(n);

        // inspect neighbours of n do BFS
        Queue<V> queue = new LinkedList<V>();
        Collection<V> neigbours = Graphs.neighborListOf(original, n);
        queue.addAll(neigbours);

        // process queue
        while (!queue.isEmpty()) {
          V next = queue.poll();
          if (!sorted.contains(next)) {

            // add to cluster and mark as sorted
            cluster.addVertex(next);
            sorted.add(next);

            // add edges to cluster
            Collection<E> nextEdges = original.edgesOf(next);
            for (E edge : nextEdges) {
              cluster.addVertex(original.getEdgeSource(edge));
              cluster.addVertex(original.getEdgeTarget(edge));
              cluster.addEdge(original.getEdgeSource(edge), original.getEdgeTarget(edge), edge);
            }

            // proceed to next level
            queue.addAll(Graphs.neighborListOf(original, next));
          }
        }
      }

      if (cancelled) return subgraphs;
    }
    return subgraphs;
  }

  private void displace(int v, int iX, int iY) {

    int t;
    int n;
    Properties p;

    if (iX != 0 || iY != 0) {
      n = Math.max(Math.abs(iX), Math.abs(iY)) / 16384;
      if (n > 1) {
        iX /= n;
        iY /= n;
      }
      p = gemProp[v];
      t = (int) p.heat;
      n = (int) Math.sqrt(iX * iX + iY * iY);
      iX = iX * t / n;
      iY = iY * t / n;
      p.x += iX;
      p.y += iY;
      centerX += iX;
      centerY += iY;
      // imp = &vi[v].imp;
      n = t * (int) Math.sqrt(p.iX * p.iX + p.iY * p.iY);
      if (n > 0) {
        temperature -= t * t;
        t += t * oscillation * (iX * p.iX + iY * p.iY) / n;
        t = (int) Math.min(t, maxtemp);
        p.dir += rotation * (iX * p.iY - iY * p.iX) / n;
        t -= t * Math.abs(p.dir) / nodeCount;
        t = Math.max(t, 2);
        temperature += t * t;
        p.heat = t;
      }
      p.iX = iX;
      p.iY = iY;
    }
  }

  /*
   * Optimisation Code
   */
  private int[] EVdistance(int thisNode, int thatNode, int v) {

    Properties thisGP = gemProp[thisNode];
    Properties thatGP = gemProp[thatNode];
    Properties nodeGP = gemProp[v];

    int aX = thisGP.x;
    int aY = thisGP.y;
    int bX = thatGP.x;
    int bY = thatGP.y;
    int cX = nodeGP.x;
    int cY = nodeGP.y;

    long m, n;

    bX -= aX;
    bY -= aY; /* b' = b - a */
    m = bX * (cX - aX) + bY * (cY - aY); /* m = <b'|c-a> = <b-a|c-a> */
    n = bX * bX + bY * bY; /* n = |b'|^2 = |b-a|^2 */
    if (m < 0) m = 0;
    if (m > n) m = n = 1;
    if ((m >> 17) > 0) {
      /* prevent integer overflow */
      n /= m >> 16;
      m /= m >> 16;
    }
    if (n != 0) {
      aX += (int) (bX * m / n); /* a' = m/n b' = a + m/n (b-a) */
      aY += (int) (bY * m / n);
    }
    return new int[] {aX, aY};
  }

  /**
   * Returns node for the graph center.
   *
   * @return int
   */
  private int graphCenter() {
    Properties p;
    int c, u, v, w; // nodes
    int h;

    c = -1; // for a contented compiler.
    u = -1;

    h = nodeCount + 1;
    for (w = 0; w < nodeCount; w++) {
      v = bfs(w);
      while (v >= 0 && gemProp[v].in < h) {
        u = v;
        v = bfs(-1); // null
      }
      p = gemProp[u];
      if (p.in < h) {
        h = p.in;
        c = w;
      }
    }

    // randomly choose a centre node if graph doesn't have a centre
    if (c == -1) return (int) Math.rint((nodeCount - 1) * Math.random());

    return c;
  }

  /*
   * INSERT code from GEM
   */
  private int[] i_impulse(int v) {

    Iterator<Integer> nodeSet;

    int iX, iY, dX, dY, pX, pY;
    int n;
    Properties p, q;

    p = gemProp[v];
    pX = p.x;
    pY = p.y;

    n = (int) (i_shake * ELEN);
    iX = rand() % (2 * n + 1) - n;
    iY = rand() % (2 * n + 1) - n;
    iX += (centerX / nodeCount - pX) * p.mass * i_gravity;
    iY += (centerY / nodeCount - pY) * p.mass * i_gravity;

    for (int u = 0; u < nodeCount; u++) {
      q = gemProp[u];
      if (q.in > 0) {
        dX = pX - q.x;
        dY = pY - q.y;
        n = dX * dX + dY * dY;
        if (n > 0) {
          iX += dX * ELENSQR / n;
          iY += dY * ELENSQR / n;
        }
      }
    }
    nodeSet = adjacent.get(v).iterator();
    int u;
    while (nodeSet.hasNext()) {
      u = nodeSet.next();
      q = gemProp[u];
      if (q.in > 0) {
        dX = pX - q.x;
        dY = pY - q.y;
        n = (int) ((dX * dX + dY * dY) / p.mass);
        n = Math.min(n, MAXATTRACT);
        iX -= dX * n / ELENSQR;
        iY -= dY * n / ELENSQR;
      }
    }

    return new int[] {iX, iY};
  }

  /** Runs the layout. */
  public void initialize() {

    cancelled = false;

    if (clustered) {
      Set<Graph<V, E>> clusters = clusterGraph(getGraph());
      nbClusters = clusters.size();
      runClustered(clusters);
    } else {
      runNormal(getGraph());

      // set location of nodes in graph
      for (int i = 0; i < nodeCount; i++) {
        Properties p = gemProp[i];
        V n = invmap[i];
        layoutModel.set(n, p.x, p.y);
      }
    }
  }

  private void insert() {

    Iterator<Integer> nodeSet;
    Properties p, q;
    int startNode;

    int v, w;

    int d;

    vertexdata_init(i_starttemp);

    oscillation = i_oscillation;
    rotation = i_rotation;
    maxtemp = (int) (i_maxtemp * ELEN);

    v = graphCenter();

    for (int ui = 0; ui < nodeCount; ui++) {
      gemProp[ui].in = 0;
    }

    gemProp[v].in = -1;

    startNode = -1;
    for (int i = 0; i < nodeCount; i++) {
      d = 0;
      for (int u = 0; u < nodeCount; u++) {
        if (gemProp[u].in < d) {
          d = gemProp[u].in;
          v = u;
        }
      }
      gemProp[v].in = 1;

      nodeSet = adjacent.get(v).iterator();
      int u;
      while (nodeSet.hasNext()) {
        u = nodeSet.next();
        if (gemProp[u].in <= 0) gemProp[u].in--;
      }
      p = gemProp[v];
      p.x = p.y = 0;

      if (startNode >= 0) {
        d = 0;
        p = gemProp[v];
        nodeSet = adjacent.get(v).iterator();
        while (nodeSet.hasNext()) {
          w = nodeSet.next();
          q = gemProp[w];
          if (q.in > 0) {
            p.x += q.x;
            p.y += q.y;
            d++;
          }
        }
        if (d > 1) {
          p.x /= d;
          p.y /= d;
        }
        d = 0;
        while ((d++ < i_maxiter) && (p.heat > i_finaltemp * ELEN)) {
          int[] i_impulse = i_impulse(v);
          displace(v, i_impulse[0], i_impulse[1]);
        }

      } else {
        startNode = i;
      }

      if (cancelled) return;
    }
  }

  private int[] o_impulse(Graph<V, E> graph, int v) {

    int u, w;
    int iX, iY, dX, dY;
    int n;
    Properties p, up, wp;
    int pX, pY;

    p = gemProp[v];
    pX = p.x;
    pY = p.y;

    n = (int) (o_shake * ELEN);
    iX = rand() % (2 * n + 1) - n;
    iY = rand() % (2 * n + 1) - n;
    iX += (centerX / nodeCount - pX) * p.mass * o_gravity;
    iY += (centerY / nodeCount - pY) * p.mass * o_gravity;

    for (E e : graph.edgeSet()) {
      Pair<V> ends = Pair.of(graph.getEdgeSource(e), graph.getEdgeTarget(e));
      u = nodeNumbers.get(ends.first);
      w = nodeNumbers.get(ends.second);
      if (u != v && w != v) {
        up = gemProp[u];
        wp = gemProp[w];
        dX = (up.x + wp.x) / 2 - pX;
        dY = (up.y + wp.y) / 2 - pY;
        n = dX * dX + dY * dY;
        if (n < 8 * ELENSQR) {
          int[] evdist = EVdistance(u, w, v); // source, dest, vert
          dX = evdist[0];
          dY = evdist[1];
          dX -= pX;
          dY -= pY;
          n = dX * dX + dY * dY;
        }
        if (n > 0) {
          iX -= dX * ELENSQR / n;
          iY -= dY * ELENSQR / n;
        }
      } else {
        if (u == v) u = w;
        up = gemProp[u];
        dX = pX - up.x;
        dY = pY - up.y;
        n = (int) ((dX * dX + dY * dY) / p.mass);
        n = Math.min(n, MAXATTRACT);
        iX -= dX * n / ELENSQR;
        iY -= dY * n / ELENSQR;
      }
    }
    return new int[] {iX, iY};
  }

  private void o_round(Graph<V, E> graph) {

    int v;
    for (int i = 0; i < nodeCount; i++) {
      v = select();
      int[] o_impulse = o_impulse(graph, v);
      displace(v, o_impulse[0], o_impulse[1]);
      iteration++;
    }
  }

  private void optimize(Graph<V, E> graph) {

    long stop_temperature;
    long stop_iteration;

    vertexdata_init(o_starttemp);
    oscillation = o_oscillation;
    rotation = o_rotation;
    maxtemp = (int) (o_maxtemp * ELEN);
    stop_temperature = (int) (o_finaltemp * o_finaltemp * ELENSQR * nodeCount);
    stop_iteration = o_maxiter * nodeCount * nodeCount;

    while (temperature > stop_temperature && iteration < stop_iteration) {
      o_round(graph);
      if (cancelled) return;
    }
  }

  /**
   * Random function returns an random int value.
   *
   * @return int
   */
  private int rand() {
    return (int) (rand.nextDouble() * Integer.MAX_VALUE);
  }

  /** Layout subgraphs on separate places. */
  public void runClustered(Set<Graph<V, E>> subgraphs) {

    // sort subgraphs according to size
    Graph<V, E>[] sortedSubgraphs = subgraphs.toArray(new Graph[0]);
    Arrays.sort(sortedSubgraphs, Comparator.comparingInt(g -> g.vertexSet().size()));

    // cache local layout
    Map<Graph<V, E>, Map<V, Point>> localLayouts = new HashMap<>();

    int minX = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxY = Integer.MIN_VALUE;

    // apply algorithm to sorted graphs
    int j = 0;
    for (Graph<?, ?> subgraph : sortedSubgraphs) {
      j++;

      // set subgraph as normal and run GEM layout on it
      runNormal((Graph<V, E>) subgraph);

      // set location of nodes in subgraph
      localLayouts.put((Graph<V, E>) subgraph, new HashMap<>());
      for (int i = 0; i < nodeCount; i++) {
        Properties p = gemProp[i];
        V n = invmap[i];

        Point coord = Point.of(p.x, p.y);
        layoutModel.set(n, coord);

        if (p.x < minX) minX = p.x;
        if (p.x > maxX) maxX = p.x;
        if (p.y < minY) minY = p.y;
        if (p.y > maxY) maxY = p.y;

        localLayouts.get(subgraph).put(n, coord);
      }
      if (cancelled) return;
    }
    int width = (Math.abs(minX) + Math.abs(maxX)) * multi;

    double offsetX = 0;
    double offsetY = 0;
    double maxposY = 0;
    for (Graph<?, ?> sub : sortedSubgraphs) {
      Graph<V, E> subgraph = (Graph<V, E>) sub;
      Map<V, Point> coords = localLayouts.get(subgraph);

      // calculate bounds required for normalisation
      Point[] result = calcBounds(subgraph, coords);
      Point min = result[0];

      // current expansion
      double tmpY = 0;
      double tmpX = 0;

      // offset all nodes of local layout
      Iterator<V> keys = coords.keySet().iterator();
      while (keys.hasNext()) {
        V n = keys.next();
        Point coord = coords.get(n);
        //				 centre at 0,0 and offset
        double newX = offsetX + coord.x - min.x;
        double newY = offsetY + coord.y - min.y;

        // calculate maximum boundaries
        if (newX > tmpX) tmpX = newX;
        if (newY > tmpY) tmpY = newY;
        localLayouts.get(subgraph).put(n, Point.of(newX, newY));
        layoutModel.set(n, Point.of(newX, newY));
      }

      // shift horizontally keep track of vertical
      offsetX = tmpX + horizontalSpacing;
      if (tmpY > maxposY) {
        maxposY = tmpY;
      }

      // line break here
      if (offsetX > width) {
        offsetY = maxposY + verticalSpacing;
        offsetX = 0;
        maxposY = 0;
      }
    }
  }

  /** Normal bubble like GEM layout. */
  private void runNormal(Graph<V, E> graph) {

    Collection<V> nodes = graph.vertexSet();

    nodeCount = nodes.size();

    // ignore empty graphs
    if (nodeCount == 0) return;

    gemProp = new Properties[nodeCount];
    invmap = (V[]) new Object[nodeCount];
    adjacent = new HashMap<Integer, List<Integer>>(nodeCount);
    nodeNumbers = new HashMap<V, Integer>();

    // initialize node lists and gemProp
    Iterator<V> nodeSet = nodes.iterator();
    for (int i = 0; nodeSet.hasNext(); i++) {
      V n = nodeSet.next();
      gemProp[i] = new Properties(graph.outgoingEdgesOf(n).size());
      invmap[i] = n;
      nodeNumbers.put(n, i);
    }

    // fill adjacent lists
    Collection<V> neighbors;
    for (int i = 0; i < nodeCount; i++) {
      neighbors = Graphs.neighborListOf(graph, invmap[i]);
      adjacent.put(i, new ArrayList<Integer>(neighbors.size()));
      for (V n : neighbors) {
        adjacent.get(i).add(nodeNumbers.get(n));
      }
    }
    if (cancelled) return;

    // actual layout
    if (i_finaltemp < i_starttemp) {
      insert();
      if (cancelled) return;
    }
    if (a_finaltemp < a_starttemp) {
      arrange();
      if (cancelled) return;
    }
    if (o_finaltemp < o_starttemp) {
      optimize(graph);
      if (cancelled) return;
    }
  }

  /**
   * Randomize selection of nodes.
   *
   * @return node id
   */
  private int select() {
    int u;
    int n, v;

    if (iteration == 0) {
      map = new int[nodeCount];
      for (int i = 0; i < nodeCount; i++) map[i] = i;
    }
    n = (int) (nodeCount - iteration % nodeCount);
    v = rand() % n; // was 1 + rand() % n due to numbering in GEM
    if (v == nodeCount) v--;
    if (n == nodeCount) n--;
    u = map[v];
    map[v] = map[n];
    map[n] = u;
    return u;
  }

  /**
   * Initialize properties of nodes.
   *
   * @param starttemp given start temperature
   */
  private void vertexdata_init(float starttemp) {

    temperature = 0;
    centerX = centerY = 0;

    for (int v = 0; v < nodeCount; v++) {
      Properties p = gemProp[v];
      p.heat = starttemp * ELEN;
      temperature += p.heat * p.heat;
      p.iX = p.iY = 0;
      p.dir = 0;
      p.mass = 1 + gemProp[v].mass / 3;
      centerX += p.x;
      centerY += p.y;
    }
  }
}
