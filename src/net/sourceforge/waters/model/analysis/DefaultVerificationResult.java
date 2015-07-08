//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   DefaultVerificationResult
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.PrintWriter;

import net.sourceforge.waters.model.des.TraceProxy;


/**
 * The standard implementation of the {@link VerificationResult} interface.
 * The default verification result provides read/write access to all the data
 * provided by the interface.
 *
 * @author Robi Malik
 */

public class DefaultVerificationResult
  extends DefaultAnalysisResult
  implements VerificationResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new verification result representing an incomplete run.
   */
  public DefaultVerificationResult()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void setSatisfied(final boolean sat)
  {
    super.setSatisfied(sat);
    if (sat) {
      mCounterExample = null;
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ProxyResult<TraceProxy>
  @Override
  public TraceProxy getComputedProxy()
  {
    return getCounterExample();
  }

  @Override
  public void setComputedProxy(final TraceProxy counterexample)
  {
    setCounterExample(counterexample);
  }

  @Override
  public String getResultDescription()
  {
    return "counterexample";
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.VerificationResult
  @Override
  public TraceProxy getCounterExample()
  {
    return mCounterExample;
  }

  @Override
  public void setCounterExample(final TraceProxy counterexample)
  {
    super.setSatisfied(false);
    mCounterExample = counterexample;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AnalysisResult
  @Override
  public void merge(final AnalysisResult other)
  {
    super.merge(other);
    if (mCounterExample == null) {
      final VerificationResult result = (VerificationResult) other;
      mCounterExample = result.getCounterExample();
    }
  }


  //#########################################################################
  //# Printing
  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    if (mCounterExample != null) {
      final int len = mCounterExample.getEvents().size();
      writer.println("Counterexample length: " + len);
    }
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(',');
    if (mCounterExample != null) {
      final int len = mCounterExample.getEvents().size();
      writer.print(len);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",CounterLength");
  }


  //#########################################################################
  //# Data Members
  private TraceProxy mCounterExample;

}
