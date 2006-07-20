//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   EventParameterSubject
//###########################################################################
//# $Id: EventParameterSubject.java,v 1.6 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventParameterProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.subject.base.ModelChangeEvent;


/**
 * The subject implementation of the {@link EventParameterProxy} interface.
 *
 * @author Robi Malik
 */

public final class EventParameterSubject
  extends ParameterSubject
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
  public EventParameterSubject(final String name,
                               final boolean required,
                               final EventDeclProxy eventDecl)
  {
    super(name, required);
    mEventDecl = (EventDeclSubject) eventDecl;
    mEventDecl.setParent(this);
  }

  /**
   * Creates a new event parameter using default values.
   * This constructor creates an event parameter with
   * the required status set to <CODE>true</CODE>.
   * @param name The name of the new event parameter.
   * @param eventDecl The event declaration of the new event parameter.
   */
  public EventParameterSubject(final String name,
                               final EventDeclProxy eventDecl)
  {
    this(name,
         true,
         eventDecl);
  }


  //#########################################################################
  //# Cloning
  public EventParameterSubject clone()
  {
    final EventParameterSubject cloned = (EventParameterSubject) super.clone();
    cloned.mEventDecl = mEventDecl.clone();
    cloned.mEventDecl.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final EventParameterSubject downcast = (EventParameterSubject) partner;
      return
        mEventDecl.equalsByContents(downcast.mEventDecl);
    } else {
      return false;
    }
  }

  public boolean equalsWithGeometry(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final EventParameterSubject downcast = (EventParameterSubject) partner;
      return
        mEventDecl.equalsWithGeometry(downcast.mEventDecl);
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mEventDecl.hashCodeByContents();
    return result;
  }

  public int hashCodeWithGeometry()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mEventDecl.hashCodeWithGeometry();
    return result;
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
  public EventDeclSubject getEventDecl()
  {
    return mEventDecl;
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the event declaration characterising this parameter.
   */
  public void setEventDecl(final EventDeclSubject eventDecl)
  {
    if (mEventDecl == eventDecl) {
      return;
    }
    eventDecl.setParent(this);
    mEventDecl.setParent(null);
    mEventDecl = eventDecl;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private EventDeclSubject mEventDecl;

}
