//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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
