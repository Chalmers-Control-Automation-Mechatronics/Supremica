//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.analysis.compositional;

import java.io.PrintWriter;
import java.util.Formatter;

import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.DefaultVerificationResult;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;


/**
 * A result record that can be returned by a compositional verification
 * algorithms such as {@link CompositionalConflictChecker}.
 *
 * @author Rachel Francis, Robi Malik
 */

public class CompositionalVerificationResult
  extends CompositionalAnalysisResult
  implements VerificationResult
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a verification result representing an incomplete run.
   * @param  verifier The model analyser creating this result.
   */
  public CompositionalVerificationResult(final ModelVerifier verifier)
  {
    this(verifier.getClass());
  }

  /**
   * Creates a verification result representing an incomplete run.
   * @param  clazz    The class of the model verifier creating this result.
   */
  public CompositionalVerificationResult(final Class<?> clazz)
  {
    super(clazz);
    mCounterExampleTime = -1L;
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the time taken for counterexample computation, in milliseconds.
   */
  public long getCounterExampleTime()
  {
    return mCounterExampleTime;
  }

  public void setCounterExampleTime(final long time)
  {
    mCounterExampleTime = time;
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
  public CounterExampleProxy getComputedProxy()
  {
    return getCounterExample();
  }

  @Override
  public void setComputedProxy(final CounterExampleProxy counterexample)
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
  public CounterExampleProxy getCounterExample()
  {
    return mCounterExample;
  }

  @Override
  public void setCounterExample(final CounterExampleProxy counterexample)
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
    if (other instanceof CompositionalVerificationResult) {
      final CompositionalVerificationResult result =
        (CompositionalVerificationResult) other;
      mCounterExampleTime = mergeAdd(mCounterExampleTime,
                                     result.mCounterExampleTime);
    }
  }


  //#########################################################################
  //# Printing
  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    if (mCounterExample != null) {
      writer.println("--------------------------------------------------");
      final int len =
        DefaultVerificationResult.getCounterExampleLength(mCounterExample);
      writer.println("Counterexample length: " + len);
      if (mCounterExampleTime >= 0) {
        @SuppressWarnings("resource")
        final Formatter formatter = new Formatter(writer);
        formatter.format("Counterexample computation time: %.3fs\n",
                         0.001f * mCounterExampleTime);
      }
    }
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print(',');
    if (mCounterExample != null) {
      final int len =
        DefaultVerificationResult.getCounterExampleLength(mCounterExample);
      writer.print(len);
    }
    if (ConflictChecker.class.isAssignableFrom(getAnalyzerClass())) {
      writer.print(',');
      if (mCounterExample == null) {
        writer.print("NONCONFLICTING");
      } else if (mCounterExample instanceof ConflictCounterExampleProxy) {
        final ConflictCounterExampleProxy conflict =
          (ConflictCounterExampleProxy) mCounterExample;
        writer.print(conflict.getKind());
      }
    }
    writer.print(',');
    if (mCounterExampleTime >= 0) {
      writer.print(mCounterExampleTime);
    }
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",CounterLength");
    if (ConflictChecker.class.isAssignableFrom(getAnalyzerClass())) {
      writer.print(",ConflictKind");
    }
    writer.print(",CounterTime");
  }


  //#########################################################################
  //# Data Members
  private CounterExampleProxy mCounterExample;
  private long mCounterExampleTime;

}
