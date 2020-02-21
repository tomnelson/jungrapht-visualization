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
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.visualization.LayeredIcon;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.decorators.EllipseShapeFunction;
import org.jungrapht.visualization.decorators.IconShapeFunction;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.CircleLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.util.RandomLocationTransformer;
import org.jungrapht.visualization.renderers.Checkmark;
import org.jungrapht.visualization.renderers.HeavyweightVertexRenderer;
import org.jungrapht.visualization.renderers.JLabelEdgeLabelRenderer;
import org.jungrapht.visualization.renderers.JLabelVertexLabelRenderer;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.jungrapht.visualization.util.ImageShapeUtils;

/**
 * Demonstrates the use of images to represent graph vertices. The images are supplied via the
 * VertexShapeFunction so that both the image and its shape can be utilized.
 *
 * <p>The images used in this demo (courtesy of slashdot.org) are rectangular but with a transparent
 * background. When vertices are represented by these images, it looks better if the actual shape of
 * the opaque part of the image is computed so that the edge arrowheads follow the visual shape of
 * the image. This demo uses the FourPassImageShaper class to compute the Shape from an image with
 * transparent background.
 *
 * @author Tom Nelson
 */
public class VertexImageShaperDemo extends JPanel {

  /** */
  private static final long serialVersionUID = -4332663871914930864L;

  /** the graph */
  Graph<Number, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Number, Number> vv;

  /** some icon names to use */
  String[] iconNames = {
    "apple",
    "os",
    "x",
    "linux",
    "inputdevices",
    "wireless",
    "graphics3",
    "gamespcgames",
    "humor",
    "music",
    "privacy"
  };

  public VertexImageShaperDemo() {
    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = createGraph();

    // Maps for the labels and icons
    Map<Number, String> map = new HashMap<>();
    Map<Number, Icon> iconMap = new HashMap<>();
    for (Number vertex : graph.vertexSet()) {
      int i = vertex.intValue();
      map.put(vertex, iconNames[i % iconNames.length]);

      String name = "/images/topic" + iconNames[i] + ".gif";
      try {
        Icon icon =
            new LayeredIcon(
                new ImageIcon(VertexImageShaperDemo.class.getResource(name)).getImage());
        iconMap.put(vertex, icon);
      } catch (Exception ex) {
        System.err.println("You need slashdoticons.jar in your classpath to see the image " + name);
      }
    }

    LayoutAlgorithm<Number> layoutAlgorithm = new CircleLayoutAlgorithm<>();
    //    layoutAlgorithm.setMaxIterations(100);
    //    treeLayoutAlgorithm.setInitializer(new RandomLocationTransformer<>(new Dimension(400, 400), 0));

    final DefaultModalGraphMouse<Number, Number> graphMouse = new DefaultModalGraphMouse<>();

    vv =
        VisualizationViewer.builder(
                (VisualizationModel<Number, Number>)
                    VisualizationModel.builder(graph)
                        .layoutAlgorithm(layoutAlgorithm)
                        .initializer(new RandomLocationTransformer<>(400, 400, 0))
                        .layoutSize(new Dimension(400, 400))
                        .build())
            .viewSize(new Dimension(400, 400))
            .graphMouse(graphMouse)
            .build();

    // This demo uses a special renderer to turn outlines on and off.
    // you do not need to do this in a real application.
    // Instead, just let vv use the Renderer it already has
    vv.getRenderer().setVertexRenderer(new DemoRenderer<>());

    Function<Number, Paint> vpf =
        new PickableElementPaintFunction<>(vv.getSelectedVertexState(), Color.white, Color.yellow);
    vv.getRenderContext().setVertexFillPaintFunction(vpf);
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(vv.getSelectedEdgeState(), Color.black, Color.cyan));

    final Function<Number, String> vertexStringerImpl = new VertexStringerImpl<>(map);
    vv.getRenderContext().setVertexLabelFunction(vertexStringerImpl);
    vv.getRenderContext().setVertexLabelRenderer(new JLabelVertexLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new JLabelEdgeLabelRenderer(Color.cyan));

    // For this demo only, I use a special class that lets me turn various
    // features on and off. For a real application, use VertexIconShapeTransformer instead.
    final DemoIconShapeFunction<Number> vertexIconShapeTransformer =
        new DemoIconShapeFunction<>(new EllipseShapeFunction<>());
    vertexIconShapeTransformer.setIconFunction(iconMap::get);

    final DemoVertexIconTransformer<Number> vertexIconTransformer =
        new DemoVertexIconTransformer<>(iconMap);

    vv.getRenderContext().setVertexShapeFunction(vertexIconShapeTransformer);
    vv.getRenderContext().setVertexIconFunction(vertexIconTransformer);

    // Get the selectedState and add a listener that will decorate the
    // Vertex images with a checkmark icon when they are selected
    MutableSelectedState<Number> ps = vv.getSelectedVertexState();
    ps.addItemListener(new PickWithIconListener<>(vertexIconTransformer));

    vv.addPostRenderPaintable(
        new VisualizationViewer.Paintable() {
          int x;
          int y;
          Font font;
          FontMetrics metrics;
          int swidth;
          int sheight;
          String str = "Thank You, slashdot.org, for the images!";

          public void paint(Graphics g) {
            Dimension d = vv.getSize();
            if (font == null) {
              font = new Font(g.getFont().getName(), Font.BOLD, 20);
              metrics = g.getFontMetrics(font);
              swidth = metrics.stringWidth(str);
              sheight = metrics.getMaxAscent() + metrics.getMaxDescent();
              x = (d.width - swidth) / 2;
              y = (int) (d.height - sheight * 1.5);
            }
            g.setFont(font);
            Color oldColor = g.getColor();
            g.setColor(Color.lightGray);
            g.drawString(str, x, y);
            g.setColor(oldColor);
          }

          public boolean useTransform() {
            return false;
          }
        });

    // add a listener for ToolTips
    vv.setVertexToolTipFunction(Object::toString);

    final VisualizationScrollPane panel = new VisualizationScrollPane(vv);
    add(panel);

    JCheckBox shape = new JCheckBox("Shape");
    shape.addItemListener(
        e -> {
          vertexIconShapeTransformer.setShapeImages(e.getStateChange() == ItemEvent.SELECTED);
          vv.repaint();
        });

    shape.setSelected(true);

    JCheckBox fill = new JCheckBox("Fill");
    fill.addItemListener(
        e -> {
          vertexIconTransformer.setFillImages(e.getStateChange() == ItemEvent.SELECTED);
          vv.repaint();
        });

    fill.setSelected(true);

    JCheckBox drawOutlines = new JCheckBox("Outline");
    drawOutlines.addItemListener(
        e -> {
          vertexIconTransformer.setOutlineImages(e.getStateChange() == ItemEvent.SELECTED);
          vv.repaint();
        });

    JComboBox<?> modeBox = graphMouse.getModeComboBox();
    JPanel modePanel = new JPanel();
    modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    modePanel.add(modeBox);

    JPanel labelFeatures = new JPanel(new GridLayout(1, 0));
    labelFeatures.setBorder(BorderFactory.createTitledBorder("Image Effects"));
    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls("Zoom", vv));
    labelFeatures.add(shape);
    labelFeatures.add(fill);
    labelFeatures.add(drawOutlines);

    controls.add(labelFeatures);
    controls.add(modePanel);
    add(controls, BorderLayout.SOUTH);
  }

  /**
   * When Vertices are selected, add a checkmark icon to the imager. Remove the icon when a Vertex
   * is unpicked
   *
   * @author Tom Nelson
   */
  public static class PickWithIconListener<V> implements ItemListener {
    Function<V, Icon> imager;
    Icon checked;

    public PickWithIconListener(Function<V, Icon> imager) {
      this.imager = imager;
      checked = new Checkmark();
    }

    public void itemStateChanged(ItemEvent e) {
      if (e.getItem() instanceof Collection) {
        ((Collection<V>) e.getItem()).forEach(n -> updatePickIcon(n, e.getStateChange()));
      } else {
        updatePickIcon((V) e.getItem(), e.getStateChange());
      }
    }

    private void updatePickIcon(V n, int stateChange) {
      Icon icon = imager.apply(n);
      if (icon instanceof LayeredIcon) {
        if (stateChange == ItemEvent.SELECTED) {
          ((LayeredIcon) icon).add(checked);
        } else {
          ((LayeredIcon) icon).remove(checked);
        }
      }
    }
  }
  /**
   * A simple implementation of Function that gets Vertex labels from a Map
   *
   * @author Tom Nelson
   */
  public static class VertexStringerImpl<V> implements Function<V, String> {

    Map<V, String> map = new HashMap<>();

    boolean enabled = true;

    public VertexStringerImpl(Map<V, String> map) {
      this.map = map;
    }

    public String apply(V v) {
      if (isEnabled()) {
        return map.get(v);
      } else {
        return "";
      }
    }

    /** @return Returns the enabled. */
    public boolean isEnabled() {
      return enabled;
    }

    /** @param enabled The enabled to set. */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  Graph<Number, Number> createGraph() {
    GraphBuilder<Number, Number, ?> graph =
        GraphTypeBuilder.<Number, Number>forGraphType(DefaultGraphType.dag()).buildGraphBuilder();
    graph.addEdge(0, 1, (Number) Math.random());
    graph.addEdge(3, 0, (Number) Math.random());
    graph.addEdge(0, 4, (Number) Math.random());
    graph.addEdge(4, 5, (Number) Math.random());
    graph.addEdge(5, 3, (Number) Math.random());
    graph.addEdge(2, 1, (Number) Math.random());
    graph.addEdge(4, 1, (Number) Math.random());
    graph.addEdge(8, 2, (Number) Math.random());
    graph.addEdge(3, 8, (Number) Math.random());
    graph.addEdge(6, 7, (Number) Math.random());
    graph.addEdge(7, 5, (Number) Math.random());
    graph.addEdge(0, 9, (Number) Math.random());
    graph.addEdge(9, 8, (Number) Math.random());
    graph.addEdge(7, 6, (Number) Math.random());
    graph.addEdge(6, 5, (Number) Math.random());
    graph.addEdge(4, 2, (Number) Math.random());
    graph.addEdge(5, 4, (Number) Math.random());
    graph.addEdge(4, 10, (Number) Math.random());
    graph.addEdge(10, 4, (Number) Math.random());

    return graph.build();
  }

  /**
   * This class exists only to provide settings to turn on/off shapes and image fill in this demo.
   *
   * <p>For a real application, just use {@code Functions.forMap(iconMap)} to provide a {@code
   * Function<V, Icon>}.
   */
  public static class DemoVertexIconTransformer<V> implements Function<V, Icon> {
    boolean fillImages = true;
    boolean outlineImages = false;
    Map<V, Icon> iconMap = new HashMap<>();

    public DemoVertexIconTransformer(Map<V, Icon> iconMap) {
      this.iconMap = iconMap;
    }

    /** @return Returns the fillImages. */
    public boolean isFillImages() {
      return fillImages;
    }
    /** @param fillImages The fillImages to set. */
    public void setFillImages(boolean fillImages) {
      this.fillImages = fillImages;
    }

    public boolean isOutlineImages() {
      return outlineImages;
    }

    public void setOutlineImages(boolean outlineImages) {
      this.outlineImages = outlineImages;
    }

    public Icon apply(V v) {
      if (fillImages) {
        return iconMap.get(v);
      } else {
        return null;
      }
    }
  }

  /**
   * this class exists only to provide settings to turn on/off shapes and image fill in this demo.
   * In a real application, use VertexIconShapeTransformer instead.
   */
  public static class DemoIconShapeFunction<V> extends IconShapeFunction<V> {

    boolean shapeImages = true;

    public DemoIconShapeFunction(Function<V, Shape> delegate) {
      super(delegate);
    }

    /** @return Returns the shapeImages. */
    public boolean isShapeImages() {
      return shapeImages;
    }
    /** @param shapeImages The shapeImages to set. */
    public void setShapeImages(boolean shapeImages) {
      shapeMap.clear();
      this.shapeImages = shapeImages;
    }

    @Override
    public Shape apply(V v) {
      Icon icon = iconFunction.apply(v);

      if (icon instanceof ImageIcon) {

        Image image = ((ImageIcon) icon).getImage();

        Shape shape = shapeMap.get(image);
        if (shape == null) {
          if (shapeImages) {
            shape = ImageShapeUtils.getShape(image, 30);
          } else {
            shape = new Rectangle2D.Float(0, 0, image.getWidth(null), image.getHeight(null));
          }
          if (shape.getBounds().getWidth() > 0 && shape.getBounds().getHeight() > 0) {
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            AffineTransform transform =
                AffineTransform.getTranslateInstance(-width / 2, -height / 2);
            shape = transform.createTransformedShape(shape);
            shapeMap.put(image, shape);
          }
        }
        return shape;
      } else {
        return delegate.apply(v);
      }
    }
  }

  /**
   * a special renderer that can turn outlines on and off in this demo. You won't need this for a
   * real application. Use HeavyweightVertexRenderer instead
   *
   * @author Tom Nelson
   */
  static class DemoRenderer<V, E> extends HeavyweightVertexRenderer<V, E> {

    @Override
    public void paintIconForVertex(
        RenderContext<V, E> renderContext, VisualizationModel<V, E> model, V v) {

      Point p = model.getLayoutModel().apply(v);
      Point2D p2d =
          renderContext
              .getMultiLayerTransformer()
              .transform(Layer.LAYOUT, new Point2D.Double(p.x, p.y));
      float x = (float) p2d.getX();
      float y = (float) p2d.getY();

      GraphicsDecorator g = renderContext.getGraphicsContext();
      boolean outlineImages = false;
      Function<V, Icon> vertexIconFunction = renderContext.getVertexIconFunction();

      if (vertexIconFunction instanceof DemoVertexIconTransformer) {
        outlineImages = ((DemoVertexIconTransformer<V>) vertexIconFunction).isOutlineImages();
      }
      Icon icon = vertexIconFunction.apply(v);
      if (icon == null || outlineImages) {

        Shape s =
            AffineTransform.getTranslateInstance(x, y)
                .createTransformedShape(renderContext.getVertexShapeFunction().apply(v));
        paintShapeForVertex(renderContext, v, s);
      }
      if (icon != null) {
        int xLoc = (int) (x - icon.getIconWidth() / 2);
        int yLoc = (int) (y - icon.getIconHeight() / 2);
        icon.paintIcon(renderContext.getScreenDevice(), g.getDelegate(), xLoc, yLoc);
      }
    }
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new VertexImageShaperDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
