//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   DefaultAnalysisResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Formatter;

import net.sourceforge.waters.model.base.ProxyTools;


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
  public DefaultAnalysisResult()
  {
    mFinished = false;
    mRunTime = -1;
    mException = null;
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
  public boolean isFinished()
  {
    return mFinished;
  }

  public boolean isSatisfied()
  {
    return mSatisfied;
  }

  public AnalysisException getException()
  {
    return mException;
  }

  public long getRunTime()
  {
    return mRunTime;
  }

  public long getPeakMemoryUsage()
  {
    return mPeakMemoryUsage;
  }

  public int getTotalNumberOfAutomata()
  {
    return mTotalNumberOfAutomata;
  }

  public double getTotalNumberOfStates()
  {
    return mTotalNumberOfStates;
  }

  public double getPeakNumberOfStates()
  {
    return mPeakNumberOfStates;
  }

  public int getPeakNumberOfNodes()
  {
    return mPeakNumberOfNodes;
  }

  public double getTotalNumberOfTransitions()
  {
    return mTotalNumberOfTransitions;
  }

  public double getPeakNumberOfTransitions()
  {
    return mPeakNumberOfTransitions;
  }


  public void setSatisfied(final boolean sat)
  {
    mFinished = true;
    mSatisfied = sat;
  }

  public void setException(final AnalysisException exception)
  {
    mFinished = true;
    mException = exception;
  }

  public void setRuntime(final long time)
  {
    mRunTime = time;
  }

  public void updatePeakMemoryUsage(final long usage)
  {
    if (usage > mPeakMemoryUsage) {
      mPeakMemoryUsage = usage;
    }
  }

  public void setNumberOfStates(final double numstates)
  {
    setTotalNumberOfStates(numstates);
    setPeakNumberOfStates(numstates);
  }

  public void setNumberOfAutomata(final int numaut)
  {
    mTotalNumberOfAutomata = numaut;
  }

  public void setTotalNumberOfStates(final double numstates)
  {
    mTotalNumberOfStates = numstates;
  }

  public void setPeakNumberOfStates(final double numstates)
  {
    mPeakNumberOfStates = numstates;
  }

  public void setNumberOfTransitions(final double numtrans)
  {
    setTotalNumberOfTransitions(numtrans);
    setPeakNumberOfTransitions(numtrans);
  }

  public void setTotalNumberOfTransitions(final double numtrans)
  {
    mTotalNumberOfTransitions = numtrans;
  }

  public void setPeakNumberOfTransitions(final double numtrans)
  {
    mPeakNumberOfTransitions = numtrans;
  }

  public void setPeakNumberOfNodes(final int numnodes)
  {
    mPeakNumberOfNodes = numnodes;
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
   * @throws ClassCaseException
   *          to indicate that the two records merged are not of exactly the
   *          same type.
   */
  public void merge(final AnalysisResult other)
  {
    if (other.getClass() == getClass()) {
      mFinished &= other.isFinished();
      mSatisfied &= other.isSatisfied();
      mRunTime = mergeAdd(mRunTime, other.getRunTime());
      if (mException != null) {
        mException = other.getException();
      }
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

  public void print(final PrintStream stream)
  {
    final PrintWriter writer = new PrintWriter(stream);
    print(writer);
    writer.flush();
  }

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
      final float seconds = 0.001f * mRunTime;
      formatter.format("Total runtime: %.3fs\n", seconds);
    }
    writer.println("Memory usage: " + (mPeakMemoryUsage >> 10) + " kB");
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
    writer.print(mPeakMemoryUsage);
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

  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    writer.print("Result,RunTime");
    writer.print(",PeakMem");
    writer.print(",TotAut");
    writer.print(",TotStates");
    writer.print(",TotTrans");
    writer.print(",PeakStates");
    writer.print(",PeakTrans");
    writer.print(",PeakNodes");
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
  private boolean mFinished;
  private boolean mSatisfied;
  private long mRunTime;
  private AnalysisException mException;
  private int mTotalNumberOfAutomata;
  private double mTotalNumberOfStates;
  private double mPeakNumberOfStates;
  private double mTotalNumberOfTransitions;
  private double mPeakNumberOfTransitions;
  private int mPeakNumberOfNodes;
  private long mPeakMemoryUsage;

}
