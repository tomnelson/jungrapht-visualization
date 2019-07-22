package org.jungrapht.visualization.layout.algorithms;

import org.junit.Test;

public class TestTreeBuilders {

  @Test
  public void test() {
    LayoutAlgorithm es =
        EdgeSortingTreeLayoutAlgorithm.sortingBuilder()
            .horizontalNodeSpacing(1)
            .edgeComparator(null)
            .build();
  }
}
