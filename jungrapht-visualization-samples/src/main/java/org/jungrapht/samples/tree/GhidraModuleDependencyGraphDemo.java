/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples.tree;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.LensControlHelper;
import org.jungrapht.samples.util.MultipleLayoutSelector;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultGraphMouse;
import org.jungrapht.visualization.control.DefaultLensGraphMouse;
import org.jungrapht.visualization.control.LensGraphMouse;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.transform.HyperbolicTransformer;
import org.jungrapht.visualization.transform.LayoutLensSupport;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.LensSupport;
import org.jungrapht.visualization.transform.shape.HyperbolicShapeTransformer;
import org.jungrapht.visualization.transform.shape.ViewLensSupport;
import org.jungrapht.visualization.util.GraphImage;
import org.jungrapht.visualization.util.LayoutPaintable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates the visualization of the Ghidra module dependencies. An examiner lens performing a
 * hyperbolic transformation of the view is also included.
 *
 * @author Tom Nelson
 */
public class GhidraModuleDependencyGraphDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(GhidraModuleDependencyGraphDemo.class);

  Graph<ModuleVertex, ModuleEdge> graph;

  VisualizationViewer<ModuleVertex, ModuleEdge> vv;

  LensSupport<LensGraphMouse> hyperbolicViewSupport;

  LensSupport<LensGraphMouse> hyperbolicSupport;

  Dimension layoutSize = new Dimension(900, 900);
  Dimension viewSize = new Dimension(600, 600);

  public GhidraModuleDependencyGraphDemo() throws IOException {
    setLayout(new BorderLayout());
    graph = this.parseFileForGraph("/moduledeps.txt");

    final DefaultGraphMouse<ModuleVertex, ModuleEdge> graphMouse = new DefaultGraphMouse<>();

    vv =
        VisualizationViewer.builder(graph)
            .layoutAlgorithm(new StaticLayoutAlgorithm<>())
            .layoutSize(layoutSize)
            .viewSize(viewSize)
            .graphMouse(graphMouse)
            .build();

    vv.setVertexToolTipFunction(Object::toString);
    vv.setEdgeToolTipFunction(Object::toString);
    vv.getRenderContext().setVertexLabelFunction(Object::toString);

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    Predicate<ModuleEdge> edgePredicate = e -> false;
    vv.addPreRenderPaintable(
        new LayoutPaintable.LayoutBounds(
            vv.getVisualizationModel(), vv.getRenderContext().getMultiLayerTransformer()));

    LayoutModel<ModuleVertex> layoutModel = vv.getVisualizationModel().getLayoutModel();
    Lens lens = new Lens();
    hyperbolicViewSupport =
        ViewLensSupport.builder(vv)
            .lensTransformer(
                HyperbolicShapeTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW))
                    .build())
            .lensGraphMouse(
                DefaultLensGraphMouse.builder()
                    .magnificationFloor(0.4f)
                    .magnificationCeiling(1.0f)
                    .magnificationDelta(0.05f)
                    .build())
            .useGradient(true)
            .build();
    hyperbolicSupport =
        LayoutLensSupport.builder(vv)
            .lensTransformer(
                HyperbolicTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(Layer.LAYOUT))
                    .build())
            .lensGraphMouse(
                DefaultLensGraphMouse.builder()
                    .magnificationFloor(0.4f)
                    .magnificationCeiling(1.0f)
                    .magnificationDelta(0.05f)
                    .build())
            .useGradient(true)
            .build();

    vv.scaleToLayout();

    Box controls = Box.createHorizontalBox();

    JButton imageButton = new JButton("Save Image");
    imageButton.addActionListener(e -> GraphImage.capture(vv));

    MultipleLayoutSelector<ModuleVertex, ModuleEdge> treeLayoutSelector =
        MultipleLayoutSelector.<ModuleVertex, ModuleEdge>builder(vv)
            .edgePredicate(edgePredicate)
            .vertexPredicate(
                v ->
                    graph.incomingEdgesOf((ModuleVertex) v).stream().anyMatch(edgePredicate)
                        | graph.outgoingEdgesOf((ModuleVertex) v).stream().anyMatch(edgePredicate))
            .initialSelection(12)
            .build();

    controls.add(ControlHelpers.getCenteredContainer("Layout Controls", treeLayoutSelector));

    controls.add(
        LensControlHelper.builder(
                Map.of(
                    "Hyperbolic View", hyperbolicViewSupport,
                    "Hyperbolic Layout", hyperbolicSupport))
            .containerSupplier(Box::createVerticalBox)
            .title("Lens Controls")
            .build()
            .container());

    controls.add(imageButton);
    add(controls, BorderLayout.SOUTH);
  }

  private static class ModuleVertex {
    private final String name;

    public ModuleVertex(String name) {
      this.name = name;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ModuleVertex that = (ModuleVertex) o;
      return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name);
    }

    @Override
    public String toString() {
      return name;
    }
  }

  private static class ModuleEdge {
    final int weight;

    public ModuleEdge(int weight) {
      this.weight = weight;
    }

    @Override
    public String toString() {
      return "w=" + weight;
    }
  }

  private Graph<ModuleVertex, ModuleEdge> parseFileForGraph(String file) throws IOException {
    Graph<ModuleVertex, ModuleEdge> graph =
        GraphTypeBuilder.<ModuleVertex, ModuleEdge>directed()
            .allowingSelfLoops(true)
            .allowingMultipleEdges(true)
            .buildGraph();
    InputStream stream = GhidraModuleDependencyGraphDemo.class.getResourceAsStream(file);
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    String line;
    while ((line = reader.readLine()) != null) {
      String[] toks = line.split("\t");
      if (toks[0].equals(toks[1])) continue;
      ModuleVertex source = new ModuleVertex(toks[0]);
      ModuleVertex target = new ModuleVertex(toks[1]);
      ModuleEdge edge = new ModuleEdge(Integer.parseInt(toks[2]));
      graph.addVertex(source);
      graph.addVertex(target);
      graph.addEdge(source, target, edge);
    }
    return graph;
  }

  public static void main(String[] args) throws IOException {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new GhidraModuleDependencyGraphDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
