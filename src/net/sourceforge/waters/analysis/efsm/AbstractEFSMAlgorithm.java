//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   AbstractEFSMAlgorithm
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyTools;

import org.apache.log4j.Logger;


/**
 * @author Sahar Mohajerani, Robi Malik
 */

public abstract class AbstractEFSMAlgorithm
  implements Abortable
{

  //#########################################################################
  //# Constructor
  public AbstractEFSMAlgorithm(final boolean trans)
  {
    mStatistics = new EFSMSimplifierStatistics(this, trans);
  }


  //#########################################################################
  //# Invocation
  public EFSMSimplifierStatistics getStatistics()
  {
    return mStatistics;
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
   * and if so, performs the abort by throwing an {@link AbortException}.
   * This method should be called periodically by any transition relation
   * simplifier that supports being aborted by user request.
   */
  protected void checkAbort()
    throws AbortException
  {
    if (mIsAborting) {
      final AbortException exception = new AbortException();
      throw exception;
    }
  }


  //#########################################################################
  //# Logging
  protected Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }


  //#########################################################################
  //# Data Members
  private boolean mIsAborting;
  private final EFSMSimplifierStatistics mStatistics;

}
