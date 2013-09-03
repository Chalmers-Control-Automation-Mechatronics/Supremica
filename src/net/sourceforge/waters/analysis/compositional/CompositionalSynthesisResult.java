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
    mSynchSize = -1;
  }


  //#########################################################################
  //# Specific Access
  void addSynchSize(final int size)
  {
    mSynchSize = mergeAdd(mSynchSize, size);
  }

  int getSynchSize()
  {
    return mSynchSize;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.DefaultAnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final CompositionalSynthesisResult result =
      (CompositionalSynthesisResult) other;
    mSynchSize = mergeAdd(mSynchSize, result.mSynchSize);

  }

  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    writer.print("Final number of states: ");
    writer.println(mSynchSize);
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(',');
    writer.print("SynchSize");
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(",");
    writer.print(getSynchSize());
  }


  //#########################################################################
  //# Data Members
  private int mSynchSize;

}
