//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   DefaultRenderingContext
//###########################################################################
//# $Id$
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
