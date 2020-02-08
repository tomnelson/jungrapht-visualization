package org.jungrapht.visualization.layout.algorithms.eiglsperger;

import java.util.Map;
import org.jungrapht.visualization.layout.algorithms.sugiyama.LV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see "Fast and Simple Horizontal Coordinate Assignment, Ulrik Brandes and Boris Köpf, Department
 *     of Computer & Information Science, University of Konstanz"
 */
public class HorizontalCompaction<V>
    extends org.jungrapht.visualization.layout.algorithms.sugiyama.HorizontalCompaction<V> {

  private static final Logger log = LoggerFactory.getLogger(HorizontalCompaction.class);

  public HorizontalCompaction(
      LV<V>[][] layers,
      Map<LV<V>, LV<V>> rootMap,
      Map<LV<V>, LV<V>> alignMap,
      int deltaX,
      int deltaY) {
    super(layers, rootMap, alignMap, deltaX, deltaY);
  }

  /**
   * overridden to return pos instead of index
   *
   * @param v
   * @return
   */
  @Override
  protected int pos(LV<V> v) {
    return v.getPos();
  }

  protected int idx(LV<V> v) {
    return v.getIndex();
  }
}
