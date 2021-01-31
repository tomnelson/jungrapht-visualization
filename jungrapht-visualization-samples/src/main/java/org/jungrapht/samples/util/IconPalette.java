package org.jungrapht.samples.util;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.io.IOException;
import javax.swing.*;
import org.jungrapht.visualization.LayeredIcon;

public class IconPalette extends JPanel {

  public IconPalette() {
    setLayout(new GridLayout(4, 3));
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

    var listener = new MyDragGestureListener();
    for (int i = 0; i < iconNames.length; i++) {
      String name = "/images/topic" + iconNames[i] + ".gif";
      try {
        Icon icon = new LayeredIcon(new ImageIcon(IconPalette.class.getResource(name)).getImage());
        //                Image icon = new ImageIcon(IconPalette.class.getResource(name)).getImage();
        var label = new JLabel(icon);
        DragSource ds = new DragSource();
        ds.createDefaultDragGestureRecognizer(label, DnDConstants.ACTION_COPY, listener);
        //                label.addMouseListener(listener);
        label.setTransferHandler(new TransferHandler("icon"));
        add(label);
      } catch (Exception ex) {
        System.err.println("You need slashdoticons.jar in your classpath to see the image " + name);
      }
    }
  }

  //    private class DragMouseAdapter extends MouseAdapter {
  //
  //        public void mousePressed(MouseEvent e) {
  //
  //            var c = (JComponent) e.getSource();
  //            var handler = c.getTransferHandler();
  //            handler.exportAsDrag(c, e, TransferHandler.COPY);
  //        }
  //    }

  class MyDragGestureListener implements DragGestureListener {

    @Override
    public void dragGestureRecognized(DragGestureEvent event) {
      JLabel label = (JLabel) event.getComponent();
      final ImageIcon ico = (ImageIcon) label.getIcon();

      Transferable transferable =
          new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
              return new DataFlavor[] {DataFlavor.imageFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
              if (!isDataFlavorSupported(flavor)) {
                return false;
              }
              return true;
            }

            @Override
            public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
              return ico.getImage();
            }
          };
      event.startDrag(null, transferable);
    }
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.getContentPane().add(new IconPalette());
    frame.pack();
    frame.setVisible(true);
  }
}
