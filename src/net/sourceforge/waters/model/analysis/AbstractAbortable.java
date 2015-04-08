//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractAbortable
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import org.apache.log4j.Logger;



/**
 * A basic implementation of the {@link Abortable} interface.
 * An abortable algorithm can be implemented by extending this class
 * and making sure the method {@link #checkAbort()} is called
 * periodically.
 *
 * @author Robi Malik
 */

public abstract class AbstractAbortable implements Abortable
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
  //# Auxiliary Methods
  /**
   * Checks whether the abortable has been requested to abort, and if so,
   * performs the abort by throwing an {@link AnalysisAbortException}.
   * This method should be called periodically by any algorithm that
   * supports being aborted by user request.
   */
  public void checkAbort()
    throws AnalysisAbortException, OverflowException
  {
    if (mIsAborting) {
      getLogger().debug("Abort request received - aborting ...");
      throw new AnalysisAbortException();
    }
  }


  //#########################################################################
  //# Logging
  public Logger getLogger()
  {
    final Class<?> clazz = getClass();
    return Logger.getLogger(clazz);
  }


 //#########################################################################
  //# Data Members
  private boolean mIsAborting;

}
