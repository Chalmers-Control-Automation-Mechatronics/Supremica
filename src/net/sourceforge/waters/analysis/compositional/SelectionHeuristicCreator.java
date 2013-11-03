//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   SelectionHeuristicCreator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;


/**
 * A factory class to obtain {@link SelectionHeuristic} objects.
 * The selection heuristic creator is obtain from a
 * {@link CompositionalSelectionHeuristicFactory} and produces the actual
 * selection heuristics through its {@link #createBaseHeuristic()} and
 * {@link #createChainHeuristic()} methods.
 *
 * @author Robi Malik
 */

public abstract class SelectionHeuristicCreator
{
  //#########################################################################
  //# Constructors
  protected SelectionHeuristicCreator(final String name)
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
   * Creates a comparator to implement this selection heuristic.
   * This returns an implementation of only one heuristic, which
   * may consider two candidates as equal.
   */
  abstract SelectionHeuristic<Candidate> createBaseHeuristic();

  /**
   * Creates a chain selection heuristic based on this method.
   * The returned heuristic first compares candidates according to the base
   * selection heuristic. If two candidates are found equal, a sequence of
   * other heuristics is used to break the tie. The specific sequence is
   * determined individually by each subclass. The default implementation
   * merely uses a one-step chain that used standard name-based comparison
   * of the base heuristic fails to distinguish two candidates.
   */
  SelectionHeuristic<Candidate> createChainHeuristic()
  {
    @SuppressWarnings("unchecked")
    final SelectionHeuristic<Candidate>[] heuristics =
      new SelectionHeuristic[1];
    heuristics[0] = createBaseHeuristic();
    return new ChainSelectionHeuristic<>(heuristics);
  }


  //#########################################################################
  //# Data Members
  private final String mName;

}
