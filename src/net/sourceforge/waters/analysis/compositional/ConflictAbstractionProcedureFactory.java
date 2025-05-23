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

package net.sourceforge.waters.analysis.compositional;

import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;


/**
 * A collection of abstraction methods to be used for compositional
 * nonblocking verification. The members of this enumeration are passed to the
 * {@link CompositionalConflictChecker} using its
 * {@link AbstractCompositionalModelAnalyzer#setAbstractionProcedureCreator(AbstractionProcedureCreator)
 * setAbstractionProcedureFactory()} method.
 *
 * @see AbstractionProcedure
 * @author Robi Malik
 */

public class ConflictAbstractionProcedureFactory
  extends ListedEnumFactory<AbstractionProcedureCreator>
{

  //#########################################################################
  //# Singleton Pattern
  public static ConflictAbstractionProcedureFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder
  {
    private static ConflictAbstractionProcedureFactory INSTANCE =
      new ConflictAbstractionProcedureFactory();
  }


  //#########################################################################
  //# Constructors
  protected ConflictAbstractionProcedureFactory()
  {
    register(CC);
    register(GNB);
    register(NB);
    register(NBA);
    register(NBC);
    register(OEQ);
    register(OP);
    register(OPVERIFIER);
    register(WOEQ);
  }


  //#########################################################################
  //# Enumeration
  /**
   * <P>Minimisation is performed according to a sequence of abstraction
   * rules for standard nonblocking, but using weak observation
   * equivalence instead of observation equivalence, and using proper
   * certain conflicts simplification instead of limited certain
   * conflicts.</P>
   * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional
   * Verification in Supervisory Control. SIAM Journal of Control and
   * Optimization, 48(3), 1914-1938, 2009.</P>
   */
  public static final AbstractionProcedureCreator CC =
    new AbstractionProcedureCreator("CC")
  {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return ThreeStepConflictEquivalenceAbstractionProcedure.
        createNBAbstractionProcedure
          (analyzer, ObservationEquivalenceTRSimplifier.Equivalence.
           OBSERVATION_EQUIVALENCE, false, true);
    }
  };

  /**
   * <P>Minimisation is performed according to a sequence of abstraction
   * rules for generalised nonblocking proposed, but using weak observation
   * equivalence instead of observation equivalence.</P>
   * <P><I>Reference:</I><BR>
   * Robi Malik, Ryan Leduc. Compositional Nonblocking Verification Using
   * Generalised Nonblocking Abstractions, IEEE Transactions on Automatic
   * Control <STRONG>58</STRONG>(8), 1-13, 2013.</P>
   */
  public static final AbstractionProcedureCreator GNB =
    new AbstractionProcedureCreator("GNB")
  {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return TRConflictEquivalenceAbstractionProcedure.
        createGeneralisedNonblockingProcedure
          (analyzer, ObservationEquivalenceTRSimplifier.Equivalence.
           WEAK_OBSERVATION_EQUIVALENCE);
    }

    @Override
    public boolean expectsAllMarkings()
    {
      return true;
    }
  };

  /**
   * <P>Minimisation is performed according to a sequence of abstraction rules
   * for standard nonblocking, but using weak observation
   * equivalence instead of observation equivalence.</P>
   * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional
   * Verification in Supervisory Control. SIAM Journal of Control and
   * Optimization, 48(3), 1914-1938, 2009.</P>
   */
  public static final AbstractionProcedureCreator NB =
    new AbstractionProcedureCreator("NB")
  {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return ThreeStepConflictEquivalenceAbstractionProcedure.
        createNBAbstractionProcedure
          (analyzer, ObservationEquivalenceTRSimplifier.Equivalence.
           WEAK_OBSERVATION_EQUIVALENCE, true, false);
    }
  };

  /**
   * <P>Minimisation is performed according to a sequence of abstraction rules
   * for standard nonblocking, but using weak observation equivalence instead
   * of observation equivalence, and with an additional step of non-alpha
   * determinisation at the end.</P>
   * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional
   * Verification in Supervisory Control. SIAM Journal of Control and
   * Optimization, 48(3), 1914-1938, 2009.</P>
   */
  public static final AbstractionProcedureCreator NBA =
    new AbstractionProcedureCreator("NBA")
  {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return ThreeStepConflictEquivalenceAbstractionProcedure.
        createNBAAbstractionProcedure
          (analyzer, ObservationEquivalenceTRSimplifier.Equivalence.
           WEAK_OBSERVATION_EQUIVALENCE);
    }
  };

  /**
   * <P>Minimisation is performed according to a sequence of abstraction rules
   * for standard nonblocking, but using weak observation
   * equivalence instead of observation equivalence, and using proper
   * certain conflicts simplification in addition to limited certain
   * conflicts.</P>
   * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional
   * Verification in Supervisory Control. SIAM Journal of Control and
   * Optimization, 48(3), 1914-1938, 2009.</P>
   */
  public static final AbstractionProcedureCreator NBC =
    new AbstractionProcedureCreator("NBC")
  {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return ThreeStepConflictEquivalenceAbstractionProcedure.
        createNBAbstractionProcedure
          (analyzer, ObservationEquivalenceTRSimplifier.Equivalence.
           WEAK_OBSERVATION_EQUIVALENCE, true, true);
    }
  };

  /**
   * Automata are minimised according to <I>observation equivalence</I>.
   */
  public static final AbstractionProcedureCreator OEQ =
    new AbstractionProcedureCreator("OEQ")
  {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return TRConflictEquivalenceAbstractionProcedure.
        createObservationEquivalenceProcedure
          (analyzer, ObservationEquivalenceTRSimplifier.Equivalence.
           OBSERVATION_EQUIVALENCE);
    }
  };

  /**
   * Automata are minimised using <I>observer projection</I>.
   * The present implementation determines a coarsest causal reporter
   * map satisfying the observer property. Nondeterminism in the projected
   * automata is not resolved, nondeterministic abstractions are used instead.
   */
  public static final AbstractionProcedureCreator OP =
    new AbstractionProcedureCreator("OP")
  {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return ObserverProjectionAbstractionProcedure.
        createObserverProjectionProcedure(analyzer);
    }
  };

  /**
   * <P>An experimental abstraction procedure that works like weak
   * observation equivalence, but in addition runs the OP-verifier
   * algorithm on each automaton to gather performance statistics.</P>
   */
  public static final AbstractionProcedureCreator OPVERIFIER =
    new AbstractionProcedureCreator("OPVERIFIER")
  {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return OPVerifierAbstractionProcedure.createOPVerifierProcedure
        (analyzer, ObservationEquivalenceTRSimplifier.Equivalence.
         WEAK_OBSERVATION_EQUIVALENCE);
    }
  };

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
  public static final AbstractionProcedureCreator WOEQ =
    new AbstractionProcedureCreator("WOEQ")
  {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return TRConflictEquivalenceAbstractionProcedure.
        createObservationEquivalenceProcedure
          (analyzer, ObservationEquivalenceTRSimplifier.Equivalence.
           WEAK_OBSERVATION_EQUIVALENCE);
    }
  };

}
