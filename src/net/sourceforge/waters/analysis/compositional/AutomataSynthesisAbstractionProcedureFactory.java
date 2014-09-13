//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   AutomataSynthesisAbstractionProcedureFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import net.sourceforge.waters.model.analysis.ListedEnumFactory;

/**
 * A collection of abstraction methods to be used for compositional
 * synthesis. The members of this enumeration are passed to the
 * {@link CompositionalAutomataSynthesizer} using its
 * {@link AbstractCompositionalModelAnalyzer#setAbstractionProcedureCreator(AbstractionProcedureCreator)
 * setAbstractionProcedureFactory()} method.
 *
 * @see AbstractionProcedure
 * @author Robi Malik
 */


public class AutomataSynthesisAbstractionProcedureFactory
  extends ListedEnumFactory<AbstractionProcedureCreator>
{

  //#########################################################################
  //# Singleton Pattern
  public static AutomataSynthesisAbstractionProcedureFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder
  {
    private static AutomataSynthesisAbstractionProcedureFactory INSTANCE =
      new AutomataSynthesisAbstractionProcedureFactory();
  }


  //#########################################################################
  //# Constructors
  protected AutomataSynthesisAbstractionProcedureFactory()
  {
    register(SOE);
    register(OE);
    register(SOE_ONLY);
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
      final CompositionalAutomataSynthesizer synthesizer =
        (CompositionalAutomataSynthesizer) analyzer;
      return AutomataSynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer, AutomataSynthesisAbstractionProcedure.CHAIN_SOE);
    }

    @Override
    public boolean supportsNondeterminism()
    {
      return false;
    }
  };

  /**
   * An abstraction chain consisting of halfway synthesis and observation equivalence.
   */
  public static final AbstractionProcedureCreator OE =
    new AbstractionProcedureCreator("OE")
  {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final CompositionalAutomataSynthesizer synthesizer =
        (CompositionalAutomataSynthesizer) analyzer;
      return AutomataSynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer, AutomataSynthesisAbstractionProcedure.CHAIN_OE);
    }

    @Override
    public boolean supportsNondeterminism()
    {
      return false;
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
      final CompositionalAutomataSynthesizer synthesizer =
        (CompositionalAutomataSynthesizer) analyzer;
      return AutomataSynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer,
           AutomataSynthesisAbstractionProcedure.USE_BISIMULATION |
           AutomataSynthesisAbstractionProcedure.USE_SOE);
    }

    @Override
    public boolean supportsNondeterminism()
    {
      return false;
    }
  };

  /**
   * An abstraction chain consisting of halfway synthesis, bisimulation,
   * and weak synthesis observation equivalence.
   */
  public static final AbstractionProcedureCreator WSOE =
    new AbstractionProcedureCreator("WSOE")
  {
    @Override
    public AbstractionProcedure createAbstractionProcedure
      (final AbstractCompositionalModelAnalyzer analyzer)
    {
      final CompositionalAutomataSynthesizer synthesizer =
        (CompositionalAutomataSynthesizer) analyzer;
      return AutomataSynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer, AutomataSynthesisAbstractionProcedure.CHAIN_WSOE);
    }

    @Override
    public boolean supportsNondeterminism()
    {
      return false;
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
      final CompositionalAutomataSynthesizer synthesizer =
        (CompositionalAutomataSynthesizer) analyzer;
      return AutomataSynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer,
           AutomataSynthesisAbstractionProcedure.USE_UNSUP |
           AutomataSynthesisAbstractionProcedure.USE_BISIMULATION |
           AutomataSynthesisAbstractionProcedure.USE_WSOE);
    }

    @Override
    public boolean supportsNondeterminism()
    {
      return false;
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
      final CompositionalAutomataSynthesizer synthesizer =
        (CompositionalAutomataSynthesizer) analyzer;
      return AutomataSynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer,
           AutomataSynthesisAbstractionProcedure.USE_BISIMULATION |
           AutomataSynthesisAbstractionProcedure.USE_WSOE);
    }

    @Override
    public boolean supportsNondeterminism()
    {
      return false;
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
      final CompositionalAutomataSynthesizer synthesizer =
        (CompositionalAutomataSynthesizer) analyzer;
      return AutomataSynthesisAbstractionProcedure.
        createSynthesisAbstractionProcedure
          (synthesizer, AutomataSynthesisAbstractionProcedure.CHAIN_ALL);
    }

    @Override
    public boolean supportsNondeterminism()
    {
      return false;
    }
  };

}