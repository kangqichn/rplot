package com.reeltwo.plot;

/**
 * Structure to hold attributes of a group of labels on a 2D graph.
 *
 * @author Richard Littin
 */

public class TextPlot2D extends Plot2D {

  /**
   * Enumeration of possible text alignments in a text plot.
   */
  public enum TextAlignment {
    /** Text centered about point.*/
    CENTER,
    /** Text aligned with left edge on point. */
    LEFT,
    /** Text aligned with right edge on point. */
    RIGHT,
    /** Text aligned with top edge on point. */
    BASELINE,
    /** Text aligned with top edge on point. */
    TOP,
    /** Text aligned with bottom edge on point. */
    BOTTOM,
  }

  /** alignment of text */
  private TextAlignment mHorizontalAlignment = TextAlignment.CENTER;
  private TextAlignment mVerticalAlignment = TextAlignment.BASELINE;

  /** inverse video on text */
  private boolean mInvert = false;

  /** use graph fg color rather than plot colors */
  private boolean mUseFGColor = true;


  private boolean mVertical = false;

  /**
   * @return whether the text should be drawing vertically
   */
  public boolean isVertical() {
    return mVertical;
  }

  /**
   * @param v whether the text should be drawing vertically
   */
  public void setVertical(boolean v) {
    mVertical = v;
  }

  /**
   * Default constructor setting the default axes used to the bottom
   * x axis and the left y axis.
   */
  public TextPlot2D() {
    super();
  }


  /**
   * Constructor setting which axes to use in the plot.
   *
   * @param x x axis to use.
   * @param y y axis to use.
   */
  public TextPlot2D(Edge x, Edge y) {
    super(x, y);
  }


  /**
   * Sets the horizontal alignment of the text relative to the point.
   * Valid alignment values are <code>LEFT</code>, <code>CENTER</code>
   * (the default) and <code>RIGHT</code>.
   *
   * @param alignment an alignment value
   * @exception IllegalArgumentException if an invalid horizontal
   * alignment value is given
   */
  public void setHorizontalAlignment(TextAlignment alignment) {
    switch (alignment) {
    case CENTER:
    case LEFT:
    case RIGHT:
      mHorizontalAlignment = alignment;
      break;
    default:
      throw new IllegalArgumentException("Bad horizontal alignment value: " + alignment);
    }
  }


  /**
   * Returns the horizontal alignment of the text.
   *
   * @return an alignment value
   */
  public TextAlignment getHorizontalAlignment() {
    return mHorizontalAlignment;
  }


  /**
   * Sets the vertical alignment of the text relative to the point.
   * Valid alignment values are <code>TOP</code>, <code>CENTER</code>
   * (the default), <code>BASELINE</code> and <code>BOTTOM</code>.
   *
   * @param alignment an alignment value
   * @exception IllegalArgumentException if an invalid vertical
   * alignment value is given
   */
  public void setVerticalAlignment(TextAlignment alignment) {
    switch (alignment) {
    case CENTER:
    case BASELINE:
    case TOP:
    case BOTTOM:
      mVerticalAlignment = alignment;
      break;
    default:
      throw new IllegalArgumentException("Bad vertical alignment value: " + alignment);
    }
  }


  /**
   * Returns the vertical alignment of the text.
   *
   * @return an alignment value
   */
  public TextAlignment getVerticalAlignment() {
    return mVerticalAlignment;
  }


  /**
   * Sets whether to draw text in inverse video mode.
   *
   * @param flag invert
   */
  public void setInvert(boolean flag) {
    mInvert = flag;
  }


  /**
   * Returns whether inverse video mode is set.
   *
   * @return inverse video mode set
   */
  public boolean isInvert() {
    return mInvert;
  }

  /**
   * Sets whether to draw text using the graphs foreground color or one
   * of the automatically selected plot colors.
   *
   * @param flag use foreground color
   */
  public void setUseFGColor(boolean flag) {
    mUseFGColor = flag;
  }


  /**
   * Returns whether using foreground color.
   *
   * @return using foreground color
   */
  public boolean isUseFGColor() {
    return mUseFGColor;
  }

  /**
   * Sets the data used in this plot.
   *
   * @param data an array of Datum2D's
   */
  @Override
  public void setData(Datum2D[] data) {
    if (!(data instanceof TextPoint2D[])) {
      throw new UnsupportedOperationException("Cannot set " + data.getClass().getName() + " in TextPlot2D.");
    }
    super.setData(data);
  }
}
