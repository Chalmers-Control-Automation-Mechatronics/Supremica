//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalSynthesisResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.io.PrintWriter;

import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.des.AutomatonProxy;


/**
 * A result returned by the compositional synthesis algorithms
 * ({@link CompositionalAutomataSynthesizer}). In addition to the common result data,
 * it includes a collection of automata representing the synthesised modular
 * supervisor.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public abstract class CompositionalSynthesisResult
  extends CompositionalAnalysisResult
  implements ProductDESResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new synthesis result representing an incomplete run.
   */
  public CompositionalSynthesisResult()
  {
    mSynchStates = -1;
    mSynchTransitions = -1;
  }


  //#########################################################################
  //# Specific Access
  void addSynchSize(final AutomatonProxy aut)
  {
    mSynchStates = mergeAdd(mSynchStates, aut.getStates().size());
    mSynchTransitions = mergeAdd(mSynchStates, aut.getTransitions().size());
  }

  int getSynchStates()
  {
    return mSynchStates;
  }

  int getSynchTransitions()
  {
    return mSynchTransitions;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ProxyResult<TraceProxy>
  @Override
  public String getResultDescription()
  {
    return "supervisor";
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.DefaultAnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final CompositionalSynthesisResult result =
      (CompositionalSynthesisResult) other;
    mSynchStates = mergeAdd(mSynchStates, result.mSynchStates);
    mSynchTransitions = mergeAdd(mSynchTransitions, result.mSynchTransitions);
  }

  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    writer.println("--------------------------------------------------");
    writer.print("Final number of states: ");
    writer.println(mSynchStates);
    writer.print("Final number of transitions: ");
    writer.println(mSynchTransitions);
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(',');
    writer.print("SynchStates");
    writer.print(',');
    writer.print("SynchTransitions");
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(",");
    writer.print(getSynchStates());
    writer.print(",");
    writer.print(getSynchTransitions());
  }


  //#########################################################################
  //# Data Members
  private int mSynchStates;
  private int mSynchTransitions;

}
