//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   EPSGraphPrinter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.sourceforge.waters.model.module.GraphProxy;

import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.java2d.ps.AbstractPSDocumentGraphics2D;


/**
 * A more convenient renderer that supports printing of graphs to
 * encapsulated PostScript.
 *
 * @author Robi Malik
 */

public class EPSGraphPrinter extends Renderer
{

  //#########################################################################
  //# Constructors
  public EPSGraphPrinter(final GraphProxy graph, final File file)
  {
    this (graph, new DefaultRenderingContext(), file);
  }

  public EPSGraphPrinter(final GraphProxy graph,
                         final RenderingContext context,
                         final File file)
  {
    this(graph, new ProxyShapeProducer(graph, context), file);
  }

  public EPSGraphPrinter(final GraphProxy graph,
                         final ProxyShapeProducer producer,
                         final File file)
  {
    super(graph, null, producer);
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
      mGraphics = new TextEPSDocumentGraphics2D();
      mGraphics.setGraphicContext(new GraphicContext());
      final Rectangle2D bounds =
        getProxyShapeProducer().getMinimumBoundingRectangle();
      mGraphics.translate(-bounds.getX(), -bounds.getY());
      final int width = (int) Math.ceil(bounds.getWidth());
      final int height = (int) Math.ceil(bounds.getHeight());
      mGraphics.setupDocument(stream, width, height);
      mGraphics.preparePainting();
      renderGraph(mGraphics);
      mGraphics.finish();
    } finally {
      close(stream);
    }
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
  private final File mFile;

  private AbstractPSDocumentGraphics2D mGraphics;

}

