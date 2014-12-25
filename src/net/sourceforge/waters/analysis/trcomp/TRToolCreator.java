//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRToolCreator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.ListedEnumFactory;


/**
 * <P>Factory class to create transition relation simplifiers or heuristics
 * objects for a compositional model analyser.</P>
 *
 * <P>The tool creator has a type argument that represents the type of tool
 * it creates, e.g., {@link TransitionRelationSimplifier}.
 * Its {@link #create(AbstractTRCompositionalAnalyzer) create()} is invoked
 * during initialisation of the model analyser to create the tool in the
 * correct context. In addition, the tool creator has a name, so it can
 * be added to a {@link ListedEnumFactory} to implement command line
 * options.</P>
 *
 * @author Robi Malik
 *
 * @see AbstractTRCompositionalAnalyzer
 */

public abstract class TRToolCreator<T>
{

  //#########################################################################
  //# Constructors
  protected TRToolCreator(final String name)
  {
    mName = name;
  }


  //#########################################################################
  //# Override for java.lang.Object
  @Override
  public String toString()
  {
    return getName();
  }


  //#########################################################################
  //# Factory Methods
  /**
   * Returns the name of the tool created by this tool creator.
   */
  public String getName()
  {
    return mName;
  }

  /**
   * Creates a tool to be used by the given model analyser.
   */
  public abstract T create(AbstractTRCompositionalAnalyzer analyzer)
    throws AnalysisConfigurationException;


  //#########################################################################
  //# Data Members
  private final String mName;

}