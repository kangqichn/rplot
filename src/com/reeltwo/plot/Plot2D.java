package com.reeltwo.plot;

/**
 * Structure to hold attributes of a single plot on a 2D graph.
 *
 * @author <a href=mailto:rlittin@reeltwo.com>Richard Littin</a>
 * @version $Revision$
 */

public abstract class Plot2D {

  /** the title of this plot */
  private String mTitle = "";
  /** the color of this plot */
  private int mColor = -1; // black is 0, other colors are up to display device
  /** width of lines in rendering units */
  private int mLineWidth = 1;

  /** range bounds of points in plot */
  private float mXLo, mXHi;
  private float mYLo, mYHi;

  /** whether to use y2 axis - default is to use y1 axis */
  //private boolean mUsesY2 = false;

  private int mXAxis = 0;
  private int mYAxis = 0;

  /** data points in plot */
  private Datum2D[] mData = null;


  /** Default constructor. */
  public Plot2D() { }


  /**
   * Constructor setting whether to uses the y2 axis when plotting.
   * Default is to use the y1 axis.
   *
   * @param x TODO Description.
   * @param y TODO Description.
   */
  public Plot2D(int x, int y) {
    setXAxis(x);
    setYAxis(y);
  }


  private void setXAxis(int x) {
    if (x < 0 || x > Graph2D.NUM_X_AXES) {
      throw new IllegalArgumentException("X axis index out of range.");
    }
    mXAxis = x;
  }


  public int getXAxis() {
    return mXAxis;
  }


  private void setYAxis(int y) {
    if (y < 0 || y > Graph2D.NUM_Y_AXES) {
      throw new IllegalArgumentException("Y axis index out of range.");
    }
    mYAxis = y;
  }


  public int getYAxis() {
    return mYAxis;
  }


  /**
   * Sets the plot's title.
   *
   * @param title some text
   */
  public void setTitle(String title) {
    if (title != null) {
      mTitle = title;
    }
  }


  /**
   * Returns the plot's title.
   *
   * @return some text
   */
  public String getTitle() {
    return mTitle;
  }


  /**
   * Sets the plot's color
   *
   * @param color a Color
   */
  public void setColor(int color) {
    mColor = color;
  }


  /**
   * Returns the plot's color
   *
   * @return a Color
   */
  public int getColor() {
    return mColor;
  }


  /**
   * Sets the plot's line width to <code>width</code> rendering units.
   *
   * @param width an <code>int</code> value
   * @exception IllegalArgumentException if <code>width</code> is less than 1
   */
  public void setLineWidth(int width) {
    if (width < 1) {
      throw new IllegalArgumentException("Line width must be greater than or equal to 1: " + width);
    }
    mLineWidth = width;
  }


  /**
   * Returns the plot's rendering line width.
   *
   * @return line width
   */
  public int getLineWidth() {
    return mLineWidth;
  }


  /**
   * Sets the data used in this plot.
   *
   * @param data an array of Datum2D's
   */
  public void setData(Datum2D[] data) {
    mData = data;
    setRanges();
  }


  /**
   * Returns the data used in this plot.
   *
   * @return an array of Datum2D's
   */
  public Datum2D[] getData() {
    return mData;
  }


  /** Returns whether this plot uses the y2 axis.  */
  //public boolean usesY2() {
  //return mUsesY2;
  //}

  /**
   * Calculates the upper and lower bounds for x and y ranges of the
   * data in the plot.
   */
  private void setRanges() {
    if (mData == null || mData.length == 0) {
      mXLo = mXHi = 0.0f;
      mYLo = mYHi = 0.0f;
    } else {
      Datum2D d = mData[0];
      mXLo = d.getXLo();
      mXHi = d.getXHi();
      mYLo = d.getYLo();
      mYHi = d.getYHi();
      for (int i = 1; i < mData.length; i++) {
        d = mData[i];
        if (d.getXLo() < mXLo) {
          mXLo = d.getXLo();
        }
        if (d.getXHi() > mXHi) {
          mXHi = d.getXHi();
        }
        if (d.getYLo() < mYLo) {
          mYLo = d.getYLo();
        }
        if (d.getYHi() > mYHi) {
          mYHi = d.getYHi();
        }
      }
    }
  }


  /**
   * Returns the lower bound of the x range.
   *
   * @return a number
   */
  public float getXLo() {
    return mXLo;
  }


  /**
   * Returns the upper bound of the x range.
   *
   * @return a number
   */
  public float getXHi() {
    return mXHi;
  }


  /**
   * Returns the lower bound of the y range.
   *
   * @return a number
   */
  public float getYLo() {
    return mYLo;
  }


  /**
   * Returns the upper bound of the y range.
   *
   * @return a number
   */
  public float getYHi() {
    return mYHi;
  }
}