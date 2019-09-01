package org.jungrapht.visualization.util.helpers;

import com.google.common.base.Preconditions;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.swing.*;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.EdgeAwareTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;
import org.jungrapht.visualization.layout.algorithms.MultiRowEdgeAwareTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.MultiRowTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.RadialEdgeAwareTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayout;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayout;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.util.LayoutPaintable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeLayoutSelector<V, E> extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(TreeLayoutSelector.class);

  public static class Builder<V, E> {
    VisualizationServer<V, E> visualizationServer;
    int intialialSelection;
    Runnable after = () -> {};
    Predicate<E> edgePredicate = e -> false;
    Comparator<E> edgeComparator = (e1, e2) -> 0;
    Predicate<V> vertexPredicate = e -> false;
    Comparator<V> vertexComparator = (e1, e2) -> 0;

    Builder(VisualizationServer<V, E> visualizationServer) {
      this.visualizationServer = visualizationServer;
    }

    public Builder initialSelection(int intialialSelection) {
      this.intialialSelection = intialialSelection;
      return this;
    }

    public Builder edgeComparator(Comparator<E> edgeComparator) {
      this.edgeComparator = edgeComparator;
      return this;
    }

    public Builder vertexComparator(Comparator<V> vertexComparator) {
      this.vertexComparator = vertexComparator;
      return this;
    }

    public Builder edgePredicate(Predicate<E> edgePredicate) {
      this.edgePredicate = edgePredicate;
      return this;
    }

    public Builder vertexPredicate(Predicate<V> vertexPredicate) {
      this.vertexPredicate = vertexPredicate;
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
    this(
        builder.visualizationServer,
        builder.intialialSelection,
        builder.vertexPredicate,
        builder.edgePredicate,
        builder.vertexComparator,
        builder.edgeComparator,
        builder.after);
  }

  VisualizationServer<V, E> vv;

  Predicate<V> vertexPredicate;

  Predicate<E> edgePredicate;

  Comparator<V> vertexComparator;

  Comparator<E> edgeComparator;

  Runnable after;

  Set<VisualizationServer.Paintable> paintables = new HashSet<>();

  final JRadioButton animateTransition = new JRadioButton("Animate Transition", true);

  private TreeLayoutSelector(
      VisualizationServer<V, E> vv,
      int initialSelection,
      Predicate<V> vertexPredicate,
      Predicate<E> edgePredicate,
      Comparator<V> vertexComparator,
      Comparator<E> edgeComparator,
      Runnable after) {
    super(new GridLayout(0, 2));
    this.vv = vv;
    this.vertexPredicate = vertexPredicate;
    this.edgePredicate = edgePredicate;
    this.vertexComparator = vertexComparator;
    this.edgeComparator = edgeComparator;
    Preconditions.checkNotNull(after);
    this.after = after;

    TreeLayoutAlgorithm<V> treeLayoutAlgorithm = TreeLayoutAlgorithm.<V>builder().build();

    MultiRowTreeLayoutAlgorithm<V> multiRowTreeLayoutAlgorithm =
        MultiRowTreeLayoutAlgorithm.<V>builder().build();

    int layoutNumber = 0;

    BalloonLayoutAlgorithm<V> balloonLayoutAlgorithm = BalloonLayoutAlgorithm.<V>builder().build();

    RadialTreeLayoutAlgorithm<V> radialTreeLayoutAlgorithm =
        RadialTreeLayoutAlgorithm.<V>builder()
            .horizontalVertexSpacing(100)
            .verticalVertexSpacing(100)
            .expandLayout(false)
            .build();

    RadialEdgeAwareTreeLayoutAlgorithm<V, E> radialEdgeAwareTreeLayoutAlgorithm =
        RadialEdgeAwareTreeLayoutAlgorithm.<V, E>edgeAwareBuilder()
            .horizontalVertexSpacing(100)
            .verticalVertexSpacing(100)
            .edgePredicate(edgePredicate)
            .vertexPredicate(vertexPredicate)
            .expandLayout(false)
            .build();

    EdgeAwareTreeLayoutAlgorithm<V, E> edgeAwareTreeLayoutAlgorithm =
        EdgeAwareTreeLayoutAlgorithm.<V, E>edgeAwareBuilder()
            .edgePredicate(edgePredicate)
            .vertexPredicate(vertexPredicate)
            .build();

    MultiRowEdgeAwareTreeLayoutAlgorithm<V, E> multiRowEdgeAwareTreeLayoutAlgorithm =
        MultiRowEdgeAwareTreeLayoutAlgorithm.<V, E>edgeAwareBuilder()
            .edgePredicate(edgePredicate)
            .vertexPredicate(vertexPredicate)
            .build();

    JRadioButton treeButton = new JRadioButton("Tree");
    treeButton.addItemListener(new LayoutItemListener(treeLayoutAlgorithm, vv));
    treeButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton multiRowTreeButton = new JRadioButton("MultiRowTree");
    multiRowTreeButton.addItemListener(new LayoutItemListener(multiRowTreeLayoutAlgorithm, vv));
    multiRowTreeButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton balloonButton = new JRadioButton("Balloon");
    balloonButton.addItemListener(new LayoutItemListener(balloonLayoutAlgorithm, vv));
    balloonButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton radialButton = new JRadioButton("Radial");
    radialButton.addItemListener(new LayoutItemListener(radialTreeLayoutAlgorithm, vv));
    radialButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton radialEdgeAwareButton = new JRadioButton("Radial Edge aware");
    radialEdgeAwareButton.addItemListener(
        new LayoutItemListener(radialEdgeAwareTreeLayoutAlgorithm, vv));
    radialEdgeAwareButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton edgeAwareTreeButton = new JRadioButton("Edge aware tree");
    edgeAwareTreeButton.addItemListener(new LayoutItemListener(edgeAwareTreeLayoutAlgorithm, vv));
    edgeAwareTreeButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton multiRowEdgeAwareTreeButton = new JRadioButton("MultiRow Edge aware");
    multiRowEdgeAwareTreeButton.addItemListener(
        new LayoutItemListener(multiRowEdgeAwareTreeLayoutAlgorithm, vv));
    multiRowEdgeAwareTreeButton.setSelected(initialSelection == layoutNumber++);

    ButtonGroup layoutRadio = new ButtonGroup();
    layoutRadio.add(treeButton);
    layoutRadio.add(multiRowTreeButton);
    layoutRadio.add(balloonButton);
    layoutRadio.add(radialButton);
    layoutRadio.add(radialEdgeAwareButton);
    layoutRadio.add(edgeAwareTreeButton);
    layoutRadio.add(multiRowEdgeAwareTreeButton);

    this.add(treeButton);
    this.add(edgeAwareTreeButton);
    this.add(multiRowTreeButton);
    this.add(multiRowEdgeAwareTreeButton);
    this.add(radialButton);
    this.add(radialEdgeAwareButton);
    this.add(balloonButton);
    this.add(animateTransition);
  }

  class LayoutItemListener implements ItemListener {

    LayoutAlgorithm layoutAlgorithm;
    VisualizationServer vv;

    LayoutItemListener(LayoutAlgorithm layoutAlgorithm, VisualizationServer vv) {
      this.layoutAlgorithm = layoutAlgorithm;
      this.vv = vv;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        if (animateTransition.isSelected()) {
          LayoutAlgorithmTransition.animate(vv, layoutAlgorithm, after);
        } else {
          LayoutAlgorithmTransition.apply(vv, layoutAlgorithm, after);
        }
        for (Iterator<VisualizationServer.Paintable> iterator = paintables.iterator();
            iterator.hasNext();
            ) {
          VisualizationServer.Paintable paintable = iterator.next();
          vv.removePreRenderPaintable(paintable);
          iterator.remove();
        }
        if (layoutAlgorithm instanceof BalloonLayoutAlgorithm) {
          paintables.add(
              new LayoutPaintable.BalloonRings<>(vv, (BalloonLayoutAlgorithm) layoutAlgorithm));

        } else if (layoutAlgorithm instanceof RadialTreeLayout) {
          paintables.add(new LayoutPaintable.RadialRings<>(vv, (RadialTreeLayout) layoutAlgorithm));

        } else if (layoutAlgorithm instanceof TreeLayout) {
          Map<V, Rectangle> cellMap = ((TreeLayout) layoutAlgorithm).getBaseBounds();
          paintables.add(
              new LayoutPaintable.TreeCells(
                  vv.getVisualizationModel().getLayoutModel(),
                  cellMap,
                  vv.getRenderContext().getMultiLayerTransformer()));
        }
        paintables.add(
            new LayoutPaintable.LayoutBounds(
                vv.getVisualizationModel(), vv.getRenderContext().getMultiLayerTransformer()));
        paintables.forEach(p -> vv.addPreRenderPaintable(p));
      }
      vv.repaint();
    }
  }
}
