package org.jungrapht.samples.util;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.CrossoverScalingControl;
import org.jungrapht.visualization.control.DefaultModalGraphMouse;
import org.jungrapht.visualization.control.ModalGraphMouse;
import org.jungrapht.visualization.control.MultiSelectionStrategy;
import org.jungrapht.visualization.control.ScalingControl;

/**
 * helpers for sample demos. Not included/supported in jungrapht-visualization jar Copy and modify
 * at will for use in other applications.
 *
 * @author Tom Nelson
 */
public class ControlHelpers {

  public static JComponent getZoomControls(String title, VisualizationServer vv) {
    return getCenteredContainer(title, getZoomControls(vv));
  }

  public static JComponent getZoomControls(VisualizationServer vv) {

    final ScalingControl scaler = new CrossoverScalingControl();
    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, 1.1f, vv.getCenter()));
    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, 1 / 1.1f, vv.getCenter()));

    JPanel zoomPanel = new JPanel();
    zoomPanel.add(plus);
    zoomPanel.add(minus);

    return zoomPanel;
  }

  public static JComponent getZoomControls(
      String title, VisualizationServer vv, LayoutManager buttonContainerLayoutManager) {
    return getCenteredContainer(title, getZoomControls(vv, buttonContainerLayoutManager));
  }

  public static JComponent getZoomControls(
      VisualizationServer vv, LayoutManager buttonContainerLayoutManager) {

    final ScalingControl scaler = new CrossoverScalingControl();
    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, 1.1f, vv.getCenter()));
    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, 1 / 1.1f, vv.getCenter()));

    JPanel zoomPanel = new JPanel(buttonContainerLayoutManager);
    zoomPanel.add(plus);
    zoomPanel.add(minus);

    return zoomPanel;
  }

  /**
   * Demo helper to make a titled toggle button to choose free-form multiselection
   *
   * @param title
   * @param vv
   * @return
   */
  public static JComponent getMultiselectPanel(String title, VisualizationViewer vv) {
    JToggleButton toggle = new JToggleButton("FreeForm Select");
    vv.setMultiSelectionStrategySupplier(
        () ->
            toggle.isSelected()
                ? MultiSelectionStrategy.arbitrary()
                : MultiSelectionStrategy.rectangular());
    return getCenteredContainer(title, toggle);
  }

  public static JComponent getModeControls(String title, VisualizationViewer vv) {
    return getCenteredContainer(title, getModeControls(vv));
  }

  public static JComponent getModeControls(VisualizationViewer vv) {
    VisualizationViewer.GraphMouse graphMouse = vv.getGraphMouse();
    if (graphMouse == null) {
      graphMouse = new DefaultModalGraphMouse();
      vv.setGraphMouse(graphMouse);
    }
    if (graphMouse instanceof DefaultModalGraphMouse) {
      JPanel modePanel = new JPanel(new GridLayout(2, 1));
      modePanel.add(((DefaultModalGraphMouse) graphMouse).getModeComboBox());
      return modePanel;
    } else {
      return new JPanel();
    }
  }

  public static JComponent getModeControls(String title, DefaultModalGraphMouse graphMouse) {
    return getCenteredContainer(title, getModeControls(graphMouse));
  }

  public static JComponent getModeControls(DefaultModalGraphMouse graphMouse) {
    JPanel modePanel = new JPanel(new GridLayout(2, 1));
    modePanel.add(graphMouse.getModeComboBox());
    return modePanel;
  }

  public static JComponent getModeRadio(String title, DefaultModalGraphMouse graphMouse) {
    return getCenteredContainer(title, getModeRadio(graphMouse));
  }

  public static JComponent getModeRadio(DefaultModalGraphMouse graphMouse) {
    JPanel container = new JPanel(new GridLayout(0, 1));
    final JRadioButton transformingButton =
        new JRadioButton(ModalGraphMouse.Mode.TRANSFORMING.toString());
    transformingButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
          }
        });

    final JRadioButton pickingButton = new JRadioButton(ModalGraphMouse.Mode.PICKING.toString());
    pickingButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
          }
        });
    ButtonGroup radio = new ButtonGroup();
    radio.add(transformingButton);
    radio.add(pickingButton);
    transformingButton.setSelected(true);
    container.add(transformingButton);
    container.add(pickingButton);
    container.setToolTipText("Menu for setting Mouse Mode");
    return container;
  }

  public static JComponent getCenteredContainer(String title, JComponent child) {
    Box box = Box.createVerticalBox();
    box.setBorder(new TitledBorder(title));
    box.add(Box.createGlue());
    box.add(child);
    box.add(Box.createGlue());
    return box;
  }

  public static JComponent getGridContainer(String title, int rows, JComponent... children) {
    Box verticalBox = Box.createVerticalBox();
    verticalBox.setBorder(new TitledBorder(title));
    int cols = children.length / rows;
    Box horizontalBox = null;
    for (int i = 0; i < children.length; i++) {
      if (i % cols == 0) {
        horizontalBox = Box.createHorizontalBox();
        verticalBox.add(Box.createGlue());
        verticalBox.add(horizontalBox);
        verticalBox.add(Box.createGlue());
      }
      horizontalBox.add(children[i]);
    }
    return verticalBox;
  }

  public static JComponent getContainer(String title, Box box, JComponent... children) {
    box.setBorder(new TitledBorder(title));
    return getContainer(box, children);
  }

  public static JComponent getContainer(Box box, JComponent... children) {
    for (int i = 0; i < children.length; i++) {
      if (i > 0) {
        box.add(Box.createGlue());
      }
      box.add(children[i]);
    }
    return box;
  }

  public static JComponent getCenteredContainer(String title, Box box, JComponent... children) {
    box.setBorder(new TitledBorder(title));
    return getCenteredContainer(box, children);
  }

  public static JComponent getCenteredContainer(Box box, JComponent... children) {
    Arrays.stream(children)
        .forEach(
            child -> {
              box.add(Box.createGlue());
              box.add(child);
              box.add(Box.createGlue());
            });
    return box;
  }
}
