//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   EPSGraphPrinter
//###########################################################################
//# $Id: EPSGraphPrinter
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.module.GraphProxy;

import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.java2d.ps.AbstractPSDocumentGraphics2D;
import org.apache.xmlgraphics.java2d.ps.EPSDocumentGraphics2D;
import org.apache.xmlgraphics.java2d.ps.TextHandler;
import org.apache.xmlgraphics.ps.PSGenerator;


/**
 * A more convenient renderer that supports printing of graphs to
 * encapsulated PostScript.
 *
 * @author Robi Malik
 */

public class EPSGraphPrinter extends Renderer
  implements TextHandler
{

  //#########################################################################
  //# Constructors
  public EPSGraphPrinter(final GraphProxy graph,
                         final ModuleContext context,
                         final File file)
  {
    this(graph, new ProxyShapeProducer(graph, context), file);
  }

  public EPSGraphPrinter(final GraphProxy graph,
			 final ProxyShapeProducer shaper,
			 final File file)
  {
    mGraph = graph;
    mProxyShapeProducer = shaper;
    mFile = file;
  }


  //#########################################################################
  //# Invocation
  public void print()
    throws IOException
  {
    OutputStream stream = new FileOutputStream(mFile);
    try {
      stream = new BufferedOutputStream(stream);
      mGraphics = new EPSDocumentGraphics2D(false);
      mGraphics.setCustomTextHandler(this);
      mGraphics.setGraphicContext(new GraphicContext());
      final Rectangle2D bounds =
	mProxyShapeProducer.getMinimumBoundingRectangle();
      mGraphics.translate(-bounds.getX(), -bounds.getY());
      final int width = (int) Math.ceil(bounds.getWidth());
      final int height = (int) Math.ceil(bounds.getHeight());
      mGraphics.setupDocument(stream, width, height);
      final Renderable renderable = DefaultRenderable.getInstance();
      renderGraph(mGraph, null, renderable, mProxyShapeProducer, mGraphics);
      mGraphics.finish();
    } finally {
      close(stream);
    }
  }


  //#########################################################################
  //# Interface org.apache.xmlgraphics.java2d.ps.Texthandler
  public void writeSetup()
  {
  }

  public void writePageSetup()
  {
  }

  public void drawString(final String text, final float x, final float y)
    throws IOException
  {
    final PSGenerator gen = mGraphics.getPSGenerator();
    final Color color = mGraphics.getColor();
    gen.useColor(color);
    final Font font = mGraphics.getFont();
    final String name = font.isItalic() ? "/Helvetica-Italic" : "/Helvetica";
    final int size = font.getSize();
    gen.useFont(name, size);
    gen.writeln("gsave");
    gen.writeln("newpath");
    final AffineTransform transform = mGraphics.getTransform();
    final Point2D point = new Point2D.Double(x, y);
    transform.transform(point, point);
    final double x1 = point.getX();
    final double y1 = point.getY();
    gen.writeln(gen.formatDouble(x1) + " " + gen.formatDouble(y1) + " M");
    gen.writeln("1 -1 scale");
    // Hm ... gen replaces special characters with ??? ...
    gen.writeln("(" + text + ") t");
    gen.writeln("grestore");
  }


  //#########################################################################
  //# Auxiliary Methods
  private void close(final OutputStream stream)
  {
    try {
      stream.close();
    } catch (final IOException exception) {
      // never mind exceptions on close ...
    }
  }


  //#########################################################################
  //# Data Members
  private final GraphProxy mGraph;
  private final ProxyShapeProducer mProxyShapeProducer;
  private final File mFile;

  private AbstractPSDocumentGraphics2D mGraphics;

}

