/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
/*
 * Created on Feb 17, 2004
 */
package org.jungrapht.visualization.control;

import java.awt.event.MouseEvent;

/**
 * This interface allows users to register listeners to register to receive vertex clicks.
 *
 * @author danyelf
 */
public interface GraphMouseListener<V> {

  void graphClicked(V v, MouseEvent me);

  void graphPressed(V v, MouseEvent me);

  void graphReleased(V v, MouseEvent me);
}
