//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeVerificationResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import java.io.PrintWriter;
import java.util.Formatter;

import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultVerificationResult;


/**
 * A result record that can returned by a native verification algorithm.
 *
 * @author Robi Malik
 */

public class NativeVerificationResult
  extends DefaultVerificationResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a verification result representing an incomplete verification run.
   */
  public NativeVerificationResult()
  {
    mEncodingSize = -1;
    mNumExploredTransitions = -1;
    mTarjanComponentCount = -1;
    mTarjanControlStackHeight = -1;
    mTarjanComponentStackHeight = -1;
    mTarjanGarbageCollections = -1;
  }


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the number of bits used to encode state tuples.
   */
  public double getEncodingSize()
  {
    return mEncodingSize;
  }

  /**
   * Gets the total number of transitions explored during verification.
   * This is a runtime estimate. If transitions are processed more than
   * once, each time is counted separately.
   */
  public double getNumberOfExploredTransitions()
  {
    return mNumExploredTransitions;
  }

  /**
   * Gets the number of strongly connected components detected by
   * Tarjan's algorithm.
   */
  public int getTarjanComponentCount()
  {
    return mTarjanComponentCount;
  }

  /**
   * Gets the maximum height of the control stack when Tarjan's algorithm
   * is used.
   */
  public int getTarjanControlStackHeight()
  {
    return mTarjanControlStackHeight;
  }

  /**
   * Gets the maximum height of the component stack when Tarjan's algorithm
   * is used.
   */
  public int getTarjanComponentStackHeight()
  {
    return mTarjanComponentStackHeight;
  }

  /**
   * Gets the number of the control stack has been subjected to garbage
   * collection when using Tarjan's algorithm.
   */
  public int getTarjanGarbageCollections()
  {
    return mTarjanGarbageCollections;
  }


  //#########################################################################
  //# Providing Statistics
  public void setEncodingSize(final int value)
  {
    mEncodingSize = value;
  }

  public void setNumberOfExploredTransitions(final double value)
  {
    mNumExploredTransitions = value;
  }

  public void setTarjanComponentCount(final int value)
  {
    mTarjanComponentCount = value;
  }

  public void setTarjanControlStackHeight(final int value)
  {
    mTarjanControlStackHeight = value;
  }

  public void setTarjanComponentStackHeight(final int value)
  {
    mTarjanComponentStackHeight = value;
  }

  public void setTarjanGarbageCollections(final int value)
  {
    mTarjanGarbageCollections = value;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    final NativeVerificationResult result = (NativeVerificationResult) other;
    mEncodingSize = mergeAdd(mEncodingSize, result.mEncodingSize);
    mNumExploredTransitions =
      mergeAdd(mNumExploredTransitions, result.mNumExploredTransitions);
    mTarjanComponentCount =
      mergeAdd(mTarjanComponentCount, result.mTarjanComponentCount);
    mTarjanControlStackHeight =
      Math.max(mTarjanControlStackHeight, result.mTarjanControlStackHeight);
    mTarjanComponentStackHeight =
      Math.max(mTarjanComponentStackHeight, result.mTarjanComponentStackHeight);
    mTarjanGarbageCollections =
      mergeAdd(mTarjanGarbageCollections, result.mTarjanGarbageCollections);
  }


  //#########################################################################
  //# Printing
  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    @SuppressWarnings("resource")
    final Formatter formatter = new Formatter(writer);
    if (mEncodingSize >= 0) {
      writer.print("Encoding size: ");
      writer.print(mEncodingSize);
      writer.println(" bits");
    }
    if (mNumExploredTransitions >= 0.0) {
      formatter.format("Number of transitions explored: %.0f\n",
                       mNumExploredTransitions);
    }
    if (mTarjanComponentCount >= 0) {
      writer.print("Number of strongly connected components: ");
      writer.println(mTarjanComponentCount);
    }
    if (mTarjanControlStackHeight >= 0) {
      writer.print("Maximum height of Tarjan control stack: ");
      writer.println(mTarjanControlStackHeight);
    }
    if (mTarjanComponentStackHeight >= 0) {
      writer.print("Maximum height of Tarjan component stack: ");
      writer.println(mTarjanComponentStackHeight);
    }
    if (mTarjanGarbageCollections >= 0) {
      writer.print("Number of Tarjan control stack garbage collections: ");
      writer.println(mTarjanGarbageCollections);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",EncodingSize");
    writer.print(",ExploredTrans");
    writer.print(",TarjanSCCs");
    writer.print(",TarjanControlStackHeight");
    writer.print(",TarjanComponentStackHeight");
    writer.print(",TarjanGarbageCollections");
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(',');
    writer.print(mEncodingSize);
    writer.print(',');
    writer.print(mNumExploredTransitions);
    writer.print(',');
    writer.print(mTarjanComponentCount);
    writer.print(',');
    writer.print(mTarjanControlStackHeight);
    writer.print(',');
    writer.print(mTarjanComponentStackHeight);
    writer.print(',');
    writer.print(mTarjanGarbageCollections);
  }


  //#########################################################################
  //# Data Members
  private int mEncodingSize;
  private double mNumExploredTransitions;
  private int mTarjanComponentCount;
  private int mTarjanControlStackHeight;
  private int mTarjanComponentStackHeight;
  private int mTarjanGarbageCollections;

}
