//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.gui.analyzer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import net.sourceforge.waters.gui.BackupGraphPanel;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.renderer.ModuleRenderingContext;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.RenderingContext;
import net.sourceforge.waters.gui.renderer.SubjectShapeProducer;
import net.sourceforge.waters.gui.springembedder.EmbedderEvent;
import net.sourceforge.waters.gui.springembedder.EmbedderEvent.EmbedderEventType;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;

import org.supremica.gui.ide.ModuleContainer;


public class AutomatonDisplayPane extends BackupGraphPanel
{
  //#########################################################################
  //# Constructor
  public AutomatonDisplayPane(final GraphSubject graph,
                              final BindingContext bindings,
                              final ModuleContainer container,
                              final SimpleExpressionCompiler compiler,
                              final AutomatonProxy aut)
    throws GeometryAbsentException
  {

    super(graph, container);
    final ModuleSubject module = container.getModule();
    final ModuleContext moduleContext = container.getModuleContext();
    final RenderingContext renderingContext =
      new ModuleRenderingContext(moduleContext);
    final ProxyShapeProducer producer =
      new SubjectShapeProducer(graph, module, renderingContext, compiler,
                               bindings);
    setShapeProducer(producer);
    if (!ensureGeometryExists()) {
      adjustSize();
    }
  }


  //#########################################################################
  //# Simple Access
  void adjustSize()
  {
    final int width;
    final int height;
    final float scaleFactor = IconAndFontLoader.GLOBAL_SCALE_FACTOR;
    final Rectangle2D imageRect =
      getShapeProducer().getMinimumBoundingRectangle();
    width = (int) Math.ceil(scaleFactor * imageRect.getWidth());
    height = (int) Math.ceil(scaleFactor * imageRect.getHeight());
    setPreferredSize(new Dimension(width, height));
  }


  //#########################################################################
  //# Painting and Transforming
  @Override
  protected void paintGrid(final Graphics graphics)
  {
  }

  @Override
  protected AffineTransform createTransform()
  {
    final ProxyShapeProducer producer = getShapeProducer();
    final Rectangle2D imageRect = producer.getMinimumBoundingRectangle();
    final AffineTransform transform = new AffineTransform();
    transform.scale(IconAndFontLoader.GLOBAL_SCALE_FACTOR,
                    IconAndFontLoader.GLOBAL_SCALE_FACTOR);
    transform.translate(-imageRect.getX(), -imageRect.getY());
    return transform;
  }

  protected RenderingContext createRenderingContext()
  {
    final ModuleContext moduleContext = getModuleContext();
    return new ModuleRenderingContext(moduleContext);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.springembedder.EmbedderObserver
  @Override
  public void embedderChanged(final EmbedderEvent event)
  {
    super.embedderChanged(event);
    if (event.getType() == EmbedderEventType.EMBEDDER_STOP) {
      adjustSize();
      revalidate();
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 3795116331859621382L;

}
