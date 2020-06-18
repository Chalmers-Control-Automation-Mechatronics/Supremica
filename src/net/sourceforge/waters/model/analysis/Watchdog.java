//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import java.util.Collection;
import java.util.LinkedList;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;


/**
 * <P>A utility class to enforce timeouts.</P>
 *
 * <P>A watchdog is a {@link Thread} linked to a {@link ModelAnalyzer} or
 * other object implementing the {@link Abortable} interface. Once initialised
 * and started, it waits for the set timeout period before requesting its
 * controlled object to abort. It is possible to restart the timer by calling
 * the watchdog's {@link #reset()} method.</P>
 *
 * <P><I>Sample code:</I></P>
 * <PRE> {@link ModelAnalyzer} analyzer;
 * Watchdog watchdog = new {@link #Watchdog(Abortable, int) Watchdog}(analyer, 10);  // 10 sec timeout
 * watchdog.{@link Thread#start() start}();
 * for (int i = 0; i &lt; 10; i++) {
 *   // configure analyzer for experiment i ...
 *   try {
 *     analyzer.{@link ModelAnalyzer#run() run}();
 *   } catch (final {@link AnalysisAbortException} exception) {
 *     // it has timed out ...
 *   }
 *   watchdog.{@link #reset() reset}();  // so the timer is reset
 * }</PRE>
 *
 * @author Robi Malik
 */

public class Watchdog extends Thread {

  //#########################################################################
  //# Constructor
  /**
   * Creates a new watchdog.
   * @param  seconds     The time in seconds before {@link
   *                     Abortable#requestAbort() requestAbort()} is called.
   */
  public Watchdog(final int seconds)
  {
    mAbortables = new LinkedList<Abortable>();
    mTimeoutMillis = 1000L * seconds;
    mStartTime = 0;
    setDaemon(true);
  }

  /**
   * Creates a new watchdog.
   * @param  abortable   The object to be controlled by timeout.
   *                     If the timeout is reached, the abortable's
   *                     {@link Abortable#requestAbort() requestAbort()}
   *                     method is called.
   * @param  seconds     The time in seconds before {@link
   *                     Abortable#requestAbort() requestAbort()} is called.
   */
  public Watchdog(final Abortable abortable, final int seconds)
  {
    this(seconds);
    mAbortables.add(abortable);
  }


  //#########################################################################
  //# Simple Access
  /**
   * Resets the watchdog timer and cancels all abort requests.
   */
  public synchronized void reset()
  {
    for (final Abortable abortable : mAbortables) {
      abortable.resetAbort();
    }
    mStartTime = System.currentTimeMillis();
    notify();
  }

  /**
   * Adds the given {@link Abortable} to the list of objects controlled
   * by this watchdog.
   */
  public synchronized void addAbortable(final Abortable abortable)
  {
    mAbortables.add(abortable);
    if (mStartTime < 0) {
      abortable.requestAbort();
    }
  }

  /**
   * Removes the given {@link Abortable} from the list of objects controlled
   * by this watchdog.
   */
  public synchronized void removeAbortable(final Abortable abortable)
  {
    mAbortables.remove(abortable);
  }

  /**
   * Sets whether this watchdog is verbose. If verbose, it will print
   * the message <CODE>&quot;aborting&nbsp;...&nbsp;&quot;</CODE> to
   * {@link System#out} when the timeout is reached. This is to help
   * debugging.
   */
  public synchronized void setVerbose(final boolean verbose)
  {
    mVerbose = verbose;
  }

  /**
   * Terminates this watchdog. This method removes all abortables,
   * interrupts the tread and waits for it to terminate.
   */
  public synchronized void terminate()
  {
    try {
      mAbortables.clear();
      interrupt();
      join();
    } catch (final InterruptedException exception) {
      // Shouldn't get interrupted ...
    }
  }


  //#########################################################################
  //# Interface java.lang.Runnable
  @Override
  public synchronized void run()
  {
    try {
      while (true) {
        while (mStartTime < 0) {
          wait(mTimeoutMillis);
        }
        if (mStartTime == 0) {
          mStartTime = System.currentTimeMillis();
        }
        do {
          final long delay =
            mStartTime + mTimeoutMillis - System.currentTimeMillis();
          if (delay > 0) {
            wait(delay);
          }
        } while (System.currentTimeMillis() < mStartTime + mTimeoutMillis);
        mStartTime = -1;
        if (mVerbose) {
          System.out.print("aborting ... ");
          System.out.flush();
        }
        for (final Abortable abortable : mAbortables) {
          abortable.requestAbort();
        }
      }
    } catch (final InterruptedException exception) {
      // Interrupt means terminate ...
    }
  }


  //#########################################################################
  //# Data Members
  private final long mTimeoutMillis;
  private final Collection<Abortable> mAbortables;
  private long mStartTime;
  private boolean mVerbose;

}
