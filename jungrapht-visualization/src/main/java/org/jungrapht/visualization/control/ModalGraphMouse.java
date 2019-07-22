/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 *
 * Created on Aug 26, 2005
 */

package org.jungrapht.visualization.control;

import java.awt.event.ItemListener;
import org.jungrapht.visualization.VisualizationViewer;

/**
 * Interface for a GraphMouse that supports modality.
 *
 * @author Tom Nelson
 */
public interface ModalGraphMouse extends VisualizationViewer.GraphMouse {

  void setMode(Mode mode);

  /** @return Returns the modeListener. */
  ItemListener getModeListener();

  /** */
  enum Mode {
    TRANSFORMING,
    PICKING,
    ANNOTATING,
    EDITING
  }
}
