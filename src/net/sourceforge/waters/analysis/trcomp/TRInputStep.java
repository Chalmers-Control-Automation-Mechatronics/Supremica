//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRInputStep
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

class TRInputStep
  extends TRAbstractionStep
  implements Comparable<TRInputStep>
{

  //#########################################################################
  //# Constructor
  TRInputStep(final AutomatonProxy aut,
              final EventEncoding enc,
              final StateProxy dumpState,
              final int config)
  {
    mInputAutomaton = aut;
    mEventEncoding = enc;
    mDumpState = dumpState;
    mInitialConfiguration = config;
  }


  //#########################################################################
  //# Simple Access
  AutomatonProxy getInputAutomaton()
  {
    return mInputAutomaton;
  }

  StateEncoding getStateEncoding()
  {
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
  public TRAbstractionStep getSuccessor()
  {
    return mSuccessor;
  }

  @Override
  public TRAutomatonProxy createOutputAutomaton()
    throws OverflowException
  {
    mStateEncoding = new StateEncoding(mInputAutomaton);
    return new TRAutomatonProxy(mInputAutomaton,
                                mEventEncoding,
                                mStateEncoding,
                                mDumpState,
                                mInitialConfiguration);
  }

  @Override
  public void expandTrace(final TRTraceProxy trace)
  {
  }


  //#########################################################################
  //# Interface java.util.Comparable<TRInputStep>
  @Override
  public int compareTo(final TRInputStep step)
  {
    return mInputAutomaton.compareTo(step.mInputAutomaton);
  }


  //#########################################################################
  //# Instance Variables
  private final AutomatonProxy mInputAutomaton;
  private final EventEncoding mEventEncoding;
  private final StateProxy mDumpState;
  private final int mInitialConfiguration;

  private StateEncoding mStateEncoding;
  private TRAbstractionStep mSuccessor;

}
