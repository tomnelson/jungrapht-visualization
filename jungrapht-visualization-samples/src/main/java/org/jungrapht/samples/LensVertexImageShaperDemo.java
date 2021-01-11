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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jungrapht.samples.util.ControlHelpers;
import org.jungrapht.samples.util.LensControlHelper;
import org.jungrapht.visualization.LayeredIcon;
import org.jungrapht.visualization.MultiLayerTransformer.Layer;
import org.jungrapht.visualization.VisualizationScrollPane;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.LensMagnificationGraphMousePlugin;
import org.jungrapht.visualization.control.ModalLensGraphMouse;
import org.jungrapht.visualization.control.modal.Modal;
import org.jungrapht.visualization.control.modal.ModeComboBox;
import org.jungrapht.visualization.control.modal.ModeMenu;
import org.jungrapht.visualization.decorators.EllipseShapeFunction;
import org.jungrapht.visualization.decorators.IconShapeFunction;
import org.jungrapht.visualization.decorators.PickableElementPaintFunction;
import org.jungrapht.visualization.layout.algorithms.KKLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.renderers.Checkmark;
import org.jungrapht.visualization.renderers.JLabelEdgeLabelRenderer;
import org.jungrapht.visualization.renderers.JLabelVertexLabelRenderer;
import org.jungrapht.visualization.selection.MutableSelectedState;
import org.jungrapht.visualization.transform.LayoutLensSupport;
import org.jungrapht.visualization.transform.Lens;
import org.jungrapht.visualization.transform.LensSupport;
import org.jungrapht.visualization.transform.MagnifyTransformer;
import org.jungrapht.visualization.transform.shape.MagnifyImageLensSupport;
import org.jungrapht.visualization.transform.shape.MagnifyShapeTransformer;
import org.jungrapht.visualization.util.GraphImage;

/**
 * Demonstrates the use of images to represent graph vertices.
 *
 * <p>The images used in this demo (courtesy of slashdot.org) are rectangular but with a transparent
 * background. When vertices are represented by these images, it looks better if the actual shape of
 * the opaque part of the image is computed so that the edge arrowheads follow the visual shape of
 * the image. This demo uses the FourPassImageShaper class to compute the Shape from an image with
 * transparent background.
 *
 * @author Tom Nelson
 */
public class LensVertexImageShaperDemo extends JPanel {

  /** */
  private static final long serialVersionUID = 5432239991020505763L;

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

  LensSupport<ModalLensGraphMouse> magnifyLayoutSupport;
  LensSupport<ModalLensGraphMouse> magnifyViewSupport;
  /** create an instance of a simple graph with controls to demo the zoom features. */
  public LensVertexImageShaperDemo() {

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
                new ImageIcon(LensVertexImageShaperDemo.class.getResource(name)).getImage());
        iconMap.put(vertex, icon);
      } catch (Exception ex) {
        System.err.println("You need slashdoticons.jar in your classpath to see the image " + name);
      }
    }

    KKLayoutAlgorithm<Number> layoutAlgorithm = new KKLayoutAlgorithm<>();
    layoutAlgorithm.setMaxIterations(100);

    final DefaultModalGraphMouse<Number, Number> graphMouse = new DefaultModalGraphMouse<>();

    vv =
        VisualizationViewer.builder(graph)
            .graphMouse(graphMouse)
            .layoutAlgorithm(layoutAlgorithm)
            .viewSize(new Dimension(600, 600))
            .build();

    Function<Number, Paint> vpf =
        new PickableElementPaintFunction<>(vv.getSelectedVertexState(), Color.white, Color.yellow);
    vv.getRenderContext().setVertexFillPaintFunction(vpf);
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableElementPaintFunction<>(vv.getSelectedEdgeState(), Color.black, Color.cyan));

    vv.getRenderContext().setVertexLabelFunction(map::get);
    vv.getRenderContext().setVertexLabelRenderer(new JLabelVertexLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new JLabelEdgeLabelRenderer(Color.cyan));

    final IconShapeFunction<Number> vertexImageShapeFunction =
        new IconShapeFunction<>(new EllipseShapeFunction<>());

    final Function<Number, Icon> vertexIconFunction = iconMap::get;

    vertexImageShapeFunction.setIconFunction(iconMap::get);

    vv.getRenderContext().setVertexShapeFunction(vertexImageShapeFunction);
    vv.getRenderContext().setVertexIconFunction(vertexIconFunction);

    // Get the pickedState and add a listener that will decorate the
    //Vertex images with a checkmark icon when they are selected
    MutableSelectedState<Number> ps = vv.getSelectedVertexState();
    ps.addItemListener(new PickWithIconListener(vertexIconFunction));

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

    Lens lens = Lens.builder().lensShape(Lens.Shape.RECTANGLE).build();
    lens.setMagnification(2.f);
    magnifyViewSupport =
        MagnifyImageLensSupport.<Number, Number, ModalLensGraphMouse>builder(vv)
            .lensTransformer(
                MagnifyShapeTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW))
                    .build())
            .lensGraphMouse(
                new ModalLensGraphMouse(new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f)))
            .build();

    magnifyLayoutSupport =
        LayoutLensSupport.<Number, Number, ModalLensGraphMouse>builder(vv)
            .lensTransformer(
                MagnifyTransformer.builder(lens)
                    .delegate(
                        vv.getRenderContext()
                            .getMultiLayerTransformer()
                            .getTransformer(Layer.LAYOUT))
                    .build())
            .lensGraphMouse(
                new ModalLensGraphMouse(new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f)))
            .build();

    //    JComboBox<Mode> modeBox =
    //            ModeControls.getStandardModeComboBox(graphMouse,
    //                    magnifyLayoutSupport.getGraphMouse(), magnifyViewSupport.getGraphMouse());
    ModeComboBox modeBox =
        ModeComboBox.builder()
            .modes(Modal.Mode.TRANSFORMING, Modal.Mode.PICKING)
            .modals(graphMouse)
            .build();
    //graphMouse.getModeComboBox();
    JPanel modePanel = new JPanel();
    modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    modePanel.add(modeBox.buildUI());

    Box controls = Box.createHorizontalBox();
    controls.add(ControlHelpers.getZoomControls("Scale", vv));

    controls.add(modePanel);
    JButton imageButton = new JButton("Save Image");
    imageButton.addActionListener(evt -> GraphImage.capture(vv));
    controls.add(imageButton);
    add(controls, BorderLayout.SOUTH);

    LayoutModel<Number> layoutModel = vv.getVisualizationModel().getLayoutModel();
    Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());

    //    Lens lens = Lens.builder().lensShape(Lens.Shape.RECTANGLE).build();
    //    lens.setMagnification(2.f);
    //    magnifyViewSupport =
    //        MagnifyImageLensSupport.<Number, Number, ModalLensGraphMouse>builder(vv)
    //            .lensTransformer(
    //                MagnifyShapeTransformer.builder(lens)
    //                    .delegate(
    //                        vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW))
    //                    .build())
    //            .lensGraphMouse(
    //                new ModalLensGraphMouse(new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f)))
    //            .build();
    //
    //    magnifyLayoutSupport =
    //        LayoutLensSupport.<Number, Number, ModalLensGraphMouse>builder(vv)
    //            .lensTransformer(
    //                MagnifyTransformer.builder(lens)
    //                    .delegate(
    //                        vv.getRenderContext()
    //                            .getMultiLayerTransformer()
    //                            .getTransformer(Layer.LAYOUT))
    //                    .build())
    //            .lensGraphMouse(
    //                new ModalLensGraphMouse(new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f)))
    //            .build();

    graphMouse.addItemListener(magnifyLayoutSupport.getGraphMouse().getModeListener());
    graphMouse.addItemListener(magnifyViewSupport.getGraphMouse().getModeListener());

    JMenuBar menubar = new JMenuBar();
    //    JMenu modeMenu = ModeControls.getStandardModeMenu();
    ModeMenu modeMenu =
        ModeMenu.builder()
            .modes(Modal.Mode.TRANSFORMING, Modal.Mode.PICKING)
            .modals(
                graphMouse,
                magnifyLayoutSupport.getGraphMouse(),
                magnifyViewSupport.getGraphMouse())
            .buttonSupplier(JRadioButtonMenuItem::new)
            .build();
    //graphMouse.getModeMenu();
    menubar.add(modeMenu);
    controls.add(
        LensControlHelper.builder(
                Map.of(
                    "Magnified View", magnifyViewSupport,
                    "Magnified Layout", magnifyLayoutSupport))
            .title("Lens Controls")
            .build()
            .container());
  }

  Graph<Number, Number> createGraph() {
    Graph<Number, Number> graph =
        GraphTypeBuilder.<Number, Number>forGraphType(DefaultGraphType.dag()).buildGraph();

    IntStream.rangeClosed(0, 10).forEach(graph::addVertex);
    graph.addEdge(0, 1, Math.random());
    graph.addEdge(3, 0, Math.random());
    graph.addEdge(0, 4, Math.random());
    graph.addEdge(4, 5, Math.random());
    graph.addEdge(5, 3, Math.random());
    graph.addEdge(2, 1, Math.random());
    graph.addEdge(4, 1, Math.random());
    graph.addEdge(8, 2, Math.random());
    graph.addEdge(3, 8, Math.random());
    graph.addEdge(6, 7, Math.random());
    graph.addEdge(7, 5, Math.random());
    graph.addEdge(0, 9, Math.random());
    graph.addEdge(9, 8, Math.random());
    graph.addEdge(7, 6, Math.random());
    graph.addEdge(6, 5, Math.random());
    graph.addEdge(4, 2, Math.random());
    graph.addEdge(5, 4, Math.random());
    graph.addEdge(4, 10, Math.random());
    graph.addEdge(10, 4, Math.random());

    return graph;
  }

  public static class PickWithIconListener implements ItemListener {
    Function<Number, Icon> imager;
    Icon checked;

    public PickWithIconListener(Function<Number, Icon> imager) {
      this.imager = imager;
      checked = new Checkmark(Color.red);
    }

    public void itemStateChanged(ItemEvent e) {
      if (e.getItem() instanceof Collection) {
        ((Collection<Number>) e.getItem()).forEach(n -> updatePickIcon(n, e.getStateChange()));
      } else {
        updatePickIcon((Number) e.getItem(), e.getStateChange());
      }
    }

    private void updatePickIcon(Number n, int stateChange) {
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

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new LensVertexImageShaperDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
