package org.jungrapht.visualization.layout.algorithms.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Stack;
import java.util.function.Function;
import org.jgrapht.Graph;
import org.jungrapht.visualization.layout.algorithms.sugiyama.GraphLayers;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LE;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The NetworkSimplex algorithm
 *
 * <p>This class leverages a modified version of the NetworkSimplex class in
 *
 * <p>Microsoft Automatic Graph Layout,MSAGL
 *
 * <p>which is licensed as follows:
 *
 * <p>Copyright (c) Microsoft Corporation
 *
 * <p>All rights reserved.
 *
 * <p>MIT License
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the ""Software""), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * <p>This file is re-licensed under the compatible BSD license.
 *
 * @see "A Technique for Drawing Directed Graphs. Emden R. Gansner, Eleftherios Koutsofios, Stephen
 *     C. North, and Gem-Phong Vo"
 * @param <V> vertex type
 * @param <E> edge type
 */
public class NetworkSimplex<V, E> {

  private static final Logger log = LoggerFactory.getLogger(NetworkSimplex.class);

  private static final Random random = new Random();

  public static class Builder<V, E, T extends NetworkSimplex<V, E>, B extends Builder<V, E, T, B>> {
    protected Graph<LV<V>, LE<V, E>> svGraph;
    protected Function<LE<V, E>, Integer> weightFunction = e -> 1;
    protected Function<LE<V, E>, Integer> separationFunction = e -> 1;

    protected Builder(Graph<LV<V>, LE<V, E>> svGraph) {
      this.svGraph = svGraph;
    }

    /** @return this builder cast to type B */
    protected B self() {
      return (B) this;
    }

    /**
     * @param weightFunction
     * @return this Builder
     */
    public B weightFunction(Function<LE<V, E>, Integer> weightFunction) {
      this.weightFunction = weightFunction;
      return self();
    }

    /**
     * @param separationFunction
     * @return this Builder
     */
    public B separationFunction(Function<LE<V, E>, Integer> separationFunction) {
      this.separationFunction = separationFunction;
      return self();
    }

    /** @return the Builder with its set parameters */
    public T build() {
      return (T) new NetworkSimplex<>(this);
    }
  }

  /**
   * @param <V> the vertex type
   * @return a {@code Builder} ready to configure
   */
  public static <V, E> Builder<V, E, ?, ?> builder(Graph<LV<V>, LE<V, E>> svGraph) {
    return new Builder<>(svGraph);
  }

  private static class Incidence<V, E> {
    final LV<V> v;
    final Iterator<LE<V, E>> outEdges;
    final Iterator<LE<V, E>> inEdges;

    static <V, E> Incidence<V, E> of(
        LV<V> v, Iterator<LE<V, E>> outEdges, Iterator<LE<V, E>> inEdges) {
      return new Incidence<>(v, outEdges, inEdges);
    }

    private Incidence(LV<V> v, Iterator<LE<V, E>> outEdges, Iterator<LE<V, E>> inEdges) {
      this.v = v;
      this.outEdges = outEdges;
      this.inEdges = inEdges;
    }
  }

  protected List<LV<V>> leaves = new ArrayList<>();
  protected Graph<LV<V>, LE<V, E>> svGraph;
  protected Function<LE<V, E>, Integer> weightFunction;
  protected Function<LE<V, E>, Integer> separationFunction;
  protected Map<LV<V>, Integer> layers = new HashMap<>();
  protected Map<LE<V, E>, Integer> cutValues = new HashMap<>();
  protected Map<LE<V, E>, Integer> cutMap = new HashMap<>();
  protected List<List<LV<V>>> layerList;
  protected Map<LV<V>, Integer> lim = new HashMap<>();
  protected Map<LV<V>, Integer> low = new HashMap<>();
  protected Map<LV<V>, LE<V, E>> parent = new HashMap<>();
  protected List<LV<V>> treeVertices = new ArrayList<>();
  protected Map<LV<V>, Boolean> vertexInTreeMap = new HashMap<>();
  protected Map<LE<V, E>, Boolean> edgeInTreeMap = new HashMap<>();

  protected NetworkSimplex(Builder<V, E, ?, ?> builder) {
    this.svGraph = builder.svGraph;
    this.weightFunction = builder.weightFunction;
    this.separationFunction = builder.separationFunction;
  }

  public void run() {
    if (svGraph.edgeSet().size() == 0 && svGraph.vertexSet().size() == 0) layers = new HashMap<>();

    svGraph.edgeSet().forEach(e -> cutMap.put(e, Integer.MAX_VALUE));

    feasibleTree();

    Pair<LE<V, E>> leaveEnter;
    while ((leaveEnter = getLeaveEnterEdge()) != null) {
      exchange(leaveEnter.first, leaveEnter.second);
    }

    shiftLayerToZero();
    //    Collections.reverse(layerList);

    for (int i = 0; i < layerList.size(); i++) {
      List<LV<V>> layer = layerList.get(i);
      for (int j = 0; j < layer.size(); j++) {
        LV<V> v = layer.get(j);
        v.setRank(i);
        v.setIndex(j);
      }
    }
    if (log.isTraceEnabled()) {
      log.trace("layersArray are {}", layerList);

      log.trace("layers are {}", layers);

      log.trace("vinTreeMap: {}", vertexInTreeMap);
      log.trace("einTreeMap: {}", edgeInTreeMap);
      for (Map.Entry<LE<V, E>, Boolean> entry : edgeInTreeMap.entrySet()) {
        log.trace("{}", entry);
      }
    }
  }

  private void feasibleTree() {
    layerList = GraphLayers.longestPathReverse(svGraph);
    svGraph.vertexSet().forEach(v -> layers.put(v, v.getRank()));

    while (tightTree() < this.svGraph.vertexSet().size()) {

      LE<V, E> e = getNonTreeEdgeIncidentToTheTreeWithMinimalAmountOfSlack();
      if (e == null) break; //all edges are tree edges
      int slack = slack(e);
      if (slack == 0) throw new IllegalArgumentException(); //"the tree should be tight");

      if (vertexInTreeMap.get(e.getSource())) slack = -slack;

      //shift the tree rigidly up or down and make e tight ; since the slack is the minimum of slacks
      //the layering will still remain feasible
      for (LV<V> v : treeVertices) {
        int newRank = layers.get(v) + slack;
        v.setRank(newRank);
        layers.put(v, newRank);
      }
    }
    initCutValues();
  }

  private int tightTree() {
    treeVertices.clear();
    for (LE<V, E> ie : svGraph.edgeSet()) {
      edgeInTreeMap.put(ie, false);
    }
    for (LV<V> v : svGraph.vertexSet()) {
      vertexInTreeMap.put(v, false);
    }
    // first vertex
    LV<V> v0 = svGraph.vertexSet().stream().findFirst().get();
    vertexInTreeMap.put(v0, true);
    treeVertices.add(v0);

    Stack<LV<V>> queue = new Stack<>();
    queue.push(v0);

    while (queue.size() > 0) {
      LV<V> v = queue.pop();

      for (LE<V, E> e : svGraph.outgoingEdgesOf(v)) {
        if (vertexInTreeMap.get(e.getTarget())) {
          continue;
        }
        if (e.getSource().getRank() - e.getTarget().getRank() == separationFunction.apply(e)) {
          queue.push(e.getTarget());
          vertexInTreeMap.put(e.getTarget(), true);
          treeVertices.add(e.getTarget());
          edgeInTreeMap.put(e, true);
        }
      }

      for (LE<V, E> e : svGraph.incomingEdgesOf(v)) {
        if (vertexInTreeMap.get(e.getSource())) {
          continue;
        }
        if (e.getSource().getRank() - e.getTarget().getRank() == separationFunction.apply(e)) {
          queue.push(e.getSource());
          vertexInTreeMap.put(e.getSource(), true);
          treeVertices.add(e.getSource());
          edgeInTreeMap.put(e, true);
        }
      }
    }
    return treeVertices.size();
  }

  public List<List<LV<V>>> getLayerList() {
    return layerList;
  }

  private int slack(LE<V, E> edge) {
    return edge.getSource().getRank() - edge.getTarget().getRank() - separationFunction.apply(edge);
  }

  public List<LV<V>> getTreeVertices() {
    return treeVertices;
  }

  public Map<LV<V>, Boolean> getVertexInTreeMap() {
    return vertexInTreeMap;
  }

  public Map<LE<V, E>, Boolean> getEdgeInTreeMap() {
    return edgeInTreeMap;
  }

  private Pair<LE<V, E>> getLeaveEnterEdge() {
    LE<V, E> leavingEdge = null;
    LE<V, E> enteringEdge = null;
    int minCut = 0;
    for (LE<V, E> e : svGraph.edgeSet()) {
      if (edgeInTreeMap.get(e)) {
        if (cutValues.getOrDefault(e, 0) < minCut) {
          minCut = cutValues.get(e);
          leavingEdge = e;
        }
      }
    }
    if (leavingEdge == null) return null;

    //now we are looking for a non-tree edge with a minimal slack belonging to TS
    //    boolean continuation = false;
    int minSlack = Integer.MAX_VALUE;
    for (LE<V, E> f : svGraph.edgeSet()) {
      int slack = slack(f);
      boolean continuation = random.nextInt(2) == 1;
      if (!edgeInTreeMap.get(f)
          && edgeSourceTargetVal(f, leavingEdge) == -1
          && (slack < minSlack || (slack == minSlack && continuation))) {
        minSlack = slack;
        enteringEdge = f;
        if (minSlack == 0 && !continuation) break;
        //        continuation = false;
      }
    }

    if (enteringEdge == null) {
      throw new RuntimeException();
    }
    return Pair.of(leavingEdge, enteringEdge);
  }

  private void initLimLowAndParent() {
    svGraph
        .vertexSet()
        .forEach(
            v -> {
              lim.put(v, 0);
              low.put(v, 0);
              parent.put(v, null);
            });

    int currentLimit = 1;
    LV<V> v = svGraph.vertexSet().stream().findFirst().get();
    initLimLowParentAndLeavesOnSubtree(currentLimit, v);
  }

  private void initCutValues() {
    initLimLowAndParent();

    //going up from the leaves following parents
    Stack<LV<V>> front = new Stack<>();
    for (LV<V> leaf : leaves) {
      front.push(leaf);
    }
    Stack<LV<V>> newFront = new Stack<>();
    while (front.size() > 0) {
      while (front.size() > 0) {
        LV<V> w = front.pop();
        LE<V, E> cutEdge = parent.get(w); //have to find the cut of e
        if (cutEdge == null) {
          continue;
        }
        int cut = 0;
        for (LE<V, E> e : svGraph.edgesOf(w)) {

          if (!edgeInTreeMap.get(e)) {
            int e0Val = edgeSourceTargetVal(e, cutEdge);
            if (e0Val != 0) {
              cut += e0Val * weightFunction.apply(e);
            }
          } else {
            //e0 is a tree edge
            if (e == cutEdge) {
              cut += weightFunction.apply(e);
            } else {
              int impact =
                  cutEdge.getSource() == e.getTarget() || cutEdge.getTarget() == e.getSource()
                      ? 1
                      : -1;
              int edgeContribution = edgeContribution(e, w);
              cut += edgeContribution * impact;
            }
          }
        }

        cutMap.put(cutEdge, cut);
        LV<V> v = cutEdge.getSource() == w ? cutEdge.getTarget() : cutEdge.getSource();
        if (allLowCutsHaveBeenDone(v)) {
          newFront.push(v);
        }
      }
      //swap newFrontAndFront
      Stack<LV<V>> t = front;
      front = newFront;
      newFront = t;
    }
  }

  private void initLimLowParentAndLeavesOnSubtree(int curLim, LV<V> v) {
    Stack<Incidence<V, E>> stack = new Stack<>();
    Iterator<LE<V, E>> outEdges = svGraph.outgoingEdgesOf(v).iterator();
    Iterator<LE<V, E>> inEdges = svGraph.incomingEdgesOf(v).iterator();
    stack.push(Incidence.of(v, outEdges, inEdges));
    low.put(v, curLim);

    while (stack.size() > 0) {
      Incidence<V, E> ss = stack.pop();
      v = ss.v;
      outEdges = ss.outEdges;
      inEdges = ss.inEdges;

      boolean done;
      do {
        done = true;
        while (outEdges.hasNext()) {
          LE<V, E> e = outEdges.next();
          if (!edgeInTreeMap.get(e) || low.get(e.getTarget()) > 0) {
            continue;
          }
          stack.push(Incidence.of(v, outEdges, inEdges));
          v = e.getTarget();
          parent.put(v, e);
          low.put(v, curLim);
          outEdges = svGraph.outgoingEdgesOf(v).iterator();
          inEdges = svGraph.incomingEdgesOf(v).iterator();
        }
        while (inEdges.hasNext()) {
          LE<V, E> e = inEdges.next();
          if (!edgeInTreeMap.get(e) || low.get(e.getSource()) > 0) {
            continue;
          }
          stack.push(Incidence.of(v, outEdges, inEdges));
          v = e.getSource();
          low.put(v, curLim);
          parent.put(v, e);
          outEdges = svGraph.outgoingEdgesOf(v).iterator();
          inEdges = svGraph.incomingEdgesOf(v).iterator();
          done = false;
          break;
        }
      } while (!done);

      lim.put(v, curLim++);
      if (Objects.equals(lim.get(v), low.get(v))) {
        leaves.add(v);
      }
    }
  }

  private void updateLayersUnderNode(LV<V> l) {
    Stack<LV<V>> front = new Stack<>();
    front.push(l);

    for (LV<V> v : svGraph.vertexSet()) {
      if (low.get(l) <= lim.get(v) && lim.get(v) <= lim.get(l) && v != l) {
        v.setRank(Integer.MAX_VALUE);
      }
    }

    while (front.size() > 0) {
      LV<V> u = front.pop();
      for (LE<V, E> oe : svGraph.outgoingEdgesOf(u)) {
        if (edgeInTreeMap.get(oe) && oe.getTarget().getRank() == Integer.MAX_VALUE) {
          oe.getTarget().setRank(u.getRank() - separationFunction.apply(oe));
          front.push(oe.getTarget());
        }
      }
      for (LE<V, E> ie : svGraph.incomingEdgesOf(u)) {
        if (edgeInTreeMap.get(ie) && ie.getSource().getRank() == Integer.MAX_VALUE) {
          ie.getSource().setRank(u.getRank() + 1);
          front.push(ie.getSource());
        }
      }
    }
  }

  private void updateCuts(LE<V, E> e) {
    Stack<LV<V>> front = new Stack<>();
    Stack<LV<V>> newFront = new Stack<>();

    front.push(e.getSource());
    front.push(e.getTarget());

    while (front.size() > 0) {
      while (front.size() > 0) {
        LV<V> w = front.pop();
        LE<V, E> cutEdge = parent.get(w);
        if (cutEdge == null) {
          continue;
        }
        if (cutMap.get(cutEdge) != Integer.MAX_VALUE) {
          continue;
        }
        int cut = 0;
        for (LE<V, E> ce : svGraph.edgesOf(w)) {
          if (!edgeInTreeMap.get(ce)) {
            int e0val = edgeSourceTargetVal(ce, cutEdge);
            if (e0val != 0) {
              cut += e0val * weightFunction.apply(ce);
            }
          } else {
            if (ce == cutEdge) {
              cut += weightFunction.apply(ce);
            } else {
              int impact =
                  cutEdge.getSource() == ce.getTarget() || cutEdge.getTarget() == ce.getSource()
                      ? 1
                      : -1;
              int edgeContribution = edgeContribution(ce, w);
              cut += edgeContribution * impact;
            }
          }
        }
        cutMap.put(cutEdge, cut);
        LV<V> u = cutEdge.getSource() == w ? cutEdge.getTarget() : cutEdge.getSource();
        if (allLowCutsHaveBeenDone(u)) {
          newFront.push(u);
        }
      }
      Stack<LV<V>> t = front;
      front = newFront;
      newFront = t;
    }
  }

  private boolean allLowCutsHaveBeenDone(LV<V> v) {
    for (LE<V, E> ie : svGraph.edgesOf(v))
      if (edgeInTreeMap.get(ie)
          && cutMap.getOrDefault(ie, 0) == Integer.MAX_VALUE
          && ie != parent.get(v)) return false;
    return true;
  }

  private int edgeSourceTargetVal(LE<V, E> e, LE<V, E> treeEdge) {

    if (edgeInTreeMap.get(e) || !edgeInTreeMap.get(treeEdge)) {
      throw new RuntimeException("Wrong parameters");
    }
    return vertexSourceTargetVal(e.getSource(), treeEdge)
        - vertexSourceTargetVal(e.getTarget(), treeEdge);
  }

  private int vertexSourceTargetVal(LV<V> v, LE<V, E> treeEdge) {

    if (!edgeInTreeMap.get(treeEdge)) {
      throw new RuntimeException("wrong params for VertexSourceTargetVal");
    }

    LV<V> s = treeEdge.getSource();
    LV<V> t = treeEdge.getTarget();
    if (lim.get(s) > lim.get(t)) { //s belongs to the tree root component
      if (lim.get(v) <= lim.get(t) && low.get(t) <= lim.get(v)) {
        return 0;
      } else {
        return 1;
      }
    } else { //t belongs to the tree root component
      if (lim.get(v) <= lim.get(s) && low.get(s) <= lim.get(v)) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  private LE<V, E> getNonTreeEdgeIncidentToTheTreeWithMinimalAmountOfSlack() {
    LE<V, E> edge = null;
    int minSlack = Integer.MAX_VALUE;

    for (LV<V> v : this.treeVertices) {
      for (LE<V, E> e : this.svGraph.outgoingEdgesOf(v)) {
        if (vertexInTreeMap.get(e.getSource()) && vertexInTreeMap.get(e.getTarget())) continue;
        int slack = slack(e);
        if (slack < minSlack) {
          edge = e;
          minSlack = slack;
          if (slack == 1) return e;
        }
      }

      for (LE<V, E> e : this.svGraph.incomingEdgesOf(v)) {
        if (vertexInTreeMap.get(e.getSource()) && vertexInTreeMap.get(e.getTarget())) continue;

        int slack = slack(e);
        if (slack < minSlack) {
          edge = e;
          minSlack = slack;
          if (slack == 1) return e;
        }
      }
    }
    return edge;
  }

  private int edgeContribution(LE<V, E> e, LV<V> w) {
    int edgeContribution = cutMap.get(e) - weightFunction.apply(e);
    for (LE<V, E> ie : svGraph.edgesOf(w)) {
      if (!edgeInTreeMap.get(ie)) {
        int sign = edgeSourceTargetVal(ie, e);
        if (sign == -1) {
          edgeContribution += weightFunction.apply(ie);
        } else if (sign == 1) {
          edgeContribution -= weightFunction.apply(ie);
        }
      }
    }
    return edgeContribution;
  }

  private void shiftLayerToZero() {
    int minLayer = Integer.MAX_VALUE;
    for (LV<V> v : layers.keySet()) if (layers.get(v) < minLayer) minLayer = layers.get(v);

    for (LV<V> v : svGraph.vertexSet()) {
      int newRank = layers.get(v) - minLayer;
      v.setRank(newRank);
      layers.put(v, newRank);
    }
  }

  private void exchange(LE<V, E> e, LE<V, E> f) {
    LV<V> node = commonPredecessorOfSourceAndTargetOfF(f);

    createPathForCutUpdates(e, f, node);
    updateLimLowLeavesAndParentsUnderNode(node);

    updateCuts(e);

    updateLayersUnderNode(node);
  }

  private void updateLimLowLeavesAndParentsUnderNode(LV<V> node) {

    //first we zero all low values in the subtree since they are an indication when positive that
    //the node has been processed
    //We are updating leaves also
    int llow = low.get(node);
    int llim = lim.get(node);

    leaves.clear();

    for (LV<V> v : svGraph.vertexSet()) {
      if (llow <= lim.get(v) && lim.get(v) <= llim) low.put(v, 0);
      else if (Objects.equals(low.get(v), lim.get(v))) leaves.add(v);
    }

    initLowLimParentAndLeavesOnSubtree(llow, node);
  }

  private void initLowLimParentAndLeavesOnSubtree(int curLim, LV<V> v) {
    Stack<Incidence<V, E>> stack = new Stack<>();
    Iterator<LE<V, E>> outEnum = this.svGraph.outgoingEdgesOf(v).iterator();
    Iterator<LE<V, E>> inEnum = this.svGraph.incomingEdgesOf(v).iterator();

    stack.push(new Incidence<>(v, outEnum, inEnum)); //vroot is 0 here
    low.put(v, curLim);

    while (stack.size() > 0) {
      Incidence<V, E> ss = stack.pop();
      v = ss.v;
      outEnum = ss.outEdges;
      inEnum = ss.inEdges;

      boolean done;
      do {
        done = true;
        while (outEnum.hasNext()) {
          LE<V, E> e = outEnum.next();
          if (!edgeInTreeMap.get(e) || low.get(e.getTarget()) > 0) continue;
          stack.push(new Incidence<>(v, outEnum, inEnum));
          v = e.getTarget();
          parent.put(v, e);
          low.put(v, curLim);
          outEnum = this.svGraph.outgoingEdgesOf(v).iterator();
          inEnum = this.svGraph.incomingEdgesOf(v).iterator();
        }
        while (inEnum.hasNext()) {
          LE<V, E> e = inEnum.next();
          if (!edgeInTreeMap.get(e) || low.get(e.getSource()) > 0) {
            continue;
          }
          stack.push(new Incidence<>(v, outEnum, inEnum));
          v = e.getSource();
          low.put(v, curLim);
          parent.put(v, e);
          outEnum = this.svGraph.outgoingEdgesOf(v).iterator();
          inEnum = this.svGraph.incomingEdgesOf(v).iterator();
          done = false;
          break;
        }
      } while (!done);

      //finally done with v
      lim.put(v, curLim++);
      if (Objects.equals(lim.get(v), low.get(v))) leaves.add(v);
    }
  }

  private void createPathForCutUpdates(LE<V, E> e, LE<V, E> f, LV<V> l) {

    LV<V> v = f.getTarget();
    while (v != l) {
      LE<V, E> p = parent.get(v);
      cutMap.put(p, Integer.MAX_VALUE);
      v = p.getSource() == v ? p.getTarget() : p.getSource();
    }

    cutMap.put(f, Integer.MAX_VALUE);

    edgeInTreeMap.put(e, false);
    edgeInTreeMap.put(f, true);
  }

  private LV<V> commonPredecessorOfSourceAndTargetOfF(LE<V, E> f) {
    //find the common predecessor of f.Source and f.Target
    int fMin, fmax;
    if (lim.get(f.getSource()) < lim.get(f.getTarget())) {
      fMin = lim.get(f.getSource());
      fmax = lim.get(f.getTarget());
    } else {
      fMin = lim.get(f.getTarget());
      fmax = lim.get(f.getSource());
    }
    //it is the best to walk up from the highest of nodes f
    //but we don't know the depths
    //so just start walking up from the source
    LV<V> l = f.getSource();

    while (!(low.get(l) <= fMin && fmax <= lim.get(l))) {
      LE<V, E> p = parent.get(l);

      cutMap.put(p, Integer.MAX_VALUE);

      l = p.getSource() == l ? p.getTarget() : p.getSource();
    }
    return l;
  }
}
