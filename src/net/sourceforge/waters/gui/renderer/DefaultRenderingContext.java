//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import java.awt.Font;
import java.util.List;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.GraphPanel;
import net.sourceforge.waters.gui.util.PropositionIcon;
import net.sourceforge.waters.gui.util.PropositionIcon.ColorInfo;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;


/**
 * A simple implementation of the {@link RenderingContext} interface that
 * provides basic formatting and colouring, while suppressing all highlighting.
 * It can be passed to printers, or extended for more sophisticated
 * formatting.
 *
 * @author Robi Malik
 */

public class DefaultRenderingContext
  extends DefaultModuleProxyVisitor
  implements RenderingContext
{

  //#########################################################################
  //# Constructors
  protected DefaultRenderingContext()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.RenderingContext
  @Override
  public Font getFont(final IdentifierProxy ident)
  {
    return EditorColor.DEFAULT_FONT;
  }

  @Override
  public RenderingInformation getRenderingInformation(final Proxy proxy)
  {
    return new RenderingInformation
      (false, false, false, false,
       EditorColor.getColor
         (proxy, GraphPanel.DragOverStatus.NOTDRAG, false, false, true),
       EditorColor.getShadowColor
         (proxy, GraphPanel.DragOverStatus.NOTDRAG, false, false, true),
       getPriority(proxy));
  }

  @Override
  public ColorInfo getColorInfo(final GraphProxy graph,
                                final SimpleNodeProxy node)
  {
    final PlainEventListProxy props = node.getPropositions();
    final List<Proxy> elist = props.getEventIdentifierList();
    if (elist.isEmpty()) {
      return PropositionIcon.getUnmarkedColors();
    } else {
      return PropositionIcon.getDefaultMarkedColors();
    }
  }

  @Override
  public boolean causesPropositionStatusChange(final ModelChangeEvent event,
                                               final GraphProxy graph)
  {
    return false;
  }


  //###########################################################################
  //# Auxiliary Methods
  protected int getPriority(final Proxy proxy)
  {
    try {
      return (Integer) proxy.acceptVisitor(this);
    } catch (final VisitorException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.Module.ModuleProxyVisitor
  @Override
  public Object visitProxy(final Proxy proxy)
  {
    return 0;
  }

  @Override
  public Object visitGroupNodeProxy(final GroupNodeProxy group)
  {
    return 1;
  }

  @Override
  public Object visitEdgeProxy(final EdgeProxy edge)
  {
    return 2;
  }

  @Override
  public Object visitNodeProxy(final NodeProxy node)
  {
    return 3;
  }

  @Override
  public Object visitLabelGeometryProxy(final LabelGeometryProxy geo)
  {
    return 4;
  }

  @Override
  public Object visitLabelBlockProxy(final LabelBlockProxy block)
  {
    return 5;
  }

  @Override
  public Object visitGuardActionBlockProxy(final GuardActionBlockProxy block)
  {
    return 6;
  }

  @Override
  public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
  {
    // guards and actions
    return 6;
  }

  @Override
  public Object visitIdentifierProxy(final IdentifierProxy ident)
  {
    return 7;
  }

  @Override
  public Object visitForeachProxy(final ForeachProxy foreach)
  {
    return 7;
  }


  //#########################################################################
  //# Singleton Pattern
  public static DefaultRenderingContext getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder
  {
    private static final DefaultRenderingContext INSTANCE =
      new DefaultRenderingContext();
  }

}
