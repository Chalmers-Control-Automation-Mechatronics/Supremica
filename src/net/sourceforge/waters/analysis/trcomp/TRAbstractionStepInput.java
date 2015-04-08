//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRAbstractionStepInput
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;

import org.apache.log4j.Logger;


/**
 * An abstraction step representing an input automaton.
 *
 * @author Robi Malik
 */

class TRAbstractionStepInput
  extends TRAbstractionStep
  implements Comparable<TRAbstractionStepInput>
{

  //#########################################################################
  //# Constructor
  TRAbstractionStepInput(final TRAutomatonProxy inputAut)
  {
    super(inputAut.getName());
    mInputAutomaton = inputAut;
    mEventEncoding = null;
    mStateEncoding = null;
    mDumpState = null;
  }

  TRAbstractionStepInput(final AutomatonProxy inputAut,
                         final EventEncoding enc)
  {
    this(inputAut, enc, null);
  }

  TRAbstractionStepInput(final AutomatonProxy inputAut,
                         final EventEncoding enc,
                         final StateProxy dumpState)
  {
    super(inputAut.getName());
    mInputAutomaton = inputAut;
    mEventEncoding = enc;
    mStateEncoding = new StateEncoding(inputAut);
    mDumpState = dumpState;
  }


  //#########################################################################
  //# Simple Access
  AutomatonProxy getInputAutomaton()
  {
    return mInputAutomaton;
  }

  StateProxy getState(final int index)
  {
    if (mEventEncoding == null) {
      final TRAutomatonProxy tr = (TRAutomatonProxy) mInputAutomaton;
      return tr.getState(index);
    } else {
      return mStateEncoding.getState(index);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.trcomp.TRAbstractionStep
  @Override
  public Collection<TRAbstractionStep> getPredecessors()
  {
    return Collections.emptyList();
  }

  @Override
  public TRAutomatonProxy createOutputAutomaton(final int preferredConfig)
    throws OverflowException
  {
    if (mEventEncoding == null) {
      final TRAutomatonProxy tr = (TRAutomatonProxy) mInputAutomaton;
      return new TRAutomatonProxy(tr);
    } else {
      final EventEncoding clonedEnc = new EventEncoding(mEventEncoding);
      return new TRAutomatonProxy(mInputAutomaton,
                                  clonedEnc,
                                  mStateEncoding,
                                  mDumpState,
                                  preferredConfig);
    }
  }

  @Override
  public void expandTrace(final TRTraceProxy trace,
                          final AbstractTRCompositionalAnalyzer analyzer)
  {
    trace.setInputAutomaton(this);
  }


  //#########################################################################
  //# Interface java.util.Comparable<TRInputStep>
  @Override
  public int compareTo(final TRAbstractionStepInput step)
  {
    return mInputAutomaton.compareTo(step.mInputAutomaton);
  }


  //#########################################################################
  //# Debugging
  @Override
  public void reportExpansion()
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      logger.debug("Linking to input automaton " + getName() + " ...");
    }
  }


  //#########################################################################
  //# Data Members
  private final AutomatonProxy mInputAutomaton;
  private final EventEncoding mEventEncoding;
  private final StateEncoding mStateEncoding;
  private final StateProxy mDumpState;

}
