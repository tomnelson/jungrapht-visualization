/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 */
package org.jungrapht.samples;

import static org.jungrapht.visualization.renderers.BiModalRenderer.HEAVYWEIGHT;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.renderers.GradientVertexRenderer;
import org.jungrapht.visualization.renderers.HeavyweightVertexLabelRenderer;
import org.jungrapht.visualization.renderers.Renderer;
import org.jungrapht.visualization.util.helpers.ControlHelpers;

/**
 * Shows a graph overlaid on a world map image. Scaling of the graph also scales the image
 * background.
 *
 * @author Tom Nelson
 */
public class WorldMapGraphDemo extends JPanel {

  /** the graph */
  Graph<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  List<String> cityList;

  /** create an instance of a simple graph with controls to demo the zoom features. */
  public WorldMapGraphDemo() {
    setLayout(new BorderLayout());

    Map<String, String[]> map = buildMap();

    cityList = new ArrayList<>(map.keySet());

    // create a simple graph for the demo
    graph = buildGraph(map);

    ImageIcon mapIcon = null;
    String imageLocation = "/images/political_world_map.jpg";
    try {
      mapIcon = new ImageIcon(getClass().getResource(imageLocation));
    } catch (Exception ex) {
      System.err.println("Can't load \"" + imageLocation + "\"");
    }
    final ImageIcon icon = mapIcon;

    LayoutAlgorithm<String> layoutAlgorithm = new StaticLayoutAlgorithm<>();

    Function<String, Point> initializer =
        new CityTransformer(map).andThen(new LatLonPixelTransformer(new Dimension(2000, 1000)));
    VisualizationModel<String, Integer> model =
        VisualizationModel.builder(graph)
            .layoutAlgorithm(layoutAlgorithm)
            .initializer(initializer)
            .layoutSize(new Dimension(2000, 1000))
            .build();

    vv = VisualizationViewer.builder(model).viewSize(new Dimension(800, 400)).build();

    if (icon != null) {
      vv.addPreRenderPaintable(
          new VisualizationViewer.Paintable() {
            public void paint(Graphics g) {
              Graphics2D g2d = (Graphics2D) g;
              AffineTransform oldXform = g2d.getTransform();
              AffineTransform lat =
                  vv.getRenderContext()
                      .getMultiLayerTransformer()
                      .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
                      .getTransform();
              AffineTransform vat =
                  vv.getRenderContext()
                      .getMultiLayerTransformer()
                      .getTransformer(MultiLayerTransformer.Layer.VIEW)
                      .getTransform();
              AffineTransform at = new AffineTransform();
              at.concatenate(g2d.getTransform());
              at.concatenate(vat);
              at.concatenate(lat);
              g2d.setTransform(at);
              g.drawImage(
                  icon.getImage(),
                  0,
                  0,
                  icon.getIconWidth(),
                  icon.getIconHeight(),
                  vv.getComponent());
              g2d.setTransform(oldXform);
            }

            public boolean useTransform() {
              return false;
            }
          });
    }

    vv.getRenderer()
        .setVertexRenderer(
            new GradientVertexRenderer<>(
                vv.getSelectedVertexState(),
                Color.white,
                Color.red,
                Color.white,
                Color.blue,
                false));

    // add my listeners for ToolTips
    vv.setVertexToolTipFunction(n -> n);
    vv.setEdgeToolTipFunction(
        edge -> "E" + graph.getEdgeSource(edge) + "-" + graph.getEdgeTarget(edge));

    vv.getRenderContext().setVertexLabelFunction(n -> n);
    vv.getRenderer()
        .getVertexLabelRenderer(HEAVYWEIGHT)
        .setPositioner(new HeavyweightVertexLabelRenderer.InsidePositioner());
    vv.getRenderContext().setVertexLabelPosition(Renderer.VertexLabel.Position.AUTO);

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    vv.setToolTipText("<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode");
    vv.scaleToLayout();
    JButton reset = new JButton("reset");
    reset.addActionListener(
        e -> {
          vv.getRenderContext()
              .getMultiLayerTransformer()
              .getTransformer(MultiLayerTransformer.Layer.LAYOUT)
              .setToIdentity();
          vv.getRenderContext()
              .getMultiLayerTransformer()
              .getTransformer(MultiLayerTransformer.Layer.VIEW)
              .setToIdentity();
        });

    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls("Zoom", vv));
    controls.add(reset);
    add(controls, BorderLayout.SOUTH);
  }

  private Map<String, String[]> buildMap() {
    Map<String, String[]> map = new HashMap<>();

    map.put("TYO", new String[] {"35 40 N", "139 45 E"});
    map.put("PEK", new String[] {"39 55 N", "116 26 E"});
    map.put("MOW", new String[] {"55 45 N", "37 42 E"});
    map.put("JRS", new String[] {"31 47 N", "35 13 E"});
    map.put("CAI", new String[] {"30 03 N", "31 15 E"});
    map.put("CPT", new String[] {"33 55 S", "18 22 E"});
    map.put("PAR", new String[] {"48 52 N", "2 20 E"});
    map.put("LHR", new String[] {"51 30 N", "0 10 W"});
    map.put("HNL", new String[] {"21 18 N", "157 51 W"});
    map.put("NYC", new String[] {"40 77 N", "73 98 W"});
    map.put("SFO", new String[] {"37 62 N", "122 38 W"});
    map.put("AKL", new String[] {"36 55 S", "174 47 E"});
    map.put("BNE", new String[] {"27 28 S", "153 02 E"});
    map.put("HKG", new String[] {"22 15 N", "114 10 E"});
    map.put("KTM", new String[] {"27 42 N", "85 19 E"});
    map.put("IST", new String[] {"41 01 N", "28 58 E"});
    map.put("STO", new String[] {"59 20 N", "18 03 E"});
    map.put("RIO", new String[] {"22 54 S", "43 14 W"});
    map.put("LIM", new String[] {"12 03 S", "77 03 W"});
    map.put("YTO", new String[] {"43 39 N", "79 23 W"});

    return map;
  }

  private Graph<String, Integer> buildGraph(Map<String, String[]> map) {
    Graph<String, Integer> graph =
        GraphTypeBuilder.<String, Integer>forGraphType(DefaultGraphType.directedPseudograph())
            .edgeSupplier(SupplierUtil.createIntegerSupplier())
            .buildGraph();

    for (String city : map.keySet()) {
      graph.addVertex(city);
    }
    for (int i = 0; i < map.keySet().size() * 1.3; i++) {
      graph.addEdge(randomCity(), randomCity());
    }
    return graph;
  }

  private String randomCity() {
    int m = cityList.size();
    return cityList.get((int) (Math.random() * m));
  }

  static class CityTransformer implements Function<String, String[]> {

    Map<String, String[]> map;

    public CityTransformer(Map<String, String[]> map) {
      this.map = map;
    }

    /** transform airport code to latlon string */
    public String[] apply(String city) {
      return map.get(city);
    }
  }

  static class LatLonPixelTransformer implements Function<String[], Point> {
    Dimension d;

    public LatLonPixelTransformer(Dimension d) {
      this.d = d;
    }
    /** transform a lat */
    public Point apply(String[] latlon) {
      String[] lat = latlon[0].split(" ");
      String[] lon = latlon[1].split(" ");
      double latitude = Integer.parseInt(lat[0]) + Integer.parseInt(lat[1]) / 60f;
      latitude *= d.height / 180f;
      double longitude = Integer.parseInt(lon[0]) + Integer.parseInt(lon[1]) / 60f;
      longitude *= d.width / 360f;
      if (lat[2].equals("N")) {
        latitude = d.height / 2 - latitude;

      } else { // assume S
        latitude = d.height / 2 + latitude;
      }

      if (lon[2].equals("W")) {
        longitude = d.width / 2 - longitude;

      } else { // assume E
        longitude = d.width / 2 + longitude;
      }

      return Point.of(longitude, latitude);
    }
  }

  public static void main(String[] args) {
    // create a frome to hold the graph
    final JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    content.add(new WorldMapGraphDemo());
    frame.pack();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }
}
