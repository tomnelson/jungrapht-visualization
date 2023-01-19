package org.jungrapht.visualization.layout.algorithms.orthogonal;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Compaction<V> {

  private static Logger log = LoggerFactory.getLogger(Compaction.class);

  public enum Direction {
    HORIZONTAL,
    VERTICAL;

    Direction toggle() {
      return this.equals(HORIZONTAL) ? VERTICAL : HORIZONTAL;
    }
  }

  double gamma;

  public Function<V, Rectangle> IDENTITY_VERTEX_DIMENSION_FUNCTION =
          v -> Rectangle.of(0,0,1,1);

  ToDoubleFunction<Cell<V>> toDoubleFunction;

  Function<Cell<V>, Span> spannerFunction;

  BiFunction<Double, Rectangle, Rectangle> movedRectangleFunction;

  Function<Cell<V>, Double> offsetFunction;

  Graph<Cell<V>, Integer> compactionGraph;

  Function<Integer, Double> edgeLengthFunction;

  Function<V, Rectangle> vertexDimensionFunction;

  public static <V> Compaction<V> of(
          Collection<Cell<V>> cells,
          Direction direction,
          double gamma,
          BiConsumer<V, Rectangle> updateMaps) {
    Compaction<V> compaction =
            direction == Direction.HORIZONTAL ?
                    new Horizontal<>(gamma, v -> Rectangle.of(0, 0, 1, 1)) :
                    new Vertical<>(gamma, v -> Rectangle.of(0, 0, 1, 1));
    Graph<Cell<V>, Integer> compactionGraph = compaction.makeCompactionGraph(cells);
    compaction.compact(compactionGraph, updateMaps);
    return compaction;
  }

  public static <V> Compaction<V> of(
      Collection<Cell<V>> cells,
      Direction direction,
      double gamma,
      Function<V, Rectangle> vertexDimensionFunction,
      BiConsumer<V, Rectangle> updateMaps) {
    Compaction<V> compaction =
        direction == Direction.HORIZONTAL ?
                new Horizontal<>(gamma, vertexDimensionFunction) :
                new Vertical<>(gamma, vertexDimensionFunction);
    Graph<Cell<V>, Integer> compactionGraph = compaction.makeCompactionGraph(cells);
    compaction.compact(compactionGraph, updateMaps);
    return compaction;
  }

  Graph<Cell<V>, Integer> makeCompactionGraph(Collection<Cell<V>> cells) {
    this.compactionGraph =
        GraphTypeBuilder.<Cell<V>, Integer>directed()
            .allowingMultipleEdges(true)
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();
    cells.forEach(compactionGraph::addVertex);

    List<Cell<V>> sorted = new ArrayList<>(cells);
    sorted.sort(Comparator.comparingDouble(toDoubleFunction));
    //Comparator.comparingDouble(c -> c.x));
    for (int i = sorted.size() - 1; i >= 0; i--) {
      // last Rectangle
      Cell<V> rightCell = sorted.get(i);
      Span rightSpan = spannerFunction.apply(rightCell);
      //Span.of(r.y, r.y+r.height);
      // check all predecessors
      for (int j = i - 1; j >= 0; j--) {
        Cell<V> leftCell = sorted.get(j);
        Span leftSpan = spannerFunction.apply(leftCell);
        if (leftSpan.overlaps(rightSpan)) {
          compactionGraph.addEdge(leftCell, rightCell);
          break;
        }
      }
    }
    return compactionGraph;
  }

  void compact(Graph<Cell<V>, Integer> compactionGraph, BiConsumer<V, Rectangle> updateMaps) {
    // for every edge in the compaction graph, move the trailing (sink) vertex
    // closer to its source vertex
    // update the Rectangles in the Graph which will update the original
    // Collection<Cell<V>> cells
    // find roots
    List<Cell<V>> roots =
        compactionGraph
            .vertexSet()
            .stream()
            .filter(v -> compactionGraph.inDegreeOf(v) == 0)
            .collect(Collectors.toList());
    // ensure that the initial roots are in the Mappings
    roots.forEach(c -> updateMaps.accept(c.occupant, c.rectangle));
    while (roots.size() > 0) {
      for (Cell<V> root : roots) {
        // process the outgoing edges of the roots
        for (Integer edge : compactionGraph.outgoingEdgesOf(root)) {
          log.trace("length of edge {} was {}", edge, edgeLengthFunction.apply(edge));
          Cell<V> target = compactionGraph.getEdgeTarget(edge);
          // move target so it is closer to root
          double newXorY = offsetFunction.apply(root);
          //                                root.getX() + root.getWidth() + delta;
          Rectangle newRectangle = movedRectangleFunction.apply(newXorY, target.getRectangle());
          //                                Rectangle.of (newX, target.getY(), target.getWidth(), target.getHeight());
          target.setRectangle(newRectangle);
          updateMaps.accept(target.occupant, newRectangle);
          // move each sink closer to its source
          // remove the roots from the compactionGraph then repeat
          log.trace("length of edge {} now {}", edge, edgeLengthFunction.apply(edge));
        }
      }
      compactionGraph.removeAllVertices(roots);
      roots =
          compactionGraph
              .vertexSet()
              .stream()
              .filter(v -> compactionGraph.inDegreeOf(v) == 0)
              .collect(Collectors.toList());
    }
  }

  static class Span {
    final double min;
    final double max;

    public static Span of(double min, double max) {
      return new Span(min, max);
    }

    Span(double min, double max) {
      this.min = min;
      this.max = max;
    }

    public boolean overlaps(Span other) {
      return other.max >= this.min && other.min <= this.max;
    }
  }

  static class Horizontal<V> extends Compaction<V> {

    Horizontal(double gamma, Function<V, Rectangle> vertexDimensionFunction) {
      this.gamma = gamma;
      this.toDoubleFunction = Cell::getX;
      this.spannerFunction = r -> Span.of(r.getY(), r.getY() + r.getHeight());
      this.movedRectangleFunction = (z, r) -> Rectangle.of(z, r.y, r.width, r.height);
      this.offsetFunction = cell -> cell.getX() +
              vertexDimensionFunction.apply(cell.occupant).width + gamma;
      this.edgeLengthFunction =
          e -> {
            Cell<V> source = compactionGraph.getEdgeSource(e);
            Cell<V> target = compactionGraph.getEdgeTarget(e);
            return target.getX() - source.getX();
          };
      this.vertexDimensionFunction = vertexDimensionFunction;
    }
  }

  static class Vertical<V> extends Compaction<V> {

    Vertical(double gamma, Function<V, Rectangle> vertexDimensionFunction) {
      this.gamma = gamma;
      this.toDoubleFunction = Cell::getY;
      this.spannerFunction = r -> Span.of(r.getX(), r.getX() + r.getWidth());
      this.movedRectangleFunction = (z, r) -> Rectangle.of(r.x, z, r.width, r.height);
      this.offsetFunction = cell -> cell.getY() +
              vertexDimensionFunction.apply(cell.occupant).height
              + gamma;
      this.edgeLengthFunction =
          e -> {
            Cell<V> source = compactionGraph.getEdgeSource(e);
            Cell<V> target = compactionGraph.getEdgeTarget(e);
            return target.getY() - source.getY();
          };
      this.vertexDimensionFunction = vertexDimensionFunction;
    }
  }
}
