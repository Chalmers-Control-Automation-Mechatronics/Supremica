//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   InstanceProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.List;


/**
 * <P>A module component to be replaced by the contents of another module.</P>
 *
 * <P>Instances can occur in a module's component list. They represent
 * instructions to insert all components of another module after
 * substitution often parameters.</P>
 *
 * <P>Each instance has the following components.</P>
 * <DL>
 * <DT><STRONG>Name.</STRONG></DT>
 * <DD>The name to be used for the instance. The name is used to prefix all
 * the components and events in the instantiated module. It can be a
 * structured identifier.</DD>
 * <DT><STRONG>Module.</STRONG></DT>
 * <DD>The module to be instantiated.</DD>
 * <DT><STRONG>Binding list.</STRONG></DT>
 * <DD>A list of pairs of names and expressions ({@link ParameterBindingProxy})
 * that describes the values to be used for the parameters of the instantiated
 * module.</DD>
 * </DL>
 *
 * <P>As an example, consider the following simple <I>machine</I> module,
 * which has two event parameters <CODE>start</CODE> and
 * <CODE>finish</CODE>.</P>
 *
 * <PRE>
 *   MODULE machine;
 *   PARAMETERS
 *     controllable start;
 *     uncontrollable finish;
 *   EVENTS
 *     uncontrollable break;
 *     controllable repair;
 *   COMPONENTS
 *     plant mach;
 * </PRE>
 *
 * <P>This module may be instantiated from another module <I>factory</I>
 * as follows.</P>
 *
 * <PRE>
 *   MODULE factory;
 *   EVENTS
 *     controllable start1;
 *     uncontrollable finish1;
 *     ...
 *   COMPONENTS
 *     machine1 = machine(
 *                  start = start1;
 *                  finish = finish1;
 *                );
 *     ...
 * </PRE>
 *
 * <P>This will include all automata of the <I>machine</I> module in
 * <I>factory</I> after replacing the events <CODE>start</CODE> and
 * <CODE>finish</CODE> in <I>machine</I> by <CODE>start1</CODE> and
 * <CODE>finish1</CODE> from <I>factory</I>, respectively. The compiled
 * <I>factory</I> model will include new events <CODE>machine1.break</CODE>
 * and <CODE>machine1.repair</CODE>, and a plant automaton
 * <CODE>machine1.mach</CODE> which is result of applying the event
 * substitution to the automaton from the <I>machine</I> module.</P>
 *
 * @author Robi Malik
 */

public interface InstanceProxy extends ComponentProxy {

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the module name of this instance.
   * The module name is given as a string identifying a complete name,
   * if necessary, but without extension.
   */
  public String getModuleName();

  /**
   * Gets the binding list of this instance.
   * @return A list of name-value pairs describing how the parameters
   *         of the instantiated module are bound to values.
   *         Each element is of type {@link ParameterBindingProxy}.
   */
  public List<ParameterBindingProxy> getBindingList();

}
