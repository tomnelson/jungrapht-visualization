package org.jungrapht.samples.quadtree;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.swing.*;
import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.jungrapht.visualization.layout.quadtree.BarnesHutQuadTree;
import org.jungrapht.visualization.layout.quadtree.ForceObject;
import org.jungrapht.visualization.layout.quadtree.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Draws a Barnes-Hut Quad Tree. Mouse clicks on empty space add a new forceObject. Mouse clicks on
 * an existing object will highlight the other forces that will act on the clicked object
 *
 * @author Tom Nelson
 */
public class BarnesHutVisualizer extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(BarnesHutVisualizer.class);

  BarnesHutQuadTree<String> tree;

  Map<String, Point> elements = new HashMap<>();

  Collection<Shape> stuffToDraw = new HashSet<>();

  public BarnesHutVisualizer() {
    setLayout(new BorderLayout());

    elements.put("A", Point.of(200, 100));
    elements.put("B", Point.of(100, 200));
    elements.put("C", Point.of(100, 100));
    elements.put("D", Point.of(500, 100));

    tree = BarnesHutQuadTree.builder().bounds(600, 600).build();
    tree.rebuild(elements.keySet(), elements::get);

    JPanel drawingPanel =
        new JPanel() {
          @Override
          public Dimension getPreferredSize() {
            return new Dimension(600, 600);
          }

          @Override
          public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2d = (Graphics2D) g;
            draw(g2d, tree.getRoot());
            for (Shape shape : stuffToDraw) {
              g2d.draw(shape);
            }
          }
        };
    add(drawingPanel);
    drawingPanel.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            stuffToDraw.clear();
            Point2D p = e.getPoint();
            String got = getVertexAt(p);
            if (got != null) {
              ForceObject<String> nodeForceObject =
                  new ForceObject<>(got, elements.get(got)) {
                    @Override
                    protected void addForceFrom(ForceObject<String> other) {
                      log.info("adding force from {}", other);
                      Ellipse2D ellipse =
                          new Ellipse2D.Double(other.p.x - 15, other.p.y - 15, 30, 30);
                      stuffToDraw.add(ellipse);
                      Line2D line = new Line2D.Double(this.p.x, this.p.y, other.p.x, other.p.y);
                      stuffToDraw.add(line);
                    }
                  };
              tree.applyForcesTo(nodeForceObject);
            } else {
              addShapeAt(p);
            }
            repaint();
          }
        });

    JButton clear = new JButton("clear");
    clear.addActionListener(e -> clearGraph());
    JButton go = new JButton("Log all forces");
    go.addActionListener(
        e -> {
          for (Map.Entry<String, Point> entry : elements.entrySet()) {
            String node = entry.getKey();
            Point point = entry.getValue();
            ForceObject<String> nodeForceObject =
                new ForceObject(node, point) {
                  @Override
                  protected void addForceFrom(ForceObject other) {

                    log.info("for node {}, next force object is {}", node, other);
                  }
                };
            tree.applyForcesTo(nodeForceObject);
          }
        });
    JPanel controls = new JPanel();
    controls.add(go);
    controls.add(clear);
    add(controls, BorderLayout.SOUTH);
  }

  private void clearGraph() {
    elements.clear();
    stuffToDraw.clear();
    tree = BarnesHutQuadTree.builder().bounds(getWidth(), getHeight()).build();
    tree.rebuild(elements.keySet(), elements::get);
    repaint();
  }

  private void addShapeAt(Point2D p) {
    String n = "N" + elements.size();
    elements.put(n, Point.of(p.getX(), p.getY()));
    tree.rebuild(elements.keySet(), elements::get);
    repaint();
  }

  private String getVertexAt(Point2D p) {

    return elements
        .entrySet()
        .stream()
        .filter(e -> e.getValue().distanceSquared(p.getX(), p.getY()) < 20)
        .findFirst()
        .map(Map.Entry::getKey)
        .orElse(null);

    //    for (Map.Entry<String, Point> entry : elements.entrySet()) {
    //      if (entry.getValue().distanceSquared(p.getX(), p.getY()) < 20) {
    //        return entry.getKey();
    //      }
    //    }
    //    return null;
  }

  private void draw(Graphics2D g, Node node) {
    Rectangle bounds = node.getBounds();
    Rectangle2D r = new Rectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height);
    g.draw(r);
    ForceObject forceObject = node.getForceObject();
    if (forceObject != null) {
      Point center = node.getForceObject().p;
      Ellipse2D forceCenter = new Ellipse2D.Double(center.x - 5, center.y - 5, 10, 10);
      Color oldColor = g.getColor();
      g.setColor(Color.red);

      Point2D centerOfVertex = new Point2D.Double((r.getCenterX()), r.getCenterY());
      Point2D centerOfForce = new Point2D.Double(center.x, center.y);
      g.draw(new Line2D.Double(centerOfVertex, centerOfForce));
      g.draw(forceCenter);
      g.setColor(oldColor);
    }
    if (node.getNW() != null) {
      draw(g, node.getNW());
    }
    if (node.getNE() != null) {
      draw(g, node.getNE());
    }
    if (node.getSW() != null) {
      draw(g, node.getSW());
    }
    if (node.getSE() != null) {
      draw(g, node.getSE());
    }
    if (forceObject != null) {
      Point p = forceObject.p;
      Ellipse2D circle = new Ellipse2D.Double(p.x - 2, p.y - 2, 4, 4);
      g.fill(circle);
      g.drawString(forceObject.getElement().toString(), (int) p.x + 4, (int) p.y - 4);
    }
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.getContentPane().add(new BarnesHutVisualizer());
    frame.pack();
    frame.setVisible(true);
  }
}
