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
 * nonblocking verification. The members of this enumeration are passed to the
 * {@link CompositionalSynthesizer} using its
 * {@link AbstractCompositionalModelAnalyzer#setAbstractionProcedureFactory(AbstractionProcedureFactory)
 * setAbstractionProcedureFactory()} method.
 *
 * @see AbstractionProcedure
 * @author Robi Malik
 */


public enum SynthesisAbstractionProcedureFactory
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
      final CompositionalSynthesizer synthesizer =
        (CompositionalSynthesizer) analyzer;
      return SynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer, SynthesisAbstractionProcedure.CHAIN_SOE);
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
      final CompositionalSynthesizer synthesizer =
        (CompositionalSynthesizer) analyzer;
      return SynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer,
           SynthesisAbstractionProcedure.USE_BISIMULATION |
           SynthesisAbstractionProcedure.USE_SOE);
    }
  },

  /**
   * An abstraction chain consisting of halfway synthesis, bisimulation,
   * and synthesis observation equivalence.
   */
  WSOE {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final CompositionalSynthesizer synthesizer =
        (CompositionalSynthesizer) analyzer;
      return SynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer, SynthesisAbstractionProcedure.CHAIN_WSOE);
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
      final CompositionalSynthesizer synthesizer =
        (CompositionalSynthesizer) analyzer;
      return SynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer,
           SynthesisAbstractionProcedure.USE_BISIMULATION |
           SynthesisAbstractionProcedure.USE_WSOE);
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
      final CompositionalSynthesizer synthesizer =
        (CompositionalSynthesizer) analyzer;
      return SynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer, SynthesisAbstractionProcedure.CHAIN_ALL);
    }
  };


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.compositional.AbstractionProcedureFactory
  @Override
  public boolean supportsNondeterminism()
  {
    return false;
  }

  @Override
  public boolean expectsAllMarkings()
  {
    return false;
  }

}