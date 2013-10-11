//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   SynthesisAbstractionProcedureFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

/**
 * A collection of abstraction methods to be used for compositional
 * synthesis. The members of this enumeration are passed to the
 * {@link CompositionalStateRepresentationSynthesizer} using its
 * {@link AbstractCompositionalModelAnalyzer#setAbstractionProcedureFactory(AbstractionProcedureFactory)
 * setAbstractionProcedureFactory()} method.
 *
 * @see AbstractionProcedure
 * @author Robi Malik, Sahar Mohajerani
 */


public enum StateRepresentationSynthesisAbstractionProcedureFactory
  implements AbstractionProcedureFactory
{

  //#########################################################################
  //# Enumeration
  /**
   * An abstraction chain consisting of halfway synthesis, bisimulation,
   * and synthesis observation equivalence.
   */
  SOE {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final CompositionalStateRepresentationSynthesizer synthesizer =
        (CompositionalStateRepresentationSynthesizer) analyzer;
      return StateRepresentationSynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer, StateRepresentationSynthesisAbstractionProcedure.CHAIN_SOE);
    }
  },

  /**
   * An abstraction chain consisting of bisimulation
   * and synthesis observation equivalence.
   */
  SOE_ONLY {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final CompositionalStateRepresentationSynthesizer synthesizer =
        (CompositionalStateRepresentationSynthesizer) analyzer;
      return StateRepresentationSynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer,
           StateRepresentationSynthesisAbstractionProcedure.USE_BISIMULATION |
           StateRepresentationSynthesisAbstractionProcedure.USE_SOE);
    }
  },

  /**
   * An abstraction chain consisting of bisimulation
   * and synthesis observation equivalence.
   */
  NO_TRANSITIONREMOVAL {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final CompositionalStateRepresentationSynthesizer synthesizer =
        (CompositionalStateRepresentationSynthesizer) analyzer;
      return StateRepresentationSynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer,
            StateRepresentationSynthesisAbstractionProcedure.USE_HALFWAY|
           StateRepresentationSynthesisAbstractionProcedure.USE_BISIMULATION |
           StateRepresentationSynthesisAbstractionProcedure.USE_WSOE
           );
    }
  },

  /**
   * An abstraction chain consisting of halfway synthesis, bisimulation,
   * weak synthesis observation equivalence and transition removal.
   */
  WSOE {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final CompositionalStateRepresentationSynthesizer synthesizer =
        (CompositionalStateRepresentationSynthesizer) analyzer;
      return StateRepresentationSynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer, StateRepresentationSynthesisAbstractionProcedure.CHAIN_WSOE);
    }
  },


  /**
   * An abstraction chain consisting of certain unsupervisability, bisimulation,
   * and weak synthesis observation equivalence.
   */
  WSOE_UNSUP {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final CompositionalStateRepresentationSynthesizer synthesizer =
        (CompositionalStateRepresentationSynthesizer) analyzer;
      return StateRepresentationSynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer,
           StateRepresentationSynthesisAbstractionProcedure.USE_UNSUP |
           StateRepresentationSynthesisAbstractionProcedure.USE_BISIMULATION |
           StateRepresentationSynthesisAbstractionProcedure.USE_WSOE |
           StateRepresentationSynthesisAbstractionProcedure.USE_TRANSITIONREMOVAL);
    }
  },

  /**
   * An abstraction chain consisting of bisimulation
   * and weak synthesis observation equivalence.
   */
  WSOE_ONLY {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final CompositionalStateRepresentationSynthesizer synthesizer =
        (CompositionalStateRepresentationSynthesizer) analyzer;
      return StateRepresentationSynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer,
           StateRepresentationSynthesisAbstractionProcedure.USE_BISIMULATION |
           StateRepresentationSynthesisAbstractionProcedure.USE_WSOE);
    }
  },

  /**
   * An abstraction chain consisting of halfway synthesis, bisimulation,
   * synthesis observation equivalence, and weak synthesis observation
   * equivalence.
   */
  SOE_WSOE {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final CompositionalStateRepresentationSynthesizer synthesizer =
        (CompositionalStateRepresentationSynthesizer) analyzer;
      return StateRepresentationSynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer, StateRepresentationSynthesisAbstractionProcedure.CHAIN_ALL);
    }
  };


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.compositional.AbstractionProcedureFactory
  @Override
  public boolean supportsNondeterminism()
  {
    return true;
  }

  @Override
  public boolean expectsAllMarkings()
  {
    return false;
  }

}