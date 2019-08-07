package org.jungrapht.samples.util;

import com.google.common.base.Preconditions;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.*;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;
import org.jungrapht.visualization.layout.algorithms.MultiRowEdgeSortingTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.MultiRowTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.util.DebugPaintable;
import org.jungrapht.visualization.util.BalloonLayoutRings;
import org.jungrapht.visualization.util.RadialLayoutRings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeLayoutSelector<V, E> extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(TreeLayoutSelector.class);

  public static class Builder<V, E> {
    VisualizationServer<V, E> visualizationServer;
    int intialialSelection;
    Set<V> roots = new HashSet<>();
    Runnable after = () -> {};

    Builder(VisualizationServer<V, E> visualizationServer) {
      this.visualizationServer = visualizationServer;
    }

    public Builder roots(Set<V> roots) {
      this.roots = roots;
      return this;
    }

    public Builder initialSelection(int intialialSelection) {
      this.intialialSelection = intialialSelection;
      return this;
    }

    public Builder after(Runnable after) {
      this.after = after;
      return this;
    }

    public TreeLayoutSelector<V, E> build() {
      return new TreeLayoutSelector<>(this);
    }
  }

  public static <V, E> Builder<V, E> builder(VisualizationServer visualizationServer) {
    return new Builder<>(visualizationServer);
  }

  private TreeLayoutSelector(Builder<V, E> builder) {
    this(builder.visualizationServer, builder.roots, builder.intialialSelection, builder.after);
  }

  Set<VisualizationServer.Paintable> paintables = new HashSet<>();

  TreeLayoutAlgorithm<V> treeLayoutAlgorithm = TreeLayoutAlgorithm.<V>builder().build();

  MultiRowTreeLayoutAlgorithm<V> multiRowTreeLayoutAlgorithm =
      MultiRowTreeLayoutAlgorithm.<V>builder().build();

  MultiRowEdgeSortingTreeLayoutAlgorithm<V, E> multiRowEdgeSortingTreeLayoutAlgorithm =
      MultiRowEdgeSortingTreeLayoutAlgorithm.<V, E>sortingBuilder()
          .horizontalVertexSpacing(100)
          .verticalVertexSpacing(100)
          .build();

  BalloonLayoutAlgorithm<V> balloonLayoutAlgorithm = BalloonLayoutAlgorithm.<V>builder().build();

  RadialTreeLayoutAlgorithm<V> radialTreeLayoutAlgorithm =
      RadialTreeLayoutAlgorithm.<V>builder().build();

  VisualizationServer<V, E> vv;

  Set<V> roots;

  final JRadioButton animateTransition = new JRadioButton("Animate Transition", true);

  private TreeLayoutSelector(
      VisualizationServer<V, E> vv, Set<V> roots, int initialSelection, Runnable after) {
    super(new GridLayout(0, 1));
    this.vv = vv;
    this.roots = roots;
    treeLayoutAlgorithm.setRoots(roots);
    multiRowTreeLayoutAlgorithm.setRoots(roots);
    balloonLayoutAlgorithm.setRoots(roots);
    radialTreeLayoutAlgorithm.setRoots(roots);
    Preconditions.checkNotNull(after);
    JRadioButton treeButton = new JRadioButton("Tree");
    treeButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (animateTransition.isSelected()) {
              LayoutAlgorithmTransition.animate(vv, treeLayoutAlgorithm, after);
            } else {
              LayoutAlgorithmTransition.apply(vv, treeLayoutAlgorithm, after);
            }
            paintables.forEach(
                p -> {
                  vv.removePreRenderPaintable(p);
                  paintables.remove(p);
                });
            //            paintables.add(new DebugPaintable.Grid());
            Map<V, Integer> widthMap = treeLayoutAlgorithm.getBaseWidths();
            Map<V, Integer> heightMap = treeLayoutAlgorithm.getBaseHeights();
            Map<V, Dimension> cellMap =
                vv.getVisualizationModel()
                    .getLayoutModel()
                    .getGraph()
                    .vertexSet()
                    .stream()
                    .collect(
                        Collectors.toMap(
                            v -> v,
                            v -> new Dimension(widthMap.get(v), heightMap.get(v)),
                            (a, b) -> b));
            paintables.add(
                new DebugPaintable.TreeCells(
                    vv.getVisualizationModel().getLayoutModel(),
                    cellMap,
                    vv.getRenderContext().getMultiLayerTransformer()));
            paintables.forEach(p -> vv.addPreRenderPaintable(p));
          }
          vv.repaint();
        });

    treeButton.setSelected(initialSelection == 0);

    JRadioButton multiRowTreeButton = new JRadioButton("MultiRowTree");
    multiRowTreeButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (animateTransition.isSelected()) {
              LayoutAlgorithmTransition.animate(vv, multiRowTreeLayoutAlgorithm, after);
            } else {
              LayoutAlgorithmTransition.apply(vv, multiRowTreeLayoutAlgorithm, after);
            }
            paintables.forEach(
                p -> {
                  vv.removePreRenderPaintable(p);
                  paintables.remove(p);
                });
            //            paintables.add(new DebugPaintable.Grid());
            Map<V, Integer> widthMap = treeLayoutAlgorithm.getBaseWidths();
            Map<V, Integer> heightMap = treeLayoutAlgorithm.getBaseHeights();
            Map<V, Dimension> cellMap =
                vv.getVisualizationModel()
                    .getLayoutModel()
                    .getGraph()
                    .vertexSet()
                    .stream()
                    .collect(
                        Collectors.toMap(
                            v -> v,
                            v -> new Dimension(widthMap.get(v), heightMap.get(v)),
                            (a, b) -> b));
            paintables.add(
                new DebugPaintable.TreeCells(
                    vv.getVisualizationModel().getLayoutModel(),
                    cellMap,
                    vv.getRenderContext().getMultiLayerTransformer()));
            paintables.forEach(p -> vv.addPreRenderPaintable(p));
          }
          vv.repaint();
        });
    multiRowTreeButton.setSelected(initialSelection == 1);

    JRadioButton balloonButton = new JRadioButton("Balloon");
    balloonButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (animateTransition.isSelected()) {
              LayoutAlgorithmTransition.animate(vv, balloonLayoutAlgorithm, after);
            } else {
              LayoutAlgorithmTransition.apply(vv, balloonLayoutAlgorithm, after);
            }
            paintables.forEach(
                p -> {
                  vv.removePreRenderPaintable(p);
                  paintables.remove(p);
                });
            paintables.add(new BalloonLayoutRings(vv, balloonLayoutAlgorithm));
            paintables.forEach(p -> vv.addPreRenderPaintable(p));
          }
          vv.repaint();
        });
    balloonButton.setSelected(initialSelection == 2);
    JRadioButton radialButton = new JRadioButton("Radial");
    radialButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (animateTransition.isSelected()) {
              LayoutAlgorithmTransition.animate(vv, radialTreeLayoutAlgorithm, after);
            } else {
              LayoutAlgorithmTransition.apply(vv, radialTreeLayoutAlgorithm, after);
            }
            paintables.forEach(
                p -> {
                  vv.removePreRenderPaintable(p);
                  paintables.remove(p);
                });
            paintables.add(new RadialLayoutRings<>(vv, radialTreeLayoutAlgorithm));
            paintables.forEach(p -> vv.addPreRenderPaintable(p));
          }
          vv.repaint();
        });
    radialButton.setSelected(initialSelection == 3);

    ButtonGroup layoutRadio = new ButtonGroup();
    layoutRadio.add(treeButton);
    layoutRadio.add(multiRowTreeButton);
    layoutRadio.add(balloonButton);
    layoutRadio.add(radialButton);

    this.add(treeButton);
    this.add(multiRowTreeButton);
    this.add(balloonButton);
    this.add(radialButton);
    this.add(animateTransition);
  }
}
