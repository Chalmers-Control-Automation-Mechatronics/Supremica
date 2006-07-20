//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   NodeSubject
//###########################################################################
//# $Id: NodeSubject.java,v 1.6 2006-07-20 02:28:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.NamedSubject;


/**
 * The subject implementation of the {@link NodeProxy} interface.
 *
 * @author Robi Malik
 */

public abstract class NodeSubject
  extends NamedSubject
  implements NodeProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new node.
   * @param name The name of the new node.
   * @param propositions The list of propositions of the new node.
   */
  protected NodeSubject(final String name,
                        final EventListExpressionProxy propositions)
  {
    super(name);
    mPropositions = (EventListExpressionSubject) propositions;
    mPropositions.setParent(this);
  }


  //#########################################################################
  //# Cloning
  public NodeSubject clone()
  {
    final NodeSubject cloned = (NodeSubject) super.clone();
    cloned.mPropositions = mPropositions.clone();
    cloned.mPropositions.setParent(cloned);
    return cloned;
  }


  //#########################################################################
  //# Equality and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final NodeSubject downcast = (NodeSubject) partner;
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
  public EventListExpressionSubject getPropositions()
  {
    return mPropositions;
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the list of propositions of this node.
   */
  public void setPropositions(final EventListExpressionSubject propositions)
  {
    if (mPropositions == propositions) {
      return;
    }
    propositions.setParent(this);
    mPropositions.setParent(null);
    mPropositions = propositions;
    final ModelChangeEvent event =
      ModelChangeEvent.createStateChanged(this);
    fireModelChanged(event);
  }


  //#########################################################################
  //# Data Members
  private EventListExpressionSubject mPropositions;

}
