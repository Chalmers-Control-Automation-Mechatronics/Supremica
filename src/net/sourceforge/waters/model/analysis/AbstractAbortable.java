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

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.base.VisitorException;

import org.apache.logging.log4j.LogManager;



/**
 * A basic implementation of the {@link Abortable} interface.
 * An abortable algorithm can be implemented by extending this class
 * and making sure the method {@link #checkAbort()} is called periodically.
 * Alternatively, the static method {@link #checkAbort(AbortRequester)} can be
 * called periodically by classes implementing the {@link Abortable} interface
 * without extending this class.
 *
 * @author Robi Malik
 */

public abstract class AbstractAbortable implements Abortable
{

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort(final AbortRequester sender)
  {
    if (mAbortRequester == null) {
      mAbortRequester = sender;
    }
  }

  @Override
  public boolean isAborting()
  {
    return mAbortRequester != null;
  }

  @Override
  public void resetAbort()
  {
    mAbortRequester = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Checks whether the abortable has been requested to abort, and if so,
   * performs the abort by throwing an {@link AnalysisAbortException}.
   * The type of exception is determined by calling the {@link
   * AbortRequester#createAbortException() createAbortException()} method of
   * the object passed to the {@link #requestAbort(AbortRequester)
   * requestAbort()} that triggered the abort.
   */
  public void checkAbort()
    throws AnalysisAbortException
  {
    checkAbort(mAbortRequester);
  }

  /**
   * Checks whether the abortable has been requested to abort, and if so,
   * performs the abort by throwing a {@link VisitorException}, with a
   * cause obtained by calling the {@link
   * AbortRequester#createAbortException() createAbortException()} method of
   * the object passed to the {@link #requestAbort(AbortRequester)
   * requestAbort()} that triggered the abort.
   */
  public void checkAbortInVisitor()
    throws VisitorException
  {
    checkAbortInVisitor(mAbortRequester);
  }

  /**
   * Throws an {@link AnalysisAbortException} if needed.
   * This method checks whether the given requester is non-<CODE>null</CODE>.
   * If so, it call the requester's {@link
   * AbortRequester#createAbortException() createAbortException()} method and
   * throws the returned exception.
   * @param  requester  The object requesting to abort, or <CODE>null</CODE>
   *                    if no abort is requested.
   */
  public static void checkAbort(final AbortRequester requester)
    throws AnalysisAbortException
  {
    //showTime();
    if (requester != null) {
      LogManager.getLogger().debug("Abort request received - aborting ...");
      throw requester.createAbortException();
    }
  }

  /**
   * Throws a {@link VisitorException} indicating an abort if needed, with a
   * cause obtained by calling the requester's {@link
   * AbortRequester#createAbortException() createAbortException()} method.
   * @param  requester  The object requesting to abort, or <CODE>null</CODE>
   *                    if no abort is requested.
   */
  public static void checkAbortInVisitor(final AbortRequester requester)
    throws VisitorException
  {
    //showTime();
    if (requester != null) {
      LogManager.getLogger().debug("Abort request received - aborting ...");
      final AnalysisAbortException exception = requester.createAbortException();
      throw new VisitorException(exception);
    }
  }

  /**
   * Gets the sender of the earliest abort request received.
   * @return  The object passed to the latest call to
   *          {@link #requestAbort(AbortRequester)} or <CODE>null</CODE>.
   */
  protected AbortRequester getAbortRequester()
  {
    return mAbortRequester;
  }


  //#########################################################################
  //# Debugging
  /*
  private static void showTime()
  {
    final long current = System.currentTimeMillis();
    if (current >= mPrevTime + 500) {
      System.err.println("check abort " + (mCount++) + " " + (current - mPrevTime));
      mPrevTime = current;
    }
  }

  private static long mPrevTime = 0;
  private static long mCount = 0;
  */


  //#########################################################################
  //# Data Members
  private AbortRequester mAbortRequester;

}
