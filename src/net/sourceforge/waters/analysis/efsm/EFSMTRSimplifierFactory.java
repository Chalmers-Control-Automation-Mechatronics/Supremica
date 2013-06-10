//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ConflictAbstractionProcedureFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelAnalyzer;
import net.sourceforge.waters.analysis.compositional.AbstractionProcedure;
import net.sourceforge.waters.analysis.compositional.AbstractionProcedureFactory;
import net.sourceforge.waters.analysis.compositional.CompositionalConflictChecker;


/**
 * A collection of abstraction methods to be used for compositional
 * nonblocking verification. The members of this enumeration are passed to the
 * {@link CompositionalConflictChecker} using its
 * {@link AbstractCompositionalModelAnalyzer#setAbstractionProcedureFactory(AbstractionProcedureFactory)
 * setAbstractionProcedureFactory()} method.
 *
 * @see AbstractionProcedure
 * @author Robi Malik
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