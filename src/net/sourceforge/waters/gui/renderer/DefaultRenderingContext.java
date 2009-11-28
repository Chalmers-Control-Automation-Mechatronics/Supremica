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
import net.sourceforge.waters.gui.PropositionIcon;
import net.sourceforge.waters.gui.PropositionIcon.ColorInfo;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


/**
 * A simple implementation of the {@link RenderingContext} interface that
 * provides basic formatting and colouring, while suppressing all highlighting.
 * It can be passed to printers, or extended for more sophisticated
 * formatting.
 *
 * @author Robi Malik
 */

public class DefaultRenderingContext
  extends AbstractModuleProxyVisitor
  implements RenderingContext
{

  //#########################################################################
  //# Constructors
  protected DefaultRenderingContext()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.RenderingContext
  public ColorInfo getColorInfo(final SimpleNodeProxy node)
  {
    final PlainEventListProxy props = node.getPropositions();
    final List<Proxy> elist = props.getEventList();
    if (elist.isEmpty()) {
      return PropositionIcon.getUnmarkedColors();
    } else {
      return PropositionIcon.getDefaultMarkedColors();
    }
  }

  public Font getFont(final IdentifierProxy ident)
  {
    return EditorColor.DEFAULT_FONT;
  }

  public RenderingInformation getRenderingInformation(final Proxy proxy)
  {
    return new RenderingInformation
      (false, false,
       EditorColor.getColor
         (proxy, GraphPanel.DragOverStatus.NOTDRAG, false, false, true),
       EditorColor.getShadowColor
         (proxy, GraphPanel.DragOverStatus.NOTDRAG, false, false, true),
       getPriority(proxy));
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
  public Object visitProxy(final Proxy proxy)
  {
    return 0;
  }

  public Object visitGroupNodeProxy(final GroupNodeProxy group)
  {
    return 1;
  }

  public Object visitEdgeProxy(final EdgeProxy edge)
  {
    return 2;
  }

  public Object visitNodeProxy(final NodeProxy node)
  {
    return 3;
  }

  public Object visitLabelGeometryProxy(final LabelGeometryProxy geo)
  {
    return 4;
  }

  public Object visitLabelBlockProxy(final LabelBlockProxy block)
  {
    return 5;
  }

  public Object visitGuardActionBlockProxy(final GuardActionBlockProxy block)
  {
    return 6;
  }

  public Object visitIdentifierProxy(final IdentifierProxy ident)
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
