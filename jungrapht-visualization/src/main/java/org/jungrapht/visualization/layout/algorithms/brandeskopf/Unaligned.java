package org.jungrapht.visualization.layout.algorithms.brandeskopf;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.jungrapht.visualization.layout.algorithms.sugiyama.SyntheticLV;
import org.jungrapht.visualization.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** does not vertically align articulated edges. Centers each row according to the max row width */
public class Unaligned {

  private static final Logger log = LoggerFactory.getLogger(Unaligned.class);

  public static <V> void centerPoints(
      LV<V>[][] layers,
      Function<V, Shape> vertexShapeFunction,
      int horizontalOffset,
      int verticalOffset,
      Map<LV<V>, Point> vertexPointMap) {

    Map<Integer, Integer> rowWidthMap = new HashMap<>();
    Map<Integer, Integer> rowMaxHeightMap = new HashMap<>();
    int layerIndex = 0;
    for (LV<V>[] layer : layers) {
      int width = horizontalOffset;
      int maxHeight = 0;
      for (LV<V> sugiyamaVertex : layer) {
        if (!(sugiyamaVertex instanceof SyntheticLV)) {
          Rectangle bounds = vertexShapeFunction.apply(sugiyamaVertex.getVertex()).getBounds();
          width += bounds.width + horizontalOffset;
          maxHeight = Math.max(maxHeight, bounds.height);
        } else {
          width += horizontalOffset;
        }
      }
      rowWidthMap.put(layerIndex, width);
      rowMaxHeightMap.put(layerIndex, maxHeight);
      layerIndex++;
    }
    int widestRowWidth = rowWidthMap.values().stream().mapToInt(v -> v).max().getAsInt();
    int x = 0; //horizontalOffset;
    int y = verticalOffset;
    layerIndex = 0;
    log.trace("layerMaxHeights {}", rowMaxHeightMap);
    for (LV<V>[] layer : layers) {
      int previousVertexWidth = 0;
      // offset against widest row
      x += (widestRowWidth - rowWidthMap.get(layerIndex)) / 2;

      y += rowMaxHeightMap.get(layerIndex) / 2;
      if (layerIndex > 0) {
        y += rowMaxHeightMap.get(layerIndex - 1) / 2;
      }

      for (LV<V> sugiyamaVertex : layer) {
        int vertexWidth = 0;
        if (!(sugiyamaVertex instanceof SyntheticLV)) {
          vertexWidth = vertexShapeFunction.apply(sugiyamaVertex.getVertex()).getBounds().width;
        }

        x += previousVertexWidth / 2 + vertexWidth / 2 + horizontalOffset;

        log.trace("layerIndex {} y is {}", layerIndex, y);
        sugiyamaVertex.setPoint(Point.of(x, y));

        if (vertexPointMap.containsKey(sugiyamaVertex)) {
          vertexPointMap.put(sugiyamaVertex, sugiyamaVertex.getPoint());
        }
        previousVertexWidth = vertexWidth;
      }
      x = horizontalOffset;
      y += verticalOffset;
      layerIndex++;
    }

    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].length; j++) {
        LV<V> sugiyamaVertex = layers[i][j];
        vertexPointMap.put(sugiyamaVertex, sugiyamaVertex.getPoint());
      }
    }
  }

  public static <V> void setPoints(
      LV<V>[][] layers,
      Function<V, Shape> vertexShapeFunction,
      int horizontalOffset,
      int verticalOffset,
      Map<LV<V>, Point> vertexPointMap) {

    //    Map<Integer, Integer> rowWidthMap = new HashMap<>();
    //    Map<Integer, Integer> rowMaxHeightMap = new HashMap<>();
    int layerIndex = 0;
    //    for (LV<V>[] layer : layers) {
    //      int width = horizontalOffset;
    //      int maxHeight = 0;
    //      for (LV<V> sugiyamaVertex : layer) {
    //        if (!(sugiyamaVertex instanceof SyntheticLV)) {
    //          Rectangle bounds = vertexShapeFunction.apply(sugiyamaVertex.vertex).getBounds();
    //          width += bounds.width + horizontalOffset;
    //          maxHeight = Math.max(maxHeight, bounds.height);
    //        } else {
    //          width += horizontalOffset;
    //        }
    //      }
    //      rowWidthMap.put(layerIndex, width);
    //      rowMaxHeightMap.put(layerIndex, maxHeight);
    //      layerIndex++;
    //    }
    //    int widestRowWidth = rowWidthMap.values().stream().mapToInt(v -> v).max().getAsInt();
    int x = horizontalOffset;
    int y = verticalOffset;
    layerIndex = 0;
    //    log.trace("layerMaxHeights {}", rowMaxHeightMap);
    for (LV<V>[] layer : layers) {
      //      int previousVertexWidth = 0;
      // offset against widest row
      //      x += (widestRowWidth - rowWidthMap.get(layerIndex)) / 2;
      //
      //      y += rowMaxHeightMap.get(layerIndex) / 2;
      //      if (layerIndex > 0) {
      //        y += rowMaxHeightMap.get(layerIndex - 1) / 2;
      //      }

      for (LV<V> sugiyamaVertex : layer) {
        int vertexWidth = 0;
        if (!(sugiyamaVertex instanceof SyntheticLV)) {
          vertexWidth = vertexShapeFunction.apply(sugiyamaVertex.getVertex()).getBounds().width;
        }

        x +=
            //                previousVertexWidth / 2 + vertexWidth / 2 +
            horizontalOffset;

        log.trace("layerIndex {} y is {}", layerIndex, y);
        sugiyamaVertex.setPoint(Point.of(x, y));

        if (vertexPointMap.containsKey(sugiyamaVertex)) {
          vertexPointMap.put(sugiyamaVertex, sugiyamaVertex.getPoint());
        }
        //        previousVertexWidth = vertexWidth;
      }
      x = horizontalOffset;
      y += verticalOffset;
      layerIndex++;
    }

    for (int i = 0; i < layers.length; i++) {
      for (int j = 0; j < layers[i].length; j++) {
        LV<V> sugiyamaVertex = layers[i][j];
        vertexPointMap.put(sugiyamaVertex, sugiyamaVertex.getPoint());
      }
    }
  }

  public static <V> void setPoints(
      List<List<LV<V>>> layers,
      Function<V, Shape> vertexShapeFunction,
      int horizontalOffset,
      int verticalOffset,
      Map<LV<V>, Point> vertexPointMap) {

    Map<Integer, Integer> rowWidthMap = new HashMap<>();
    Map<Integer, Integer> rowMaxHeightMap = new HashMap<>();
    int layerIndex = 0;
    for (List<LV<V>> layer : layers) {
      int width = horizontalOffset;
      int maxHeight = 0;
      for (LV<V> sugiyamaVertex : layer) {
        if (!(sugiyamaVertex instanceof SyntheticLV)) {
          Rectangle bounds = vertexShapeFunction.apply(sugiyamaVertex.getVertex()).getBounds();
          width += bounds.width + horizontalOffset;
          maxHeight = Math.max(maxHeight, bounds.height);
        } else {
          width += horizontalOffset;
        }
      }
      rowWidthMap.put(layerIndex, width);
      rowMaxHeightMap.put(layerIndex, maxHeight);
      layerIndex++;
    }
    int widestRowWidth = rowWidthMap.values().stream().mapToInt(v -> v).max().getAsInt();
    int x = 0; //horizontalOffset;
    int y = verticalOffset;
    layerIndex = 0;
    log.trace("layerMaxHeights {}", rowMaxHeightMap);
    for (List<LV<V>> layer : layers) {
      int previousVertexWidth = 0;
      // offset against widest row
      x += (widestRowWidth - rowWidthMap.get(layerIndex)) / 2;

      y += rowMaxHeightMap.get(layerIndex) / 2;
      if (layerIndex > 0) {
        y += rowMaxHeightMap.get(layerIndex - 1) / 2;
      }

      for (LV<V> sugiyamaVertex : layer) {
        int vertexWidth = 0;
        if (!(sugiyamaVertex instanceof SyntheticLV)) {
          vertexWidth = vertexShapeFunction.apply(sugiyamaVertex.getVertex()).getBounds().width;
        }

        x += previousVertexWidth / 2 + vertexWidth / 2 + horizontalOffset;

        log.trace("layerIndex {} y is {}", layerIndex, y);
        sugiyamaVertex.setPoint(Point.of(x, y));

        if (vertexPointMap.containsKey(sugiyamaVertex)) {
          vertexPointMap.put(sugiyamaVertex, sugiyamaVertex.getPoint());
        }
        previousVertexWidth = vertexWidth;
      }
      x = horizontalOffset;
      y += verticalOffset;
      layerIndex++;
    }

    for (int i = 0; i < layers.size(); i++) {
      for (int j = 0; j < layers.get(i).size(); j++) {
        LV<V> sugiyamaVertex = layers.get(i).get(j);
        vertexPointMap.put(sugiyamaVertex, sugiyamaVertex.getPoint());
      }
    }
  }
}
