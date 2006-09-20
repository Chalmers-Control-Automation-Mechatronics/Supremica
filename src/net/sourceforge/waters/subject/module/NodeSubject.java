//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   NodeSubject
//###########################################################################
//# $Id: NodeSubject.java,v 1.8 2006-09-20 16:24:13 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
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
   * @param propositions The list of propositions of the new node, or <CODE>null</CODE> if empty.
   */
  protected NodeSubject(final String name,
                        final PlainEventListProxy propositions)
  {
    super(name);
    if (propositions == null) {
      mPropositions = new PlainEventListSubject();
    } else {
      mPropositions = (PlainEventListSubject) propositions;
    }
    mPropositions.setParent(this);
  }

  /**
   * Creates a new node using default values.
   * This constructor creates a node with
   * an empty list of propositions.
   * @param name The name of the new node.
   */
  protected NodeSubject(final String name)
  {
    this(name,
         null);
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
  public PlainEventListSubject getPropositions()
  {
    return mPropositions;
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the list of propositions of this node.
   */
  public void setPropositions(final PlainEventListSubject propositions)
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
  private PlainEventListSubject mPropositions;

}
