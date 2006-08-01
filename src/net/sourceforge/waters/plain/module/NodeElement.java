//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   NodeElement
//###########################################################################
//# $Id: NodeElement.java,v 1.6 2006-08-01 04:14:47 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.plain.base.NamedElement;


/**
 * An immutable implementation of the {@link NodeProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class NodeElement
  extends NamedElement
  implements NodeProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new node.
   * @param name The name of the new node.
   * @param propositions The list of propositions of the new node.
   */
  protected NodeElement(final String name,
                        final PlainEventListProxy propositions)
  {
    super(name);
    mPropositions = propositions;
  }


  //#########################################################################
  //# Cloning
  public NodeElement clone()
  {
    return (NodeElement) super.clone();
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final NodeElement downcast = (NodeElement) partner;
      return
        mPropositions.equalsByContents(downcast.mPropositions);
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mPropositions.hashCodeByContents();
    return result;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.NodeProxy
  public PlainEventListProxy getPropositions()
  {
    return mPropositions;
  }


  //#########################################################################
  //# Data Members
  private final PlainEventListProxy mPropositions;

}
