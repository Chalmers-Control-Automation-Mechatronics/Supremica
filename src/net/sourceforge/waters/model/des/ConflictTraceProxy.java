//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   ConflictTraceProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.des;

import net.sourceforge.waters.xsd.des.ConflictKind;


/**
 * <P>A conflict counterexample trace for some automata of a product DES.</P>
 *
 * @see ProductDESProxy
 *
 * @author Robi Malik
 */

public interface ConflictTraceProxy
  extends TraceProxy
{

  //#########################################################################
  //# Getters
  /**
   * Gets the type of this conflict trace.
   * @return One of {@link ConflictKind#CONFLICT},
   *         {@link ConflictKind#DEADLOCK}, or
   *         {@link ConflictKind#LIVELOCK}.
   */
  public ConflictKind getKind();

}
