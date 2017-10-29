//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.model.analysis;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Formatter;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersException;


/**
 * The standard implementation of the {@link AnalysisResult} interface.
 * The default analysis provides read/write access to all the data provided
 * by the interface.
 *
 * @author Robi Malik
 */

public class DefaultAnalysisResult
  implements AnalysisResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an analysis result representing an incomplete run.
   * @param  analyzer The model analyser creating this result.
   */
  public DefaultAnalysisResult(final ModelAnalyzer analyzer)
  {
    this(analyzer.getClass());
  }

  /**
   * Creates an analysis result representing an incomplete run.
   * @param  clazz    The class of the model analyser creating this result.
   */
  public DefaultAnalysisResult(final Class<?> clazz)
  {
    mAnalyzerClass = clazz;
    mFinished = mSatisfied = false;
    mRunTime = -1;
    mCompileTime = -1;
    mException = null;
    mTotalNumberOfEvents = -1;
    mTotalNumberOfAutomata = -1;
    mTotalNumberOfStates = -1.0;
    mPeakNumberOfStates = -1.0;
    mTotalNumberOfTransitions = -1.0;
    mPeakNumberOfTransitions = -1.0;
    mPeakNumberOfNodes = -1;
    mPeakMemoryUsage = getCurrentMemoryUsage();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public Class<?> getAnalyzerClass()
  {
    return mAnalyzerClass;
  }

  @Override
  public boolean isFinished()
  {
    return mFinished;
  }

  @Override
  public boolean isSatisfied()
  {
    return mSatisfied;
  }

  @Override
  public WatersException getException()
  {
    return mException;
  }

  @Override
  public long getRunTime()
  {
    return mRunTime;
  }

  @Override
  public long getCompileTime()
  {
    return mCompileTime;
  }

  @Override
  public long getPeakMemoryUsage()
  {
    return mPeakMemoryUsage;
  }

  @Override
  public int getTotalNumberOfEvents()
  {
    return mTotalNumberOfEvents;
  }

  @Override
  public int getTotalNumberOfAutomata()
  {
    return mTotalNumberOfAutomata;
  }

  @Override
  public double getTotalNumberOfStates()
  {
    return mTotalNumberOfStates;
  }

  @Override
  public double getPeakNumberOfStates()
  {
    return mPeakNumberOfStates;
  }

  @Override
  public int getPeakNumberOfNodes()
  {
    return mPeakNumberOfNodes;
  }

  @Override
  public double getTotalNumberOfTransitions()
  {
    return mTotalNumberOfTransitions;
  }

  @Override
  public double getPeakNumberOfTransitions()
  {
    return mPeakNumberOfTransitions;
  }


  @Override
  public void setSatisfied(final boolean sat)
  {
    mFinished = true;
    mSatisfied = sat;
  }

  @Override
  public void setException(final WatersException exception)
  {
    mFinished = true;
    mException = exception;
  }

  @Override
  public void setRuntime(final long time)
  {
    mRunTime = time;
  }

  @Override
  public void setCompileTime(final long time)
  {
    mCompileTime = time;
  }

  @Override
  public void setNumberOfStates(final double numstates)
  {
    setTotalNumberOfStates(numstates);
    setPeakNumberOfStates(numstates);
  }

  @Override
  public void setNumberOfAutomata(final int numaut)
  {
    mTotalNumberOfAutomata = numaut;
  }

  @Override
  public void setTotalNumberOfEvents(final int numEvents)
  {
    mTotalNumberOfEvents = numEvents;
  }

  @Override
  public void setTotalNumberOfStates(final double numstates)
  {
    mTotalNumberOfStates = numstates;
  }

  @Override
  public void setPeakNumberOfStates(final double numstates)
  {
    mPeakNumberOfStates = numstates;
  }

  @Override
  public void setNumberOfTransitions(final double numtrans)
  {
    setTotalNumberOfTransitions(numtrans);
    setPeakNumberOfTransitions(numtrans);
  }

  @Override
  public void setTotalNumberOfTransitions(final double numtrans)
  {
    mTotalNumberOfTransitions = numtrans;
  }

  @Override
  public void setPeakNumberOfTransitions(final double numtrans)
  {
    mPeakNumberOfTransitions = numtrans;
  }

  @Override
  public void setPeakNumberOfNodes(final int numnodes)
  {
    mPeakNumberOfNodes = numnodes;
  }


  @Override
  public void updatePeakMemoryUsage(final long usage)
  {
    if (usage > mPeakMemoryUsage) {
      mPeakMemoryUsage = usage;
    }
  }


  /**
   * <P>Merges this result with another.</P>
   *
   * <P>This method destructively modifies the contents of this result record
   * by merging in the contents of the given other record.</P>
   *
   * <P>Merging is done for the purpose of accumulating statistics over
   * multiple runs of the same algorithm. The way how information is merged
   * depends on the type of data stored, e.g., run times and totals of state
   * numbers will be added, while for other values such as peak state number
   * the maximum value will be chosen. Not all result data makes sense for
   * multiple runs, so items that are only relevant for a single run (such
   * as counterexamples) may be removed by this method.</P>
   * @param other
   *          The record to be merged into this record.
   * @throws ClassCastException
   *          to indicate that the two records merged are not of exactly the
   *          same type.
   */
  @Override
  public void merge(final AnalysisResult other)
  {
    if (other.getClass() == getClass()) {
      mFinished &= other.isFinished();
      mSatisfied &= other.isSatisfied();
      mCompileTime = mergeAdd(mCompileTime, other.getCompileTime());
      mRunTime = mergeAdd(mRunTime, other.getRunTime());
      if (mException != null) {
        mException = other.getException();
      }
      mTotalNumberOfEvents =
        mergeAdd(mTotalNumberOfEvents, other.getTotalNumberOfEvents());
      mTotalNumberOfAutomata =
        mergeAdd(mTotalNumberOfAutomata, other.getTotalNumberOfAutomata());
      mTotalNumberOfStates =
        mergeAdd(mTotalNumberOfStates, other.getTotalNumberOfStates());
      mTotalNumberOfTransitions =
        mergeAdd(mTotalNumberOfTransitions,
                 other.getTotalNumberOfTransitions());
      mPeakNumberOfStates =
        Math.max(mPeakNumberOfStates, other.getPeakNumberOfStates());
      mPeakNumberOfTransitions =
        Math.max(mPeakNumberOfTransitions, other.getPeakNumberOfTransitions());
      mPeakNumberOfNodes =
        Math.max(mPeakNumberOfNodes, other.getPeakNumberOfNodes());
      mPeakMemoryUsage =
        Math.max(mPeakMemoryUsage, other.getPeakMemoryUsage());
    } else {
      throw new ClassCastException
        ("Attempting to merge " + ProxyTools.getShortClassName(this) +
         " with " + ProxyTools.getShortClassName(other) + "!");
    }
  }

  /**
   * Updates the recorded memory usage.
   * This method checks whether the amount of memory currently in use by
   * the Java virtual machine exceeds the currently recorded memory usage,
   * and if so, updates the recorded usage.
   */
  public void updatePeakMemoryUsage()
  {
    final long usage = getCurrentMemoryUsage();
    updatePeakMemoryUsage(usage);
  }


  //#########################################################################
  //# Printing
  @Override
  public String toString()
  {
    final StringWriter writer = new StringWriter();
    final PrintWriter stream = new PrintWriter(writer);
    print(stream);
    return writer.toString();

  }

  @Override
  public void print(final PrintStream stream)
  {
    final PrintWriter writer = new PrintWriter(stream);
    print(writer);
    writer.flush();
  }

  @Override
  public void print(final PrintWriter writer)
  {
    if (mException != null) {
      writer.println("Exception: " + ProxyTools.getShortClassName(mException));
      final String msg = mException.getMessage();
      if (msg != null) {
        writer.println("           " + msg);
      }
    } else {
      writer.println("Verification result: " + mSatisfied);
    }
    @SuppressWarnings("resource")
    final Formatter formatter = new Formatter(writer);
    if (mRunTime >= 0) {
      formatter.format("Total runtime: %.3fs\n", 0.001f * mRunTime);
    }
    if (mCompileTime >= 0) {
      formatter.format("Compile time: %.3fs\n", 0.001f * mCompileTime);
    }
    writer.println("Memory usage: " + (mPeakMemoryUsage >> 10) + " KiB");
    if (mTotalNumberOfEvents >= 0) {
      writer.println("Total number of events: " + mTotalNumberOfEvents);
    }
    if (mTotalNumberOfAutomata >= 0) {
      writer.println("Total number of automata: " + mTotalNumberOfAutomata);
    }
    if (mTotalNumberOfStates >= 0) {
      formatter.format("Total number of states: %.0f\n", mTotalNumberOfStates);
    }
    if (mTotalNumberOfTransitions >= 0) {
      formatter.format("Total number of transitions: %.0f\n",
                       mTotalNumberOfTransitions);
    }
    if (mPeakNumberOfStates >= 0) {
      formatter.format("Peak number of states: %.0f\n", mPeakNumberOfStates);
    }
    if (mPeakNumberOfTransitions >= 0) {
      formatter.format("Peak number of transitions: %.0f\n",
                       mPeakNumberOfTransitions);
    }
    if (mPeakNumberOfNodes >= 0) {
      writer.println("Peak number of nodes: " + mPeakNumberOfNodes);
    }
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    if (mException != null) {
      writer.print(ProxyTools.getShortClassName(mException));
    } else {
      writer.print(mSatisfied);
    }
    writer.print(',');
    if (mRunTime >= 0) {
      writer.print(mRunTime);
    }
    writer.print(',');
    if (mCompileTime >= 0) {
      writer.print(mCompileTime);
    }
    writer.print(',');
    writer.print(mPeakMemoryUsage);
    writer.print(',');
    if (mTotalNumberOfAutomata >= 0) {
      writer.print(mTotalNumberOfEvents);
    }
    writer.print(',');
    if (mTotalNumberOfAutomata >= 0) {
      writer.print(mTotalNumberOfAutomata);
    }
    writer.print(',');
    @SuppressWarnings("resource")
    final Formatter formatter = new Formatter(writer);
    if (mTotalNumberOfStates >= 0) {
      formatter.format("%.0f", mTotalNumberOfStates);
    }
    writer.print(',');
    if (mTotalNumberOfTransitions >= 0) {
      formatter.format("%.0f", mTotalNumberOfTransitions);
    }
    writer.print(',');
    if (mPeakNumberOfStates >= 0) {
      formatter.format("%.0f", mPeakNumberOfStates);
    }
    writer.print(',');
    if (mPeakNumberOfTransitions >= 0) {
      formatter.format("%.0f", mPeakNumberOfTransitions);
    }
    writer.print(',');
    if (mPeakNumberOfNodes >= 0) {
      writer.print(mPeakNumberOfNodes);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    writer.print("Result");
    writer.print(",RunTime");
    writer.print(",CompileTime");
    writer.print(",PeakMem");
    writer.print(",TotEvents");
    writer.print(",TotAut");
    writer.print(",TotStates");
    writer.print(",TotTrans");
    writer.print(",PeakStates");
    writer.print(",PeakTrans");
    writer.print(",PeakNodes");
  }


  //#########################################################################
  //# Specific Access
  public void updateNumberOfAutomata(final int numaut)
  {
    mTotalNumberOfAutomata = mergeAdd(mTotalNumberOfAutomata, numaut);
  }

  public void updateNumberOfStates(final double numstates)
  {
    mTotalNumberOfStates = mergeAdd(mTotalNumberOfStates, numstates);
    mPeakNumberOfStates = Math.max(mPeakNumberOfStates, numstates);
  }

  public void updateNumberOfTransitions(final double numtrans)
  {
    mTotalNumberOfTransitions = mergeAdd(mTotalNumberOfTransitions, numtrans);
    mPeakNumberOfTransitions = Math.max(mPeakNumberOfTransitions, numtrans);
  }

  public void updateNumberOfNodes(final int numnodes)
  {
    mPeakNumberOfNodes = Math.max(mPeakNumberOfNodes, numnodes);
  }


  //#########################################################################
  //# Static Methods
  public static int mergeAdd(final int data1, final int data2)
  {
    if (data1 < 0) {
      return data2;
    } else if (data2 < 0) {
      return data1;
    } else {
      return data1 + data2;
    }
  }

  public static long mergeAdd(final long data1, final long data2)
  {
    if (data1 < 0) {
      return data2;
    } else if (data2 < 0) {
      return data1;
    } else {
      return data1 + data2;
    }
  }

  public static double mergeAdd(final double data1, final double data2)
  {
    if (data1 < 0.0) {
      return data2;
    } else if (data2 < 0.0) {
      return data1;
    } else {
      return data1 + data2;
    }
  }

  public static long getCurrentMemoryUsage()
  {
    final Runtime runtime = Runtime.getRuntime();
    return runtime.totalMemory() - runtime.freeMemory();
  }


  //#########################################################################
  //# Data Members
  private final Class<?> mAnalyzerClass;
  private boolean mFinished;
  private boolean mSatisfied;
  private long mRunTime;
  private long mCompileTime;
  private WatersException mException;
  private int mTotalNumberOfEvents;
  private int mTotalNumberOfAutomata;
  private double mTotalNumberOfStates;
  private double mPeakNumberOfStates;
  private double mTotalNumberOfTransitions;
  private double mPeakNumberOfTransitions;
  private int mPeakNumberOfNodes;
  private long mPeakMemoryUsage;

}
