//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.efa.base;

import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.VisitorException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author Sahar Mohajerani, Robi Malik
 */

public abstract class AbstractEFAAlgorithm
  implements Abortable
{

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    mIsAborting = true;
  }

  @Override
  public boolean isAborting()
  {
    return mIsAborting;
  }

  @Override
  public void resetAbort()
  {
    mIsAborting = false;
  }


  //#########################################################################
  //# Simple Access
  public EFASimplifierStatistics createStatistics(final boolean trans)
  {
    mStatistics = new EFASimplifierStatistics(this, trans);
    return mStatistics;
  }

  public EFASimplifierStatistics getStatistics()
  {
    return mStatistics;
  }


  //#########################################################################
  //# Algorithm Support
  protected void setUp()
    throws AnalysisException
  {
    checkAbort();
    updatePeakMemoryUsage();
  }

  /**
   * Cleans up temporary data.
   */
  protected void tearDown()
  {
    updatePeakMemoryUsage();
  }

  /**
   * Adds the given runtime to the simplifier's statistics record.
   */
  protected void recordRunTime(final long runtime)
  {
    if (mStatistics != null) {
      mStatistics.recordRunTime(runtime);
    }
  }

  protected void updatePeakMemoryUsage()
  {
    if (mStatistics != null) {
      mStatistics.updatePeakMemoryUsage();
    }
  }

  /**
   * Prints a message to the current logger indicating that this simplifier
   * has just started.
   */
  protected void logStart()
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      logger.debug("ENTER " + ProxyTools.getShortClassName(this) + " ...");
    }
  }

  /**
   * Prints a message to the current logger indicating that this simplifier
   * has just completed.
   * @param  success  Whether or not the simplifier has been able to
   *                  actually simplify the transition relation.
   */
  protected void logFinish(final boolean success)
  {
    if (success) {
      final Logger logger = getLogger();
      if (logger.isDebugEnabled()) {
        logger.debug("DONE " + ProxyTools.getShortClassName(this) + " ...");
      }
    }
  }

  /**
   * Checks whether this simplifier has been requested to abort,
   * and if so, performs the abort by throwing an {@link AnalysisAbortException}.
   * This method should be called periodically by any transition relation
   * simplifier that supports being aborted by user request.
   */
  protected void checkAbort()
    throws AnalysisAbortException
  {
    if (mIsAborting) {
      final AnalysisAbortException exception = new AnalysisAbortException();
      throw exception;
    }
  }

  /**
   * Checks whether this simplifier has been requested to abort,
   * and if so, performs the abort by throwing a {@link VisitorException}
   * wrapped around an {@link AnalysisAbortException}. This method is used
   * instead of {@link #checkAbort()} when inside a {@link
   * net.sourceforge.waters.model.base.ProxyVisitor ProxyVisitor}.
   */
  protected void checkAbortInVisitor()
    throws VisitorException
  {
    if (mIsAborting) {
      final AnalysisAbortException exception = new AnalysisAbortException();
      throw new VisitorException(exception);
    }
  }


  //#########################################################################
  //# Logging
  protected Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return LogManager.getLogger(clazz);
  }


  //#########################################################################
  //# Data Members
  private boolean mIsAborting;
  private EFASimplifierStatistics mStatistics;

}
