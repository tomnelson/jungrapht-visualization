package org.jungrapht.samples.util;

import java.awt.*;
import java.awt.event.ItemEvent;
import javax.swing.*;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.util.BalloonLayoutRings;
import org.jungrapht.visualization.util.RadialLayoutRings;

public class TreeLayoutSelector<V, E> extends JPanel {

  VisualizationServer.Paintable balloonPaintable;
  VisualizationServer.Paintable radialPaintable;

  TreeLayoutAlgorithm<V> treeLayoutAlgorithm = TreeLayoutAlgorithm.<V>builder().build();

  BalloonLayoutAlgorithm<V> balloonLayoutAlgorithm = BalloonLayoutAlgorithm.<V>builder().build();

  RadialTreeLayoutAlgorithm<V> radialTreeLayoutAlgorithm =
      RadialTreeLayoutAlgorithm.<V>builder().build();

  VisualizationServer<V, E> vv;

  final JRadioButton animateTransition = new JRadioButton("Animate Transition", true);

  public TreeLayoutSelector(VisualizationServer<V, E> vv) {
    this(vv, 0, null);
  }

  public TreeLayoutSelector(VisualizationServer<V, E> vv, Runnable after) {
    this(vv, 0, after);
  }

  public TreeLayoutSelector(VisualizationServer<V, E> vv, int initialSelection) {
    this(vv, initialSelection, null);
  }

  public TreeLayoutSelector(VisualizationServer<V, E> vv, int initialSelection, Runnable after) {
    super(new GridLayout(0, 1));
    this.vv = vv;
    JRadioButton treeButton = new JRadioButton("Tree");
    treeButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (animateTransition.isSelected()) {
              LayoutAlgorithmTransition.animate(vv, treeLayoutAlgorithm, after);
            } else {
              LayoutAlgorithmTransition.apply(vv, treeLayoutAlgorithm, after);
              //              vv.scaleToLayout();
            }
          }
          vv.repaint();
        });
    treeButton.setSelected(initialSelection == 0);
    JRadioButton balloonButton = new JRadioButton("Balloon");
    balloonButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (animateTransition.isSelected()) {
              LayoutAlgorithmTransition.animate(vv, balloonLayoutAlgorithm, after);
            } else {
              LayoutAlgorithmTransition.apply(vv, balloonLayoutAlgorithm, after);
              //                vv.scaleToLayout();
            }
            balloonPaintable = new BalloonLayoutRings(vv, balloonLayoutAlgorithm);
            vv.addPreRenderPaintable(balloonPaintable);
          } else {
            vv.removePreRenderPaintable(balloonPaintable);
          }
          vv.repaint();
        });
    balloonButton.setSelected(initialSelection == 1);
    JRadioButton radialButton = new JRadioButton("Radial");
    radialButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (animateTransition.isSelected()) {
              LayoutAlgorithmTransition.animate(vv, radialTreeLayoutAlgorithm, after);
            } else {
              LayoutAlgorithmTransition.apply(vv, radialTreeLayoutAlgorithm, after);
              //                vv.scaleToLayout();
            }
            radialPaintable = new RadialLayoutRings<>(vv, radialTreeLayoutAlgorithm);
            vv.addPreRenderPaintable(radialPaintable);
          } else {
            vv.removePreRenderPaintable(radialPaintable);
          }
          vv.repaint();
        });
    radialButton.setSelected(initialSelection == 2);

    ButtonGroup layoutRadio = new ButtonGroup();
    layoutRadio.add(treeButton);
    layoutRadio.add(balloonButton);
    layoutRadio.add(radialButton);

    this.add(treeButton);
    this.add(balloonButton);
    this.add(radialButton);
    this.add(animateTransition);
  }
}
