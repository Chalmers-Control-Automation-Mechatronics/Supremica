//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   EventAliasProxy
//###########################################################################
//# $Id: EventAliasProxy.java,v 1.3 2007-06-08 10:45:20 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;


/**
 * <P>An alias representing a group of events.</P>
 *
 * <P>An event alias is used to give a name of a group events used together
 * in different edges or graphs of a module. This kind of alias can occur
 * in the <I>event alias list</I> of a module, or in the body of a
 * <I>foreach alias</I> construct ({@link ForeachEventAliasProxy}).  The
 * name of an event can be an arbitrary identifier; it can be indexed to
 * support the use of the <I>foreach alias</I> construct. Its value must be
 * an event list expression ({@link EventListExpressionProxy}).</P>
 *
 * <P>This interface does not contain any new methods, because all required
 * attributes are contained in the superinterface {@link AliasProxy}. It is
 * merely a marker interface.</P>
 *
 * @author Robi Malik
 */

public interface EventAliasProxy extends AliasProxy {

}
