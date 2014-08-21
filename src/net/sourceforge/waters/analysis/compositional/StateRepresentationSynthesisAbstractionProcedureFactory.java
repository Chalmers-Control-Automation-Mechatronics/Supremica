//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   StateRepresentationSynthesisAbstractionProcedureFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import net.sourceforge.waters.model.analysis.ListedEnumFactory;

/**
 * A collection of abstraction methods to be used for compositional
 * synthesis. The members of this enumeration are passed to the
 * {@link CompositionalStateRepresentationSynthesizer} using its
 * {@link AbstractCompositionalModelAnalyzer#setAbstractionProcedureCreator(AbstractionProcedureCreator)
 * setAbstractionProcedureFactory()} method.
 *
 * @see AbstractionProcedure
 * @author Robi Malik, Sahar Mohajerani
 */


public class StateRepresentationSynthesisAbstractionProcedureFactory
  extends ListedEnumFactory<AbstractionProcedureCreator>
{

  //#########################################################################
  //# Singleton Pattern
  public static StateRepresentationSynthesisAbstractionProcedureFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder
  {
    private static StateRepresentationSynthesisAbstractionProcedureFactory INSTANCE =
      new StateRepresentationSynthesisAbstractionProcedureFactory();
  }


  //#########################################################################
  //# Constructors
  protected StateRepresentationSynthesisAbstractionProcedureFactory()
  {
    register(SOE);
    register(SOE_ONLY);
    register(NO_TRANSITIONREMOVAL);
    register(WSOE);
    register(WSOE_UNSUP);
    register(WSOE_ONLY);
    register(SOE_WSOE);
  }


  //#########################################################################
  //# Enumeration
  /**
   * An abstraction chain consisting of halfway synthesis, bisimulation,
   * and synthesis observation equivalence.
   */
  public static final AbstractionProcedureCreator SOE =
    new AbstractionProcedureCreator("SOE")
  {
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
  };

  /**
   * An abstraction chain consisting of bisimulation
   * and synthesis observation equivalence.
   */
  public static final AbstractionProcedureCreator SOE_ONLY =
    new AbstractionProcedureCreator("SOE_ONLY")
  {
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
  };

  /**
   * An abstraction chain consisting of bisimulation
   * and synthesis observation equivalence.
   */
  public static final AbstractionProcedureCreator NO_TRANSITIONREMOVAL =
    new AbstractionProcedureCreator("NO_TRANSITIONREMOVAL")
  {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final CompositionalStateRepresentationSynthesizer synthesizer =
        (CompositionalStateRepresentationSynthesizer) analyzer;
      return StateRepresentationSynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer,
           StateRepresentationSynthesisAbstractionProcedure.USE_HALFWAY |
           StateRepresentationSynthesisAbstractionProcedure.USE_BISIMULATION |
           StateRepresentationSynthesisAbstractionProcedure.USE_WSOE);
    }
  };

  /**
   * An abstraction chain consisting of halfway synthesis, bisimulation,
   * weak synthesis observation equivalence and transition removal.
   */
  public static final AbstractionProcedureCreator WSOE =
    new AbstractionProcedureCreator("WSOE")
  {
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
  };

  /**
   * An abstraction chain consisting of certain unsupervisability, bisimulation,
   * and weak synthesis observation equivalence.
   */
  public static final AbstractionProcedureCreator WSOE_UNSUP =
    new AbstractionProcedureCreator("WSOE_UNSUP")
  {
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
  };

  /**
   * An abstraction chain consisting of certain unsupervisability, bisimulation,
   * and weak synthesis observation equivalence.
   */
  public static final AbstractionProcedureCreator WSOE_UNSUP_NOTR =
    new AbstractionProcedureCreator("WSOE_UNSUP_NOTR")
  {
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
           StateRepresentationSynthesisAbstractionProcedure.USE_WSOE);
    }
  };

  /**
   * An abstraction chain consisting of bisimulation
   * and weak synthesis observation equivalence.
   */
  public static final AbstractionProcedureCreator WSOE_ONLY =
    new AbstractionProcedureCreator("WSOE_ONLY")
  {
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
  };

  /**
   * An abstraction chain consisting of halfway synthesis, bisimulation,
   * synthesis observation equivalence, and weak synthesis observation
   * equivalence.
   */
  public static final AbstractionProcedureCreator SOE_WSOE =
    new AbstractionProcedureCreator("SOE_WSOE")
  {
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

}