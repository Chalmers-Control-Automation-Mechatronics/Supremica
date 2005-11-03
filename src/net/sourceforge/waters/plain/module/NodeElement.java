//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   NodeElement
//###########################################################################
//# $Id: NodeElement.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.module;

import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.NodeProxy;
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
                        final EventListExpressionProxy propositions)
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
  //# Equality
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final NodeElement downcast = (NodeElement) partner;
      return
        mPropositions.equals(downcast.mPropositions);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.NodeProxy
  public EventListExpressionProxy getPropositions()
  {
    return mPropositions;
  }


  //#########################################################################
  //# Data Members
  private final EventListExpressionProxy mPropositions;

}
