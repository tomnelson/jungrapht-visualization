package org.jungrapht.visualization.layout.algorithms;

import org.junit.Test;

public class TestTreeBuilders {

  @Test
  public void test() {
    LayoutAlgorithm es =
        EdgeSortingTreeLayoutAlgorithm.sortingBuilder()
            .horizontalVertexSpacing(1)
            .edgeComparator(null)
            .build();
  }
}
