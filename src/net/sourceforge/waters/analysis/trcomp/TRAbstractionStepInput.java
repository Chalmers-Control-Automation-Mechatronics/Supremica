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
  TRAbstractionStepInput(final AutomatonProxy aut,
                         final EventEncoding enc,
                         final StateProxy dumpState)
  {
    mInputAutomaton = aut;
    mEventEncoding = enc;
    mDumpState = dumpState;
  }


  //#########################################################################
  //# Simple Access
  AutomatonProxy getInputAutomaton()
  {
    return mInputAutomaton;
  }

  StateEncoding getStateEncoding()
  {
    if (mStateEncoding == null) {
      mStateEncoding = new StateEncoding(mInputAutomaton);
    }
    return mStateEncoding;
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
    return new TRAutomatonProxy(mInputAutomaton,
                                mEventEncoding,
                                getStateEncoding(),
                                mDumpState,
                                preferredConfig);
  }

  @Override
  public void expandTrace(final TRTraceProxy trace)
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
  //# Data Members
  private final AutomatonProxy mInputAutomaton;
  private final EventEncoding mEventEncoding;
  private final StateProxy mDumpState;

  private StateEncoding mStateEncoding;

}
