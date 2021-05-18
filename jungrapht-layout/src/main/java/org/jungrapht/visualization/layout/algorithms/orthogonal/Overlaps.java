package org.jungrapht.visualization.layout.algorithms.orthogonal;

import org.jungrapht.visualization.layout.model.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Overlaps {

    // sort by x
    // start at max -> min
    // for each, find another that has overlap
    // create an edge
    // continue till min is reached
    // for each edge, move the box to min separation

    static class Span {
        final int min;
        final int max;

        public static Span of(int min, int max) {
            return new Span(min, max);
        }

        Span(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }

    // a list of the Rectangle vertices, sorted by x
    List<Rectangle> list;

    Map<Rectangle, List<Span>> map = new HashMap<>();

    boolean overlaps(Span left, Span right) {
        return right.max >= left.min && right.min <= left.max;
    }

    /**
     * five cases:
     * no overlap
     * left is inside right
     * right is inside left
     * right covers top of left
     * right covers bottom of left
     * @param left
     * @param right
     * @return
     */
    List<Span> split(Span left, Span right) {
        List<Span> list = new ArrayList<>();
        // no overlap
        if (!overlaps(left, right)) {
            return List.of(left); // no overlap
        }
        // right covers all of left
        if (right.min <= left.min && right.max >= left.max) {
            return list; // empty
        }
        // right is inside left (make 2 Spans)
        if (right.min >= left.min && right.max <= left.max) {
            // right inside left
            // make 2 segments
            list.add(Span.of(left.min, right.min));
            list.add(Span.of(right.max, left.max));
            return list;
        }
        if (right.min > left.min) {
            list.add(new Span(left.min, right.min));
        } else if (right.max < left.max) {
            list.add(new Span(right.max, left.max));
        }
        return list;
    }
}
