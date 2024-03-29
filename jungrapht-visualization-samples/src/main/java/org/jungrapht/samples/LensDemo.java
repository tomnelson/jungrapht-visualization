/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.LensControlHelper;
import org.jungrapht.samples.util.TestGraphs;
import org.jungrapht.samples.util.VerticalLabelUI;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.*;
import org.jungrapht.visualization.control.modal.Modal;
import org.jungrapht.visualization.control.modal.ModeMenu;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.RandomLocationTransformer;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.transform.HyperbolicTransformer;
import org.jungrapht.visualization.transform.LayoutLensSupport;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.LensSupport;
import org.jungrapht.visualization.transform.MagnifyTransformer;
import org.jungrapht.visualization.transform.shape.HyperbolicShapeTransformer;
import org.jungrapht.visualization.transform.shape.MagnifyShapeTransformer;
import org.jungrapht.visualization.transform.shape.ViewLensSupport;
import org.jungrapht.visualization.util.LayoutAlgorithmTransition;

/**
 * Demonstrates the use of <code>HyperbolicTransform</code> and <code>MagnifyTransform</code>
 * applied to either the model (graph layout) or the view (VisualizationViewer) The transforms are
 * applied in an elliptical lens that affects that part of the visualization.
 *
 * @author Tom Nelson
 */
public class LensDemo extends JPanel {

  /** the graph */
  Graph<String, Integer> graph;

  FRLayoutAlgorithm<String> graphLayoutAlgorithm;

  /** a grid shaped graph */
  Graph<String, Integer> grid;

  LayoutAlgorithm<String> gridLayoutAlgorithm;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  /** provides a Hyperbolic lens for the view */
  LensSupport<ModalLensGraphMouse> hyperbolicViewSupport;
  /** provides a magnification lens for the view */
  LensSupport<ModalLensGraphMouse> magnifyViewSupport;

  /** provides a Hyperbolic lens for the model */
  LensSupport<ModalLensGraphMouse> hyperbolicLayoutSupport;
  /** provides a magnification lens for the model */
  LensSupport<ModalLensGraphMouse> magnifyLayoutSupport;

  /** create an instance of a simple graph with controls to demo the zoomand hyperbolic features. */
  public LensDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    graphLayoutAlgorithm = new FRLayoutAlgorithm<>();
    graphLayoutAlgorithm.setMaxIterations(1000);

    Dimension preferredSize = new Dimension(600, 600);
    Map<String, Point> map = new HashMap<>();
    Function<String, Point> vlf = map::get;
    grid = this.generateVertexGrid(map, preferredSize, 25);
    gridLayoutAlgorithm = new StaticLayoutAlgorithm<>();

    // the regular graph mouse for the normal view
    final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();

    final VisualizationModel<String, Integer> visualizationModel =
        VisualizationModel.builder(graph)
            .layoutAlgorithm(graphLayoutAlgorithm)
            .layoutSize(preferredSize)
            .build();
    vv =
        VisualizationViewer.builder(visualizationModel)
            .graphMouse(graphMouse)
            .viewSize(preferredSize)
            .build();

    MutableSelectedState<String> ps = vv.getSelectedVertexState();
    MutableSelectedState<Integer> pes = vv.getSelectedEdgeState();
    vv.getRenderContext()
        .setVertexFillPaintFunction(
            new PickableElementPaintFunction<>(ps, Color.red, Color.yellow));
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(new PickableElementPaintFunction<>(pes, Color.black, Color.cyan));

    vv.getRenderContext().setVertexLabelFunction(Object::toString);

    final Function<String, Shape> ovals = vv.getRenderContext().getVertexShapeFunction();
    final Function<String, Shape> squares = n -> new Rectangle2D.Double(-10, -10, 20, 20);

    // add a listener for ToolTips
    vv.setVertexToolTipFunction(n -> n); //Object::toString);

    VisualizationScrollPane visualizationScrollPane = new VisualizationScrollPane(vv);
    add(visualizationScrollPane);

    // create a lens to share between the two hyperbolic transformers
    LayoutModel<String> layoutModel = vv.getVisualizationModel().getLayoutModel();
    //    Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());

    hyperbolicViewSupport =
        ViewLensSupport.<String, Integer, ModalLensGraphMouse>builder(vv)
            .lensTransformer(
                HyperbolicShapeTransformer.builder(
                        Lens.builder().lensShape(Lens.Shape.ELLIPSE).build())
                    .delegate(
                        vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW))
                    .build())
            .lensGraphMouse(new ModalLensGraphMouse<>()) // use default for hyperbolic projection
            .useGradient(true)
            .build();

    hyperbolicLayoutSupport =
        LayoutLensSupport.<String, Integer, ModalLensGraphMouse>builder(vv)
            .lensTransformer(
                HyperbolicTransformer.builder(Lens.builder().lensShape(Lens.Shape.ELLIPSE).build())
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(Layer.LAYOUT))
                    .build())
            .lensGraphMouse(new ModalLensGraphMouse<>()) // use default for hyperbolic projection
            .useGradient(true)
            .build();

    // the magnification lens uses a different magnification than the hyperbolic lens
    magnifyViewSupport =
        ViewLensSupport.<String, Integer, ModalLensGraphMouse>builder(vv)
            .lensTransformer(
                MagnifyShapeTransformer.builder(
                        Lens.builder().lensShape(Lens.Shape.ELLIPSE).magnification(3.f).build())
                    .delegate(
                        vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW))
                    .build())
            .lensGraphMouse( // override with range for magnificaion
                ModalLensGraphMouse.builder()
                    .magnificationFloor(1.0f)
                    .magnificationCeiling(4.0f)
                    .build())
            .build();

    magnifyLayoutSupport =
        LayoutLensSupport.<String, Integer, ModalLensGraphMouse>builder(vv)
            .lensTransformer(
                MagnifyTransformer.builder(
                        Lens.builder().lensShape(Lens.Shape.ELLIPSE).magnification(3.f).build())
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(Layer.LAYOUT))
                    .build())
            .lensGraphMouse(
                ModalLensGraphMouse.builder()
                    .magnificationFloor(1.0f)
                    .magnificationCeiling(4.0f)
                    .build())
            .build();

    JLabel modeLabel = new JLabel("Mode >>");
    modeLabel.setUI(new VerticalLabelUI(false));

    ButtonGroup graphRadio = new ButtonGroup();
    JRadioButton graphButton = new JRadioButton("Graph");
    graphButton.setSelected(true);
    graphButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            layoutModel.setInitializer(
                new RandomLocationTransformer<>(layoutModel.getWidth(), layoutModel.getHeight()));
            visualizationModel.setGraph(graph, false);
            LayoutAlgorithmTransition.apply(vv, graphLayoutAlgorithm);
            vv.getRenderContext().setVertexShapeFunction(ovals);
            vv.getRenderContext().setVertexLabelFunction(Object::toString);
            vv.repaint();
          }
        });

    JRadioButton gridButton = new JRadioButton("Grid");
    gridButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            layoutModel.setInitializer(vlf);
            // so it won't start running the old layout algorithm on the new graph
            visualizationModel.setGraph(grid, false);
            LayoutAlgorithmTransition.apply(vv, gridLayoutAlgorithm);
            vv.getRenderContext().setVertexShapeFunction(squares);
            vv.getRenderContext().setVertexLabelFunction(n -> null);
            vv.repaint();
          }
        });

    graphRadio.add(graphButton);
    graphRadio.add(gridButton);

    JPanel modePanel = new JPanel(new GridLayout(3, 1));
    modePanel.setBorder(BorderFactory.createTitledBorder("Display"));
    modePanel.add(graphButton);
    modePanel.add(gridButton);

    JMenuBar menubar = new JMenuBar();
    JMenu modeMenu =
        ModeMenu.builder()
            .modes(Modal.Mode.TRANSFORMING, Modal.Mode.PICKING)
            .mode(Modal.Mode.TRANSFORMING)
            .buttonSupplier(JRadioButtonMenuItem::new)
            .modals(
                graphMouse,
                hyperbolicLayoutSupport.getGraphMouse(),
                hyperbolicViewSupport.getGraphMouse(),
                magnifyLayoutSupport.getGraphMouse(),
                magnifyViewSupport.getGraphMouse())
            .build()
            .buildUI();
    menubar.add(modeMenu);
    visualizationScrollPane.setCorner(menubar);

    Box controls = Box.createHorizontalBox();
    controls.add(ControlHelpers.getZoomControls("Zoom", vv));
    Map<String, LensSupport> lensSupportMap = new LinkedHashMap<>();
    lensSupportMap.put("Hyperbolic View", hyperbolicViewSupport);
    lensSupportMap.put("Hyperbolic Layout", hyperbolicLayoutSupport);
    lensSupportMap.put("Magnified View", magnifyViewSupport);
    lensSupportMap.put("Magnified Layout", magnifyLayoutSupport);
    controls.add(
        LensControlHelper.builder(lensSupportMap)
            .containerSupplier(JPanel::new)
            .containerLayoutManager(new GridLayout(0, 2))
            .title("Lens Controls")
            .build()
            .container());

    controls.add(modePanel);
    controls.add(modeLabel);
    add(controls, BorderLayout.SOUTH);
  }

  private Graph<String, Integer> generateVertexGrid(
      Map<String, Point> vlf, Dimension d, int interval) {
    int count = d.width / interval * d.height / interval;
    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.directedMultigraph())
            .buildGraph();
    for (int i = 0; i < count; i++) {
      int x = interval * i;
      int y = x / d.width * interval;
      x %= d.width;

      Point location = Point.of(x, y);
      String vertex = "v" + i;
      vlf.put(vertex, location);
      graph.addVertex(vertex);
    }
    return graph;
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new LensDemo());
    f.pack();
    f.setVisible(true);
  }
}
