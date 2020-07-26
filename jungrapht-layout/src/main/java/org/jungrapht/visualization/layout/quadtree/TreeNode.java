package org.jungrapht.visualization.layout.quadtree;

import java.util.Collection;
import org.jungrapht.visualization.layout.model.Rectangle;

/** @author Tom Nelson */
public interface TreeNode {

  Rectangle getBounds();

  Collection<? extends TreeNode> getChildren();
}
