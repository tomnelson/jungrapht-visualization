package org.jungrapht.visualization.util;

import static org.jungrapht.visualization.renderers.BiModalRenderer.HEAVYWEIGHT;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.visualization.VisualizationImageServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;

public class GraphImage {

  /**
   * @param vv
   * @param scale how much to scale the image by
   */
  public static <V, E> void capture(VisualizationViewer<V, E> vv, double scale) {

    JFileChooser fileChooser = new JFileChooser();
    int returnValue = fileChooser.showOpenDialog(vv.getComponent());
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      File outputFile = fileChooser.getSelectedFile();
      if (outputFile != null) {
        // stuff from the view
        Graph<V, E> graph = vv.getVisualizationModel().getGraph();
        Dimension vvLayoutSize = vv.getVisualizationModel().getLayoutSize();
        Dimension vvViewSize = vv.getSize();
        LayoutModel<V> layoutModel = vv.getVisualizationModel().getLayoutModel();
        // stuff for the image
        // make the image scale times the incoming vv's layout size
        Dimension imageLayoutSize =
            new Dimension((int) (vvLayoutSize.width * scale), (int) (vvLayoutSize.height * scale));

        VisualizationImageServer<V, E> vis =
            VisualizationImageServer.builder(graph)
                .viewSize(imageLayoutSize)
                .layoutSize(imageLayoutSize)
                .layoutAlgorithm(new StaticLayoutAlgorithm<>())
                .build();

        // make the vertexShapeFunction draw the vertices scaled up by 'scale'
        AffineTransform scaler = AffineTransform.getScaleInstance(scale, scale);
        Function<V, Shape> visVertexShapeFunction =
            v ->
                scaler.createTransformedShape(
                    vv.getRenderContext().getVertexShapeFunction().apply(v));
        vis.getRenderContext().setVertexShapeFunction(visVertexShapeFunction);

        // scale up the arrows by 'scale'
        vis.getRenderContext()
            .setEdgeArrowWidth((int) (vv.getRenderContext().getEdgeArrowWidth() * scale));
        vis.getRenderContext()
            .setEdgeArrowLength((int) (vv.getRenderContext().getEdgeArrowLength() * scale));

        // make the edge stroke 'scale' times wider
        vis.getRenderContext().setEdgeWidth((float) (vv.getRenderContext().getEdgeWidth() * scale));

        // make the label fonts 'scale' times bigger
        Function<V, Font> vertexFontFunction = vv.getRenderContext().getVertexFontFunction();
        vis.getRenderContext()
            .setVertexFontFunction(
                v -> {
                  Font font = vertexFontFunction.apply(v);
                  return new Font(font.getName(), font.getStyle(), (int) (font.getSize() * scale));
                });
        Function<E, Font> edgeFontFunction = vv.getRenderContext().getEdgeFontFunction();
        vis.getRenderContext()
            .setEdgeFontFunction(
                e -> {
                  Font font = edgeFontFunction.apply(e);
                  return new Font(font.getName(), font.getStyle(), (int) (font.getSize() * scale));
                });

        // use the same edge shape function (real important for articulated edges)
        vis.getRenderContext().setEdgeShapeFunction(vv.getRenderContext().getEdgeShapeFunction());

        vis.getRenderContext()
            .setVertexLabelFunction(vv.getRenderContext().getVertexLabelFunction());
        vis.getRenderContext().setEdgeLabelFunction(vv.getRenderContext().getEdgeLabelFunction());
        vis.getRenderContext()
            .setVertexLabelPosition(vv.getRenderContext().getVertexLabelPosition());

        // move all the layout points by the scale factor
        Map<V, Point> scaledPoints =
            graph
                .vertexSet()
                .stream()
                .collect(Collectors.toMap(v -> v, v -> layoutModel.apply(v).multiply(scale)));
        // initialize with the (scaled) layout points
        vis.getVisualizationModel().getLayoutModel().setInitializer(scaledPoints::get);
        // this should be the default in the VisualizationImageServer but for now it is here
        vis.getRenderer().setMode(HEAVYWEIGHT);
        // makes the layout 'fit' in the view for the image
        vis.scaleToLayout(true);

        Image image =
            vis.getImage(
                new Point2D.Double(imageLayoutSize.width / 2, imageLayoutSize.height / 2),
                new Dimension(imageLayoutSize.width, imageLayoutSize.height));
        try {
          ImageIO.write((BufferedImage) image, "jpg", outputFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
