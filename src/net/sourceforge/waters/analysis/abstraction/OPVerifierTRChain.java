//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>A transition relation simplifier that checks for a given transition
 * relation whether its natural projection removing tau events satisfies the
 * observer property, using the OP-Verifier II algorithm.</P>
 *
 * <P>This is a wrapper that calls a {@link TauLoopRemovalTRSimplifier}
 * followed by a {@link OPVerifierTRSimplifier}. This emulates the
 * OP-Verifier II algorithm, which checks the observer property for transition
 * relations with or without silent loops. If silent loops are present,
 * strongly connected components are merged as a side-effect of the first
 * step.</P>
 *
 * <P><I>References:</I><BR>
 * H. J. Bravo, A. E. C. da Cunha, P. N. Pena, R. Malik, J. E. R. Cury.
 * Generalised verification of the observer property in discrete event
 * systems. Proc. 12th Workshop on Discrete Event Systems, WODES'12,
 * Guadalajara, Mexico, 337-342, 2012.</P>
 *
 * @author Robi Malik
 */

public class OPVerifierTRChain
  extends ChainTRSimplifier
{

  //#######################################################################
  //# Constructors
  public OPVerifierTRChain()
  {
    this(null);
  }

  public OPVerifierTRChain(final ListBufferTransitionRelation rel)
  {
    final TauLoopRemovalTRSimplifier loopRemover =
      new TauLoopRemovalTRSimplifier();
    add(loopRemover);
    mOPVerifier = new OPVerifierTRSimplifier();
    add(mOPVerifier);
    setTransitionRelation(rel);
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the state limit. The states limit specifies the maximum
   * number of verifier pairs that will be created by the OP-Verifier.
   * @param limit
   *          The new state limit, or {@link Integer#MAX_VALUE} to allow
   *          an unlimited number of states.
   */
  public void setStateLimit(final int limit)
  {
    mOPVerifier.setStateLimit(limit);
  }

  /**
   * Gets the state limit.
   * @see #setStateLimit(int) setStateLimit()
   */
  public int getStateLimit()
  {
    return mOPVerifier.getStateLimit();
  }


  //#########################################################################
  //# Simple Access
  /**
   * Returns whether or not the last invocation found the observer property
   * to be satisfied.
   */
  public boolean getOPResult()
  {
    return mOPVerifier.getOPResult();
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier
  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    super.runSimplifier();
    return mOPVerifier.getOPResult();
  }


  //#########################################################################
  //# Data Members
  private final OPVerifierTRSimplifier mOPVerifier;

}
