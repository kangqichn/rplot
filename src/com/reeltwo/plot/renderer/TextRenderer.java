package com.reeltwo.plot.renderer;

import com.reeltwo.plot.Graph2D;
import com.reeltwo.plot.Plot2D;
import com.reeltwo.plot.Point2D;

/**
 * Code to render a Graph2D object to a String.
 *
 * @author <a href=mailto:rlittin@reeltwo.com>Richard Littin</a>
 * @version $Revision$
 */
public class TextRenderer extends AbstractRenderer {
  private static final char SPACE = ' ';
  private static final char CR = '\n';

  private static final char[] LINE_CHARS = new char[] {'*', '#', '$', '%', '@', '&', '=', 'o'};
  // ANSI escape sequence color codes
  private static final String[] COLORS =
    new String[]{"",
      "31", "32", "34", "33", "35", "36",
      "1;31", "1;32", "1;34", "1;33", "1;35", "1;36",
      "41", "42", "44", "43", "45", "46",
      "1;41", "1;42", "1;44", "1;43", "1;45", "1;46"};


  // inherited from AbstractRenderer
  public int getTextWidth(Object canvas, String text) {
    return text.length();
  }

  public int getTextHeight(Object canvas, String text) {
    return 1;
  }

  public int getTextDescent(Object canvas, String text) {
    return 0;
  }

  public int setPlotColor(Object canvas, Plot2D plot, int colorIndex) {
    int plotColorIndex = plot.getColor();
    if (plotColorIndex < 0) {
      plotColorIndex = colorIndex;
      colorIndex = (colorIndex + 1) % COLORS.length;
      if (colorIndex == 0) {
        colorIndex = 1;
      }
      plot.setColor(plotColorIndex);
    } else if (plotColorIndex >= COLORS.length) {
      plotColorIndex = (plotColorIndex + 1) % COLORS.length;
      if (plotColorIndex == 0) {
        plotColorIndex = 1;
      }
      plot.setColor(plotColorIndex);
    }
    ((Canvas) canvas).setColor(plotColorIndex);
    return colorIndex; 
  }

  public void setClip(Object canvas, int x, int y, int w, int h) {
    ((Canvas) canvas).setClipRectangle(x, y, w, h);
  }

  public void drawString(Object canvas, int x, int y, String text) {
    for (int k = 0; k < text.length(); k++) {
      ((Canvas) canvas).putChar(x + k, y, text.charAt(k));
    }
  }

  private char getLineChar() {
    return LINE_CHARS[getPointIndex() % LINE_CHARS.length];
  }

  public void drawPoint(Object canvas, int x, int y) {
    ((Canvas) canvas).putChar(x, y, getLineChar());
  }

  public void drawLine(Object canvas, int x1, int y1, int x2, int y2) {
    ((Canvas) canvas).putChar(x1, y1, x2, y2, getLineChar());
  }

  public void drawRectangle(Object canvas, int x, int y, int w, int h) {
    drawLine(canvas, x, y, x + w, y);
    drawLine(canvas, x, y, x, y + h);
    drawLine(canvas, x, y + h, x + w, y + h);
    drawLine(canvas, x + w, y, x + w, y + h);
  }

  public void fillRectangle(Object canvas, int x, int y, int w, int h) {
    for (int i = 0; i <= h; i++) {
      drawLine(canvas, x, y + i, x + w, y + i);
    }
  }


  public void drawCircle(Object canvas, int x, int y, int diameter) {
    int radius = diameter / 2;

    drawPoint(canvas, x, y + radius);
    drawPoint(canvas, x, y - radius);
    drawPoint(canvas, x + radius, y);
    drawPoint(canvas, x - radius, y);

    int r2 = radius * radius;
    int xr = 1;
    int yr = (int) (Math.sqrt(r2 - 1) + 0.5);
    while (xr < yr) {
      drawPoint(canvas, x + xr, y + yr);
      drawPoint(canvas, x + xr, y - yr);
      drawPoint(canvas, x - xr, y + yr);
      drawPoint(canvas, x - xr, y - yr);
      drawPoint(canvas, x + yr, y + xr);
      drawPoint(canvas, x + yr, y - xr);
      drawPoint(canvas, x - yr, y + xr);
      drawPoint(canvas, x - yr, y - xr);
      xr += 1;
      yr = (int) (Math.sqrt(r2 - xr * xr) + 0.5);
    }
    if (xr == yr) {
      drawPoint(canvas, x + xr, y + yr);
      drawPoint(canvas, x + xr, y - yr);
      drawPoint(canvas, x - xr, y + yr);
      drawPoint(canvas, x - xr, y - yr);
    }
        
  }

  public void fillCircle(Object canvas, int x, int y, int diameter) {
    drawCircle(canvas, x, y, diameter);
  }

  public void drawPolygon(Object canvas, int [] xs, int [] ys) {
    if (xs.length != 0) {
      for (int i = 1; i < xs.length; i++) {
        drawLine(canvas, xs[i - 1], ys[i - 1], xs[i], ys[i]);
      }
      drawLine(canvas, xs[xs.length - 1], ys[ys.length - 1], xs[0], ys[0]);
    }
  }

  public void fillPolygon(Object canvas, int [] xs, int [] ys) {
    // todo
    for (int y = 0; y < ((Canvas) canvas).getHeight(); y++) {
      for (int x = 0; x < ((Canvas) canvas).getWidth(); x++) {
        if (inside(xs, ys, x, y)) {
          drawPoint(canvas, x, y);
        }
      }
    }
  }


  ///////
  public String drawGraph(Graph2D graph, int screenWidth, int screenHeight, boolean inColor) {
    if (graph == null) {
      return null;
    }
    Canvas canvas = new Canvas(screenWidth, screenHeight);
    Mapping[] mapping = drawPeriphery(graph, canvas);
    drawData(canvas, graph.getPlots(), mapping);
    drawKey(graph, canvas);
    return canvas.toString(inColor);
  }


  private Mapping[] drawPeriphery(Graph2D graph, Canvas canvas) {
    Mapping[] mapping = new Mapping[4];
    int sxlo = 0;
    int sxhi = canvas.getWidth() - 1;
    int sylo = canvas.getHeight() - 2;
    int syhi = 0;
    final String title = graph.getTitle();
    if (title.length() != 0) {
      syhi++;
    }
    if (graph.usesX(0) && graph.getXLabel(0).length() > 0) {
      sylo--;
    }
    if ((graph.usesX(1) && graph.getXLabel(1).length() > 0)
      || (graph.usesY(0) && graph.getYLabel(0).length() > 0)
      || (graph.usesY(1) && graph.getYLabel(1).length() > 0)) {
      syhi++;
    }

    if (graph.usesX(0) && graph.getShowXTics(0)) {
      sylo--;
    }
    if (graph.usesX(1) && graph.getShowXTics(1)) {
      syhi++;
    }

    canvas.setColorDefault();
    // titles
    int xstart = canvas.getWidth() / 2 - title.length() / 2;
    for (int i = 0; i < title.length(); i++) {
      canvas.putChar(xstart + i, 0, title.charAt(i));
    }

    if (graph.getBorder()) {
      // Draw labels
      drawLabels(graph, canvas, sxlo, sylo, sxhi, syhi);

      final float xlo = graph.getXLo(0);
      final float xhi = graph.getXHi(0);
      final float ylo = graph.getYLo(0);
      final float yhi = graph.getYHi(0);
      TicInfo yTicInfo = calcYTicSize(graph, 0, ylo, yhi);
      if (yTicInfo != null) {
        sxlo = yTicInfo.mMaxWidth;
      }

      final float x2lo = graph.getXLo(1);
      final float x2hi = graph.getXHi(1);
      final float y2lo = graph.getYLo(1);
      final float y2hi = graph.getYHi(1);
      TicInfo y2TicInfo = calcYTicSize(graph, 1, y2lo, y2hi);
      if (y2TicInfo != null) {
        sxhi -= y2TicInfo.mMaxWidth;
      }
      mapping[0] = new Mapping(xlo, xhi, sxlo, sxhi);
      mapping[1] = new Mapping(ylo, yhi, sylo, syhi);
      mapping[2] = new Mapping(x2lo, x2hi, sxlo, sxhi);
      mapping[3] = new Mapping(y2lo, y2hi, sylo, syhi);

      // border
      drawBorder(canvas, sxlo, sylo, sxhi, syhi);

      // scales
      drawYTics(graph, canvas, 0, yTicInfo, mapping[1], sxlo, sxhi, sylo, syhi);
      drawYTics(graph, canvas, 1, y2TicInfo, mapping[3], sxlo, sxhi, sylo, syhi);
      drawXTics(graph, canvas, 0, mapping[0], xlo, xhi, sxlo, sxhi, sylo, syhi);
      drawXTics(graph, canvas, 1, mapping[2], xlo, xhi, sxlo, sxhi, sylo, syhi);
    } else {
      mapping[0] = new Mapping(graph.getXLo(0), graph.getXHi(0), sxlo, sxhi);
      mapping[1] = new Mapping(graph.getYLo(0), graph.getYHi(0), sylo, syhi);
      mapping[2] = new Mapping(graph.getXLo(1), graph.getXHi(1), sxlo, sxhi);
      mapping[3] = new Mapping(graph.getYLo(1), graph.getYHi(1), sylo, syhi);
    }
    // set clipping rectangle for canvas - to keep inside graph...
    canvas.setClipRectangle(sxlo, syhi, sxhi + 1, sylo + 1);
    return mapping;
  }


  public int calculateKeyWidth(Object canvas, Graph2D graph) {
    int keyWidth = 0;
    if (graph.getShowKey()) {
      String keyTitle = graph.getKeyTitle();
      if (keyTitle != null) {
        keyWidth += getTextWidth(canvas, keyTitle);
      } else {
        keyWidth += getTextWidth(canvas, "KEY");
      }
    }

    keyWidth += 1; // for ':'

    Plot2D[] plots = graph.getPlots();
    for (int j = 0; j < plots.length; j++) {
      Plot2D plot = plots[j];
      String dtitle = plot.getTitle();
      if (dtitle != null && dtitle.length() != 0
          && plot.getData() != null && plot.getData().length != 0) {
        int sw = getTextWidth(canvas, dtitle) + 4; // + point and spaces and comma
        keyWidth += sw;
      }
    }
    if (plots.length > 0) {
      keyWidth -= 1; // remove last comma
    }
    return keyWidth;
  }

  public int calculateKeyHeight(Object canvas, Graph2D graph) {
    return graph.getShowKey() ? getTextHeight(canvas, "A") : 0;
  }

  private void drawKey(Graph2D graph, Canvas canvas) {
    canvas.setColorDefault();
    canvas.setClipRectangle(0, 0, canvas.getWidth(), canvas.getHeight());
    String key = graph.getKeyTitle();
    if (key == null || key.length() == 0) {
      key = "KEY";
    }
    key += ":";
    for (int i = 0; i < key.length(); i++) {
      canvas.putChar(i, canvas.getHeight() - 1, key.charAt(i));
    }

    int offset = key.length() + 1;

    boolean comma = false;
    final Plot2D[] plots = graph.getPlots();
    for (int j = 0; j < plots.length; j++) {
      setPointIndex(j);
      if ((key = plots[j].getTitle()) != null && key.length() != 0) {
        if (comma) {
          canvas.setColorDefault();
          canvas.putChar(offset, canvas.getHeight() - 1, ',');
          offset += 2;
        }
        canvas.setColor(plots[j].getColor());
        drawPoint(canvas, offset, canvas.getHeight() - 1);
        offset += 2;
        for (int i = 0; i < key.length(); i++) {
          canvas.putChar(offset + i, canvas.getHeight() - 1, key.charAt(i));
        }
        offset += key.length();
        comma = true;
      }
    }
  }


  private void drawBorder(Canvas canvas, int sxlo, int sylo, int sxhi, int syhi) {
    for (int i = sxlo + 1; i < sxhi; i++) {
      canvas.putChar(i, sylo, '-');
      canvas.putChar(i, syhi, '-');
    }
    for (int i = syhi + 1; i < sylo; i++) {
      canvas.putChar(sxlo, i, '|');
      canvas.putChar(sxhi, i, '|');
    }
  }


  private void drawYTics(Graph2D graph, Canvas canvas, int whichTic, TicInfo ticInfo, Mapping mapping, int sxlo, int sxhi, int sylo, int syhi) {
    if (graph.usesY(whichTic) && graph.getShowYTics(whichTic)) {
      setNumDecimalDigits(ticInfo.mTic);
      for (int k = ticInfo.mStart; k <= ticInfo.mEnd; k++) {
        float num = ticInfo.mTic * k;
        int y = (int) mapping.worldToScreen(num);

        if (y >= syhi && y <= sylo) {
          String snum = mNF.format(num);
          if (ticInfo.mLabels != null && ticInfo.mLabels.length != 0) {
            snum = ticInfo.mLabels[k % ticInfo.mLabels.length];
          }
          int yy = (whichTic == 0) ? sxlo - snum.length() : sxhi + 1;
          for (int i = 0; i < snum.length(); i++) {
            char ch = snum.charAt(i);
            canvas.putChar(yy + i, y, ch);
          }
          if (whichTic == 0) {
            canvas.putChar(sxlo, y, '+');
            if (!graph.usesY(1)) {
              canvas.putChar(sxhi, y, '+');
            }
          } else {
            canvas.putChar(sxhi, y, '+');
            if (!graph.usesY(0)) {
              canvas.putChar(sxlo, y, '+');
            }
          }
        }
      }
    }
  }


  private void drawXTics(Graph2D graph, Canvas canvas, int whichTic, Mapping mapping, float xlo, float xhi, int sxlo, int sxhi, int sylo, int syhi) {
    if (graph.usesX(whichTic) && graph.getShowXTics(whichTic)) {
      float xtic = graph.getXTic(whichTic);
      setNumDecimalDigits(xtic);
      
      int start = (int) (xlo / xtic);
      int end = (int) (xhi / xtic);
      String[] xLabels = graph.getXTicLabels(whichTic);
      for (int k = start; k <= end; k++) {
        float num = xtic * k;
        int x = (int) mapping.worldToScreen(num);
        
        if (x >= sxlo && x <= sxhi) {
          if (whichTic == 0) {
            canvas.putChar(x, sylo, '+');
            if (!graph.usesX(1)) {
              canvas.putChar(x, syhi, '+');
            }
          } else {
            canvas.putChar(x, syhi, '+');
            if (!graph.usesX(0)) {
              canvas.putChar(x, sylo, '+');
            }
          }
        
          String snum = mNF.format(num);
          if (xLabels != null && xLabels.length != 0) {
            snum = xLabels[k % xLabels.length];
          }
        
          int xx = x - snum.length() / 2;
          for (int i = 0; i < snum.length(); i++) {
            char ch = snum.charAt(i);
            canvas.putChar(xx + i, (whichTic == 0) ? sylo + 1 : syhi - 1, ch);
          }
        }
      }
    }
  }


  private TicInfo calcYTicSize(Graph2D graph, int whichTic, float ylo, float yhi) {
    if (graph.usesY(whichTic) && graph.getShowYTics(whichTic)) {
      TicInfo ticInfo = new TicInfo();
      ticInfo.mTic = graph.getYTic(whichTic);
      setNumDecimalDigits(ticInfo.mTic);
      ticInfo.mStart = (int) (ylo / ticInfo.mTic);
      ticInfo.mEnd = (int) (yhi / ticInfo.mTic);
      ticInfo.mMaxWidth = 0;
      ticInfo.mLabels = graph.getYTicLabels(whichTic);
      for (int k = ticInfo.mStart; k <= ticInfo.mEnd; k++) {
        float num = ticInfo.mTic * k;
        String snum = mNF.format(num);
        if (ticInfo.mLabels != null && ticInfo.mLabels.length != 0) {
          snum = ticInfo.mLabels[k % ticInfo.mLabels.length];
        }
        if (snum.length() > ticInfo.mMaxWidth) {
          ticInfo.mMaxWidth = snum.length();
        }
      }
      return ticInfo;
    }
    return null;
  }


  private void drawLabels(Graph2D graph, Canvas canvas, int sxlo, int sylo, int sxhi, int syhi) {
    String ylabel = graph.getYLabel(0);
    if (graph.usesY(0) && ylabel.length() > 0) {
      for (int i = 0; i < ylabel.length(); i++) {
        canvas.putChar(i, syhi - 2, ylabel.charAt(i));
      }
    }
    ylabel = graph.getYLabel(1);
    if (graph.usesY(1) && ylabel.length() > 1) {
      for (int i = 0; i < ylabel.length(); i++) {
        canvas.putChar(sxhi + 1 - ylabel.length() + i, syhi - 2, ylabel.charAt(i));
      }
    }

    int centerWidth = (sxhi + 1) / 2;
    String xlabel = graph.getXLabel(0);
    if (graph.usesX(0) && xlabel.length() > 0) {
      int labelHeight = sylo + 1 + ((graph.usesX(0) && graph.getShowXTics(0)) ? 1 : 0);
      int xstart = centerWidth - xlabel.length() / 2;
      for (int i = 0; i < xlabel.length(); i++) {
        canvas.putChar(xstart + i, labelHeight, xlabel.charAt(i));
      }
    }
    xlabel = graph.getXLabel(1);
    if (graph.usesX(1) && xlabel.length() > 0) {
      int xstart = centerWidth - xlabel.length() / 2;
      for (int i = 0; i < xlabel.length(); i++) {
        canvas.putChar(xstart + i, syhi - 2, xlabel.charAt(i));
      }
    }
  }


  private static boolean inside(int [] xs, int [] ys, int x, int y) {
    if (xs == null || xs.length <= 2) {
      return false;
    }
    int hits = 0;

    int lastx = xs[xs.length - 1];
    int lasty = ys[ys.length - 1];
    int curx;
    int cury;

    // Walk the edges of the polygon
    for (int i = 0; i < xs.length; lastx = curx, lasty = cury, i++) {
      curx = xs[i];
      cury = ys[i];

      if (cury == lasty) {
        continue;
      }

      int leftx;
      if (curx < lastx) {
        if (x >= lastx) {
          continue;
        }
        leftx = curx;
      } else {
        if (x >= curx) {
          continue;
        }
        leftx = lastx;
      }

      float test1;
      float test2;
      if (cury < lasty) {
        if (y < cury || y >= lasty) {
          continue;
        }
        if (x < leftx) {
          hits++;
          continue;
        }
        test1 = x - curx;
        test2 = y - cury;
      } else {
        if (y < lasty || y >= cury) {
          continue;
        }
        if (x < leftx) {
          hits++;
          continue;
        }
        test1 = x - lastx;
        test2 = y - lasty;
      }
      if (test1 < (test2 / (lasty - cury) * (lastx - curx))) {
        hits++;
      }
    }
    return ((hits & 1) != 0);
  }


  private static class Canvas {
    private final int mWidth, mHeight;
    private int mClipLeft, mClipRight, mClipTop, mClipBottom;
    private final char[] mCanvas; // grid of characters
    private final byte[] mColors; // grid of colors
    private byte mColor; // current color to use


    Canvas(int width, int height) {
      mWidth = width;
      mHeight = height;
      setClipRectangle(0, 0, width, height);

      mCanvas = new char[mHeight * (mWidth + 1)];
      mColors = new byte[mHeight * (mWidth + 1)];
      int i = 0;
      for (int y = 0; y < mHeight; y++) {
        for (int x = 0; x < mWidth; x++) {
          mColors[i] = 0;
          mCanvas[i++] = SPACE;
        }
        mCanvas[i++] = CR;
      }
    }

    private int getWidth() {
      return mWidth;
    }

    private int getHeight() {
      return mHeight;
    }

    private void setColorDefault() {
      mColor = 0;
    }


    private void setColor(int color) {
      mColor = (byte) color;
    }


    private void setClipRectangle(int left, int top, int right, int bottom) {
      mClipLeft = Math.max(left, 0);
      mClipRight = Math.min(right, mWidth);
      mClipTop = Math.max(top, 0);
      mClipBottom = Math.min(bottom, mHeight);
    }


    private void putChar(int x, int y, char c) {
      if (x >= mClipLeft && x < mClipRight && y >= mClipTop && y < mClipBottom) {
        // in bounds
        mCanvas[y * (mWidth + 1) + x] = c;
        mColors[y * (mWidth + 1) + x] = mColor;
      }
    }
    //private void putChar(Point2D pt,char c) {
    //putChar((int)pt.getX(),(int)pt.getY(),c);
    //}
    private void putChar(Point2D ptStart, Point2D ptEnd, char c) {
      putChar((int) ptStart.getX(), (int) ptStart.getY(), (int) ptEnd.getX(), (int) ptEnd.getY(), c);
    }


    private void putChar(int startX, int startY, int endX, int endY, char c) {
      int sx;
      int ex;
      int sy;
      int ey;

      if (Math.abs(endX - startX) >= Math.abs(endY - startY)) {
        if (startX <= endX) {
          sx = startX;
          sy = startY;
          ex = endX;
          ey = endY;
        } else {
          ex = startX;
          ey = startY;
          sx = endX;
          sy = endY;
        }
      } else {
        if (startY <= endY) {
          sx = startX;
          sy = startY;
          ex = endX;
          ey = endY;
        } else {
          ex = startX;
          ey = startY;
          sx = endX;
          sy = endY;
        }
      }

      if (ex - sx >= ey - sy) {
        int range = ex - sx;
        if (range == 0) {
          putChar(sx, sy, c);
        } else {
          for (int i = 0; i <= range; i++) {
            putChar(sx + i, sy + (ey - sy) * i / range, c);
          }
        }
      } else {
        int range = ey - sy;
        if (range == 0) {
          putChar(sx, sy, c);
        } else {
          for (int i = 0; i <= range; i++) {
            putChar(sx + (ex - sx) * i / range, sy + i, c);
          }
        }
      }
      //putChar(sx,sy,'s');
      //putChar(ex,ey,'e');
    }


    private char getChar(int x, int y) {
      if (x >= 0 && x <= mWidth && y >= 0 && y <= mHeight) {
        return mCanvas[y * (mWidth + 1) + x];
      }
      return '\0';
    }
    //private char getChar(Point2D pt) {
    //return getChar((int)pt.getX(),(int)pt.getY());
    //}
    public String toString() {
      return toString(false);
    }


    public String toString(boolean inColor) {
      String cr = System.getProperty("line.separator");
      if (inColor || !cr.equals("" + CR)) {
        StringBuffer s = new StringBuffer();
        //String lineChars = new String(LINE_CHARS);

        for (int i = 0; i < mCanvas.length; i++) {
          //int index = lineChars.indexOf(mCanvas[i]);
          int color = mColors[i];
          if (inColor && color > 0) {
            s.append("\033[;")
              .append(COLORS[color % COLORS.length])
              .append(";m")
              .append(mCanvas[i])
              .append("\033[0m");
          } else {
            if (mCanvas[i] == CR) {
              s.append(cr);
            } else {
              s.append(mCanvas[i]);
            }
          }
        }
        return s.toString();
      }
      return new String(mCanvas);
    }
  }
}