//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.module
//# CLASS:   EventParameterProxy
//###########################################################################
//# $Id: EventParameterProxy.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ModelPrinter;
import net.sourceforge.waters.xsd.base.ElementType;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.EventParameterType;


/**
 * <P>An event parameter for a Waters module.</P>
 *
 * <P>An event parameter is a parameter that is bound to an event or set of
 * events when a module is compiled. An event parameter behaves like an
 * event declaration ({@link EventDeclProxy}), in that it introduces an
 * event or event array that can be used in the module's components.  The
 * only difference is that the events do not come from their module itself,
 * but from another module from which their module is instantiated.</P>
 *
 * <P>The documentation of class {@link ModuleProxy} contains an example
 * demonstrating the use of event parameters.</P>
 *
 * @author Robi Malik
 */

public class EventParameterProxy extends ParameterProxy {

  //#########################################################################
  //# Constructors
  /**
   * Creates an event parameter.
   * @param  name        The name of the new parameter.
   * @param  kind        The event kind of the new parameter, one of
   *                     {@link EventKind#CONTROLLABLE},
   *                     {@link EventKind#UNCONTROLLABLE}, or
   *                     {@link EventKind#PROPOSITION}.
   * @param  required    A flag, <CODE>true</CODE> if the new parameter should
   *                     be required, <CODE>false</CODE> otherwise.
   */
  public EventParameterProxy(final String name,
			     final EventKind kind,
			     final boolean required)
  {
    super(name);
    mIsRequired = required;
    mEventDecl = new EventDeclProxy(name, kind);
  }

  /**
   * Creates an event parameter.
   * @param  decl        An event declaration describing the event name
   *                     and kind of the parameter.
   * @param  required    A flag, <CODE>true</CODE> if the new parameter should
   *                     be required, <CODE>false</CODE> otherwise.
   */
  public EventParameterProxy(final EventDeclProxy decl,
			     final boolean required)
  {
    super(decl.getName());
    mIsRequired = required;
    mEventDecl = decl;
  }

  /**
   * Creates an event parameter from a parsed XML structure.
   * @param  param       The parsed XML structure of the parameter.
   * @throws ModelException to indicate that the XML structure could
   *                     not be converted due to serious semantic
   *                     inconsistencies.
   */
  EventParameterProxy(final EventParameterType param)
    throws ModelException
  {
    super(param);
    mIsRequired = param.isRequired();
    mEventDecl = new EventDeclProxy(param);
  }


  //#########################################################################
  //# Getters and Setters
  public EventDeclProxy getEventDecl()
  {
    return mEventDecl;
  }

  public boolean isRequired()
  {
    return mIsRequired;
  }

  public void setRequired(final boolean required)
  {
    mIsRequired = required;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final EventParameterProxy param = (EventParameterProxy) partner;
      return
	isRequired() == param.isRequired() &&
	getEventDecl().equals(param.getEventDecl());
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Printing
  public void pprint(final ModelPrinter printer)
    throws IOException
  {
    super.pprint(printer);
    mEventDecl.pprint(printer);
  }


  //#########################################################################
  //# Marshalling
  public void toJAXBElement(final ElementType element)
    throws JAXBException
  {
    getEventDecl().toJAXBElement(element);
    final EventParameterType param = (EventParameterType) element;
    param.setRequired(isRequired());
  }


  //#########################################################################
  //# Data Members
  private boolean mIsRequired;
  private final EventDeclProxy mEventDecl;

}
