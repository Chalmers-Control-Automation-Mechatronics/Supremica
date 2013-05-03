//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   OPVerifierTRChain
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>A transition relation simplifier that checks for a given automaton
 * whether its natural projection removing tau events satisfies the
 * observer property, using the OP-Verifier II algorithm.</P>
 *
 * <P>This is a wrapper that calls a {@link TauLoopRemovalTRSimplifier}
 * followed by a {@link OPVerifierTRSimplifier}. This emulates the
 * OP-Verifier II algorithm, which checks the observer for transition
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
