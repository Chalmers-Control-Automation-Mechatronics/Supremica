//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   Watchdog
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;


/**
 * <P>A utility class to enforce timeouts.</P>
 *
 * <P>A watchdog is a {@link Thread} linked to a {@link ModelAnalyser} or
 * other object implementing the {@link Abortable} interface. Once initialised
 * and started, it waits for the set timeout period before requesting its
 * controlled object to abort. It is possible to restart the timer by calling
 * the watchdog's {@link Thread#interrupt() interrupt()} method.</P>
 *
 * <P>Sample code:<P>
 * <PRE> {@link ModelAnalyzer} analyzer;
 * Watchdog watchdog = new {@link #Watchdog(Abortable, int) Watchdog}(analyer, 10);  // 10 sec timeout
 * watchdog.{@link Thread#start() start}();
 * for (int i = 0; i < 10; i++) {
 *   // configure analyzer for experiment i ...
 *   try {
 *     analyzer.{@link ModelAnalyzer#run() run}();
 *   } catch (final {@link AbortException} exception) {
 *     // it has timed out ...
 *   }
 *   watchdog.{@link Thread#interrupt() interrupt}();  // so the timer is reset
 * }</PRE>
 *
 * @author Robi Malik
 */

public class Watchdog extends Thread {

  //#########################################################################
  //# Constructor
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
    mAbortable = abortable;
    mTimeoutMillis = seconds >= 0 ? 1000L * seconds : -1;
    setDaemon(true);
  }


  //#########################################################################
  //# Simple Access
  /**
   * Resets the watchdog to control the given new {@link Abortable}.
   */
  public void resetAbortable(final Abortable abortable)
  {
    interrupt();
    mAbortable = abortable;
  }


  //#########################################################################
  //# Interface java.lang.Runnable
  @Override
  public void run()
  {
    while (true) {
      try {
        Thread.sleep(mTimeoutMillis);
        mAbortable.requestAbort();
      } catch (final InterruptedException exception) {
        // Start over ...
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final long mTimeoutMillis;
  private Abortable mAbortable;

}
