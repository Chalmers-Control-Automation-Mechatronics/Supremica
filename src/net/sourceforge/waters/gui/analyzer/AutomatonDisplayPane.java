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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import net.sourceforge.waters.gui.BackupGraphPanel;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;

import org.supremica.gui.ide.ModuleContainer;


public class AutomatonDisplayPane extends BackupGraphPanel
{
  //#########################################################################
  //# Constructors
  public AutomatonDisplayPane(final AutomatonProxy aut,
                              final GraphSubject graph,
                              final ModuleContainer container)
    throws GeometryAbsentException
  {
    super(graph, container.getModule());
    mAutomaton = aut;
    mContainer = container;
    @SuppressWarnings("unused")
    final ModuleSubject module = container.getModule();
    //final RenderingContext context = new RenderingCon
    final Map<Object,SourceInfo> infoMap = mContainer.getSourceInfoMap();
    final SourceInfo info = infoMap.get(aut);
    //final SimpleExpressionCompiler compiler = sim.getSimpleExpressionCompiler();
    @SuppressWarnings("unused")
    final BindingContext bindings = info.getBindingContext();
    //final ProxyShapeProducer producer =
    //  new SubjectShapeProducer(graph, module, context, compiler, bindings);
    //setShapeProducer(producer);
    final int width;
    final int height;
    final float scaleFactor = IconAndFontLoader.GLOBAL_SCALE_FACTOR;
    if (ensureGeometryExists()) {
      // Spring embedder is running, guessing window size ...
      final int numStates = aut.getStates().size();
      width = height = Math.round(scaleFactor * (128 + 32 * numStates));
    } else {
      final Rectangle2D imageRect = getMinimumBoundingRectangle();
      width = (int) Math.ceil(scaleFactor * imageRect.getWidth());
      height = (int) Math.ceil(scaleFactor * imageRect.getHeight());
    }
    setPreferredSize(new Dimension(width, height));
  }

  //#########################################################################
  //# Simple Access
  public Rectangle2D getMinimumBoundingRectangle()
  {
    return getShapeProducer().getMinimumBoundingRectangle();
  }

  public AutomatonProxy getAutomaton()
  {
    return mAutomaton;
  }

  @Override
  public void close()
  {
    super.close();
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
    final Dimension panelSize = getSize();
    final double scaleX = panelSize.getWidth() / imageRect.getWidth();
    final double scaleY = panelSize.getHeight() / imageRect.getHeight();
    final double min = Math.min(scaleX, scaleY);
    final AffineTransform transform = new AffineTransform();
    transform.scale(min, min);
    transform.translate(-imageRect.getX(), -imageRect.getY());
    return transform;
  }


  //#########################################################################
  //# Inner Class RenderingStatus
  @SuppressWarnings("unused")
  private static class RenderingStatus
  {
    //#######################################################################
    //# Constructor
    private RenderingStatus(final Proxy proxy)
    {
      mCount = 1;
      mAutomatonItem = proxy;
    }

    //#######################################################################
    //# Simple Access

    private int getCount()
    {
      return mCount;
    }

    private Proxy getAutomatonItem()
    {
      return mAutomatonItem;
    }

    private void addStatus(final boolean active, final boolean enabled)
    {
      mCount++;
      mAutomatonItem = null;
    }

    //#######################################################################
    //# Data Members
    /**
     * The number of automaton transitions compiled from this graph element.
     * This is used when generating tooltips, to identify a label as an
     * &quot;event group&quot;.
     */
    private int mCount;
    /**
     * The unique item in the automaton corresponding to this graph element,
     * or <CODE>null</CODE> if more than one item has been created from it.
     */
    private Proxy mAutomatonItem;
  }

  //#########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;
  private final ModuleContainer mContainer;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
}
