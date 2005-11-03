//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   EventProxy
//###########################################################################
//# $Id: EventProxy.java,v 1.3 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.des;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>An event used by the automata in a DES.</P>
 *
 * <P>Each {@link ProductDESProxy} object consists of a set of automata
 * and an event alphabet. Each event can be used by one or more automata;
 * synchronisation is modelled by several automata using the same event.
 * The {@link ProductDESProxy} data structure is set up in such a way
 * that each event object is used only once. If several automata (or
 * transitions) share an event, they all will use the same object.</P>
 *
 * <P>In contrast to the event declarations of a module (class {@link
 * net.sourceforge.waters.model.module.EventDeclProxy}), the events
 * in a {@link ProductDESProxy} represent single events only.
 * Each event contains the following information.</P>
 *
 * <DL>
 * <DT><STRONG>Name.</STRONG></DT>
 * <DD>A string defining the name of the event. This name may be
 * a result from compilation of a module and therefore may contain
 * special charcters, e.g., <CODE>machine[1].start</CODE>.</DD>
 * <DT><STRONG>Kind.</STRONG></DT>
 * <DD>The type of the event. This can be <I>controllable</I>,
 * <I>uncontrollable</I>, or <I>proposition</I>.</DD>
 * <DT><STRONG>Observability.</STRONG></DT>
 * <DD>A boolean flag, indicating whether the event is
 * <I>observable</I>.</DD>
 * </DL>
 *
 * @author Robi Malik
 */

public interface EventProxy
  extends NamedProxy
{

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the kind of this event.
   * @return One of {@link EventKind#CONTROLLABLE},
   *         {@link EventKind#UNCONTROLLABLE}, or
   *         {@link EventKind#PROPOSITION}.
   */
  public EventKind getKind();

  /**
   * Gets the observability status of this event.
   * @return <CODE>true</CODE> if the event is observable,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean isObservable();

}
