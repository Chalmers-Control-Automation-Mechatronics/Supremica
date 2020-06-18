//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.analysis.compositional;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;

/**
 * An abstraction step that uses a {@link ListBufferTransitionRelation}
 * for trace expansion
 *
 * @author Robi Malik
 */

class TRAbstractionStep extends AbstractionStep
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new abstraction step record.
   * @param  resultAut         The automaton resulting from abstraction.
   * @param  originalAut       The automaton before abstraction.
   * @param  tau               The event represent silent transitions,
   *                           or <CODE>null</CODE>.
   * @param  originalStateEnc  State encoding of input automaton, or
   *                           <CODE>null</CODE> to use a temporary
   *                           encoding.
   */
  protected TRAbstractionStep(final AbstractCompositionalModelAnalyzer analyzer,
                              final AutomatonProxy resultAut,
                              final AutomatonProxy originalAut,
                              final EventProxy tau,
                              final StateEncoding originalStateEnc)
  {
    super(analyzer, resultAut, originalAut);
    mTau = tau;
    mOriginalStateEncoding = originalStateEnc;
  }


  //#########################################################################
  //# Simple Access
  protected EventProxy getTau()
  {
    return mTau;
  }

  protected ListBufferTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  protected EventEncoding getEventEncoding()
  {
    return mEventEncoding;
  }

  protected StateEncoding getOriginalStateEncoding()
  {
    return mOriginalStateEncoding;
  }


  //#########################################################################
  //# Data Members
  /**
   * The event that was hidden from the original automaton,
   * or <CODE>null</CODE>.
   */
  private final EventProxy mTau;
  /**
   * State encoding of original automaton. Maps state codes in the input
   * transition relation to state objects in the input automaton.
   */
  private final StateEncoding mOriginalStateEncoding;
  /**
   * Transition relation that was simplified.
   * Only used when expanding trace.
   */
  private ListBufferTransitionRelation mTransitionRelation;
  /**
   * Event encoding for {@link #mTransitionRelation}.
   * Only used when expanding trace.
   */
  private EventEncoding mEventEncoding;

}
