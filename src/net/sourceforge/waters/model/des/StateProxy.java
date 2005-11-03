//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   StateProxy
//###########################################################################
//# $Id: StateProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.des;

import java.util.Collection;

import net.sourceforge.waters.model.base.NamedProxy;


/**
 * <P>A state of an automaton.</P>
 *
 * @author Robi Malik
 */

public interface StateProxy
  extends NamedProxy
{

  //#########################################################################
  //# Getters and Setters
  public boolean isInitial();

  /**
   * Get the list of propositions associated with this state.
   * @return  An unmodifiable list of objects of type EventProxy.
   */
  public Collection<EventProxy> getPropositions();

}
