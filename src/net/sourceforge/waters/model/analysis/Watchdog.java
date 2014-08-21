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
 * <P>A watchdog is a {@link Thread} linked to a {@link ModelAnalyzer} or
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
