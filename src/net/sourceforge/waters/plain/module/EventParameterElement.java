//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   EventParameterElement
//###########################################################################
//# $Id: EventParameterElement.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventParameterProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;


/**
 * An immutable implementation of the {@link EventParameterProxy} interface.
 *
 * @author Robi Malik
 */

public final class EventParameterElement
  extends ParameterElement
  implements EventParameterProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new event parameter.
   * @param name The name of the new event parameter.
   * @param required The required status of the new event parameter.
   * @param eventDecl The event declaration of the new event parameter.
   */
  public EventParameterElement(final String name,
                               final boolean required,
                               final EventDeclProxy eventDecl)
  {
    super(name, required);
    mEventDecl = eventDecl;
  }

  /**
   * Creates a new event parameter using default values.
   * This constructor creates an event parameter with
   * the required status set to <CODE>true</CODE>.
   * @param name The name of the new event parameter.
   * @param eventDecl The event declaration of the new event parameter.
   */
  public EventParameterElement(final String name,
                               final EventDeclProxy eventDecl)
  {
    this(name,
         true,
         eventDecl);
  }


  //#########################################################################
  //# Cloning
  public EventParameterElement clone()
  {
    return (EventParameterElement) super.clone();
  }


  //#########################################################################
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final EventParameterElement downcast = (EventParameterElement) partner;
      return
        mEventDecl.equals(downcast.mEventDecl);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Object partner)
  {
    if (super.equalsWithGeometry(partner)) {
      final EventParameterElement downcast = (EventParameterElement) partner;
      return
        mEventDecl.equalsWithGeometry(downcast.mEventDecl);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitEventParameterProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.EventParameterProxy
  public EventDeclProxy getEventDecl()
  {
    return mEventDecl;
  }


  //#########################################################################
  //# Data Members
  private final EventDeclProxy mEventDecl;

}
