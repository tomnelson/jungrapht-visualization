/*
 * Created on Mar 10, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/tomnelson/jungrapht-visualization/blob/master/LICENSE for a description.
 */
package org.jungrapht.visualization.decorators;

import com.google.common.base.Preconditions;
import java.util.function.Function;
import org.jungrapht.visualization.selection.SelectedState;

/**
 * Paints each edge according to the <code>P</code> parameters given in the constructor, so that
 * selected and non-selected edges can be made to look different.
 *
 * @author Tom Nelson
 */
public class PickableElementPaintFunction<E, P> implements Function<E, P> {
  protected SelectedState<E> selectedState;
  protected P drawPaint;
  protected P selectedPaint;

  /**
   * @param selectedState specifies which elements report as "selected"
   * @param drawPaint <code>P</code> used to draw element shapes
   * @param selectedPaint <code>P</code> used to draw selected element shapes
   */
  public PickableElementPaintFunction(
      SelectedState<E> selectedState, P drawPaint, P selectedPaint) {
    this.selectedState = Preconditions.checkNotNull(selectedState);
    this.drawPaint = Preconditions.checkNotNull(drawPaint);
    this.selectedPaint = Preconditions.checkNotNull(selectedPaint);
  }

  /** */
  public P apply(E element) {
    if (selectedState.isSelected(element)) {
      return selectedPaint;
    } else {
      return drawPaint;
    }
  }
}
