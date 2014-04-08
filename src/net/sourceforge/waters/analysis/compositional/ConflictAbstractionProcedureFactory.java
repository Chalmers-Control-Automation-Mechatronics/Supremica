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
    register(EENB);
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
   * <P>Minimisation considers events that are known to be always-enabled or
   * only-selfloop outside of the automaton being simplified. This is the
   * same abstraction sequence as {@link #NB}, with special events enabled
   * and additional steps in the chain to measure performance.</P>
   */
  public static final AbstractionProcedureCreator EENB =
    new AbstractionProcedureCreator("EENB")
  {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      return EnabledEventsThreeStepConflictEquivalenceAbstractionProcedure.
        createThreeStepConflictEquivalenceAbstractionProcedure
          (analyzer, ObservationEquivalenceTRSimplifier.Equivalence.
           WEAK_OBSERVATION_EQUIVALENCE, true, false, true);
    }
  };

  /**
   * <P>Minimisation is performed according to a sequence of abstraction
   * rules for generalised nonblocking proposed, but using weak observation
   * equivalence instead of observation equivalence.</P>
   * <P><I>Reference:</I> Robi Malik, Ryan Leduc. A Compositional Approach
   * for Verifying Generalised Nonblocking, Proc. 7th International
   * Conference on Control and Automation, ICCA'09, 448-453, Christchurch,
   * New Zealand, 2009.</P>
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