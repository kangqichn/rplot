package com.reeltwo.plot.renderer;

import com.reeltwo.plot.BWPlot2D;
import com.reeltwo.plot.BWPoint2D;
import com.reeltwo.plot.Box2D;
import com.reeltwo.plot.BoxPlot2D;
import com.reeltwo.plot.Circle2D;
import com.reeltwo.plot.CirclePlot2D;
import com.reeltwo.plot.CurvePlot2D;
import com.reeltwo.plot.Datum2D;
import com.reeltwo.plot.Graph2D;
import com.reeltwo.plot.Plot2D;
import com.reeltwo.plot.Point2D;
import com.reeltwo.plot.PointPlot2D;
import com.reeltwo.plot.ScatterPlot2D;
import com.reeltwo.plot.ScatterPoint2D;
import com.reeltwo.plot.TextPlot2D;
import com.reeltwo.plot.TextPoint2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;


/**
 * Provides functions common across all renderers.
 *
 * @author Richard Littin (richard@reeltwo.com) 
 * @version $Revision$
 */
public abstract class AbstractRenderer {

  /** a number formatter */
  final NumberFormat mNF = NumberFormat.getInstance();

  private int mColorIndex = 0;

  private int mPointIndex = 0;

  private int mLineWidth = 1;

  private Mapping [] mMappings = null;

  /** A small class to hold information about tick spacing. */
  protected static class TicInfo {
    float mTic;
    float mMinorTic;
    int mStart;
    int mEnd;
    int mMinorStart;
    int mMinorEnd;
    int mMaxWidth;
    int mMaxHeight;
    String[] mLabels;
  }

  public Mapping [] getMappings() {
    // an array of Mapping [ x0, y0, x1, y1 ]
    return mMappings;
  }

  protected void setMappings(Mapping [] mappings) {
    mMappings = mappings;
  }

  abstract int getTextWidth(Object canvas, String text);
  abstract int getTextHeight(Object canvas, String text);
  abstract int getTextDescent(Object canvas, String text);

  // somthing to return ticinfo objects for each axis...

  // something to return bounds for axis labels, key titles...


  public void setColor(Object canvas, int colorIndex) {
    // ??
    mColorIndex = colorIndex;
  }

  public int getColor(Object canvas) {
    return mColorIndex;
  }

  abstract int setPlotColor(Object canvas, Plot2D plot, int colorIndex);

  protected void setPointIndex(int pointIndex) {
    mPointIndex = pointIndex;
  }

  protected int getPointIndex() {
    return mPointIndex;
  }

  // drawing primitives - protected
  protected void setLineWidth(Object canvas, int width) {
    mLineWidth = width;
  }

  protected int getLineWidth() {
    return mLineWidth;
  }

  protected abstract void setClip(Object canvas, int x, int y, int w, int h);

  protected abstract void drawString(Object canvas, int x, int y, String text);
  protected abstract void drawPoint(Object canvas, int x, int y);
  protected abstract void drawLine(Object canvas, int x1, int y1, int x2, int y2);
  protected void drawHorizontalLine(Object canvas, int x1, int x2, int y) {
    drawLine(canvas, x1, y, x2, y);
  }
  protected void drawVerticalLine(Object canvas, int x, int y1, int y2) {
    drawLine(canvas, x, y1, x, y2);
  }
  protected abstract void drawRectangle(Object canvas, int x, int y, int w, int h);
  protected abstract void fillRectangle(Object canvas, int x, int y, int w, int h);
  protected abstract void drawCircle(Object canvas, int x, int y, int diameter);
  protected abstract void fillCircle(Object canvas, int x, int y, int diameter);
  protected abstract void drawPolygon(Object canvas, int [] xs, int [] ys);
  protected abstract void fillPolygon(Object canvas, int [] xs, int [] ys);

  // methods to help when drawing curves
  private Point2D tangent(int x1, int y1, int x2, int y2) {
    float m = distance(x1, y1, x2, y2);
    if (m == 0) {
      return new Point2D(0.0f, 0.0f);
    }
    return new Point2D((x2 - x1) / m, (y2 - y1) / m);
  }


  private float distance(float x1, float y1, float x2, float y2) {
    float x = x2 - x1;
    float y = y2 - y1;
    return (float) Math.sqrt(x * x + y * y);
  }


  private int distanceX(int x1, int x2) {
    return Math.abs(x2 - x1);
  }

  private Point2D bezier(int [] xs, int [] ys, double mu) {
    int k, kn, nn, nkn;
    double blend, muk, munk;
    float x = 0, y = 0;

    muk = 1;
    munk = Math.pow(1 - mu, (double) xs.length - 1);

    for (k = 0; k < xs.length; k++) {
      nn = xs.length - 1;
      kn = k;
      nkn = nn - k;
      blend = muk * munk;
      muk *= mu;
      munk /= (1 - mu);
      while (nn >= 1) {
         blend *= nn;
         nn--;
         if (kn > 1) {
            blend /= (double) kn;
            kn--;
         }
         if (nkn > 1) {
            blend /= (double) nkn;
            nkn--;
         }
      }
      x += xs[k] * blend;
      y += ys[k] * blend;
    }
    return new Point2D(x, y);
  }

  private Point2D cubicBezier(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, double mu) {
    float cx = 3 * (x1 - x0);
    float cy = 3 * (y1 - y0);
    float bx = 3 * (x2 - x1) - cx;
    float by = 3 * (y2 - y1) - cy;
    float ax = x3 - x0 - cx - bx;
    float ay = y3 - y0 - cy - by;

    return new Point2D((float) ((((ax * mu + bx) * mu) + cx) * mu + x0),
                       (float) ((((ay * mu + by) * mu) + cy) * mu + y0));
  }


  private void doCurve(Object canvas, int [] xs, int [] ys, int type, boolean filled) {
    assert xs != null;
    assert ys != null;
    assert xs.length == ys.length;

    Polygon polygon = filled ? new Polygon() : null;

    if (type == CurvePlot2D.BSPLINE) {
      int m = 50;
      int x = 0, y = 0, x0, y0;
      boolean first = true;

      for (int i = 1; i < xs.length - 2 ; i++) {
        float xA = xs[i - 1]; 
        float xB = xs[i]; 
        float xC = xs[i + 1]; 
        float xD = xs[i + 2];
        float yA = ys[i - 1]; 
        float yB = ys[i];
        float yC = ys[i + 1];
        float yD = ys[i + 2];
        float a3 = (-xA + 3 * (xB - xC) + xD) / 6;
        float b3 = (-yA + 3 * (yB - yC) + yD) / 6;
        float a2 = (xA - 2 * xB + xC) / 2;
        float b2 = (yA - 2 * yB + yC) / 2;
        float a1 = (xC - xA) / 2;
        float b1 = (yC - yA) / 2;
        float a0 = (xA + 4 * xB + xC) / 6;
        float b0 = (yA + 4 * yB + yC) / 6;

        for (int j = 0; j <= m; j++) {
          x0 = x;
          y0 = y;
          float t = (float) j / (float) m;
          x = (int) (((a3 * t + a2) * t + a1) * t + a0);
          y = (int) (((b3 * t + b2) * t + b1) * t + b0);

          if (filled) {
            polygon.addPoint(x, y);
          } else {
            if (first) {
              first = false;
            } else {
              drawLine(canvas, x0, y0, x, y);
            }
          }
        }
      }
    } else if (type == CurvePlot2D.BEZIER) {
      int m = 50;

      int x = 0;
      int y = 0;
      boolean first = true;
      for (int j = 0; j < m; j++) {
        Point2D p = bezier(xs, ys, j / (double) m);
        int x0 = x;
        int y0 = y;
        x = (int) p.getX();
        y = (int) p.getY();
        if (filled) {
          polygon.addPoint(x, y);
        } else {
          if (first) {
            first = false;
          } else {
            drawLine(canvas, x0, y0, x, y);
          }
        }
      }

      if (filled) {
        polygon.addPoint(xs[xs.length - 1], ys[ys.length - 1]);
      }
    } else if (type == CurvePlot2D.CUBIC_BEZIER) {
      int x = 0;
      int y = 0;
      boolean first = true;
      for (int i = 1; i < xs.length - 2; i++) {
        int xim1 = xs[i - 1];
        int xi = xs[i];
        int xip1 = xs[i + 1];
        int xip2 = xs[i + 2];

        int yim1 = ys[i - 1];
        int yi = ys[i];
        int yip1 = ys[i + 1];
        int yip2 = ys[i + 2];
          
        // create tangent vector parallel to (pim1, pip1) whose length if min of the X magnitudes of (pi, pim1) and (pi, pip1) divided by 3
        float minMag = distanceX(xi, xip1);
        float mag = distanceX(xi, xim1);
        if (mag != 0 && mag < minMag) { // 0 length - assume at first point
          minMag = mag;
        }
        minMag /= 3.0f;
        Point2D tangent = tangent(xim1, yim1, xip1, yip1);
        float x1 = xi + minMag * tangent.getX();
        float y1 = yi + minMag * tangent.getY();
        
        // same thing around pip1
        minMag = distanceX(xip1, xi);
        mag = distanceX(xip1, xip2);
        if (mag != 0 && mag < minMag) {
          minMag = mag;
        }
        minMag /= 3.0f;
        tangent = tangent(xip2, yip2, xi, yi);

        float x2 = xip1 + minMag * tangent.getX();
        float y2 = yip1 + minMag * tangent.getY();

        //CubicCurve2D.Double cubic = new CubicCurve2D.Double();
        //cubic.setCurve(xi, yi, x1, y1, x2, y2, xip1, yip1);
        int m2 = Math.max(10, (int) (distance(x1, y1, x2, y2) / 5.0f));
        for (int j = 0; j <= m2; j++) {
          int x0 = x;
          int y0 = y;
          Point2D p = cubicBezier(xi, yi, x1, y1, x2, y2, xip1, yip1, j / (double) m2);
          x = (int) p.getX();
          y = (int) p.getY();
          if (filled) {
            // get the next bezier point
            polygon.addPoint(x, y);
          } else {
            //((Graphics2D) canvas).draw(cubic);
            if (first) {
              first = false;
            } else {
              drawLine(canvas, x0, y0, x, y);
            }
          }
        }       
      }
    }
    if (filled) {
      fillPolygon(canvas, polygon.getXs(), polygon.getYs());
    }
  }

  protected void drawCurve(Object canvas, int [] xs, int [] ys, int type) {
    doCurve(canvas, xs, ys, type, false);
  }

  protected void fillCurve(Object canvas, int [] xs, int [] ys, int type) {
    doCurve(canvas, xs, ys, type, true);
  }

  protected abstract int calculateKeyWidth(Object canvas, Graph2D graph);
  protected abstract int calculateKeyHeight(Object canvas, Graph2D graph);

  // functions that actully plot the different types of plots

  protected Mapping [] createMappings(Graph2D graph, int sxlo, int sylo, int sxhi, int syhi) {
    Mapping [] mappings = new Mapping[4]; // x1, y1, x2, y2

    mappings[0] = new Mapping(graph.getXLo(0), graph.getXHi(0), sxlo, sxhi, graph.getLogScaleX(0));
    mappings[1] = new Mapping(graph.getYLo(0), graph.getYHi(0), sylo, syhi, graph.getLogScaleY(0));
    mappings[2] = new Mapping(graph.getXLo(1), graph.getXHi(1), sxlo, sxhi, graph.getLogScaleX(1));
    mappings[3] = new Mapping(graph.getYLo(1), graph.getYHi(1), sylo, syhi, graph.getLogScaleY(1));
    
    return mappings;
  }

  private TicInfo calcXTicInfo(Object canvas, Graph2D graph, int whichTic) {
    if (graph.usesX(whichTic) && graph.getShowXTics(whichTic)) {
      TicInfo ticInfo = new TicInfo();
      ticInfo.mTic = graph.getXTic(whichTic);
      ticInfo.mMinorTic = graph.getXMinorTic(whichTic);
      setNumDecimalDigits(ticInfo.mTic);
      ticInfo.mStart = (int) (graph.getXLo(whichTic) / ticInfo.mTic);
      ticInfo.mEnd = (int) (graph.getXHi(whichTic) / ticInfo.mTic);
      if (ticInfo.mMinorTic > 0.0f) {
        ticInfo.mMinorStart = (int) (graph.getXLo(whichTic) / ticInfo.mMinorTic);
        ticInfo.mMinorEnd = (int) (graph.getXHi(whichTic) / ticInfo.mMinorTic);
      }

      ticInfo.mLabels = graph.getXTicLabels(whichTic);
      ticInfo.mMaxWidth = 0;
      ticInfo.mMaxHeight = 0;
      for (int k = ticInfo.mStart; k <= ticInfo.mEnd; k++) {
        float num = ticInfo.mTic * k;
        String snum = mNF.format(num);
        if (ticInfo.mLabels != null && ticInfo.mLabels.length != 0) {
          snum = ticInfo.mLabels[(k - ticInfo.mStart) % ticInfo.mLabels.length];
        }
        String [] nums = snum.split("\n");
        for (int i = 0; i < nums.length; i++) {
          int width = getTextWidth(canvas, nums[i]);
          if (width > ticInfo.mMaxWidth) {
            ticInfo.mMaxWidth = width;
          }
        }
        int height = nums.length * getTextHeight(canvas, snum);
        if (height > ticInfo.mMaxHeight) {
          ticInfo.mMaxHeight = height;
        }
      }
      return ticInfo;
    }
    return null;
  }

  private TicInfo calcYTicInfo(Object canvas, Graph2D graph, int whichTic) {
    if (graph.usesY(whichTic) && graph.getShowYTics(whichTic)) {
      TicInfo ticInfo = new TicInfo();
      ticInfo.mTic = graph.getYTic(whichTic);
      setNumDecimalDigits(ticInfo.mTic);
      ticInfo.mStart = (int) (graph.getYLo(whichTic) / ticInfo.mTic);
      ticInfo.mEnd = (int) (graph.getYHi(whichTic) / ticInfo.mTic);

      ticInfo.mLabels = graph.getYTicLabels(whichTic);
      ticInfo.mMaxWidth = 0;
      ticInfo.mMaxHeight = 0;
      for (int k = ticInfo.mStart; k <= ticInfo.mEnd; k++) {
        float num = ticInfo.mTic * k;
        String snum = mNF.format(num);
        if (ticInfo.mLabels != null && ticInfo.mLabels.length != 0) {
          snum = ticInfo.mLabels[(k - ticInfo.mStart) % ticInfo.mLabels.length];
        }
        int width = getTextWidth(canvas, snum);
        if (width > ticInfo.mMaxWidth) {
          ticInfo.mMaxWidth = width;
        }
        int height = getTextHeight(canvas, snum);
        if (height > ticInfo.mMaxHeight) {
          ticInfo.mMaxHeight = height;
        }
      }
      return ticInfo;
    }
    return null;
  }




  protected TicInfo [] createTicInfos(Object canvas, Graph2D graph) {
    TicInfo [] ticInfos = new TicInfo[4]; // x1, y1, x2, y2

    ticInfos[0] = calcXTicInfo(canvas, graph, 0);
    ticInfos[1] = calcYTicInfo(canvas, graph, 0);
    ticInfos[2] = calcXTicInfo(canvas, graph, 1);
    ticInfos[3] = calcYTicInfo(canvas, graph, 1);

    return ticInfos;
  }

  /**
   * Sets the number of decimal digits to display for a given tic size.
   *
   * @param ticSize tic size
   */
  protected void setNumDecimalDigits(float ticSize) {
    int digits = 0;
    float tens = 1.0f;
    for (int i = 0; i < 5; i++) {
      if (ticSize < tens) {
        digits++;
      }
      tens /= 10.0f;
    }
    mNF.setMinimumFractionDigits(digits);
    mNF.setMaximumFractionDigits(digits);
  }


  protected void drawData(Object canvas, Plot2D[] plots, Mapping[] mapping) {
    int colorIndex = 1;
    for (int j = 0; j < plots.length; j++) {
      Plot2D plot = plots[j];
      Mapping convertX = mapping[2 * plot.getXAxis()];
      Mapping convertY = mapping[2 * plot.getYAxis() + 1];
      colorIndex = setPlotColor(canvas, plot, colorIndex);

      int lineWidth = plot.getLineWidth();
      if (lineWidth < 1) {
        lineWidth = 1;
      }
      setLineWidth(canvas, lineWidth);

      setPointIndex(j);
      if (plot instanceof PointPlot2D) {
        drawPointPlot(canvas, (PointPlot2D) plot, convertX, convertY);
      } else if (plot instanceof BWPlot2D) {
        drawBWPlot(canvas, (BWPlot2D) plot, convertX, convertY);
      } else if (plot instanceof CurvePlot2D) {
        drawCurvePlot(canvas, (CurvePlot2D) plot, convertX, convertY);
      } else if (plot instanceof TextPlot2D) {
        drawTextPlot(canvas, (TextPlot2D) plot, convertX, convertY);
      } else if (plot instanceof ScatterPlot2D) {
        drawScatterPlot(canvas, (ScatterPlot2D) plot, convertX, convertY);
      } else if (plot instanceof BoxPlot2D) {
        drawBoxPlot(canvas, (BoxPlot2D) plot, convertX, convertY);
      } else if (plot instanceof CirclePlot2D) {
        drawCirclePlot(canvas, (CirclePlot2D) plot, convertX, convertY);
      }
    }
    setLineWidth(canvas, 1);
  }


  protected void drawPointPlot(Object canvas, PointPlot2D lplot, Mapping convertX, Mapping convertY) {
    boolean doLines = lplot.getLines();
    boolean doPoints = lplot.getPoints();
    boolean doFill = lplot.getFill();
    Datum2D[] points = lplot.getData();
    if (points != null && points.length != 0) {
      if (doFill) {
        Polygon polygon = new Polygon();
        for (int i = 0; i < points.length; i++) {
          Point2D point = (Point2D) points[i];
          int sptX = (int) convertX.worldToScreen(point.getX());
          int sptY = (int) convertY.worldToScreen(point.getY());
          polygon.addPoint(sptX, sptY);
        }
        fillPolygon(canvas, polygon.getXs(), polygon.getYs());
      } else {
        Point2D point = (Point2D) points[0];
        int lastX = (int) convertX.worldToScreen(point.getX());
        int lastY = (int) convertY.worldToScreen(point.getY());
        if (doPoints) {
          drawPoint(canvas, lastX, lastY);
        }
        drawLine(canvas, lastX, lastY, lastX, lastY);
        for (int i = 1; i < points.length; i++) {
          point = (Point2D) points[i];
          int sptX = (int) convertX.worldToScreen(point.getX());
          int sptY = (int) convertY.worldToScreen(point.getY());
          if (sptX != lastX || sptY != lastY) {
            if (doLines) {
              drawLine(canvas, lastX, lastY, sptX, sptY);
            }
            if (doPoints) {
              drawPoint(canvas, sptX, sptY);
            }
            drawLine(canvas, sptX, sptY, sptX, sptY);

            lastX = sptX;
            lastY = sptY;
          }
        }
      }
    }
  }

  protected void drawBWPlot(Object canvas, BWPlot2D bwplot, Mapping convertX, Mapping convertY) {
    Datum2D[] points = bwplot.getData();
    if (points != null && points.length != 0) {
      if (bwplot.getType() == BWPlot2D.STANDARD) {
        int width = bwplot.getWidth();
        for (int i = 0; i < points.length; i++) {
          BWPoint2D point = (BWPoint2D) points[i];
          int sptX = (int) convertX.worldToScreen(point.getX());
          int sptY1 = (int) convertY.worldToScreen(point.getY(0));
          int sptY2 = (int) convertY.worldToScreen(point.getY(1));
          drawLine(canvas, sptX, sptY1, sptX, sptY2);

          sptY1 = (int) convertY.worldToScreen(point.getY(3));
          drawRectangle(canvas, sptX - width / 2, sptY1, width, sptY2 - sptY1);

          sptY2 = (int) convertY.worldToScreen(point.getY(4));
          drawLine(canvas, sptX, sptY1, sptX, sptY2);

          sptY1 = (int) convertY.worldToScreen(point.getY(2));
          drawLine(canvas, sptX - width / 2, sptY1, sptX + width / 2, sptY1);
        }
      } else if (bwplot.getType() == BWPlot2D.MINIMAL) {
        for (int i = 0; i < points.length; i++) {
          BWPoint2D point = (BWPoint2D) points[i];
          int sptX = (int) convertX.worldToScreen(point.getX());
          int sptY1 = (int) convertY.worldToScreen(point.getY(0));
          int sptY2 = (int) convertY.worldToScreen(point.getY(1));
          drawLine(canvas, sptX, sptY1, sptX, sptY2);

          sptY1 = (int) convertY.worldToScreen(point.getY(3));
          sptY2 = (int) convertY.worldToScreen(point.getY(4));
          drawLine(canvas, sptX, sptY1, sptX, sptY2);

          sptY1 = (int) convertY.worldToScreen(point.getY(2));
          drawPoint(canvas, sptX, sptY1);
        }
        // } else if (bwplot.getType() == BWPlot2D.JOINED) {
        // can't be handled in general???
      }
    }
  }


  protected void drawTextPlot(Object canvas, TextPlot2D tplot, Mapping convertX, Mapping convertY) {
    Datum2D[] points = tplot.getData();
    int tHeight = getTextHeight(canvas, "A");
    
    int halign = tplot.getHorizontalAlignment();
    switch (halign) {
    case TextPlot2D.LEFT: halign = 0; break;
    case TextPlot2D.CENTER: halign = 1; break;
    case TextPlot2D.RIGHT: halign = 2; break;
    }

    int valign = tplot.getVerticalAlignment();
    int descent = getTextDescent(canvas, "A");
    switch (valign) {
    case TextPlot2D.CENTER: valign = tHeight / 2 - descent; break;
    case TextPlot2D.BASELINE: valign = 0; break;
    case TextPlot2D.TOP: valign = tHeight - descent; break;
    case TextPlot2D.BOTTOM: valign = -descent; break;
    }

    if (points != null && points.length != 0) {
      for (int i = 0; i < points.length; i++) {
        TextPoint2D point = (TextPoint2D) points[i];
        String text = point.getText();
        int sptX = (int) convertX.worldToScreen(point.getX());
        int sptY = (int) convertY.worldToScreen(point.getY());
        int sw = getTextWidth(canvas, text);

        drawString(canvas, (int) (sptX - halign * sw / 2.0f), (int) (sptY + valign), text);
      }
    }
  }


  protected void drawScatterPlot(Object canvas, ScatterPlot2D splot, Mapping convertX, Mapping convertY) {
    Datum2D[] points = splot.getData();
    if (points != null && points.length != 0) {
      float scatterFactor = Math.abs(splot.getScatterFactor()) + 1;
      Random random = new Random();
      for (int i = 0; i < points.length; i++) {
        ScatterPoint2D point = (ScatterPoint2D) points[i];

        float sptX = convertX.worldToScreen(point.getX());
        float sptY = convertY.worldToScreen(point.getY());

        if (point.getNumberOfPoints() > 0) {
          float sf = (float) (scatterFactor * Math.log(point.getNumberOfPoints()));
          for (int p = 0; p < point.getNumberOfPoints(); p++) {
            double radius = random.nextGaussian() * sf;
            double angle = random.nextFloat() * (2 * Math.PI);
            float xx = (float) (radius * Math.sin(angle));
            float yy = (float) (radius * Math.cos(angle));
            drawRectangle(canvas, (int) (sptX + xx), (int) (sptY + yy), 1, 1);
          }
        }
      }
    }
  }


  protected void drawBoxPlot(Object canvas, BoxPlot2D bplot, Mapping convertX, Mapping convertY) {
    Datum2D[] points = bplot.getData();

    if (points != null && points.length != 0) {
      boolean doFill = bplot.getFill();      
      boolean doBorder = bplot.getBorder();
      for (int i = 0; i < points.length; i++) {
        Box2D box = (Box2D) points[i];

        int x = (int) convertX.worldToScreen(box.getLeft());
        int y = (int) convertY.worldToScreen(box.getTop());
        int width = (int) convertX.worldToScreen(box.getRight()) - x;
        int height = (int) convertY.worldToScreen(box.getBottom()) - y;

        if (doFill) {
          fillRectangle(canvas, x, y, width, height);
          if (doBorder) {
            int color = getColor(canvas);
            setColor(canvas, 0);
            drawRectangle(canvas, x, y, width, height);
            setColor(canvas, color);
          }
        } else {
          drawRectangle(canvas, x, y, width, height);
        }
      }
    }
  }


  protected void drawCirclePlot(Object canvas, CirclePlot2D cplot, Mapping convertX, Mapping convertY) {
    boolean doFill = cplot.getFill();
    Datum2D[] points = cplot.getData();

    if (points != null && points.length != 0) {
      for (int i = 0; i < points.length; i++) {
        Circle2D circle = (Circle2D) points[i];
        float diameter = circle.getDiameter();

        int x = (int) convertX.worldToScreen(circle.getX());
        int y = (int) convertY.worldToScreen(circle.getY());

        int idiameter = (int) diameter + 1;

        if (doFill) {
          fillCircle(canvas, x, y, idiameter);
        } else {
          drawCircle(canvas, x, y, idiameter);
        }
      }
    }
  }


  protected void drawCurvePlot(Object canvas, CurvePlot2D cplot, Mapping convertX, Mapping convertY) {
    boolean doFill = cplot.getFill();
    int type = cplot.getType();
    Point2D[] points = (Point2D []) cplot.getData();

    int [] xs = new int[points.length];
    int [] ys = new int[points.length];
    for (int i = 0; i < points.length; i++) {
      xs[i] = (int) convertX.worldToScreen(points[i].getX());
      ys[i] = (int) convertY.worldToScreen(points[i].getY());
    }

    if (doFill) {
      fillCurve(canvas, xs, ys, type);
    } else {
      drawCurve(canvas, xs, ys, type);
    }
  }


  // our own special polygon class
  private static class Polygon {
    
    ArrayList mPoints = new ArrayList();

    public void addPoint(int x, int y) {
      mPoints.add(new Point2D(x, y));
    }

    public int [] getXs() {
      int [] xs = new int[mPoints.size()];
      for (int i = 0; i < mPoints.size(); i++) {
        xs[i] = (int) ((Point2D) mPoints.get(i)).getX();
      }
      return xs;
    }

    public int [] getYs() {
      int [] ys = new int[mPoints.size()];
      for (int i = 0; i < mPoints.size(); i++) {
        ys[i] = (int) ((Point2D) mPoints.get(i)).getY();
      }
      return ys;
    }
  }
}