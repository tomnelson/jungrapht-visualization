package org.jungrapht.samples.rtree;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.swing.*;
import org.jungrapht.visualization.spatial.rtree.Bounded;
import org.jungrapht.visualization.spatial.rtree.InnerNode;
import org.jungrapht.visualization.spatial.rtree.LeafNode;
import org.jungrapht.visualization.spatial.rtree.Node;
import org.jungrapht.visualization.spatial.rtree.RStarLeafSplitter;
import org.jungrapht.visualization.spatial.rtree.RStarSplitter;
import org.jungrapht.visualization.spatial.rtree.RTree;
import org.jungrapht.visualization.spatial.rtree.SplitterContext;
import org.jungrapht.visualization.spatial.rtree.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A visualization of the R-Tree structure. users can add random elements, elements at mouse-click
 * location, or 2000 randomly generated elements. The structure of the R-Tree is also drawn
 *
 * @author Tom Nelson
 */
public class RTreeVisualizer extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(RTreeVisualizer.class);

  private final int INITIAL_WIDTH = 600;
  private final int INITIAL_HEIGHT = 600;

  SplitterContext<Object> splitterContext =
      SplitterContext.of(new RStarLeafSplitter<>(), new RStarSplitter<>());
  RTree<Object> rTree = RTree.create();
  int count;
  boolean running;

  public RTreeVisualizer() {
    setBackground(Color.white);
    setLayout(new BorderLayout());

    JToggleButton timerAdd = new JToggleButton("Timed add");
    timerAdd.addItemListener( // who cares
        this::itemStateChanged);

    JButton addStuff = new JButton("Add something");
    addStuff.addActionListener(e -> addRandomShape());
    JPanel drawingPane =
        new JPanel() {
          public Dimension getPreferredSize() {
            return new Dimension(INITIAL_WIDTH, INITIAL_HEIGHT);
          }

          @Override
          public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2d = (Graphics2D) g;
            Color oldColor = g2d.getColor();
            Map<Rectangle2D, Color> map = getGridInColor();
            for (Map.Entry<Rectangle2D, Color> entry : map.entrySet()) {
              g2d.setColor(entry.getValue());
              g2d.draw(entry.getKey());
            }
            g2d.setColor(oldColor);
          }
        };
    JButton addLots = new JButton("Add Many");
    addLots.addActionListener(e -> addMany());

    JButton bulkAddLots = new JButton("Bulk Add");
    bulkAddLots.addActionListener(e -> bulkInsertMany());

    drawingPane.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);

            if (SwingUtilities.isRightMouseButton(e)) {
              Object o = rTree.getPickedObject(e.getPoint());

              rTree = RTree.remove(rTree, o);
              log.trace("after removing {} rtree:{}", o, rTree);
              repaint();

            } else {
              addShapeAt(e.getPoint());
            }
            repaint();
          }
        });
    JButton samePoint = new JButton("Add Same");
    samePoint.addActionListener(e -> addShapeAt(new Point2D.Double(200, 200)));

    final java.util.List<Map.Entry<Object, Rectangle2D>> goners = new ArrayList<>();
    JButton removeForReinsert = new JButton("Remove for re-insert");
    removeForReinsert.addActionListener(
        e -> {
          goners.clear();
          rTree = RTree.removeForReinsert(rTree, goners);
          repaint();
        });

    JButton reinsertThese = new JButton("Reinsert them");
    reinsertThese.addActionListener(
        e -> {
          rTree = RTree.addAll(rTree, splitterContext, goners);
          repaint();
        });

    JButton reinsert = new JButton("Re-insert");
    reinsert.addActionListener(
        e -> {
          rTree = RTree.reinsert(rTree, splitterContext);
          repaint();
        });
    JButton clear = new JButton("clear");
    clear.addActionListener(
        e -> {
          rTree = RTree.create();
          repaint();
        });

    JPanel controls = new JPanel();
    controls.add(timerAdd);
    controls.add(addStuff);
    controls.add(addLots);
    controls.add(bulkAddLots);
    controls.add(clear);
    controls.add(samePoint);
    controls.add(reinsert);
    controls.add(removeForReinsert);
    controls.add(reinsertThese);
    add(drawingPane);
    add(controls, BorderLayout.SOUTH);
  }

  private void addRandomShape() {
    double width = 10;
    double height = 10;
    double x = Math.random() * getWidth() - width;
    double y = Math.random() * getHeight() - height;
    Rectangle2D r = new Rectangle2D.Double(x, y, width, height);
    rTree = RTree.add(rTree, splitterContext, "N" + count++, r);
    repaint();
  }

  private void addMany() {
    for (int i = 0; i < 2000; i++) {
      double width = 4;
      double height = 4;
      double x = Math.random() * getWidth() - width;
      double y = Math.random() * getHeight() - height;
      Rectangle2D r = new Rectangle2D.Double(x, y, width, height);
      rTree = RTree.add(rTree, splitterContext, "N" + count++, r);
      checkBounds(rTree);
      //      repaint();
    }
    repaint();
  }

  private void bulkInsertMany() {
    java.util.List<Map.Entry<Object, Rectangle2D>> list = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      double width = 4;
      double height = 4;
      double x = Math.random() * getWidth() - width;
      double y = Math.random() * getHeight() - height;
      Rectangle2D r = new Rectangle2D.Double(x, y, width, height);
      list.add(new AbstractMap.SimpleEntry("N" + count++, r));
    }
    rTree = RTree.bulkAdd(rTree, splitterContext, list);
    checkBounds(rTree);
    repaint();
  }

  private void addShapeAt(Point2D p) {
    double width = 10;
    double height = 10;
    Rectangle2D r =
        new Rectangle2D.Double(p.getX() - width / 2, p.getY() - height / 2, width, height);
    rTree = RTree.add(rTree, splitterContext, "N" + count++, r);
    log.trace("after adding {} at {}, rtree:{}", "N" + (count - 1), p, rTree);
    checkBounds(rTree);
    repaint();
  }

  private void checkBounds(RTree<?> tree) {
    checkBounds(tree.getRoot().get());
  }

  private Rectangle2D getBounds(Collection<? extends Bounded> nodes) {
    Rectangle2D bounds = null;
    for (Bounded b : nodes) {
      if (bounds == null) bounds = b.getBounds();
      else {
        bounds = bounds.createUnion(b.getBounds());
      }
    }
    return bounds;
  }

  private void checkBounds(InnerNode<?> node) {
    if (!node.getBounds().equals(getBounds(node.getChildren()))) {
      log.error("bounds not equal \n{} != \n{}", node.getBounds(), getBounds(node.getChildren()));
    }
  }

  private void checkBounds(Node<?> node) {
    if (node instanceof InnerNode) {
      checkBounds((InnerNode) node);
    } else if (node instanceof LeafNode) {
      log.trace("leafVertex: {}", node);
    }
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new RTreeVisualizer());
    f.pack();
    f.setVisible(true);
  }

  private Map<Rectangle2D, Color> getGridInColor() {
    Optional<Node<Object>> maybeRoot = rTree.getRoot();
    return maybeRoot.map(this::getGridFor).orElse(Collections.emptyMap());
  }

  private Map<Rectangle2D, Color> getGridFor(TreeNode parent) {
    Map<Rectangle2D, Color> map = new HashMap<>();
    if (parent instanceof LeafNode) {
      LeafNode<Object> leafParent = (LeafNode<Object>) parent;
      // get a color from the hashcode of this parent, and use that color for the parent and its children
      String hashString = "" + parent.hashCode();
      try {
        String lastSix = hashString.substring(hashString.length() - 6);
        int rgb = Integer.parseInt(lastSix, 16);
        Color color = new Color(rgb);
        map.put(parent.getBounds(), color);
        for (Shape kidShape : leafParent.collectGrids(new ArrayList<>())) {
          map.put(kidShape.getBounds(), color);
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    } else {
      map.put(parent.getBounds(), Color.pink);
      for (TreeNode child : parent.getChildren()) {
        map.putAll(getGridFor(child));
      }
    }

    return map;
  }

  private void itemStateChanged(ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      running = true;
      Thread timerAddThread =
          new Thread(
              () -> {
                while (running) {
                  SwingUtilities.invokeLater(() -> addRandomShape());
                  try {
                    Thread.sleep(100);
                  } catch (InterruptedException ex) {
                    // who cares
                  }
                }
              });
      timerAddThread.start();
    } else {
      running = false;
    }
  }
}
