//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   AbstractionProcedureCreator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

/**
 * Factory class to create an abstraction procedure for compositional
 * model analysers.
 *
 * @author Robi Malik
 *
 * @see AbstractCompositionalModelAnalyzer
 */

public abstract class AbstractionProcedureCreator
{

  //#########################################################################
  //# Constructors
  protected AbstractionProcedureCreator(final String name)
  {
    mName = name;
  }


  //#########################################################################
  //# Override for java.lang.Object
  @Override
  public String toString()
  {
    return mName;
  }


  //#########################################################################
  //# Factory Methods
  /**
   * Creates an abstraction procedure to be used by the given model analyser.
   */
  public abstract AbstractionProcedure createAbstractionProcedure
    (AbstractCompositionalModelAnalyzer analyzer);

  /**
   * Returns whether or not this abstraction procedure supports
   * nondeterministic automata.
   */
  public boolean supportsNondeterminism()
  {
    return true;
  }

  /**
   * Returns whether or not this abstraction procedure expects both
   * default and precondition markings to be set when the model analyser
   * is running, even for models without these propositions.
   */
  public boolean expectsAllMarkings()
  {
    return false;
  }


  //#########################################################################
  //# Data Members
  private final String mName;

}