package org.jungrapht.visualization.layout.algorithms.orthogonal;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.IntStream;

public class TestOrthogonalLayoutAlgorithm {

    private static final Logger log = LoggerFactory.getLogger(TestOrthogonalLayoutAlgorithm.class);
    private Graph<String, Integer> graph;
    private LayoutModel<String> layoutModel;
    private Mappings<String> mappings;
    private OrthogonalLayoutAlgorithm<String, Integer> layoutAlgorithm;
    @Before
    public void before() {
        graph = GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag())
                .edgeSupplier(SupplierUtil.createIntegerSupplier())
                .buildGraph();
        layoutModel =
                LayoutModel.<String>builder()
                        .graph(graph)
                        .size(100,100).build();
        mappings = new Mappings<>();
        layoutAlgorithm =
                new OrthogonalLayoutAlgorithm(layoutModel);
    }
    @Test
    public void testMappingsToFillLayoutModel() {
        IntStream.range(0,10).forEach(n -> {
            graph.addVertex("V"+n);
            mappings.accept("V" + n, Rectangle.of(
                    Math.random() * 100 - 50, Math.random() * 100 - 50, 1, 1));
                }
        );
        layoutAlgorithm.mappingsToFillLayoutModel(mappings, layoutModel);
        System.err.println("LayoutModel: "+layoutModel);
    }

    @Test
    public void testNeighborsOf() {
        List<Rectangle> list =layoutAlgorithm.neighborsOf(Rectangle.of(1, 1, 1, 1), 1);
        System.err.println(list);
        list =layoutAlgorithm.neighborsOf(Rectangle.of(2, 2, 1, 1), 2);
        System.err.println(list);

        list =layoutAlgorithm.neighborsOf(Rectangle.of(5, 5, 1, 1), 2);
        System.err.println(list);
        list =layoutAlgorithm.neighborsOf(Rectangle.of(5, 5, 1, 1), 3);
        System.err.println(list);
    }

    @Test
    public void testNeighbors() {
        List<Point> list =layoutAlgorithm.neighborsOf(Point.of(0, 0), 1);
        Assert.assertEquals(list.size(), 4);
        Assert.assertTrue(list.containsAll(
                List.of(Point.of(1,0), Point.of(0,1), Point.of(-1,0), Point.of(0, -1))));
        System.err.println(list);
        list =layoutAlgorithm.neighborsOf(Point.of(0, 0), 2);
        Assert.assertEquals(list.size(), 8);
        Assert.assertTrue(list.containsAll(
                List.of(Point.of(2,0), Point.of(1,1), Point.of(0,2), Point.of(-1, 1),
                        Point.of(-2,0), Point.of(-1,-1), Point.of(0, -2), Point.of(1,-1))));
        System.err.println(list);
        list =layoutAlgorithm.neighborsOf(Point.of(0, 0), 3);
        Assert.assertTrue(list.containsAll(
                List.of(Point.of(3,0), Point.of(2,1), Point.of(1,2), Point.of(0,3),
                        Point.of(-1, 2), Point.of(-2,1), Point.of(-3,0), Point.of(-2, -1),
                        Point.of(-1,-2), Point.of(0,-3), Point.of(1,-2), Point.of(2,-1))));
        Assert.assertEquals(list.size(), 12);
        System.err.println(list);
        list =layoutAlgorithm.neighborsOf(Point.of(0, 0), 4);
        Assert.assertTrue(list.containsAll(
                List.of(Point.of(4,0), Point.of(3,1), Point.of(2,2), Point.of(1,3),
                        Point.of(0, 4), Point.of(-1, 3), Point.of(-2,2), Point.of(-3,1),
                        Point.of(-4, 0), Point.of(-3,-1), Point.of(-2,-2), Point.of(-1,-3),
                        Point.of(0,-4), Point.of(1, -3), Point.of(2, -2), Point.of(3, -1))));
        Assert.assertEquals(list.size(), 16);
        System.err.println(list);
    }

    @Test
    public void testInitialSize() {
        int vertexCount = 3;
        IntStream.range(0,vertexCount).forEach(n -> graph.addVertex("V"+n));
        graph.addEdge("V0", "V1");
        graph.addEdge("V1", "V2");
        int initialGridSize = layoutAlgorithm.initialGridSize();
        Assert.assertEquals(8, initialGridSize);
        this.mappings = layoutAlgorithm.mappings;
        mappings.accept("V0", Rectangle.of(Point.of(0,0),1,1));
        mappings.accept("V1", Rectangle.of(Point.of(0,0),1,1));
        mappings.accept("V2", Rectangle.of(Point.of(7,0),1,1));

        double sqrtVertexCount = Math.sqrt(vertexCount);
        double temperature = 2 * sqrtVertexCount;
        Point median = layoutAlgorithm.neighborsMedian("V1", temperature);

//        layoutAlgorithm.placeVerticesRandomlyInGridSpace(graph, initialGridSize);
        System.err.println("mappings: "+layoutAlgorithm.mappings);
    }

    @Test
    public void testTotalEdgeLength() {
        graph = paperGraph();
        layoutModel =
                LayoutModel.<String>builder()
                        .graph(graph)
                        .size(100,100).build();
//        mappings = new Mappings<>();
        layoutAlgorithm =
                new OrthogonalLayoutAlgorithm(layoutModel);
        layoutAlgorithm.placeVerticesRandomlyInGridSpace(graph, layoutAlgorithm.initialGridSize());
        double totalEdgeLength = layoutAlgorithm.totalEdgeLength();
        log.info("total edge length {}", totalEdgeLength);

        totalEdgeLength = layoutAlgorithm.totalDistanceSwapped("V1", "V2");
        log.info("total edge length {}", totalEdgeLength);

        totalEdgeLength = layoutAlgorithm.totalDistanceSwapped("V3", "V5");
        log.info("total edge length {}", totalEdgeLength);

    }

    @Test
    public void testClosestFreeRectangleTo() {
        Mappings<String> mappings = layoutAlgorithm.mappings;
        mappings.accept("A", Rectangle.of(0,0, 1, 1));
        mappings.accept("B", Rectangle.of(0,1, 1, 1));
        mappings.accept("C", Rectangle.of(1,0, 1, 1));
        mappings.accept("D", Rectangle.of(1,1,1, 1));

        Rectangle closestFree = layoutAlgorithm.closestFreeRectangleTo(.2, .2);
        log.info("closest is {}", closestFree);



    }
    static Graph<String, Integer> paperGraph() {
        Graph<String, Integer> graph = GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.dag())
                .edgeSupplier(SupplierUtil.createIntegerSupplier())
                .buildGraph();
        IntStream.rangeClosed(1,6).forEach(n ->
            graph.addVertex("V" + n));

        graph.addEdge("V1", "V2");
        graph.addEdge("V1", "V6");
        graph.addEdge("V2", "V4");
        graph.addEdge("V2", "V3");
        graph.addEdge("V2", "V5");
        graph.addEdge("V2", "V6");
        graph.addEdge("V3", "V4");
        graph.addEdge("V3", "V5");
        graph.addEdge("V4", "V1");
        graph.addEdge("V4", "V6");
        graph.addEdge("V5", "V4");
        graph.addEdge("V6", "V5");
            return graph;
        }
}
