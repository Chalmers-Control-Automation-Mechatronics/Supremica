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

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.ConflictKindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>A monolithic conflict checker implementation, written in C++. The
 * native conflict checker implements the standard {@link ConflictChecker}
 * interface and determines whether a given input model is <I>blocking</I>
 * or <I>nonblocking</I>.</P>
 *
 * <P><STRONG>Supported Features.</STRONG></P>
 *
 * <P>This implementation supports both deterministic and nondeterministic
 * models.</P>
 *
 * <P>Counterexamples are computed for all blocking models, and
 * efforts are made to distinguish between deadlock and livelock. If a
 * blocking model contains a state without any outgoing transitions, or
 * with only selfloops outgoing, a trace marked {@link
 * net.sourceforge.waters.model.des.ConflictKind#DEADLOCK DEADLOCK} leading
 * to that state is returned; otherwise a shortest trace marked {@link
 * net.sourceforge.waters.model.des.ConflictKind#LIVELOCK LIVELOCK} leading
 * to a non-coreachable state is returned.</P>
 *
 * <P><STRONG>Algorithm.</STRONG></P>
 *
 * <P>This algorithm proceeds in two passes. In the first pass, the entire
 * state space of the synchronous product is constructed and stored, and in
 * the second pass, a backwards search starting from the marked states is
 * performed to determine whether all the states constructed in the first
 * pass are coreachable.</P>
 *
 * <P>State tuples are stored in bit-packed form in a hash table. The number
 * and size of the state tuples is the main limiting factor in terms of
 * memory requirements. The number of states can be limited by specifying
 * the node limit ({@link #setNodeLimit(int) setNodeLimit()}).</P>
 *
 * <P>The algorithm to determine which transitions are enabled is highly
 * optimised. A lot of effort is taken to suppress the exploration of
 * disabled transitions or transitions known to be selfloops early.</P>
 *
 * <P><STRONG>Configuration.</STRONG></P>
 *
 * <UL>
 * <LI>The algorithm can be configured to choose between three different
 *     approaches for the coreachability search in the second pass (see
 *     {@link ConflictCheckMode}).
 *     With {@link ConflictCheckMode#STORED_BACKWARDS_TRANSITIONS}, all
 *     transitions encountered in the first pass are stored and used to speed
 *     up the coreachability search in the second pass.
 *     With {@link ConflictCheckMode#COMPUTED_BACKWARDS_TRANSITIONS}, no
 *     transitions are stored in the first pass, and the reverse transitions
 *     are calculated from the component automata in the second pass.
 *     With {@link ConflictCheckMode#NO_BACKWARDS_TRANSITIONS}, the second
 *     pass is avoided altogether, using a different search strategy
 *     (Tarjan's algorithm) in the first pass.</LI>
 * <LI>If the node limit is specified ({@link #setNodeLimit(int)
 *     setNodeLimit()}), it defines the maximum number of states that can
 *     be constructed. If the synchronous product state space turns out to
 *     be larger during the first pass, the verification attempt is aborted
 *     and an {@link net.sourceforge.waters.model.analysis.OverflowException
 *     OverflowException} is thrown.</LI>
 * <LI>If the transition limit is specified ({@link #setTransitionLimit(int)
 *     setTransitionLimit()}), it defines the maximum number of transitions
 *     that can be stored when the mode is {@link
 *     ConflictCheckMode#STORED_BACKWARDS_TRANSITIONS}. If the synchronous
 *     product turns out to include more transitions than specified by the
 *     transition limit, the verification attempt is aborted and an {@link
 *     net.sourceforge.waters.model.analysis.OverflowException
 *     OverflowException} is thrown.</LI>
 * </UL>
 *
 * @author Robi Malik
 */

public class NativeConflictChecker
  extends NativeModelVerifier
  implements ConflictChecker
{

  //#########################################################################
  //# Constructors
  public NativeConflictChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public NativeConflictChecker(final ProductDESProxy model,
                               final ProductDESProxyFactory factory)
  {
    this(model, null, factory);
  }

  public NativeConflictChecker(final ProductDESProxy model,
                               final EventProxy marking,
                               final ProductDESProxyFactory factory)
  {
    super(model, factory, ConflictKindTranslator.getInstanceUncontrollable());
    mMarking = marking;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public void setModel(final ProductDESProxy model)
  {
    super.setModel(model);
    mUsedMarking = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ConflictChecker
  @Override
  public void setConfiguredDefaultMarking(final EventProxy marking)
  {
    mMarking = marking;
    mUsedMarking = null;
    clearAnalysisResult();
  }

  @Override
  public EventProxy getConfiguredDefaultMarking()
  {
    return mMarking;
  }

  @Override
  public void setConfiguredPreconditionMarking(final EventProxy marking)
  {
    mPreconditionMarking = marking;
    clearAnalysisResult();
  }

  @Override
  public EventProxy getConfiguredPreconditionMarking()
  {
    return mPreconditionMarking;
  }

  @Override
  public ConflictCounterExampleProxy getCounterExample()
  {
    return (ConflictCounterExampleProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Specific Configuration
  /**
   * Sets the conflict check algorithm to be used.
   * @see ConflictCheckMode
   */
  public void setConflictCheckMode(final ConflictCheckMode mode)
  {
    mConflictCheckMode = mode;
  }

  /**
   * Gets the conflict check algorithm used.
   * @return The configured conflict check mode, with two exceptions.
   *         If the configured mode is
   *         {@link ConflictCheckMode#STORED_BACKWARDS_TRANSITIONS} and the
   *         transition limit is zero, or if the configured mode is
   *         {@link ConflictCheckMode#NO_BACKWARDS_TRANSITIONS}
   *         and a precondition marking is configured, then a mode of
   *         {@link ConflictCheckMode#COMPUTED_BACKWARDS_TRANSITIONS} is
   *         returned instead.
   * @see ConflictCheckMode
   */
  public ConflictCheckMode getConflictCheckMode()
  {
    switch (mConflictCheckMode) {
    case STORED_BACKWARDS_TRANSITIONS:
      if (getTransitionLimit() == 0) {
        return ConflictCheckMode.COMPUTED_BACKWARDS_TRANSITIONS;
      }
      break;
    case NO_BACKWARDS_TRANSITIONS:
      if (getConfiguredPreconditionMarking() != null) {
        return ConflictCheckMode.COMPUTED_BACKWARDS_TRANSITIONS;
      }
      break;
    default:
      break;
    }
    return mConflictCheckMode;
  }

  /**
   * Set whether this conflict checker should stop when encountering
   * a local dump state. A local dump state is an obvious deadlock state in
   * a single automaton of the system. If such a state is reached, the system
   * is clearly blocking, and the conflict may stop immediately. However,
   * it may fail to produce a full deadlock counterexample, as other
   * components may allow further transitions before the global systems
   * reaches a deadlock.
   */
  public void setDumpStateAware(final boolean aware)
  {
    mDumpStateAware = aware;
  }

  /**
   * Returns whether this conflict checker stops when encountering a local
   * dump state.
   * @return The <CODE>true</CODE> if local dump states are enabled,
   *         <CODE>false</CODE> otherwise. However, always returns
   *         <CODE>false</CODE> if a precondition marking is configured.
   * @see #setDumpStateAware(boolean) setDumpStateAware()
   */
  public boolean isDumpStateAware()
  {
    if (getConfiguredPreconditionMarking() == null) {
      return mDumpStateAware;
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  public EventProxy getUsedDefaultMarking()
    throws EventNotFoundException
  {
    if (mUsedMarking == null) {
      if (mMarking == null) {
        final ProductDESProxy model = getModel();
        mUsedMarking = AbstractConflictChecker.findMarkingProposition(model);
      } else {
        mUsedMarking = mMarking;
      }
    }
    return mUsedMarking;
  }


  //#########################################################################
  //# Native Methods
  @Override
  native VerificationResult runNativeAlgorithm();

  @Override
  public String getTraceName()
  {
    return getModel().getName() + "-conflicting";
  }


  //#########################################################################
  //# Data Members
  private EventProxy mMarking;
  private EventProxy mUsedMarking;
  private EventProxy mPreconditionMarking;
  private ConflictCheckMode mConflictCheckMode =
    ConflictCheckMode.STORED_BACKWARDS_TRANSITIONS;
  private boolean mDumpStateAware = true;

}
