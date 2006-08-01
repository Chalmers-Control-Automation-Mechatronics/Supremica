//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   NodeProxy
//###########################################################################
//# $Id: NodeProxy.java,v 1.4 2006-08-01 04:14:47 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.Set;

import net.sourceforge.waters.model.base.NamedProxy;


/**
 * <P>The base class for all nodes.</P>
 *
 * <P>This class serves as a common base for simple nodes ({@link
 * SimpleNodeProxy}) and group nodes ({@link GroupNodeProxy}) and
 * extends some functionality common to both classes.</P>
 *
 * <P>All nodes can be associated with <I>propositions</I>. Propositions
 * are a particular type of event used to define properties of nodes.  The
 * common application to define <I>marked</I> or <I>terminal</I> states is
 * achieved by associating each node to be marked with a particular
 * proposition, e.g., <CODE>:omega</CODE>. The general node structure
 * supports a list of proposition in order to facilitate several marking
 * conditions to check a model for mutual nonblocking conditions or to
 * perform CTL model checking.</P>
 *
 * @author Robi Malik
 */

public interface NodeProxy extends NamedProxy {

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the list of propositions of this node.
   * @return An event list that defines the proposition events for this
   *         node.
   */
  public PlainEventListProxy getPropositions();

  /**
   * Gets the set of immediate child nodes of this group node.
   * This method returns the set of simple nodes or group nodes
   * that are directly contained in this group node.
   * @return An unmodifiable set of nodes.
   *         Each element is of type {@link NodeProxy}.
   */
  public Set<NodeProxy> getImmediateChildNodes();

}
