package org.jungrapht.samples.util;

import org.jungrapht.visualization.MultiLayerTransformer;
import org.jungrapht.visualization.VisualizationServer;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.layout.model.LayoutModel;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.jungrapht.visualization.transform.shape.ShapeTransformer;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class LayoutGrid implements VisualizationServer.Paintable {

    Rectangle rectangle;
    LayoutModel<?> layoutModel;
    int divisions;
    VisualizationViewer<?, ?> vv;

    public LayoutGrid(VisualizationViewer<?, ?> vv, int divisions) {
        this.vv = vv;
        this.divisions = divisions;
        this.layoutModel = vv.getVisualizationModel().getLayoutModel();
        this.rectangle = Rectangle.of(layoutModel.getWidth(), layoutModel.getHeight());
    }

    public void paint(Graphics g) {
        // get new rectangle, but first change divisions for resize
        divisions *= layoutModel.getHeight() / rectangle.height;
        rectangle = Rectangle.of(layoutModel.getWidth(), layoutModel.getHeight());
//        ShapeTransformer masterViewTransformer =
//                vv.getRenderContext().getMultiLayerTransformer().getTransformer(MultiLayerTransformer.Layer.VIEW);
//        ShapeTransformer masterLayoutTransformer =
//                vv.getRenderContext().getMultiLayerTransformer().getTransformer(MultiLayerTransformer.Layer.LAYOUT);
        ShapeTransformer vvLayoutTransformer =
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(MultiLayerTransformer.Layer.LAYOUT);

        vv.getBounds();
        GeneralPath path = new GeneralPath();
        path.moveTo(rectangle.x, rectangle.y);
        path.lineTo(rectangle.width, rectangle.y);
        path.lineTo(rectangle.width, rectangle.height);
        path.lineTo(rectangle.x, rectangle.height);
        path.lineTo(rectangle.x, rectangle.y);

        for (int i = 0; i <= rectangle.width; i += rectangle.width / 10) {
            path.moveTo(rectangle.x + i, rectangle.y);
            path.lineTo(rectangle.x + i, rectangle.height);
        }
        for (int i = 0; i <= rectangle.height; i += rectangle.height / 10) {
            path.moveTo(rectangle.x, rectangle.y + i);
            path.lineTo(rectangle.width, rectangle.y + i);
        }
        Shape lens = path;
//        lens = masterViewTransformer.inverseTransform(lens);
//        lens = masterLayoutTransformer.inverseTransform(lens);
        lens = vvLayoutTransformer.transform(lens);
        Graphics2D g2d = (Graphics2D) g;
        Color old = g.getColor();
        g.setColor(Color.cyan);
        g2d.draw(lens);

        path = new GeneralPath();
        path.moveTo((float) rectangle.x, (float) rectangle.getCenterY());
        path.lineTo((float) rectangle.maxX, (float) rectangle.getCenterY());
        path.moveTo((float) rectangle.getCenterX(), (float) rectangle.y);
        path.lineTo((float) rectangle.getCenterX(), (float) rectangle.maxY);
//        Shape crosshairShape = path;
//        crosshairShape = masterViewTransformer.inverseTransform(crosshairShape);
//        crosshairShape = masterLayoutTransformer.inverseTransform(crosshairShape);
//        crosshairShape = vvLayoutTransformer.transform(crosshairShape);
//        g.setColor(Color.black);
//        g2d.setStroke(new BasicStroke(3));
//        g2d.draw(crosshairShape);

        g.setColor(old);
    }

    public boolean useTransform() {
        return true;
    }
}
