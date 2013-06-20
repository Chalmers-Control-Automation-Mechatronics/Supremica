//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   Watchdog
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Collection;
import java.util.LinkedList;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;


/**
 * <P>A utility class to enforce timeouts.</P>
 *
 * <P>A watchdog is a {@link Thread} linked to a {@link ModelAnalyser} or
 * other object implementing the {@link Abortable} interface. Once initialised
 * and started, it waits for the set timeout period before requesting its
 * controlled object to abort. It is possible to restart the timer by calling
 * the watchdog's {@link #reset()} method.</P>
 *
 * <P>Sample code:<P>
 * <PRE> {@link ModelAnalyzer} analyzer;
 * Watchdog watchdog = new {@link #Watchdog(Abortable, int) Watchdog}(analyer, 10);  // 10 sec timeout
 * watchdog.{@link Thread#start() start}();
 * for (int i = 0; i < 10; i++) {
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
          wait(mTimeoutMillis);
        } while (System.currentTimeMillis() < mStartTime + mTimeoutMillis);
        mStartTime = -1;
        for (final Abortable abortable : mAbortables) {
          abortable.requestAbort();
        }
      }
    } catch (final InterruptedException exception) {
      // Shouldn't get interrupted ...
    }
  }


  //#########################################################################
  //# Data Members
  private final long mTimeoutMillis;
  private final Collection<Abortable> mAbortables;
  private long mStartTime;

}
