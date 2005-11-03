//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ModuleProxy
//###########################################################################
//# $Id: ModuleProxy.java,v 1.3 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.List;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>The top-level information container in Waters.</P>
 *
 * <P>A module contains a list of automata and associated events.
 * Modules are stored in XML files with the extension <CODE>.wmod</CODE>,
 * which uses the XML schema called <CODE>waters-module.dtd</CODE>.</P>
 *
 * <P>In the simplest case, a module contains a list of event declarations
 * ({@link EventDeclProxy}) and a list of components, i.e. automata
 * ({@link SimpleComponentProxy}). For example, a simple version of
 * <I>Small Factory</I> may consist of the following parts.</P>
 *
 * <PRE>
 * MODULE small_factory_simple;
 * EVENTS
 *   controllable start1;
 *   controllable start2;
 *   uncontrollable finish1;
 *   uncontrollable finish2;
 *   uncontrollable break1;
 *   uncontrollable break2;
 *   controllable repair1;
 *   controllable repair2;
 * COMPONENTS
 *   plant mach1;
 *   plant mach2;
 *   spec buffer;
 * </PRE>
 *
 * <P>More advanced modules can also have parameters and aliases. These
 * features make it possible to describe complex parameterised structures.
 * Similar automata can be reused by replacing their events in various
 * ways.</P>
 *
 * <P>An more advanced version of the <I>small factory</I> example above
 * would use only one automaton for the two almost identical machines
 * <CODE>mach1</CODE> and <CODE>mach2</CODE>. This is achieved by creating
 * a machine module containing the one machine automaton. The advanced
 * small factory module uses event arrays and instantiates the machine
 * module twice using a loop.</P>
 *
 * <PRE>
 * MODULE machine;
 * PARAMETERS
 *   controllable start;
 *   uncontrollable finish;
 * EVENTS
 *   uncontrollable break;
 *   controllable repair;
 * COMPONENTS
 *   plant mach;
 *
 * MODULE small_factory_advanced;
 * EVENTS
 *   controllable start[1..2];
 *   uncontrollable finish[1..2];
 * COMPONENTS
 *   FOR i IN 1..2
 *     instance machine[i] = machine(
 *       start = start[i];
 *       finish = finish[i];
 *     );
 *   ENDFOR
 *   spec buffer;
 * </PRE>
 *
 * @author Robi Malik
 */

public interface ModuleProxy
  extends DocumentProxy
{

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the parameter list of this module.
   * @return The parameter list.
   */
  public List<ParameterProxy> getParameterList();

  /**
   * Gets the constant definition list of this module.
   * @return The constant definition list.
   */
  public List<AliasProxy> getConstantAliasList();

  /**
   * Gets the event declaration list of this module.
   * @return The list of event declarations.
   */
  public List<EventDeclProxy> getEventDeclList();

  /**
   * Gets the event alias list of this module.
   * @return The list event aliases.
   *         Each element is of type {@link AliasProxy}
   *         or {@link ForeachEventAliasProxy}.
   */
  public List<Proxy> getEventAliasList();

  /**
   * Gets the component list of this module.
   * @return The component list.
   *         Each element is of type {@link ComponentProxy}
   *         or {@link ForeachComponentProxy}.
   */
  public List<Proxy> getComponentList();

}
