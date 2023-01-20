package org.jungrapht.visualization.layout.algorithms.orthogonal;

import org.jungrapht.visualization.layout.model.Point;
import org.jungrapht.visualization.layout.model.Rectangle;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.IntStream;

public class TestMappings {

    Mappings<String> mappings = new Mappings<>();

    @Test
    public void testAddRemove() {
        Point r0011 = Point.of(0,0);
        Point r0111 = Point.of(0,1);
        Point r1011 = Point.of(1,0);
        Point r1111 = Point.of(1,1);
        Point r2211 = Point.of(2,2);
        // map 4 of the Rectangles
        mappings.accept("A", r0011);
        mappings.accept("B", r0111);
        mappings.accept("C", r1011);
        mappings.accept("D", r1111);

        Assert.assertEquals(4, mappings.entries().size());

        // try an update with the same mapped values
        mappings.accept("A", r0011);
        Assert.assertEquals(4, mappings.entries().size());

        // replace the Rectangle for 'A'
        mappings.accept("A", r2211);
        // size unchanged?
        Assert.assertEquals(4, mappings.entries().size());
        // test that the previous Rectangle for 'A' is gone
        Assert.assertFalse(mappings.rectangles().contains(r0011));

        // test that r1111 is not empty (its mapped to 'D')
        Assert.assertFalse(mappings.empty(r1111));
        // test that r0011 is empty (previously mapped to 'A')
        Assert.assertTrue(mappings.empty(r0011));

        // put the B rectangle in A
        mappings.accept("A", r0111);
    }

    /**
     * ensure that normalize leaves no (x,y) values that are < 0
     */
    @Test
    public void testNormalize() {
        for(int i=0; i<10; i++) {
            Mappings<String> mappings = new Mappings<>();
            IntStream.range(0,10).forEach(n ->
                    mappings.accept("V"+n, Point.of(
                            Math.random()*100-50, Math.random()*100-50))
            );
            mappings.normalize();
            Assert.assertTrue(
                    mappings.rectangles().stream()
                            .filter(r -> r.x < 0 || r.y < 0).findAny().isEmpty());
        }
    }
}
