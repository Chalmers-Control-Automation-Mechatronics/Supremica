//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   EventListExpressionProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.List;

import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>An expression defined by an event list.</P>
 *
 * <P>Event lists are used in various places in a module, where several
 * events are grouped together. The most common application is on
 * transitions in graphs ({@link LabelBlockProxy}), but event lists can
 * also be used for parameter bindings ({@link ParameterBindingProxy}) of
 * instance components or alias declarations ({@link AliasProxy}) in a
 * module's event alias list.</P>
 *
 * <P>Technically, an event list is a wrapper of an object implementing the
 * {@link java.util.List} interface, which can have two different kinds of
 * elements.</P>
 * <DL>
 * <DT>{@link IdentifierProxy}</DT>
 * <DD>Identifiers are used to include a single event with a given name (or
 * all elements of an array of events) in an event list. There can be
 * simple identifiers ({@link SimpleIdentifierProxy}) that are just
 * names or indexed identifiers ({@link IndexedIdentifierProxy}) that can have
 * one or more array indexes.</DD>
 * <DT>{@link ForeachEventProxy}</DT>
 * <DD>This construct can be used to include several events by processing a
 * loop.</DD>
  *
 * @author Robi Malik
 */

public interface EventListExpressionProxy extends ExpressionProxy {

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the list of events consituting this event list expression.
   * @return The list of components, each element is of type
   *         {@link IdentifierProxy} or {@link ForeachEventProxy}.
   */
  public List<Proxy> getEventList();

}
