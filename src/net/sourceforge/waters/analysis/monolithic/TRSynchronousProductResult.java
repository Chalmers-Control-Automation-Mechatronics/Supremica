//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   TRSynchronousProductResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import java.io.PrintWriter;

import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TRSynchronousProductStateMap;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.DefaultSynchronousProductResult;


/**
 * A synchronous product result record returned by transition-relation
 * based synchronous product builders.
 *
 * @see TRAbstractSynchronousProductBuilder
 * @author Robi Malik
 */

public class TRSynchronousProductResult
  extends DefaultSynchronousProductResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a synchronous product result representing an incomplete run.
   */
  public TRSynchronousProductResult()
  {
    mReducedDiamondsCount = -1;
  }


  //#########################################################################
  //# Simple Access Methods
  @Override
  public TRAutomatonProxy getComputedAutomaton()
  {
    return (TRAutomatonProxy) super.getComputedAutomaton();
  }

  @Override
  public TRSynchronousProductStateMap getStateMap()
  {
    return (TRSynchronousProductStateMap) super.getStateMap();
  }

  /**
   * Gets the number of states that were reduced by means of reducing
   * synchronous composition.
   * @see TRReducingSynchronousProductBuilder
   */
  public int getReducedDiamondsCount()
  {
    return mReducedDiamondsCount;
  }


  //#########################################################################
  //# Providing Statistics
  public void setReducedDiamondsCount(final int count)
  {
    mReducedDiamondsCount = count;
  }

  public void addReducedDiamond()
  {
    mReducedDiamondsCount++;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final TRSynchronousProductResult result =
      (TRSynchronousProductResult) other;
    mReducedDiamondsCount =
      mergeAdd(mReducedDiamondsCount, result.mReducedDiamondsCount);
  }

  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    if (mReducedDiamondsCount >= 0) {
      writer.print("Number of reduced diamonds: ");
      writer.println(mReducedDiamondsCount);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",ReducedDiamonds");
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(',');
    writer.print(mReducedDiamondsCount);
  }


  //#########################################################################
  //# Data Members
  private int mReducedDiamondsCount;

}
