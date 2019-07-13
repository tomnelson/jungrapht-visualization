package org.jungrapht.samples.util;

import java.awt.*;
import javax.swing.*;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ScalingControl;

/** @author Tom Nelson */
public class ControlHelpers {

  public static JComponent getZoomControls(VisualizationServer vv, String title) {

    final ScalingControl scaler = new CrossoverScalingControl();
    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));
    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JPanel zoomPanel = new JPanel();
    zoomPanel.setBorder(BorderFactory.createTitledBorder(title));
    zoomPanel.add(plus);
    zoomPanel.add(minus);

    return zoomPanel;
  }

  public static JComponent getZoomControls(
      VisualizationServer vv, String title, LayoutManager buttonContainerLayoutManager) {

    final ScalingControl scaler = new CrossoverScalingControl();
    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));
    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JPanel zoomPanel = new JPanel(buttonContainerLayoutManager);
    zoomPanel.setBorder(BorderFactory.createTitledBorder(title));
    zoomPanel.add(plus);
    zoomPanel.add(minus);

    return zoomPanel;
  }

  public static JComponent getModeControls(VisualizationViewer vv, String title) {
    final DefaultModalGraphMouse<Integer, Number> graphMouse = new DefaultModalGraphMouse();
    vv.setGraphMouse(graphMouse);

    JPanel modePanel = new JPanel(new GridLayout(2, 1));
    modePanel.setBorder(BorderFactory.createTitledBorder(title));
    modePanel.add(graphMouse.getModeComboBox());
    return modePanel;
  }
}
