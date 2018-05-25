//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
