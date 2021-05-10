//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.efa.efsm;

import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;


/**
 * A collection of abstraction methods to be used for compositional
 * nonblocking verification. The members of this enumeration are passed to the
 * {@link EFSMConflictChecker} using its
 * {@link EFSMConflictChecker#setSimplifierFactory(EFSMTRSimplifierFactory)
 * setSimplifierFactory()} method.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public enum EFSMTRSimplifierFactory
{

  //#########################################################################
  //# Enumeration
  /**
   * <P>Minimisation is performed according to a sequence of abstraction
   * rules for generalised nonblocking, but using weak observation
   * equivalence instead of observation equivalence.</P>
   * <P><I>Reference:</I> Robi Malik, Ryan Leduc. A Compositional Approach
   * for Verifying Generalised Nonblocking, Proc. 7th International
   * Conference on Control and Automation, ICCA'09, 448-453, Christchurch,
   * New Zealand, 2009.</P>
   */
  GNB {
    @Override
    public EFSMTRSimplifier createAbstractionProcedure
      (final EFSMConflictChecker analyzer)
    {
      return EFSMTRSimplifier.createGeneralisedNonblockingProcedure
        (ObservationEquivalenceTRSimplifier.Equivalence.
         WEAK_OBSERVATION_EQUIVALENCE,
         analyzer.getInternalTransitionLimit(), analyzer.getOperatorTable());
    }
  },
  /**
   * <P>Minimisation is performed according to a sequence of abstraction
   * rules for standard nonblocking.</P>
   */
  NB {
    @Override
    public EFSMTRSimplifier createAbstractionProcedure
      (final EFSMConflictChecker analyzer)
    {
      return EFSMTRSimplifier.createStandardNonblockingProcedure
        (ObservationEquivalenceTRSimplifier.Equivalence.
         WEAK_OBSERVATION_EQUIVALENCE,
         analyzer.getInternalTransitionLimit(), analyzer.getOperatorTable());
    }
  },
  /**
   * Automata are minimised according to <I>observation equivalence</I>.
   */
  OEQ {
    @Override
    public EFSMTRSimplifier createAbstractionProcedure
      (final EFSMConflictChecker analyzer)
    {
      return EFSMTRSimplifier.createObservationEquivalenceProcedure
          (ObservationEquivalenceTRSimplifier.Equivalence.
           OBSERVATION_EQUIVALENCE,
           analyzer.getInternalTransitionLimit(), analyzer.getOperatorTable());
    }
  },
  /**
   * <P>Automata are minimised according to <I>weak observation
   * equivalence</I>. Initial states and markings are not saturated, silent
   * transitions are retained instead in a bid to reduce the overall number of
   * transitions.</P>
   *
   * <P><I>Reference.</I> Rong Su, Jan H. van Schuppen, Jacobus E. Rooda,
   * Albert T. Hofkamp. Nonconflict check by using sequential automaton
   * abstractions based on weak observation equivalence. Automatica,
   * <STRONG>46</STRONG>(6), 968-978, 2010.</P>
   */
  WOEQ {
    @Override
    public EFSMTRSimplifier createAbstractionProcedure
      (final EFSMConflictChecker analyzer)
    {
      return EFSMTRSimplifier.createObservationEquivalenceProcedure
          (ObservationEquivalenceTRSimplifier.Equivalence.
           WEAK_OBSERVATION_EQUIVALENCE,
           analyzer.getInternalTransitionLimit(), analyzer.getOperatorTable());
    }
  };


  //#########################################################################
  //# Invocation
  public abstract EFSMTRSimplifier createAbstractionProcedure
    (final EFSMConflictChecker analyzer);

}
