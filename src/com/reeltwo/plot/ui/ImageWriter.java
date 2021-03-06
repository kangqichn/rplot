package com.reeltwo.plot.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import com.reeltwo.plot.Graph2D;
import com.reeltwo.plot.renderer.GraphicsRenderer;
import com.reeltwo.plot.renderer.Mapping;

import de.erichseifert.vectorgraphics2d.Document;
import de.erichseifert.vectorgraphics2d.VectorGraphics2D;
import de.erichseifert.vectorgraphics2d.svg.SVGProcessor;
import de.erichseifert.vectorgraphics2d.util.PageSize;

/**
 * Routines to write Graph2D's to graphics files of various formats.
 *
 * @author Richard Littin
 */
public class ImageWriter {
  /** the thing that does the graph rendering */
  private final GraphicsRenderer mGraphicsRenderer;

  /** PNG image type. */
  public static final int PNG_IMAGE = 0;

  //public static final int GIF_IMAGE = 1;

  /**
   * Private to prevent instantiation.
   *
   * @param gr TODO Description.
   */
  public ImageWriter(GraphicsRenderer gr) {
    mGraphicsRenderer = gr;
  }


  /**
   * Writes the given graph out to a formatted file of the specified
   * <tt>type</tt> . The width and height parameters determine the
   * dimension of the image (in pixels). The mappings from world to
   * screen data points for each axis pair is returned.
   *
   * @param type type of image to produce.
   * @param file File to save graph to.
   * @param graph graph to save.
   * @param width width of image.
   * @param height height of image.
   * @param font font to use in graph.
   * @return an array of world to screen mappings.
   * @exception IOException if a file writing error occurs.
   */
  public Mapping[] toImage(int type, File file, Graph2D graph, int width, int height, Font font) throws IOException {
    if (file == null) {
      throw new NullPointerException("null file given.");
    }

    final FileOutputStream fos = new FileOutputStream(file);
    try {
      return toImage(type, fos, graph, width, height, font);
    } finally {
      fos.close();
    }
  }


  /**
   * Writes the given graph out to a formatted output stream of the
   * specified <tt>type</tt> . The width and height parameters
   * determine the dimension of the image (in pixels). The mappings
   * from world to screen data points for each axis pair is returned.
   *
   * @param type type of image to produce.
   * @param os stream to write to.
   * @param graph graph to save.
   * @param width width of image.
   * @param height height of image.
   * @param font font to use in graph.
   * @return an array of world to screen mappings.
   * @exception IOException if a file writing error occurs.
   */
  public Mapping[] toImage(int type, OutputStream os, Graph2D graph, int width, int height, Font font) throws IOException {
    switch (type) {
    //case GIF_IMAGE:
    //return toGIF(os, graph, width, height, font);
    case PNG_IMAGE:
      return toPNG(os, graph, width, height, font);
    default:
      throw new IllegalArgumentException("Illegal image type '" + type + "' given.");
    }
  }


  /**
   * Writes the given graph out to a PNG formatted file. The width and
   * height parameters determine the dimension of the image (in
   * pixels). The mappings from world to screen data points for each
   * axis pair is returned.
   *
   * @param file File to save graph to.
   * @param graph graph to save.
   * @param width width of image.
   * @param height height of image.
   * @param font font to use in graph.
   * @return an array of world to screen mappings.
   * @exception IOException if a file writing error occurs.
   */
  public Mapping[] toPNG(File file, Graph2D graph, int width, int height, Font font) throws IOException {
    if (file == null) {
      throw new NullPointerException("null file given.");
    }

    final FileOutputStream fos = new FileOutputStream(file);
    try {
      return toPNG(fos, graph, width, height, font);
    } finally {
      fos.close();
    }
  }


  /**
   * Writes the given graph out to a PNG formatted output stream. The
   * width and height parameters determine the dimension of the image
   * (in pixels). The mappings from world to screen data points for
   * each axis pair is returned.
   *
   * @param os stream to write to.
   * @param graph graph to save.
   * @param width width of image.
   * @param height height of image.
   * @param font font to use in graph.
   * @return an array of world to screen mappings.
   * @exception IOException if a file writing error occurs.
   */
  public Mapping[] toPNG(OutputStream os, Graph2D graph, int width, int height, Font font) throws IOException {
    if (os == null) {
      throw new NullPointerException("null output stream given.");
    }
    if (graph == null) {
      throw new NullPointerException("null graph given.");
    }
    if (width < 0 || height < 0) {
      throw new IllegalArgumentException("dimensions must be greater than 0");
    }

    final BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    final Graphics2D g = bi.createGraphics();
    if (font != null) {
      g.setFont(font);
    }
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, width, height);

    mGraphicsRenderer.drawGraph(graph, g, 5, 5, width - 10, height - 10);
    final Mapping[] mapping = mGraphicsRenderer.getMappings();
    ImageIO.write(bi, "png", os);

    return mapping;
  }
  /**
   * Writes the given graph out to a PNG formatted file. The width and
   * height parameters determine the dimension of the image (in
   * pixels). The mappings from world to screen data points for each
   * axis pair is returned.
   *
   * @param file File to save graph to.
   * @param graph graph to save.
   * @param width width of image.
   * @param height height of image.
   * @param font font to use in graph.
   * @return an array of world to screen mappings.
   * @exception IOException if a file writing error occurs.
   */
  public Mapping[] toSVG(File file, Graph2D graph, int width, int height, Font font) throws IOException {
    if (file == null) {
      throw new NullPointerException("null file given.");
    }

    final FileOutputStream fos = new FileOutputStream(file);
    try {
      return toSVG(fos, graph, width, height, font);
    } finally {
      fos.close();
    }
  }

  /**
   * Writes the given graph out to a SVG formatted output stream. The
   * width and height parameters determine the dimension of the image
   * (in pixels). The mappings from world to screen data points for
   * each axis pair is returned.
   *
   * @param os stream to write to.
   * @param graph graph to save.
   * @param width width of image.
   * @param height height of image.
   * @param font font to use in graph.
   * @return an array of world to screen mappings.
   * @exception IOException if a file writing error occurs.
   */
  public Mapping[] toSVG(OutputStream os, Graph2D graph, int width, int height, Font font) throws IOException {
    if (os == null) {
      throw new NullPointerException("null output stream given.");
    }
    if (graph == null) {
      throw new NullPointerException("null graph given.");
    }
    if (width < 0 || height < 0) {
      throw new IllegalArgumentException("dimensions must be greater than 0");
    }

    final VectorGraphics2D g = new VectorGraphics2D();
    if (font != null) {
      g.setFont(font);
    } else {
      g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
    }
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, width, height);
    int inset = 5;
    mGraphicsRenderer.drawGraph(graph, g, inset, inset, width - 2 * inset, height - 2 * inset);
    final Mapping[] mapping = mGraphicsRenderer.getMappings();
    final SVGProcessor proc = new SVGProcessor();
    final Document document = proc.getDocument(g.getCommands(), new PageSize(width, height));
    document.writeTo(os);
    return mapping;
  }

}
