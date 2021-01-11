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

import java.awt.event.KeyListener;
import org.jungrapht.visualization.VisualizationViewer;
import org.jungrapht.visualization.control.modal.Modal;

/**
 * Interface for a GraphMouse that supports modality.
 *
 * @author Tom Nelson
 */
public interface ModalGraphMouse extends VisualizationViewer.GraphMouse, Modal {

  //  void setMode(Mode mode);

  //  Mode getMode();

  /** @return Returns the modeListener. */
  //    ItemListener getModeListener();
  //
  KeyListener getModeKeyListener();

  //  /** */
  //  enum Mode {
  //    TRANSFORMING,
  //    PICKING,
  //    ANNOTATING,
  //    EDITING
  //  }
}
