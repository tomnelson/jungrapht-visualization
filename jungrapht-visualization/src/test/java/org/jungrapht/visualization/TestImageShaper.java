package org.jungrapht.visualization;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import org.jungrapht.visualization.util.ImageShapeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestImageShaper {

  BufferedImage image;

  @BeforeEach
  public void setUp() {
    int width = 6;
    int height = 5;
    image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        image.setRGB(j, i, 0x00000000);
      }
    }
    image.setRGB(3, 1, 0xffffffff);
    image.setRGB(4, 1, 0xffffffff);
    image.setRGB(2, 2, 0xffffffff);
    image.setRGB(4, 2, 0xffffffff);
    image.setRGB(1, 3, 0xffffffff);
    image.setRGB(2, 3, 0xffffffff);
    image.setRGB(3, 3, 0xffffffff);
    image.setRGB(4, 3, 0xffffffff);
  }

  @Test
  public void testShaper() {
    Shape shape = ImageShapeUtils.getShape(image, 30);
    //		System.err.println("complete shape = "+shape);
    float[] seg = new float[6];
    for (PathIterator i = shape.getPathIterator(null, 1); !i.isDone(); i.next()) {
      int ret = i.currentSegment(seg);
      //			if (ret == PathIterator.SEG_MOVETO) {
      //				System.err.println("move to "+seg[0]+","+seg[1]);
      //			} else if (ret == PathIterator.SEG_LINETO) {
      //				System.err.println("line to "+seg[0]+","+seg[1]);
      //			} else if(ret == PathIterator.SEG_CLOSE) {
      //				System.err.println("done");
      //			}
    }
  }
}
