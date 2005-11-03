//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   EventParameterProxy
//###########################################################################
//# $Id: EventParameterProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;


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

public interface EventParameterProxy extends ParameterProxy {

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the event declaration characterising this parameter.
   */
  public EventDeclProxy getEventDecl();

}
