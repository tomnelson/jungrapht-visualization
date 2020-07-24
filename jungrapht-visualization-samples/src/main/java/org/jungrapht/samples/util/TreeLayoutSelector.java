package org.jungrapht.samples.util;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.*;
import org.jgrapht.Graph;
import org.jungrapht.visualization.DefaultRenderContext;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.layout.algorithms.BalloonLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.CircleLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.DAGLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.EdgeAwareTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.EiglspergerLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.FRLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.ForceAtlas2LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.GEMLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.HierarchicalMinCrossLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.Layered;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.LayoutAlgorithmTransition;
import org.jungrapht.visualization.layout.algorithms.MultiRowEdgeAwareTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.MultiRowTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.RadialEdgeAwareTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayout;
import org.jungrapht.visualization.layout.algorithms.RadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.SpringLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.SugiyamaLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TidierRadialTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TidierTreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.TreeLayout;
import org.jungrapht.visualization.layout.algorithms.TreeLayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFA2Repulsion;
import org.jungrapht.visualization.layout.algorithms.sugiyama.Layering;
import org.jungrapht.visualization.layout.algorithms.util.LayoutPaintable;
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

  LayoutPaintable.LayoutBounds paintable;

  Set<VisualizationServer.Paintable> paintables = new HashSet<>();

  final JRadioButton animateTransition = new JRadioButton("Animate Transition", true);

  BiFunction<Graph<V, E>, E, Shape> originalEdgeShapeFunction;

  JComboBox<Layering> layeringComboBox =
      new JComboBox<>(
          new Layering[] {
            Layering.TOP_DOWN, Layering.LONGEST_PATH,
            Layering.NETWORK_SIMPLEX, Layering.COFFMAN_GRAHAM
          });

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
    Objects.requireNonNull(after);
    this.after = after;
    this.originalEdgeShapeFunction = vv.getRenderContext().getEdgeShapeFunction();

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

    HierarchicalMinCrossLayoutAlgorithm<V, E> minCrossLayoutAlgorithm =
        HierarchicalMinCrossLayoutAlgorithm.<V, E>edgeAwareBuilder()
            .straightenEdges(true)
            .postStraighten(true)
            .separateComponents(true)
            .layering(Layering.NETWORK_SIMPLEX)
            .after(after)
            .build();

    SugiyamaLayoutAlgorithm<V, E> sugiyamaLayoutAlgorithm =
        SugiyamaLayoutAlgorithm.<V, E>edgeAwareBuilder()
            .straightenEdges(true)
            .postStraighten(true)
            .separateComponents(true)
            .after(after)
            .build();

    EiglspergerLayoutAlgorithm<V, E> eiglspergerLayoutAlgorithm =
        EiglspergerLayoutAlgorithm.<V, E>edgeAwareBuilder()
            .straightenEdges(true)
            .postStraighten(true)
            .separateComponents(true)
            .layering(Layering.COFFMAN_GRAHAM)
            .threaded(true)
            .after(after)
            .build();

    MultiRowTreeLayoutAlgorithm<V> multiRowTreeLayoutAlgorithm =
        MultiRowTreeLayoutAlgorithm.<V>builder().vertexShapeFunction(vertexShapeFunction).build();

    int layoutNumber = 0;

    BalloonLayoutAlgorithm<V> balloonLayoutAlgorithm = new BalloonLayoutAlgorithm<>();

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

    CircleLayoutAlgorithm<V> circleLayoutAlgorithm =
        CircleLayoutAlgorithm.<V>builder().reduceEdgeCrossing(false).build();

    CircleLayoutAlgorithm<V> reduceEdgeCrossingCircleLayoutAlgorithm =
        CircleLayoutAlgorithm.<V>builder().reduceEdgeCrossing(true).build();

    GEMLayoutAlgorithm<V, E> gemLayoutAlgorithm = new GEMLayoutAlgorithm<>();

    DAGLayoutAlgorithm<V, E> dagLayoutAlgorithm = new DAGLayoutAlgorithm<>();

    FRLayoutAlgorithm<V> frLayoutAlgorithm = new FRLayoutAlgorithm<>();

    ForceAtlas2LayoutAlgorithm<V> forceAtlas2LayoutAlgorithm =
        ForceAtlas2LayoutAlgorithm.<V>builder()
            .repulsionContractBuilder(BarnesHutFA2Repulsion.builder())
            .build();

    SpringLayoutAlgorithm<V, E> springLayoutAlgorithm = new SpringLayoutAlgorithm<>();

    JRadioButton treeButton = new JRadioButton("Tree");
    treeButton.addItemListener(new LayoutItemListener(treeLayoutAlgorithm, vv));
    treeButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton tidierTreeButton = new JRadioButton("Tidier Tree");
    tidierTreeButton.addItemListener(new LayoutItemListener(tidierTreeLayoutAlgorithm, vv));
    tidierTreeButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton minCrossButton = new JRadioButton("MinCross");
    minCrossButton.addItemListener(new LayoutItemListener(minCrossLayoutAlgorithm, vv));
    minCrossButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton sugiyamaButton = new JRadioButton("Sugiyama");
    sugiyamaButton.addItemListener(new LayoutItemListener(sugiyamaLayoutAlgorithm, vv));
    sugiyamaButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton eiglspergerButton = new JRadioButton("Eiglsperger");
    eiglspergerButton.addItemListener(new LayoutItemListener(eiglspergerLayoutAlgorithm, vv));
    eiglspergerButton.setSelected(initialSelection == layoutNumber++);

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

    JRadioButton tidierRadialEdgeAwareButton = new JRadioButton("Tidier Radial Tree");
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

    JRadioButton circleButton = new JRadioButton("Circle");
    circleButton.addItemListener(new LayoutItemListener(circleLayoutAlgorithm, vv));
    circleButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton betterCircleButton = new JRadioButton("Reduced-Xing Circle");
    betterCircleButton.addItemListener(
        new LayoutItemListener(reduceEdgeCrossingCircleLayoutAlgorithm, vv));
    betterCircleButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton gemButton = new JRadioButton("GEM");
    gemButton.addItemListener(new LayoutItemListener(gemLayoutAlgorithm, vv));
    gemButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton dagButton = new JRadioButton("DAG");
    dagButton.addItemListener(new LayoutItemListener(dagLayoutAlgorithm, vv));
    dagButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton frButton = new JRadioButton("FR");
    frButton.addItemListener(new LayoutItemListener(frLayoutAlgorithm, vv));
    frButton.setSelected(initialSelection == layoutNumber++);

    JRadioButton fa2Button = new JRadioButton("FA2");
    fa2Button.addItemListener(new LayoutItemListener(forceAtlas2LayoutAlgorithm, vv));
    fa2Button.setSelected(initialSelection == layoutNumber++);

    JRadioButton springButton = new JRadioButton("Spring");
    springButton.addItemListener(new LayoutItemListener(springLayoutAlgorithm, vv));
    springButton.setSelected(initialSelection == layoutNumber++);

    ButtonGroup layoutRadio = new ButtonGroup();
    layoutRadio.add(treeButton);
    layoutRadio.add(tidierTreeButton);
    layoutRadio.add(minCrossButton);
    layoutRadio.add(sugiyamaButton);
    layoutRadio.add(eiglspergerButton);
    layoutRadio.add(multiRowTreeButton);
    layoutRadio.add(gemButton);
    layoutRadio.add(dagButton);
    layoutRadio.add(balloonButton);
    layoutRadio.add(radialButton);
    layoutRadio.add(radialEdgeAwareButton);
    layoutRadio.add(tidierRadialEdgeAwareButton);
    layoutRadio.add(edgeAwareTreeButton);
    layoutRadio.add(multiRowEdgeAwareTreeButton);
    layoutRadio.add(circleButton);
    layoutRadio.add(betterCircleButton);
    layoutRadio.add(frButton);
    layoutRadio.add(fa2Button);
    layoutRadio.add(springButton);

    layeringComboBox.addItemListener(
        item -> {
          if (item.getStateChange() == ItemEvent.SELECTED) {
            LayoutAlgorithm<V> layoutAlgotithm = vv.getVisualizationModel().getLayoutAlgorithm();
            if (layoutAlgotithm instanceof Layered) {
              ((Layered) layoutAlgotithm).setLayering((Layering) item.getItem());
              vv.getVisualizationModel().setLayoutAlgorithm(layoutAlgotithm);
            }
          }
        });
    layeringComboBox.setToolTipText(
        "Layering Algorithm for Sugiyama, Eiglsperger, and HierarchicalMinCross");

    this.add(treeButton);
    this.add(edgeAwareTreeButton);
    this.add(multiRowTreeButton);
    this.add(multiRowEdgeAwareTreeButton);
    this.add(radialButton);
    this.add(radialEdgeAwareButton);
    this.add(balloonButton);
    this.add(tidierTreeButton);
    this.add(minCrossButton);
    this.add(sugiyamaButton);
    this.add(eiglspergerButton);
    this.add(tidierRadialEdgeAwareButton);
    this.add(gemButton);
    this.add(dagButton);
    this.add(circleButton);
    this.add(betterCircleButton);
    this.add(frButton);
    this.add(fa2Button);
    this.add(springButton);
    this.add(animateTransition);
    this.add(layeringComboBox);
  }

  /**
   * manages the transition between {@link LayoutAlgorithm}s and the {@code Paintable} decorations
   * for {@link RadialTreeLayout} and {@link BalloonLayoutAlgorithm}
   */
  class LayoutItemListener implements ItemListener {

    LayoutAlgorithm layoutAlgorithm;
    VisualizationServer<V, E> vv;

    LayoutItemListener(LayoutAlgorithm layoutAlgorithm, VisualizationServer vv) {
      this.layoutAlgorithm = layoutAlgorithm;
      this.vv = vv;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        //        if (!(layoutAlgorithm instanceof EdgeShapeFunctionSupplier)) {
        //          vv.getRenderContext().setEdgeShapeFunction(originalEdgeShapeFunction);
        //        }
        if (layoutAlgorithm instanceof Layered) {
          ((Layered) layoutAlgorithm)
              .setLayering(layeringComboBox.getItemAt(layeringComboBox.getSelectedIndex()));
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
          if (log.isTraceEnabled()) {
            Map<V, Rectangle> cellMap = ((TreeLayout) layoutAlgorithm).getBaseBounds();
            paintables.add(
                new LayoutPaintable.TreeCells(
                    vv.getVisualizationModel().getLayoutModel(),
                    cellMap,
                    vv.getRenderContext().getMultiLayerTransformer()));
          }
        }
        if (paintable != null) {
          vv.removePreRenderPaintable(paintable);
        }
        paintable = new LayoutPaintable.LayoutBounds(vv);
        vv.addPreRenderPaintable(paintable);
        if (log.isTraceEnabled()) {
          paintables.add(
              new LayoutPaintable.LayoutBounds(
                  vv.getVisualizationModel(),
                  vv.getRenderContext().getMultiLayerTransformer(),
                  Color.pink));
        }
        paintables.forEach(p -> vv.addPreRenderPaintable(p));
      }
      vv.repaint();
    }
  }
}
