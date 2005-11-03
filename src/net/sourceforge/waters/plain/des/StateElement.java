//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des
//# CLASS:   StateElement
//###########################################################################
//# $Id: StateElement.java,v 1.3 2005-11-03 03:45:57 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.des;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.base.EqualCollection;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.plain.base.NamedElement;


/**
 * A state of an automaton.
 * This is a simple immutable implementation of the {@link StateProxy}
 * interface.
 *
 * @author Robi Malik
 */

public final class StateElement
  extends NamedElement
  implements StateProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new state.
   * @param  name         The name to be given to the new state.
   * @param  initial      A flag indicating whether the new state is to be
   *                      an initial state.
   * @param  propositions The initial list of propositions for the new state.
   *                      Each element should be of type {@link EventElement}.
   */
  StateElement(final String name,
               final boolean initial,
               final Collection<? extends EventProxy> propositions)
  {
    super(name);
    mIsInitial = initial;
    final List<EventProxy> modifiable =
      new ArrayList<EventProxy>(propositions);
    mPropositions = Collections.unmodifiableList(modifiable);
  }

  /**
   * Creates a new state using default values.
   * This constructor creates a state that is not initial
   * and has no propositions.
   * @param  name         The name to be given to the new state.
   */
  StateElement(final String name)
  {
    this(name, false, emptyEventProxyList());
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this state.
   */
  public StateElement clone()
  {
    return (StateElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
    return desvisitor.visitStateProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.StateProxy
  public boolean isInitial()
  {
    return mIsInitial;
  }

  public Collection<EventProxy> getPropositions()
  {
    return mPropositions;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final StateElement state = (StateElement) partner;
      return
        (isInitial() == state.isInitial()) &&
        EqualCollection.equalSet(mPropositions, state.mPropositions);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Auxiliary Methods
  private static List<EventProxy> emptyEventProxyList()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Data Members
  private final boolean mIsInitial;
  private final Collection<EventProxy> mPropositions;

}
