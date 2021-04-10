package org.jungrapht.samples.experimental;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jungrapht.visualization.util.Attributed;

public class EdgeComparator implements Comparator<Attributed<Integer>> {

  private static String DEFAULT_EDGE_TYPE_PRIORITY_LIST =
      "Fall-Through"
          + "Conditional-Return,"
          + "Unconditional-Jump,"
          + "Conditional-Jump,"
          + "Unconditional-Call,"
          + "Conditional-Call,"
          + "Terminator,"
          + "Computed,"
          + "Indirection,"
          + "Entry";

  /** {@code Map} of EdgeType attribute value to integer priority */
  private Map<String, Integer> edgePriorityMap = new HashMap();

  /**
   * Create an instance and place the list values into the {@code edgePriorityMap} with a one-up
   * counter expressing their relative priority
   */
  public EdgeComparator() {
    List<String> edgeTypePriorityList = getEdgeTypePriorityList();
    edgeTypePriorityList.forEach(s -> edgePriorityMap.put(s, edgeTypePriorityList.indexOf(s)));
  }

  /**
   * {@inheritdoc} Compares the {@code AttributedEdge}s using their priority in the supplied {@code
   * edgePriorityMap}
   */
  @Override
  public int compare(Attributed<Integer> edgeOne, Attributed<Integer> edgeTwo) {
    return priority(edgeOne).compareTo(priority(edgeTwo));
  }

  private Integer priority(Attributed<Integer> e) {
    return edgePriorityMap.getOrDefault(e.get("EdgeType"), 0);
  }

  private List<String> getEdgeTypePriorityList() {
    return Arrays.asList(DEFAULT_EDGE_TYPE_PRIORITY_LIST.split(","));
  }
}
