package com.reeltwo.plot.patterns;

import java.awt.Paint;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * JUnit tests for the DefaultColorGroup class.
 *
 * @author <a href=mailto:rlittin@reeltwo.com>Richard Littin</a>
 * @version $Revision$
 */
public class DefaultColorGroupTest extends AbstractPatternGroupTest {

  /**
   * Constructor (needed for JUnit)
   *
   * @param name A string which names the object.
   */
  public DefaultColorGroupTest(String name) {
    super(name);
  }


  public void setUp() {
    super.setUp();
  }


  public void tearDown() {
    super.tearDown();
  }

  public PatternGroup getPatternGroup() {
    return new DefaultColorGroup();
  }

  public void testDefaults() {
    PatternGroup pg = getPatternGroup();
    assertNotNull(pg);
    Paint [] patterns = pg.getPatterns();
    assertNotNull(patterns);
    assertEquals(8, patterns.length);

    for (int i = 0; i < patterns.length; i++) {
      assertNotNull(i + " is null", patterns[i]);
    }

    assertEquals("Default Colors", pg.getName());    
  }

  public static Test suite() {
    return new TestSuite(DefaultColorGroupTest.class);
  }


  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

}