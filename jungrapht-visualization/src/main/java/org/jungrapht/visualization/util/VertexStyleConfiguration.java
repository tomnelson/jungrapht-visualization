package org.jungrapht.visualization.util;

import static org.jungrapht.visualization.renderers.BiModalRenderer.HEAVYWEIGHT;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.*;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.decorators.EllipseShapeFunction;
import org.jungrapht.visualization.decorators.IconShapeFunction;
import org.jungrapht.visualization.renderers.HeavyweightVertexLabelRenderer;
import org.jungrapht.visualization.renderers.Renderer.VertexLabel.Position;
import org.jungrapht.visualization.renderers.VertexLabelAsShapeRenderer;

public class VertexStyleConfiguration<V, E> {

  public static class Builder<
      V, E, T extends VertexStyleConfiguration<V, E>, B extends Builder<V, E, T, B>> {
    private VisualizationServer<V, E> vv;
    private Supplier<Boolean> showVertexIconSupplier = () -> false;
    private Supplier<Boolean> showVertexLabelSupplier = () -> false;
    private Supplier<Position> labelPositionSupplier = () -> Position.SE;
    private Function<V, String> labelFunction = v -> null;
    private Function<V, Icon> iconFunction = null;
    private Function<V, Shape> vertexShapeFunction = new EllipseShapeFunction<>();

    public B self() {
      return (B) this;
    }

    public B showVertexIconSupplier(Supplier<Boolean> showVertexIconSupplier) {
      this.showVertexIconSupplier = showVertexIconSupplier;
      return self();
    }

    public B showVertexLabelSupplier(Supplier<Boolean> showVertexLabelSupplier) {
      this.showVertexLabelSupplier = showVertexLabelSupplier;
      return self();
    }

    public B labelPositionSupplier(Supplier<Position> labelPositionSupplier) {
      this.labelPositionSupplier = labelPositionSupplier;
      return self();
    }

    public B labelFunction(Function<V, String> labelFunction) {
      this.labelFunction = labelFunction;
      return self();
    }

    public B iconFunction(Function<V, Icon> iconFunction) {
      this.iconFunction = iconFunction;
      return self();
    }

    public B vertexShapeFunction(Function<V, Shape> vertexShapeFunction) {
      this.vertexShapeFunction = vertexShapeFunction;
      return self();
    }

    private Builder(VisualizationServer<V, E> vv) {
      this.vv = vv;
    }

    public T build() {
      return (T) new VertexStyleConfiguration<>(this);
    }
  }

  public static <V, E> Builder<V, E, ?, ?> builder(VisualizationServer<V, E> vv) {
    return new Builder<>(vv);
  }

  private VisualizationServer<V, E> vv;
  private VertexLabelAsShapeRenderer<V, E> vlasr;
  private IconShapeFunction<V> vertexImageShapeFunction =
      new IconShapeFunction(v -> new Ellipse2D.Float(-20, -20, 40, 40));
  private Supplier<Boolean> showVertexIconSupplier;
  private Supplier<Boolean> showVertexLabelSupplier;
  private Supplier<Position> labelPositionSupplier;
  private Function<V, String> labelFunction;
  private Function<V, Icon> iconFunction;
  private Function<V, Shape> vertexShapeFunction;

  protected VertexStyleConfiguration(Builder<V, E, ?, ?> builder) {
    this.vv = builder.vv;
    this.vertexShapeFunction = builder.vertexShapeFunction;
    this.showVertexIconSupplier = builder.showVertexIconSupplier;
    this.showVertexLabelSupplier = builder.showVertexLabelSupplier;
    this.iconFunction = builder.iconFunction;
    this.labelPositionSupplier = builder.labelPositionSupplier;
    this.labelFunction = builder.labelFunction;
    this.vlasr =
        new VertexLabelAsShapeRenderer<>(
            vv.getVisualizationModel().getLayoutModel(), vv.getRenderContext());
  }

  public void configure() {
    if (showVertexIconSupplier.get()) {
      // see if there is an image available
      vertexImageShapeFunction.setIconFunction(iconFunction);
      vv.getRenderContext().setVertexIconFunction(iconFunction);
      vv.getRenderContext().setVertexShapeFunction(vertexImageShapeFunction);
      if (showVertexLabelSupplier.get()) {
        vv.getRenderContext().setVertexLabelFunction(labelFunction);
        vv.getRenderContext().setVertexLabelPosition(labelPositionSupplier.get());
      } else {
        vv.getRenderContext().setVertexLabelFunction(v -> null);
      }
    } else {
      vv.getRenderContext().setVertexIconFunction(null);
      if (showVertexLabelSupplier.get()) {
        Position position = labelPositionSupplier.get();
        vv.getRenderContext().setVertexLabelPosition(position);
        vv.getRenderContext().setVertexLabelFunction(labelFunction);
        if (position == Position.CNTR) {
          vv.getRenderContext().setVertexShapeFunction(vlasr);
          vv.getRenderer().setVertexLabelRenderer(HEAVYWEIGHT, vlasr);

        } else { // label not in center
          vv.getRenderer()
              .setVertexLabelRenderer(HEAVYWEIGHT, new HeavyweightVertexLabelRenderer<>());
          vv.getRenderContext().setVertexShapeFunction(vertexShapeFunction); // restore
        }
      } else {
        vv.getRenderContext().setVertexLabelFunction(vertex -> null);
      }
    }
    vv.repaint();
  }
}
