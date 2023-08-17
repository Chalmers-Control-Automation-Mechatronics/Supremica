//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import java.util.List;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.OverflowKind;


/**
 * <P>A transition relation simplifier that accepts its result under
 * programmable conditions.</P>
 *
 * <P>The conditional simplifier encapsulates a transition relation
 * simplifier chain ({@link ChainTRSimplifier}), which is only used
 * under certain conditions. It is possible to program a <I>precondition</I>
 * to prevent the simplification chain to be invoked under certain
 * conditions, and a <I>postcondition</I> to decide whether or not the
 * result of the simplification chain is to be used or discarded.</P>
 *
 * <P>The conditional simplifier can also be configured to guard against
 * overflow conditions. If the encapsulated chain then fails due to an
 * overflow or out-of-memory condition, the exception is caught and the input
 * transition relation is returned unchanged.</P>
 *
 * @author Robi Malik
 */

public class ConditionalTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new conditional transition relation simplifier.
   * This constructor creates a conditional simplifier with an empty
   * encapsulated chain. The method {@link #add(TransitionRelationSimplifier)
   * add()} is used to add simplification steps.
   */
  public ConditionalTRSimplifier()
  {
    mConditionalChain = new ChainTRSimplifier();
    mChainWasApplied = false;
    createStatistics();
  }


  //#########################################################################
  //# Configuration
  /**
   * Adds a simplification step to this conditional simplifier.
   * This methods adds the given transition relation simplifier to the
   * end of the encapsulated simplifier chain.
   * @return The index position of the added simplifier in the chain.
   */
  public int add(final TransitionRelationSimplifier step)
  {
    return mConditionalChain.add(step);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.abstraction.
  //# TransitionRelationSimplifier
  @Override
  public int getPreferredInputConfiguration()
  {
    return mConditionalChain.getPreferredOutputConfiguration();
  }

  @Override
  public void setPreferredOutputConfiguration(final int config)
  {
    mConditionalChain.setPreferredOutputConfiguration(config);
  }

  @Override
  public void setMarkings(final int preconditionID, final int defaultID)
  {
    mConditionalChain.setMarkings(preconditionID, defaultID);
  }

  @Override
  public void setPropositionMask(final long mask)
  {
    mConditionalChain.setPropositionMask(mask);
  }

  @Override
  public void setStateLimit(final int limit)
  {
    mConditionalChain.setStateLimit(limit);
  }

  @Override
  public int getStateLimit()
  {
    return mConditionalChain.getStateLimit();
  }

  @Override
  public void setTransitionLimit(final int limit)
  {
    mConditionalChain.setTransitionLimit(limit);
  }

  @Override
  public int getTransitionLimit()
  {
    return mConditionalChain.getTransitionLimit();
  }

  @Override
  public boolean isPartitioning()
  {
    return mConditionalChain.isPartitioning();
  }

  @Override
  public boolean isObservationEquivalentAbstraction()
  {
    if (mChainWasApplied) {
      return mConditionalChain.isObservationEquivalentAbstraction();
    } else {
      return true;
    }
  }

  @Override
  public boolean isAlwaysEnabledEventsSupported()
  {
    return mConditionalChain.isAlwaysEnabledEventsSupported();
  }

  @Override
  public boolean isReducedMarking(final int prop)
  {
    if (mChainWasApplied) {
      return mConditionalChain.isReducedMarking(prop);
    } else {
      return false;
    }
  }

  @Override
  public TRSimplifierStatistics createStatistics()
  {
    if (mConditionalChain != null) {
      return mConditionalChain.createStatistics();
    } else {
      return null;
    }
  }

  @Override
  public void collectStatistics(final List<TRSimplifierStatistics> list)
  {
    mConditionalChain.collectStatistics(list);
  }

  @Override
  public void reset()
  {
    super.reset();
    mConditionalChain.reset();
  }

  @Override
  protected void logStart()
  {
  }

  @Override
  protected void logFinish(final boolean success)
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    mConditionalChain.requestAbort();
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    mConditionalChain.resetAbort();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.abstraction.
  //# AbstractTRSimplifier
  @Override
  protected void setUp()
    throws AnalysisException
  {
    // Do not call super to avoid reconfiguration of TR ahead of time.
    setResultPartition(null);
    mChainWasApplied = false;
  }

  @Override
  protected boolean runSimplifier()
    throws AnalysisException
  {
    if (!checkPreCondition()) {
      return false;
    }
    final ListBufferTransitionRelation input = getTransitionRelation();
    final int config = mConditionalChain.getPreferredInputConfiguration();
    ListBufferTransitionRelation output =
      isCopying() ? new ListBufferTransitionRelation(input, config) : input;
    mConditionalChain.setTransitionRelation(output);
    final boolean result;
    if (isRecoveringFromOverflow()) {
      try {
        result = mConditionalChain.run();
      } catch (final OverflowException exception) {
        handleOverflow(exception.getOverflowKind(), exception);
        return false;
      } catch (final OutOfMemoryError error) {
        System.gc();
        handleOverflow(OverflowKind.MEMORY, error);
        return false;
      }
    } else {
      try {
        result = mConditionalChain.run();
      } catch (final OverflowException exception) {
        handleOverflow(exception.getOverflowKind(), exception);
        throw exception;
      } catch (final OutOfMemoryError error) {
        System.gc();
        handleOverflow(OverflowKind.MEMORY, error);
        throw new OverflowException(error);
      }
    }
    if (!result) {
      return false;
    }
    output = mConditionalChain.getTransitionRelation();
    if (!checkPostCondition(output)) {
      return false;
    }
    if (isCopying()) {
      input.copyFrom(output);
    } else {
      setTransitionRelation(output);
    }
    final TRPartition partition = mConditionalChain.getResultPartition();
    setResultPartition(partition);
    mChainWasApplied = true;
    return true;
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mConditionalChain.reset();
  }


  //#########################################################################
  //# Hooks
  /**
   * <P>Checks the precondition.</P>
   *
   * <P>This method is called when the conditional simplifier starts.
   * If it returns <CODE>true</CODE>, the encapsulated simplifier chain is
   * invoked, otherwise nothing happens and the conditional simplifier
   * reports an unchanged transition relation.</P>
   *
   * <P>This method is typically overridden in a subclass. Its default
   * behaviour simply returns <CODE>true</CODE>. When invoked,
   * the simplifier's transition relation is set to the input transition
   * relation to be simplified.</P>
   */
  protected boolean checkPreCondition()
    throws AnalysisException
  {
    return true;
  }

  /**
   * <P>Checks the postcondition.</P>
   *
   * <P>This method is called after the encapsulated simplifier chain has
   * finished and returned a successful result. If the method returns
   * <CODE>true</CODE>, the result of the encapsulated chain is accepted to
   * become the result of the conditional simplifier, otherwise it is
   * rejected and the conditional simplifier reports an unchanged
   * transition relation.</P>
   *
   * <P>This method is typically overridden in a subclass. Its default
   * behaviour simply returns <CODE>true</CODE>. When invoked,
   * the simplifier's transition relation still holds the input transition
   * relation (before a copy was made and passed to the encapsulated chain),
   * so that it can be compared to the result.</P>
   *
   * @param  result  The transition relation returned by the encapsulated
   *                 chain, which may become the result of the conditional
   *                 simplifier.
   * @see #isCopying()
   */
  protected boolean checkPostCondition(final ListBufferTransitionRelation result)
    throws AnalysisException
  {
    return true;
  }

  /**
   * <P>Returns whether the conditional simplifier makes a copy of the
   * transition relation before invoking the encapsulated chain.</P>
   *
   * <P>This method is typically overridden in a subclass. Its default
   * behaviour returns <CODE>true</CODE> so that a copy is made.</P>
   *
   * @see #checkPostCondition(ListBufferTransitionRelation) checkPostCondition()
   */
  protected boolean isCopying()
  {
    return true;
  }

  /**
   * <P>Returns whether the conditional simplifier recovers from overflow
   * conditions.</P>
   *
   * <P>This method is called before starting the encapsulated simplifier
   * chain. If it returns <CODE>true</CODE> and the encapsulated simplifier
   * throws and overflow or out-of-memory exception, the exception is caught
   * and the conditional simplifier reports an unchanged transition relation.
   * Otherwise exceptions thrown by the encapsulated simplifier chain are also
   * thrown by the conditional simplifier.</P>
   *
   * <P>This method is typically overridden in a subclass. Its default
   * behaviour returns <CODE>true</CODE> so that exceptions are caught.</P>
   *
   * @see OverflowException
   * @see OutOfMemoryError
   */
  protected boolean isRecoveringFromOverflow()
  {
    return true;
  }

  /**
   * <P>Callback for overflow conditions within the encapsulated simplifier
   * chain. This method is called after catching an {@link OverflowException}
   * or {@link OutOfMemoryError} when running the encapsulated chain.</P>
   *
   * <P>The default implementation of this handler is empty and does not
   * need to be called when overriding it.</P>
   *
   * @param  kind    The type of overflow, such as {@link OverflowKind#STATE}
   *                 or {@link OverflowKind#MEMORY}.
   * @param  cause   The caught exception.
   * @throws AnalysisException If the callback returns without throwing an
   *         exception, the conditional simplifier decides based on its
   *         configuration whether or not to throw an exception.
   *         The callback may throw an exception to override the configured
   *         behaviour.
   * @see #isRecoveringFromOverflow()
   */
  protected void handleOverflow(final OverflowKind kind,
                                final Throwable cause)
    throws AnalysisException
  {
  }


  //#########################################################################
  //# Data Members
  private final ChainTRSimplifier mConditionalChain;
  private boolean mChainWasApplied;

}
