//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   AbstractionProcedureFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

/**
 * @author Robi Malik
 */

public interface AbstractionProcedureFactory
{

  //#########################################################################
  //# Factory Methods
  /**
   * Creates an abstraction procedure to be used by the given model analyser.
   */
  public AbstractionProcedure createAbstractionProcedure
    (AbstractCompositionalModelAnalyzer analyzer);

  /**
   * Returns whether or not this abstraction procedure supports
   * nondeterministic automata.
   */
  public boolean supportsNondeterminism();

  /**
   * Returns whether or not this abstraction procedure expects both
   * default and precondition markings to be set when the model analyser
   * is running, even for models without these propositions.
   */
  public boolean expectsAllMarkings();

}