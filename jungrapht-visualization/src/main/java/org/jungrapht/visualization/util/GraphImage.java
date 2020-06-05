package org.jungrapht.visualization.util;

import static org.jungrapht.visualization.renderers.BiModalRenderer.HEAVYWEIGHT;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.visualization.VisualizationImageServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.algorithms.StaticLayoutAlgorithm;

public class GraphImage {

  /**
   * Creates a higher resolution image of a graph visualization
   *
   * @param vv // * @param scale how much to scale the image by
   */
  public static <V, E> void capture(VisualizationViewer<V, E> vv) {

    JFileChooser fileChooser = new JFileChooser();
    int returnValue = fileChooser.showOpenDialog(vv.getComponent());
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      File outputFile = fileChooser.getSelectedFile();
      if (outputFile != null) {

        Graph<V, E> graph = vv.getVisualizationModel().getGraph();
        Dimension vvLayoutSize = vv.getVisualizationModel().getLayoutSize();

        VisualizationImageServer<V, E> vis =
            VisualizationImageServer.builder(graph)
                .viewSize(vvLayoutSize)
                .layoutSize(vvLayoutSize)
                .layoutAlgorithm(new StaticLayoutAlgorithm<>())
                .build();

        // initialize the layout model for the VisualizationImageServer with the
        // LayoutModel from the incoming VisualizationViewer so it will have the same
        // vertex locations
        vis.setRenderContext(vv.getRenderContext());
        vis.getVisualizationModel()
            .getLayoutModel()
            .setInitializer(vv.getVisualizationModel().getLayoutModel());

        vis.getRenderer().setMode(HEAVYWEIGHT);

        Image image = vis.getFullImage();
        try {
          ImageIO.write((BufferedImage) image, "jpg", outputFile);

        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
