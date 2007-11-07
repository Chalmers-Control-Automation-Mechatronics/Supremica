//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   DefaultRenderable
//###########################################################################
//# $Id: DefaultRenderable.java,v 1.2 2007-11-07 06:16:04 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.EditorSurface;
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


/**
 * A simple implementation of the {@link Renderable} interface that
 * provides basic formatting and colouring, while suppressing all highlighting.
 * It can be passed to printers, or extended for more sophisticated
 * formatting.
 *
 * @author Robi Malik
 */

public class DefaultRenderable
  extends AbstractModuleProxyVisitor
  implements Renderable
{

  //#########################################################################
  //# Constructors
  protected DefaultRenderable()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.Renderable
  public RenderingInformation getRenderingInformation(final Proxy proxy)
  {
    return new RenderingInformation
      (false, false,
       EditorColor.getColor
         (proxy, EditorSurface.DRAGOVERSTATUS.NOTDRAG, false, false),
       EditorColor.getShadowColor
         (proxy, EditorSurface.DRAGOVERSTATUS.NOTDRAG, false, false),
       getPriority(proxy));
  }


  //###########################################################################
  //# Auxiliary Methods
  private int getPriority(final Proxy proxy)
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
  public static DefaultRenderable getInstance()
  {
    return INSTANCE;
  }

  private static final DefaultRenderable INSTANCE = new DefaultRenderable();

}
