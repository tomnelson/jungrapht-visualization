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

public class TreeLayoutSelector<N, E> extends JPanel {

  VisualizationServer.Paintable balloonPaintable;
  VisualizationServer.Paintable radialPaintable;

  TreeLayoutAlgorithm<N> treeLayoutAlgorithm = TreeLayoutAlgorithm.<N>builder().build();

  BalloonLayoutAlgorithm<N> balloonLayoutAlgorithm = BalloonLayoutAlgorithm.<N>builder().build();

  RadialTreeLayoutAlgorithm<N> radialTreeLayoutAlgorithm =
      RadialTreeLayoutAlgorithm.<N>builder().build();

  VisualizationServer<N, E> vv;

  final JRadioButton animateTransition = new JRadioButton("Animate Transition", true);

  public TreeLayoutSelector(VisualizationServer<N, E> vv) {
    this(vv, 0);
  }

  public TreeLayoutSelector(VisualizationServer<N, E> vv, int initialSelection) {
    super(new GridLayout(4, 1));
    this.vv = vv;
    JRadioButton treeButton = new JRadioButton("Tree Layout"); //, initialSelection == 0);
    treeButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (animateTransition.isSelected()) {
              LayoutAlgorithmTransition.animate(vv, treeLayoutAlgorithm);
            } else {
              LayoutAlgorithmTransition.apply(vv, treeLayoutAlgorithm);
            }
          }
          vv.repaint();
        });
    treeButton.setSelected(initialSelection == 0);
    JRadioButton balloonButton = new JRadioButton("Balloon Layout"); //, initialSelection == 1);
    balloonButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (animateTransition.isSelected()) {
              LayoutAlgorithmTransition.animate(vv, balloonLayoutAlgorithm);
            } else {
              LayoutAlgorithmTransition.apply(vv, balloonLayoutAlgorithm);
            }
            balloonPaintable = new BalloonLayoutRings(vv, balloonLayoutAlgorithm);
            vv.addPreRenderPaintable(balloonPaintable);
          } else {
            vv.removePreRenderPaintable(balloonPaintable);
          }
          vv.repaint();
        });
    balloonButton.setSelected(initialSelection == 1);
    JRadioButton radialButton = new JRadioButton("Radial Layout"); //, initialSelection == 2);
    radialButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (animateTransition.isSelected()) {
              LayoutAlgorithmTransition.animate(vv, radialTreeLayoutAlgorithm);
            } else {
              LayoutAlgorithmTransition.apply(vv, radialTreeLayoutAlgorithm);
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
