/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization.selection;

import java.util.Set;

/** @author Tom Nelson */
public interface SelectedState<T> {

  boolean isSelected(T t);

  /** @return all selected elements. */
  Set<T> getSelected();
}
