package org.jungrapht.visualization.control.dnd;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.geom.Point2D;
import java.util.function.BiConsumer;
import javax.swing.*;
import org.jungrapht.visualization.LayeredIcon;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.util.PointUtils;

public class VertexImageDropTargetListener<V, E> extends DropTargetAdapter {

  private DropTarget dropTarget;
  private VisualizationViewer<V, E> viewer;
  private BiConsumer<V, Icon> iconConsumer; // holds the icons for vertices created here

  public VertexImageDropTargetListener(
      VisualizationViewer<V, E> viewer, BiConsumer<V, Icon> iconConsumer) {
    this.viewer = viewer;
    this.iconConsumer = iconConsumer;
    dropTarget = new DropTarget(viewer.getComponent(), DnDConstants.ACTION_COPY, this, true, null);
  }

  @Override
  public void drop(DropTargetDropEvent event) {
    try {
      DropTarget dropTarget = (DropTarget) event.getSource();
      Component component = dropTarget.getComponent();
      Point2D dropPoint = component.getMousePosition();
      Point layoutDropPoint =
          PointUtils.convert(
              viewer.getRenderContext().getMultiLayerTransformer().inverseTransform(dropPoint));
      Transferable transferable = event.getTransferable();

      if (event.isDataFlavorSupported(DataFlavor.imageFlavor)) {
        Image image = (Image) transferable.getTransferData(DataFlavor.imageFlavor);
        ImageIcon icon = new LayeredIcon(image);

        if (icon != null) {

          V vertex = viewer.getVisualizationModel().getGraph().addVertex();
          viewer.getVertexSpatial().recalculate();
          iconConsumer.accept(vertex, icon);
          viewer.getVisualizationModel().getLayoutModel().set(vertex, layoutDropPoint);
          event.dropComplete(true);
        }
      } else {
        event.rejectDrop();
      }
    } catch (Exception e) {
      e.printStackTrace();
      event.rejectDrop();
    }
  }
}
