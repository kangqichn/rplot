package com.reeltwo.plot;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * JUnit tests for the BWPointPlot2D class.
 *
 * @author <a href=mailto:rlittin@reeltwo.com>Richard Littin</a>
 * @version $Revision$
 */

public class BWPlot2DTest extends AbstractPlot2DTest {

  /**
   * Constructor (needed for JUnit)
   *
   * @param name A string which names the object.
   */
  public BWPlot2DTest(String name) {
    super(name);
  }


  public Plot2D getPlot() {
    return new BWPlot2D();
  }


  public Plot2D getPlot(int x, int y) {
    return new BWPlot2D(x, y);
  }


  public Datum2D[] getData() {
    return new BWPoint2D[]{new BWPoint2D(1, 2, 3, 4, 5, 6), new BWPoint2D(5, 6, 7, 8, 9, 10)};
  }


  public void testSets() {
    BWPlot2D plot = (BWPlot2D) getPlot();

    assertEquals(BWPlot2D.STANDARD, plot.getType());
    plot.setType(BWPlot2D.MINIMAL);
    assertEquals(BWPlot2D.MINIMAL, plot.getType());
    plot.setType(BWPlot2D.JOINED);
    assertEquals(BWPlot2D.JOINED, plot.getType());
    plot.setType(BWPlot2D.STANDARD);
    assertEquals(BWPlot2D.STANDARD, plot.getType());

    assertEquals(20, plot.getWidth());
    plot.setWidth(10);
    assertEquals(10, plot.getWidth());
  }

  public void testBadArgs() {
    BWPlot2D plot = (BWPlot2D) getPlot();

    try {
      plot.setType(BWPlot2D.STANDARD - 1);
      fail("accepted bad type");
    } catch (IllegalArgumentException e) {
      ; // expected
    }
    try {
      plot.setType(BWPlot2D.JOINED + 1);
      fail("accepted bad type");
    } catch (IllegalArgumentException e) {
      ; // expected
    }
    try {
      plot.setWidth(0);
      fail("accepted bad width");
    } catch (IllegalArgumentException e) {
      ; // expected
    }
  }

  public static Test suite() {
    return new TestSuite(BWPlot2DTest.class);
  }


  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

}