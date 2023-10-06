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

package net.sourceforge.waters.analysis.trcomp;

import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;

import org.apache.logging.log4j.Logger;


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
      return tr.getTRState(index);
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
                          final AbstractTRCompositionalModelAnalyzer analyzer)
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
