//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   AbstractEFSMAlgorithm
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;

import org.apache.log4j.Logger;


/**
 * @author Sahar Mohajerani, Robi Malik
 */

abstract class AbstractEFSMAlgorithm
  implements Abortable
{

  //#########################################################################
  //# Constructor
  AbstractEFSMAlgorithm()
  {
  }


  //#########################################################################
  //# Simple Access
  EFSMSimplifierStatistics createStatistics(final boolean trans)
  {
    mStatistics = new EFSMSimplifierStatistics(this, trans);
    return mStatistics;
  }

  EFSMSimplifierStatistics getStatistics()
  {
    return mStatistics;
  }

  /**
   * Returns a collection of updates found to be selfloops.
   * This method is called after running an EFSM simplifier.
   * The default implementation returns an empty list, but subclasses
   * may provide more useful information.
   */
  Collection<ConstraintList> getSelfloopedUpdates()
  {
    return Collections.emptyList();
  }


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
  //# Algorithm Support
  void setUp()
    throws AnalysisException
  {
    checkAbort();
    updatePeakMemoryUsage();
  }

  /**
   * Cleans up temporary data.
   */
  void tearDown()
  {
    updatePeakMemoryUsage();
  }

  /**
   * Adds the given runtime to the simplifier's statistics record.
   */
  void recordRunTime(final long runtime)
  {
    if (mStatistics != null) {
      mStatistics.recordRunTime(runtime);
    }
  }

  void updatePeakMemoryUsage()
  {
    if (mStatistics != null) {
      mStatistics.updatePeakMemoryUsage();
    }
  }

  /**
   * Prints a message to the current logger indicating that this simplifier
   * has just started.
   */
  void logStart()
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
  void logFinish(final boolean success)
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
  void checkAbort()
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
  void checkAbortInVisitor()
    throws VisitorException
  {
    if (mIsAborting) {
      final AnalysisAbortException exception = new AnalysisAbortException();
      throw new VisitorException(exception);
    }
  }


  //#########################################################################
  //# Logging
  Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }


  //#########################################################################
  //# Data Members
  private boolean mIsAborting;
  private EFSMSimplifierStatistics mStatistics;

}
