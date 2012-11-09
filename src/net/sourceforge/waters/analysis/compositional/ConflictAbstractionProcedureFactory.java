//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ConflictAbstractionProcedureFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;

/**
 * <P>A collection of abstraction methods to be used for compositional
 * nonblocking verification.</P>
 *
 * <P>To configure a conflict checker, call the method
 * {@link #createAbstractionProecudure(AbstractCompositionalModelAnalyzer)
 * createAbstractionProecudure()} of the desired enumeration member,
 * and pass the result to the conflict checker's {@link
 * AbstractCompositionalModelAnalyzer#setAbstractionProcedure(AbstractionProcedure)
 * setAbstractionProcedure()} method.</P>
 *
 * <PRE>AbstractionProcedure proc =
 * &nbsp;&nbsp;ConflictAbstractionProcedureFactory.{@link #NBA}.{@link #createAbstractionProecudure(AbstractCompositionalModelAnalyzer) createAbstractionProecudure}(analyzer);
 * &nbsp;&nbsp;analyzer.{@link AbstractCompositionalModelAnalyzer#setAbstractionProcedure(AbstractionProcedure) setAbstractionProcedure}(proc);</PRE>
 *
 * @see AbstractionProcedure
 * @see CompositionalConflictChecker
 */

public enum ConflictAbstractionProcedureFactory
{

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
  CC {
    @Override
    public AbstractionProcedure createAbstractionProecudure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return ThreeStepConflictEquivalenceAbstractionProcedure.
        createThreeStepConflictEquivalenceAbstractionProcedure
          (analyzer, ObservationEquivalenceTRSimplifier.Equivalence.
           OBSERVATION_EQUIVALENCE, false, false, true);
    }
  },
  /**
    * <P>Minimisation is performed according to a sequence of abstraction
   * rules for generalised nonblocking proposed, but using weak observation
   * equivalence instead of observation equivalence.</P>
   * <P><I>Reference:</I> Robi Malik, Ryan Leduc. A Compositional Approach
   * for Verifying Generalised Nonblocking, Proc. 7th International
   * Conference on Control and Automation, ICCA'09, 448-453, Christchurch,
   * New Zealand, 2009.</P>
   */
  GNB {
    @Override
    public AbstractionProcedure createAbstractionProecudure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return TRConflictEquivalenceAbstractionProcedure.
        createGeneralisedNonblockingProcedure
          (analyzer, ObservationEquivalenceTRSimplifier.Equivalence.
           WEAK_OBSERVATION_EQUIVALENCE);
    }
  },
  /**
   * <P>Minimisation is performed according to a sequence of abstraction rules
   * for standard nonblocking, but using weak observation
   * equivalence instead of observation equivalence.</P>
   * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional
   * Verification in Supervisory Control. SIAM Journal of Control and
   * Optimization, 48(3), 1914-1938, 2009.</P>
   */
  NB {
    @Override
    public AbstractionProcedure createAbstractionProecudure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return ThreeStepConflictEquivalenceAbstractionProcedure.
        createThreeStepConflictEquivalenceAbstractionProcedure
          (analyzer, ObservationEquivalenceTRSimplifier.Equivalence.
           WEAK_OBSERVATION_EQUIVALENCE, false, true, false);
    }
  },
  /**
   * <P>Minimisation is performed according to a sequence of abstraction rules
   * for standard nonblocking, but using weak observation equivalence instead
   * of observation equivalence, and with an additional step of non-alpha
   * determinisation at the end.</P>
   * <P><I>Reference:</I> Hugo Flordal, Robi Malik. Compositional
   * Verification in Supervisory Control. SIAM Journal of Control and
   * Optimization, 48(3), 1914-1938, 2009.</P>
   */
  NBA {
    @Override
    public AbstractionProcedure createAbstractionProecudure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return ThreeStepConflictEquivalenceAbstractionProcedure.
        createThreeStepConflictEquivalenceAbstractionProcedure
          (analyzer, ObservationEquivalenceTRSimplifier.Equivalence.
           WEAK_OBSERVATION_EQUIVALENCE, true, true, false);
    }
  },
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
  NBC {
    @Override
    public AbstractionProcedure createAbstractionProecudure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return ThreeStepConflictEquivalenceAbstractionProcedure.
        createThreeStepConflictEquivalenceAbstractionProcedure
          (analyzer, ObservationEquivalenceTRSimplifier.Equivalence.
           WEAK_OBSERVATION_EQUIVALENCE, false, true, true);
    }
  },
  /**
   * Automata are minimised according to <I>observation equivalence</I>.
   */
  OEQ {
    @Override
    public AbstractionProcedure createAbstractionProecudure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return TRConflictEquivalenceAbstractionProcedure.
        createObservationEquivalenceProcedure
          (analyzer, ObservationEquivalenceTRSimplifier.Equivalence.
           OBSERVATION_EQUIVALENCE);
    }
  },
  /**
   * Automata are minimised according using <I>observer projection</I>.
   * The present implementation determines a coarsest causal reporter
   * map satisfying the observer property. Nondeterminism in the projected
   * automata is not resolved, nondeterministic abstractions are used instead.
   */
  OP {
    @Override
    public AbstractionProcedure createAbstractionProecudure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return ObserverProjectionAbstractionProcedure.
        createObserverProjectionProcedure(analyzer);
    }
  },
  /**
   * <P>An experimental abstraction procedure that works like weak
   * observation equivalence, but in addition runs the OP-verifier
   * algorithm on each automaton to gather performance statistics.</P>
   */
  OPVERIFIER {
    @Override
    public AbstractionProcedure createAbstractionProecudure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return OPVerifierAbstractionProcedure.createOPVerifierProcedure
        (analyzer, ObservationEquivalenceTRSimplifier.Equivalence.
         WEAK_OBSERVATION_EQUIVALENCE);
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
    public AbstractionProcedure createAbstractionProecudure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return TRConflictEquivalenceAbstractionProcedure.
        createObservationEquivalenceProcedure
          (analyzer, ObservationEquivalenceTRSimplifier.Equivalence.
           WEAK_OBSERVATION_EQUIVALENCE);
    }
  };

  //#########################################################################
  //# Factory Methods
  /**
   * Creates an abstraction procedure to be used by the given model analyzer.
   */
  public abstract AbstractionProcedure createAbstractionProecudure
    (AbstractCompositionalModelAnalyzer analyzer);

}