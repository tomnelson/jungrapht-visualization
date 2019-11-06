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
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.visualization.DefaultRenderContext;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.algorithms.*;
import org.jungrapht.visualization.layout.algorithms.sugiyama.RenderContextAware;
import org.jungrapht.visualization.layout.algorithms.util.LayoutPaintable;
import org.jungrapht.visualization.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * convenience tool to configure and supply {@link TreeLayout} instances
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class TreeLayoutSelector<V, E> extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(TreeLayoutSelector.class);

  /**
   * Builder for {@code TreeLayoutSelector}
   *
   * @param <V> vertex type
   * @param <E> edge type
   */
  public static class Builder<V, E> {
    VisualizationServer<V, E> visualizationServer;
    int intialialSelection;
    Function<V, Shape> vertexShapeFunction = new DefaultRenderContext.ShapeFunctionSupplier().get();
    Runnable after = () -> {};
    Predicate<E> edgePredicate = e -> false;
    Comparator<E> edgeComparator = (e1, e2) -> 0;
    Predicate<V> vertexPredicate = e -> false;
    Comparator<V> vertexComparator = (e1, e2) -> 0;
    boolean alignFavoredEdges = true;

    /** @param visualizationServer required {@link VisualizationServer} */
    Builder(VisualizationServer<V, E> visualizationServer) {
      this.visualizationServer = visualizationServer;
    }

    public Builder vertexShapeFunction(Function<V, Shape> vertexShapeFunction) {
      this.vertexShapeFunction = vertexShapeFunction;
      return this;
    }
    /**
     * @param intialialSelection initial selection for {@code TreeLayoutSelector}
     * @return this Builder
     */
    public Builder initialSelection(int intialialSelection) {
      this.intialialSelection = intialialSelection;
      return this;
    }

    /**
     * @param edgeComparator {@link Comparator} to sort edges
     * @return this Builder
     */
    public Builder edgeComparator(Comparator<E> edgeComparator) {
      this.edgeComparator = edgeComparator;
      return this;
    }

    /**
     * @param vertexComparator {@link Comparator} to sort vertices
     * @return this Builder
     */
    public Builder vertexComparator(Comparator<V> vertexComparator) {
      this.vertexComparator = vertexComparator;
      return this;
    }

    /**
     * @param edgePredicate {@link Predicate} to identify preferred edges
     * @return this Builder
     */
    public Builder edgePredicate(Predicate<E> edgePredicate) {
      this.edgePredicate = edgePredicate;
      return this;
    }

    /**
     * @param vertexPredicate {@link Predicate} to identify preferred vertices
     * @return this Builder
     */
    public Builder vertexPredicate(Predicate<V> vertexPredicate) {
      this.vertexPredicate = vertexPredicate;
      return this;
    }

    /**
     * @param after {@link Runnable} to execute after layout change
     * @return this Builder
     */
    public Builder after(Runnable after) {
      this.after = after;
      return this;
    }

    public Builder alignFavoredEdges(boolean alignFavoredEdges) {
      this.alignFavoredEdges = alignFavoredEdges;
      return this;
    }

    /** @return a configured {@link TreeLayoutSelector} */
    public TreeLayoutSelector<V, E> build() {
      return new TreeLayoutSelector<>(this);
    }
  }

  /**
   * @param visualizationServer required {@link VisualizationServer}
   * @param <V> vertex type
   * @param <E> edge type
   * @return a Builder to configure
   */
  public static <V, E> Builder<V, E> builder(VisualizationServer visualizationServer) {
    return new Builder<>(visualizationServer);
  }

  /**
   * create an instance with a {@code Builder}
   *
   * @param builder {@code Builder} with configurations
   */
  private TreeLayoutSelector(Builder<V, E> builder) {
    this(
        builder.visualizationServer,
        builder.vertexShapeFunction,
        builder.intialialSelection,
        builder.vertexPredicate,
        builder.edgePredicate,
        builder.vertexComparator,
        builder.edgeComparator,
        builder.alignFavoredEdges,
        builder.after);
  }

  VisualizationServer<V, E> vv;

  Predicate<V> vertexPredicate;

  Predicate<E> edgePredicate;

  Comparator<V> vertexComparator;

  Comparator<E> edgeComparator;

  Function<V, Shape> vertexShapeFunction;

  boolean alignFavoredEdgess;

  Runnable after;

  Set<VisualizationServer.Paintable> paintables = new HashSet<>();

  final JRadioButton animateTransition = new JRadioButton("Animate Transition", true);

  private TreeLayoutSelector(
      VisualizationServer<V, E> vv,
      Function<V, Shape> vertexShapeFunction,
      int initialSelection,
      Predicate<V> vertexPredicate,
      Predicate<E> edgePredicate,
      Comparator<V> vertexComparator,
      Comparator<E> edgeComparator,
      boolean alignFavoredEdges,
      Runnable after) {
    super(new GridLayout(0, 2));
    this.vv = vv;
    this.vertexShapeFunction = vertexShapeFunction;
    this.vertexPredicate = vertexPredicate;
    this.edgePredicate = edgePredicate;
    this.vertexComparator = vertexComparator;
    this.edgeComparator = edgeComparator;
    this.alignFavoredEdgess = alignFavoredEdges;
    Preconditions.checkNotNull(after);
    this.after = after;

    TreeLayoutAlgorithm<V> treeLayoutAlgorithm =
        TreeLayoutAlgorithm.<V>builder().vertexShapeFunction(vertexShapeFunction).build();

    TidierTreeLayoutAlgorithm<V, E> tidierTreeLayoutAlgorithm =
        TidierTreeLayoutAlgorithm.<V, E>edgeAwareBuilder()
            .edgeComparator(edgeComparator)
            .edgePredicate(edgePredicate)
            .vertexComparator(vertexComparator)
            .vertexPredicate(vertexPredicate)
            .vertexShapeFunction(vertexShapeFunction)
            .build();

    SugiyamaLayoutAlgorithm<V, E> sugiyamaLayoutAlgorithm =
        SugiyamaLayoutAlgorithm.<V, E>edgeAwareBuilder().after(after).build();

    MultiRowTreeLayoutAlgorithm<V> multiRowTreeLayoutAlgorithm =
        MultiRowTreeLayoutAlgorithm.<V>builder().vertexShapeFunction(vertexShapeFunction).build();

    int layoutNumber = 0;

    BalloonLayoutAlgorithm<V> balloonLayoutAlgorithm = BalloonLayoutAlgorithm.<V>builder().build();

    RadialTreeLayoutAlgorithm<V> radialTreeLayoutAlgorithm =
        RadialTreeLayoutAlgorithm.<V>builder()
            .horizontalVertexSpacing(100)
            .verticalVertexSpacing(100)
            .vertexShapeFunction(vertexShapeFunction)
            .expandLayout(false)
            .build();

    RadialEdgeAwareTreeLayoutAlgorithm<V, E> radialEdgeAwareTreeLayoutAlgorithm =
        RadialEdgeAwareTreeLayoutAlgorithm.<V, E>edgeAwareBuilder()
            .horizontalVertexSpacing(100)
            .verticalVertexSpacing(100)
            .vertexShapeFunction(vertexShapeFunction)
            .edgePredicate(edgePredicate)
            .vertexPredicate(vertexPredicate)
            .expandLayout(false)
            .build();

    TidierRadialTreeLayoutAlgorithm<V, E> tidierRadialTreeLayoutAlgorithm =
        TidierRadialTreeLayoutAlgorithm.<V, E>edgeAwareBuilder()
            .edgeComparator(edgeComparator)
            .edgePredicate(edgePredicate)
            .vertexComparator(vertexComparator)
            .vertexPredicate(vertexPredicate)
            .vertexShapeFunction(vertexShapeFunction)
            .build();

    EdgeAwareTreeLayoutAlgorithm<V, E> edgeAwareTreeLayoutAlgorithm =
        EdgeAwareTreeLayoutAlgorithm.<V, E>edgeAwareBuilder()
            .vertexShapeFunction(vertexShapeFunction)
            .edgePredicate(edgePredicate)
            .vertexPredicate(vertexPredicate)
            .alignFavoredEdges(alignFavoredEdges)
            .build();

    MultiRowEdgeAwareTreeLayoutAlgorithm<V, E> multiRowEdgeAwareTreeLayoutAlgorithm =
        MultiRowEdgeAwareTreeLayoutAlgorithm.<V, E>edgeAwareBuilder()
            .vertexShapeFunction(vertexShapeFunction)
            .edgePredicate(edgePredicate)
            .vertexPredicate(vertexPredicate)
            .alignFavoredEdges(alignFavoredEdges)
            .build();

    JRadioButton treeButton = new JRadioButton("Tree");
    treeButton.addItemListener(new LayoutItemListener(treeLayoutAlgorithm, vv));
    treeButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton compactTreeButton = new JRadioButton("Compact Tree");
    compactTreeButton.addItemListener(new LayoutItemListener(tidierTreeLayoutAlgorithm, vv));
    compactTreeButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton mySugiyamaButton = new JRadioButton("Sugiyama");
    mySugiyamaButton.addItemListener(new LayoutItemListener(sugiyamaLayoutAlgorithm, vv));
    mySugiyamaButton.setSelected(initialSelection == layoutNumber++);

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

    JRadioButton tidierRadialEdgeAwareButton = new JRadioButton("Tidier Radial Edge aware");
    tidierRadialEdgeAwareButton.addItemListener(
        new LayoutItemListener(tidierRadialTreeLayoutAlgorithm, vv));
    tidierRadialEdgeAwareButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton edgeAwareTreeButton = new JRadioButton("Edge aware tree");
    edgeAwareTreeButton.addItemListener(new LayoutItemListener(edgeAwareTreeLayoutAlgorithm, vv));
    edgeAwareTreeButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton multiRowEdgeAwareTreeButton = new JRadioButton("MultiRow Edge aware");
    multiRowEdgeAwareTreeButton.addItemListener(
        new LayoutItemListener(multiRowEdgeAwareTreeLayoutAlgorithm, vv));
    multiRowEdgeAwareTreeButton.setSelected(initialSelection == layoutNumber++);

    ButtonGroup layoutRadio = new ButtonGroup();
    layoutRadio.add(treeButton);
    layoutRadio.add(compactTreeButton);
    layoutRadio.add(mySugiyamaButton);
    layoutRadio.add(multiRowTreeButton);
    layoutRadio.add(balloonButton);
    layoutRadio.add(radialButton);
    layoutRadio.add(radialEdgeAwareButton);
    layoutRadio.add(tidierRadialEdgeAwareButton);
    layoutRadio.add(edgeAwareTreeButton);
    layoutRadio.add(multiRowEdgeAwareTreeButton);

    this.add(treeButton);
    this.add(edgeAwareTreeButton);
    this.add(multiRowTreeButton);
    this.add(multiRowEdgeAwareTreeButton);
    this.add(radialButton);
    this.add(radialEdgeAwareButton);
    this.add(balloonButton);
    this.add(compactTreeButton);
    this.add(mySugiyamaButton);
    this.add(tidierRadialEdgeAwareButton);
    this.add(animateTransition);
  }

  /**
   * manages the transition between {@link LayoutAlgorithm}s and the {@code Paintable} decorations
   * for {@link RadialTreeLayout} and {@link BalloonLayoutAlgorithm}
   */
  class LayoutItemListener implements ItemListener {

    LayoutAlgorithm layoutAlgorithm;
    VisualizationServer vv;
    Function<Context<Graph<V, E>, E>, Shape> originalEdgeShapeFunction;

    LayoutItemListener(LayoutAlgorithm layoutAlgorithm, VisualizationServer vv) {
      this.layoutAlgorithm = layoutAlgorithm;
      this.vv = vv;
      this.originalEdgeShapeFunction = vv.getRenderContext().getEdgeShapeFunction();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        if (layoutAlgorithm instanceof RenderContextAware) {
          ((RenderContextAware) layoutAlgorithm).setRenderContext(vv.getRenderContext());
        } else {
          vv.getRenderContext().setEdgeShapeFunction(originalEdgeShapeFunction);
        }
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
                vv.getVisualizationModel(),
                vv.getRenderContext().getMultiLayerTransformer(),
                Color.pink));
        paintables.forEach(p -> vv.addPreRenderPaintable(p));
      }
      vv.repaint();
    }
  }
}
