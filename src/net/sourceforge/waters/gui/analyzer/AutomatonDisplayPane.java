//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

import java.awt.Graphics;
import java.util.Map;

import net.sourceforge.waters.gui.BackupGraphPanel;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.renderer.ModuleRenderingContext;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.RenderingContext;
import net.sourceforge.waters.gui.renderer.SubjectShapeProducer;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;

import org.supremica.gui.ide.ModuleContainer;


public class AutomatonDisplayPane extends BackupGraphPanel
{
  //#########################################################################
  //# Constructor
  public AutomatonDisplayPane(final AutomatonProxy aut,
                              final GraphSubject graph,
                              final WatersAnalyzerPanel parent)
    throws GeometryAbsentException
  {
    super(graph, parent.getModule());
    mParent = parent;
    final ModuleContainer container = parent.getModuleContainer();
    final ModuleSubject module = container.getModule();
    final RenderingContext renderingContext = createRenderingContext();
    final Map<Object,SourceInfo> infoMap = container.getSourceInfoMap();
    final SimpleExpressionCompiler compiler = parent.getSimpleExpressionCompiler();
    final SourceInfo info = infoMap.get(aut);
    final BindingContext bindings = info.getBindingContext();
    final ProxyShapeProducer producer =
      new SubjectShapeProducer(graph, module, renderingContext, compiler, bindings);
    setShapeProducer(producer);
  }


  //#########################################################################
  //# Simple Access
  public ModuleContainer getModuleContainer()
  {
    return mParent.getModuleContainer();
  }


  //#########################################################################
  //# Painting and Transforming
  @Override
  protected void paintGrid(final Graphics graphics)
  {
  }

  /*
  @Override
  protected AffineTransform createTransform()
  {
    final ProxyShapeProducer producer = getShapeProducer();
    final Rectangle2D imageRect = producer.getMinimumBoundingRectangle();
    final Dimension panelSize = getSize();
    final double scaleX = panelSize.getWidth() / imageRect.getWidth();
    final double scaleY = panelSize.getHeight() / imageRect.getHeight();
    final double min = Math.min(scaleX, scaleY);
    final AffineTransform transform = new AffineTransform();
    transform.scale(min, min);
    transform.translate(-imageRect.getX(), -imageRect.getY());
    return transform;
  }
  */

  protected RenderingContext createRenderingContext()
  {
    final ModuleContainer container = getModuleContainer();
    final ModuleContext moduleContext = container.getModuleContext();
    return new ModuleRenderingContext(moduleContext);
  }


  //#########################################################################
  //# Data Members
  private final WatersAnalyzerPanel mParent;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 3795116331859621382L;

}
