package org.jungrapht.visualization.renderers;

import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import org.jgrapht.Graph;
import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.RenderContext;
import org.jungrapht.visualization.decorators.ExpandXY;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.transform.MutableTransformer;
import org.jungrapht.visualization.transform.shape.GraphicsDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <V> vertex type
 * @param <E> edge type
 */
public abstract class AbstractEdgeRenderer<V, E> implements Renderer.Edge<V, E> {

  private static final Logger log = LoggerFactory.getLogger(AbstractEdgeRenderer.class);

  @Override
  public void paintEdge(RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, E e) {
    GraphicsDecorator g2d = renderContext.getGraphicsContext();
    if (!renderContext.getEdgeIncludePredicate().test(e)) {
      return;
    }

    // don't draw edge if either incident vertex is not drawn
    V u = layoutModel.getGraph().getEdgeSource(e);
    V v = layoutModel.getGraph().getEdgeTarget(e);
    Predicate<V> vertexIncludePredicate = renderContext.getVertexIncludePredicate();
    if (!vertexIncludePredicate.test(u) || !vertexIncludePredicate.test(v)) {
      return;
    }

    Stroke new_stroke = renderContext.edgeStrokeFunction().apply(e);
    Stroke old_stroke = g2d.getStroke();
    if (new_stroke != null) {
      g2d.setStroke(new_stroke);
    }

    drawSimpleEdge(renderContext, layoutModel, e);

    // restore paint and stroke
    if (new_stroke != null) {
      g2d.setStroke(old_stroke);
    }
  }

  protected Shape prepareFinalEdgeShape(
      RenderContext<V, E> renderContext,
      LayoutModel<V> layoutModel,
      E e,
      int[] coords,
      boolean[] loop) {
    V source = layoutModel.getGraph().getEdgeSource(e);
    V target = layoutModel.getGraph().getEdgeTarget(e);

    Point sourcePoint = layoutModel.apply(source);
    Point targetPoint = layoutModel.apply(target);
    Point2D sourcePoint2D =
        renderContext
            .getMultiLayerTransformer()
            .transform(
                MultiLayerTransformer.Layer.LAYOUT,
                new Point2D.Double(sourcePoint.x, sourcePoint.y));
    Point2D targetPoint2D =
        renderContext
            .getMultiLayerTransformer()
            .transform(
                MultiLayerTransformer.Layer.LAYOUT,
                new Point2D.Double(targetPoint.x, targetPoint.y));
    float sourcePoint2DX = (float) sourcePoint2D.getX();
    float sourcePoint2DY = (float) sourcePoint2D.getY();
    float targetPoint2DX = (float) targetPoint2D.getX();
    float targetPoint2DY = (float) targetPoint2D.getY();
    coords[0] = (int) sourcePoint2DX;
    coords[1] = (int) sourcePoint2DY;
    coords[2] = (int) targetPoint2DX;
    coords[3] = (int) targetPoint2DY;

    boolean isLoop = loop[0] = source.equals(target);
    Shape targetShape = renderContext.getVertexShapeFunction().apply(target);
    Shape edgeShape = getEdgeShape(renderContext.getEdgeShapeFunction(), e, layoutModel.getGraph());

    AffineTransform xform = AffineTransform.getTranslateInstance(sourcePoint2DX, sourcePoint2DY);

    if (isLoop) {
      // this is a self-loop. scale it is larger than the vertex
      // it decorates and translate it so that its nadir is
      // at the center of the vertex.
      Rectangle2D targetShapeBounds2D = targetShape.getBounds2D();
      xform.scale(targetShapeBounds2D.getWidth(), targetShapeBounds2D.getHeight());
      xform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);
    } else {
      // this is a normal edge. Rotate it to the angle between
      // vertex endpoints, then scale it to the distance between
      // the vertices
      float dx = targetPoint2DX - sourcePoint2DX;
      float dy = targetPoint2DY - sourcePoint2DY;
      float thetaRadians = (float) Math.atan2(dy, dx);
      xform.rotate(thetaRadians);
      double dist = Math.sqrt(dx * dx + dy * dy);

      if (edgeShape instanceof ExpandXY) {
        // this is for the Articulated edges in the min cross layouts
        // and (future) orthogonal layout edges
        MutableTransformer layoutTransformer =
            renderContext
                .getMultiLayerTransformer()
                .getTransformer(MultiLayerTransformer.Layer.LAYOUT);
        double scaleX = layoutTransformer.getScaleX();
        double scaleY = layoutTransformer.getScaleY();
        // account for any single axis scaling by mutating the 'Y' coords of the articulated edge
        AffineTransform singleAxisScalingTransform =
            AffineTransform.getScaleInstance(1, scaleX / scaleY);
        log.trace("scaleX: {} ", scaleX);
        log.trace("scaleY: {}", scaleY);
        edgeShape = singleAxisScalingTransform.createTransformedShape(edgeShape);
        xform.scale(dist, dist);
      } else {
        // all other edges are scaled only in the x axis (to span from vertex to vertex)
        xform.scale(dist, 1.0);
      }
    }
    edgeShape = xform.createTransformedShape(edgeShape);

    return edgeShape;
  }

  /**
   * For Heavyweight graph visualizations, edges are rendered with the user requested
   * edgeShapeFunction. For Lightweight graph visualizations, edges are rendered with a
   * (lightweight) line edge except when they are articulated edges (sugiyama layout). The
   * LightweightEdgeRenderer overrides this method to supply the correct edge shape.
   *
   * @param edgeShapeFunction the user specified edgeShapeFunction
   * @param edge the edge to render
   * @param graph for the Function context
   * @return the edge shape, heavyweight (anything) or lightweight (line or articulated line)
   */
  protected abstract Shape getEdgeShape(
      BiFunction<Graph<V, E>, E, Shape> edgeShapeFunction, E edge, Graph<V, E> graph);

  protected abstract void drawSimpleEdge(
      RenderContext<V, E> renderContext, LayoutModel<V> layoutModel, E e);
}
