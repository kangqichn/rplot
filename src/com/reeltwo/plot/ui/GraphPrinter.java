package com.reeltwo.plot.ui;

import com.reeltwo.plot.Graph2D;
import com.reeltwo.plot.patterns.DefaultColorGroup;
import com.reeltwo.plot.renderer.GraphicsRenderer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;


/**
 * Handy utility class for sending graphs to the printer.
 *
 * @author Richard Littin
 */
public class GraphPrinter implements Printable {

  private Color[] mColors = (Color[]) new DefaultColorGroup().getPatterns();
  private Paint[] mPatterns = null;
  private Graph2D mGraph = null;

  private int mFontSize = 0; // use default font size

  private PrinterJob mPrintJob = null;

  /**
   * Creates a new <code>GraphPrinter</code>.
   */
  public GraphPrinter() {
    mPrintJob = PrinterJob.getPrinterJob();
    mPrintJob.setPrintable(this);
  }

  /**
   * Sets the font size to use.
   *
   * @param size font size
   */
  public void setFontSize(int size) {
    mFontSize = size;
  }

  /**
   * Returns the font size.
   *
   * @return font size
   */
  public int getFontSize() {
    return mFontSize;
  }

  /**
   * Sets the colors to render plots with.
   *
   * @param colors an array of colors
   */
  public void setColors(Color[] colors) {
    if (colors == null) {
      throw new NullPointerException("no colors given");
    }
    mColors = colors;
  }

  /**
   * Sets the patterns to use when rendering plots.
   *
   * @param patterns an array of patterns
   */
  public void setPatterns(Paint[] patterns) {
    mPatterns = patterns;
  }

  /**
   * Prints the given <code>graph</code>.
   *
   * @param graph a <code>Graph2D</code>
   */
  public void printGraph(Graph2D graph) {
    mGraph = graph;
    if (mGraph != null) {
      if (mPrintJob.printDialog()) {
        try {
          mPrintJob.print();
        } catch (final PrinterException pe) {
          System.err.println(pe.getMessage());
          //pe.printStackTrace();
        }
      }
    }
  }

  // implementation of java.awt.print.Printable interface

  /**
   * Renders the current graph to a printer graphics device.
   *
   * @param g Graphics to draw on
   * @param pf a PageFormat
   * @param pi page index
   * @return whether page exists
   * @exception PrinterException if an error occurs
   */
  public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
    if (mGraph == null || pi >= 1) {
      return Printable.NO_SUCH_PAGE;
    }
    if (mFontSize > 0) {
      final Font old = g.getFont();
      g.setFont(new Font(old.getName(), old.getStyle(), mFontSize));
    }

    pf.setOrientation(PageFormat.LANDSCAPE);
    final GraphicsRenderer gr = new GraphicsRenderer(mColors, mPatterns);
    final Graphics g2 = g.create((int) pf.getImageableX(), (int) pf.getImageableY(),
        (int) pf.getImageableWidth(), (int) pf.getImageableHeight());
    gr.drawGraph(mGraph, g2, (int) pf.getImageableWidth(), (int) pf.getImageableHeight());
    return Printable.PAGE_EXISTS;
  }

}
